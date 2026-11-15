package diana.dev.quality_control_service.api;

import diana.dev.quality_control_service.api.dto.kafka.FlangeCheckResultDto;
import diana.dev.quality_control_service.api.dto.web.*;
import diana.dev.quality_control_service.domain.FlangeCheckProcessor;
import diana.dev.quality_control_service.domain.SseNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/quality-control")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QualityControlController {

    private final FlangeCheckProcessor flangeCheckProcessor;
    private final SseNotificationService notificationService;

    /** Временная заглушка вместо Keycloak. Заменить на JWT, когда поднимем авторизацию. */
    private static final String DEV_OPERATOR = "dev-operator";

    // ─────────────────────────────────────────────────────────────
    // Вход: результат от нейросети (для тестов; позже — Kafka)
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/test-receive")
    public ResponseEntity<String> testReceive(@RequestBody FlangeCheckResultDto event) {
        log.info("Тестовый запуск: получили данные от нейросети: {}", event);
        flangeCheckProcessor.saveResult(event);
        return ResponseEntity.ok("Данные успешно обработаны процессором!");
    }

    // ─────────────────────────────────────────────────────────────
    // Список проверок + детали
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/checks")
    public ResponseEntity<List<FlangeCheckListItemDto>> getCheckHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam Map<String, String> allParams
    ) {
        log.info("Getting history of checks page={} size={} filters={}", page, size, allParams);
        return ResponseEntity.ok(flangeCheckProcessor.receiveCheckHistory(page, size, allParams));
    }

    @GetMapping("/checks/{id}")
    public ResponseEntity<FlangeCheckDetailsDto> getFlangeCheckById(@PathVariable("id") Long id) {
        log.info("Getting flange check by id={}", id);
        return ResponseEntity.ok(flangeCheckProcessor.getCheckById(id));
    }

    @GetMapping(value = "/checks/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        log.info("Streaming to frontend");
        return notificationService.createConnection();
    }

    // ─────────────────────────────────────────────────────────────
    // Ручной контроль
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/manual-review")
    public ResponseEntity<List<FlangeCheckListItemDto>> getManualReviewQueue() {
        log.info("Getting queue of details sent to manual review");
        return ResponseEntity.ok(flangeCheckProcessor.getManualReviewQueue());
    }

    @PostMapping("/checks/{id}/review")
    public ResponseEntity<FlangeCheckDetailsDto> saveManuallyEnteredData(
            @PathVariable("id") Long id,
            @RequestBody @Valid ManualReviewRequest request
    ) {
        log.info("Manual review for id={}, approved={}, operator={}",
                id, request.approved(), DEV_OPERATOR);
        return ResponseEntity.ok(flangeCheckProcessor.manualReview(id, request, DEV_OPERATOR));
    }

    // ─────────────────────────────────────────────────────────────
    // Статистика и аналитика
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<StatsDto> getStats(
            @RequestParam("from") String from,
            @RequestParam("to") String to
    ) {
        log.info("Getting stats from={} to={}", from, to);
        return ResponseEntity.ok(flangeCheckProcessor.processStats(from, to));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDto> getAnalytics(
            @RequestParam("from") String from,
            @RequestParam("to") String to
    ) {
        log.info("Getting analytics from={} to={}", from, to);
        return ResponseEntity.ok(flangeCheckProcessor.processAnalytics(from, to));
    }

    @GetMapping("/analytics/spc-control-chart")
    public ResponseEntity<SpcDto> getSpcChart() {
        return ResponseEntity.ok(flangeCheckProcessor.getSpcControlChart());
    }
}