package diana.dev.quality_control_service.domain;

import diana.dev.quality_control_service.api.dto.common.MeasurementsDto;
import diana.dev.quality_control_service.api.dto.kafka.FlangeCheckResultDto;
import diana.dev.quality_control_service.api.dto.web.*;
import diana.dev.quality_control_service.domain.db.entity.FlangeCheckEntity;
import diana.dev.quality_control_service.domain.db.entity.FlangeCheckEventEntity;
import diana.dev.quality_control_service.domain.db.entity.FlangeMeasurementEntity;
import diana.dev.quality_control_service.domain.db.repository.FlangeCheckEventRepository;
import diana.dev.quality_control_service.domain.db.repository.FlangeCheckRepository;
import diana.dev.quality_control_service.domain.db.repository.FlangeMeasurementRepository;
import diana.dev.quality_control_service.domain.enums.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlangeCheckProcessor {

    private final FlangeCheckRepository flangeCheckRepository;
    private final FlangeCheckEventRepository eventRepository;
    private final FlangeMeasurementRepository measurementRepository;
    private final SseNotificationService notificationService;

    /** Порог уверенности YOLO. Если confidence < этого значения — деталь уходит на ручной контроль. */
    @Value("${quality-control.thresholds.auto-defect:0.75}")
    private double autoDefectThreshold;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    // ================================================================
    // ВХОД: результат от нейросети
    // ================================================================

    @Transactional
    public void saveResult(FlangeCheckResultDto event) {
        CheckStatus status = resolveStatus(event);

        FlangeCheckEntity entity = new FlangeCheckEntity();
        entity.setFlangeId(event.flangeId());
        entity.setLineId(event.lineId());
        entity.setFlangeType(event.flangeType() != null ? event.flangeType() : FlangeType.UNKNOWN);
        entity.setTimestamp(event.timestamp() != null ? event.timestamp() : LocalDateTime.now());
        entity.setMlVerdict(event.mlVerdict());
        entity.setMlDefectType(event.mlDefectType() != null ? event.mlDefectType() : DefectType.NONE);
        entity.setConfidence(event.confidence() != null ? event.confidence() : 0.0);
        entity.setUrl(event.url());
        entity.setStatus(status);

        // финальный вердикт ставим только для автоматических статусов
        if (status == CheckStatus.AUTO_PASSED) {
            entity.setFinalVerdict(Verdict.NORMAL);
            entity.setFinalDefectType(DefectType.NONE);
        } else if (status == CheckStatus.AUTO_DEFECT) {
            entity.setFinalVerdict(Verdict.DEFECT);
            entity.setFinalDefectType(entity.getMlDefectType());
        }
        // PENDING_MANUAL — оставляем finalVerdict/finalDefectType = null, решает оператор

        FlangeCheckEntity saved = flangeCheckRepository.save(entity);

        if (event.measurements() != null) {
            measurementRepository.save(toMeasurementEntity(saved.getId(), event.measurements(), MeasurementSource.ML));
        }

        CheckEventType eventType = switch (status) {
            case AUTO_PASSED -> CheckEventType.AUTO_PASSED;
            case AUTO_DEFECT -> CheckEventType.AUTO_DEFECT;
            case PENDING_MANUAL -> CheckEventType.SENT_TO_MANUAL_REVIEW;
            default -> CheckEventType.RECEIVED_FROM_ML;
        };
        eventRepository.save(newEvent(saved.getId(), null, status, "ML", eventType,
                "mlVerdict=" + entity.getMlVerdict() + ", confidence=" + entity.getConfidence()));

        broadcastSafe(toListItemDto(saved));
    }

    // ================================================================
    // РУЧНОЙ КОНТРОЛЬ
    // ================================================================

    public List<FlangeCheckListItemDto> getManualReviewQueue() {
        return flangeCheckRepository.findByStatusOrderByTimestampAsc(CheckStatus.PENDING_MANUAL)
                .stream()
                .map(this::toListItemDto)
                .toList();
    }

    @Transactional
    public FlangeCheckDetailsDto manualReview(Long id, ManualReviewRequest req, String operatorId) {
        FlangeCheckEntity entity = flangeCheckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flange check not found: " + id));

        if (entity.getStatus() != CheckStatus.PENDING_MANUAL) {
            throw new IllegalStateException(
                    "Check " + id + " is not pending manual review (current status=" + entity.getStatus() + ")");
        }

        CheckStatus fromStatus = entity.getStatus();
        CheckStatus toStatus;
        CheckEventType eventType;

        if (Boolean.TRUE.equals(req.approved())) {
            toStatus = CheckStatus.MANUAL_PASSED;
            eventType = CheckEventType.MANUAL_PASSED;
            entity.setFinalVerdict(Verdict.NORMAL);
            entity.setFinalDefectType(DefectType.NONE);
        } else {
            toStatus = CheckStatus.MANUAL_DEFECT;
            eventType = CheckEventType.MANUAL_DEFECT;
            entity.setFinalVerdict(Verdict.DEFECT);
            entity.setFinalDefectType(
                    req.defectType() != null ? req.defectType() : effectiveDefectType(entity));
        }

        entity.setStatus(toStatus);
        entity.setReviewedBy(operatorId);
        entity.setReviewedAt(LocalDateTime.now());
        flangeCheckRepository.save(entity);

        if (req.measurements() != null) {
            measurementRepository.save(toMeasurementEntity(id, req.measurements(), MeasurementSource.MANUAL));
        }

        eventRepository.save(newEvent(id, fromStatus, toStatus, operatorId, eventType,
                req.comment() != null ? req.comment() : ""));

        broadcastSafe(toListItemDto(entity));

        return getCheckById(id);
    }

    // ================================================================
    // ЧТЕНИЕ
    // ================================================================

    public FlangeCheckDetailsDto getCheckById(Long id) {
        FlangeCheckEntity entity = flangeCheckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flange check not found: " + id));

        List<FlangeCheckEventEntity> events =
                eventRepository.findByFlangeCheckIdOrderByOccurredAtAsc(id);

        MeasurementsDto measurements = measurementRepository
                .findFirstByFlangeCheckIdOrderByIdDesc(id)
                .map(this::toMeasurementsDto)
                .orElse(null);

        return toDetailsDto(entity, events, measurements);
    }

    public List<FlangeCheckListItemDto> receiveCheckHistory(int page, int size, Map<String, String> filters) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        String statusStr = filters != null ? filters.get("status") : null;
        String lineId = filters != null ? filters.get("lineId") : null;

        Page<FlangeCheckEntity> result;
        if (statusStr != null && !statusStr.isBlank()) {
            result = flangeCheckRepository.findByStatus(CheckStatus.valueOf(statusStr), pageable);
        } else if (lineId != null && !lineId.isBlank()) {
            result = flangeCheckRepository.findByLineId(lineId, pageable);
        } else {
            result = flangeCheckRepository.findAll(pageable);
        }
        return result.getContent().stream().map(this::toListItemDto).toList();
    }

    // ================================================================
    // АНАЛИТИКА
    // ================================================================

    public StatsDto processStats(String from, String to) {
        LocalDateTime fromDt = parseDateTime(from, LocalDate.now().atStartOfDay());
        LocalDateTime toDt = parseDateTime(to, LocalDateTime.now());

        List<FlangeCheckEntity> checks = flangeCheckRepository.findByTimestampBetween(fromDt, toDt);

        long total = checks.size();
        long defects = checks.stream().filter(c -> effectiveVerdict(c) == Verdict.DEFECT).count();
        long passed = total - defects;
        long manualDone = checks.stream()
                .filter(c -> c.getStatus() == CheckStatus.MANUAL_PASSED
                        || c.getStatus() == CheckStatus.MANUAL_DEFECT)
                .count();
        long pending = checks.stream()
                .filter(c -> c.getStatus() == CheckStatus.PENDING_MANUAL)
                .count();

        double defectRate = total == 0 ? 0.0 : round((double) defects / total * 100);
        double manualRate = total == 0 ? 0.0 : round((double) (manualDone + pending) / total * 100);

        return new StatsDto(total, defects, passed, manualDone, pending, defectRate, manualRate);
    }

    public AnalyticsDto processAnalytics(String from, String to) {
        LocalDateTime fromDt = parseDateTime(from, LocalDate.now().minusDays(30).atStartOfDay());
        LocalDateTime toDt = parseDateTime(to, LocalDateTime.now());

        List<FlangeCheckEntity> checks = flangeCheckRepository.findByTimestampBetween(fromDt, toDt);

        // По дням
        Map<LocalDate, List<FlangeCheckEntity>> byDay = checks.stream()
                .collect(Collectors.groupingBy(c -> c.getTimestamp().toLocalDate()));
        List<AnalyticsDto.DailyRow> daily = byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    long t = e.getValue().size();
                    long d = e.getValue().stream().filter(c -> effectiveVerdict(c) == Verdict.DEFECT).count();
                    double r = t == 0 ? 0.0 : round((double) d / t * 100);
                    return new AnalyticsDto.DailyRow(e.getKey().toString(), t, d, r);
                })
                .toList();

        // По типам дефектов
        Map<DefectType, Long> byType = checks.stream()
                .filter(c -> effectiveVerdict(c) == Verdict.DEFECT)
                .collect(Collectors.groupingBy(this::effectiveDefectType, Collectors.counting()));
        long totalDefects = byType.values().stream().mapToLong(Long::longValue).sum();
        List<AnalyticsDto.DefectRow> defectRows = byType.entrySet().stream()
                .sorted(Map.Entry.<DefectType, Long>comparingByValue().reversed())
                .map(e -> new AnalyticsDto.DefectRow(
                        e.getKey(), displayName(e.getKey()), e.getValue(),
                        totalDefects == 0 ? 0.0 : round((double) e.getValue() / totalDefects * 100)))
                .toList();

        // По линиям
        Map<String, List<FlangeCheckEntity>> byLine = checks.stream()
                .collect(Collectors.groupingBy(FlangeCheckEntity::getLineId));
        List<AnalyticsDto.LineRow> lineRows = byLine.entrySet().stream()
                .map(e -> {
                    long t = e.getValue().size();
                    long d = e.getValue().stream().filter(c -> effectiveVerdict(c) == Verdict.DEFECT).count();
                    double r = t == 0 ? 0.0 : round((double) d / t * 100);
                    return new AnalyticsDto.LineRow(e.getKey(), t, d, r);
                })
                .sorted(Comparator.comparing(AnalyticsDto.LineRow::lineId))
                .toList();

        return new AnalyticsDto(
                fromDt.format(DT_FMT), toDt.format(DT_FMT),
                processStats(from, to),
                daily, defectRows, lineRows);
    }

    // ================================================================
    // SPC — карта Шухарта
    // ================================================================

    public SpcDto getSpcControlChart() {
        List<FlangeMeasurementEntity> measurements =
                measurementRepository.findTop30BySourceOrderByIdDesc(MeasurementSource.ML);

        if (measurements.isEmpty()) {
            return new SpcDto("outerDiameter", null, null, null, null, List.of());
        }

        // стабилизируем порядок: от старых к новым
        List<FlangeMeasurementEntity> ordered = new ArrayList<>(measurements);
        Collections.reverse(ordered);

        List<Double> values = ordered.stream()
                .map(FlangeMeasurementEntity::getOuterDiameter)
                .filter(Objects::nonNull)
                .toList();

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum() / Math.max(1, values.size() - 1);
        double sigma = Math.sqrt(variance);

        double ucl = mean + 3 * sigma;
        double lcl = mean - 3 * sigma;

        List<SpcDto.SpcPoint> points = values.stream()
                .map(v -> new SpcDto.SpcPoint(
                        null,  // timestamp на точке не критичен, оставим null
                        round(v),
                        v > ucl || v < lcl))
                .toList();

        return new SpcDto(
                "outerDiameter",
                round(mean), round(ucl), round(lcl),
                null,  // Cpk — считается от spec-полей, которых пока нет
                points);
    }

    // ================================================================
    // ХЕЛПЕРЫ
    // ================================================================

    private CheckStatus resolveStatus(FlangeCheckResultDto event) {
        double conf = event.confidence() != null ? event.confidence() : 0.0;
        if (conf < autoDefectThreshold) {
            return CheckStatus.PENDING_MANUAL;
        }
        return event.mlVerdict() == Verdict.DEFECT
                ? CheckStatus.AUTO_DEFECT
                : CheckStatus.AUTO_PASSED;
    }

    private Verdict effectiveVerdict(FlangeCheckEntity e) {
        return e.getFinalVerdict() != null ? e.getFinalVerdict() : e.getMlVerdict();
    }

    private DefectType effectiveDefectType(FlangeCheckEntity e) {
        if (e.getFinalDefectType() != null) return e.getFinalDefectType();
        return e.getMlDefectType() != null ? e.getMlDefectType() : DefectType.NONE;
    }

    private FlangeCheckEventEntity newEvent(Long checkId, CheckStatus from, CheckStatus to,
                                            String actor, CheckEventType type, String details) {
        FlangeCheckEventEntity ev = new FlangeCheckEventEntity();
        ev.setFlangeCheckId(checkId);
        ev.setOccurredAt(LocalDateTime.now());
        ev.setFromStatus(from);
        ev.setToStatus(to);
        ev.setActor(actor);
        ev.setCheckEventType(type);
        ev.setDetails(details);
        return ev;
    }

    private FlangeMeasurementEntity toMeasurementEntity(Long checkId, MeasurementsDto dto, MeasurementSource source) {
        FlangeMeasurementEntity m = new FlangeMeasurementEntity();
        m.setFlangeCheckId(checkId);
        m.setOuterDiameter(dto.outerDiameter());
        m.setInnerDiameter(dto.innerDiameter());
        m.setBoltHoleCount(dto.boltHoleCount());
        m.setBoltHoleDiameter(dto.boltHoleDiameter());
        m.setBoltCircleDiameter(dto.boltCircleDiameter());
        m.setOvality(dto.ovality());
        m.setSource(source);
        return m;
    }

    private MeasurementsDto toMeasurementsDto(FlangeMeasurementEntity m) {
        return new MeasurementsDto(
                m.getOuterDiameter(), m.getInnerDiameter(), m.getBoltHoleCount(),
                m.getBoltHoleDiameter(), m.getBoltCircleDiameter(), m.getOvality());
    }

    private FlangeCheckListItemDto toListItemDto(FlangeCheckEntity e) {
        return new FlangeCheckListItemDto(
                e.getId(), e.getFlangeId(), e.getLineId(), e.getFlangeType(),
                formatTime(e.getTimestamp()),
                effectiveVerdict(e), e.getStatus(), effectiveDefectType(e));
    }

    private FlangeCheckDetailsDto toDetailsDto(FlangeCheckEntity e,
                                               List<FlangeCheckEventEntity> events,
                                               MeasurementsDto measurements) {
        List<FlangeCheckEventDto> history = events.stream()
                .map(ev -> new FlangeCheckEventDto(
                        formatTime(ev.getOccurredAt()), ev.getActor(),
                        ev.getFromStatus(), ev.getToStatus(),
                        ev.getCheckEventType(), ev.getDetails()))
                .toList();
        return new FlangeCheckDetailsDto(
                e.getId(), e.getFlangeId(), e.getLineId(), e.getFlangeType(),
                formatTime(e.getTimestamp()),
                e.getMlVerdict(), e.getFinalVerdict(),
                e.getMlDefectType(), e.getFinalDefectType(),
                e.getConfidence(), e.getStatus(),
                e.getReviewedBy(), formatTime(e.getReviewedAt()),
                measurements, history, e.getUrl());
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(DT_FMT);
    }

    private String displayName(DefectType type) {
        if (type == null) return "—";
        return switch (type) {
            case CRACK -> "Трещина";
            case GEOMETRY_OUT_OF_TOLERANCE -> "Нарушение геометрии";
            case SURFACE_DEFECT -> "Поверхностный дефект";
            case HOLE_DEFECT -> "Дефект отверстия";
            case POROSITY -> "Пористость";
            case BURR -> "Заусенец";
            case UNKNOWN -> "Неизвестный дефект";
            case NONE -> "Без дефекта";
        };
    }

    private LocalDateTime parseDateTime(String s, LocalDateTime defaultValue) {
        if (s == null || s.isBlank()) return defaultValue;
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(s).atStartOfDay();
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Invalid date format: " + s, e2);
            }
        }
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private void broadcastSafe(FlangeCheckListItemDto dto) {
        try {
            notificationService.broadcast(dto);
        } catch (Exception ex) {
            log.warn("SSE broadcast failed: {}", ex.getMessage());
        }
    }
}