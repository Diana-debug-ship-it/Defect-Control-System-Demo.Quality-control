package diana.dev.quality_control_service.api.dto.web;

import diana.dev.quality_control_service.domain.enums.DefectType;

import java.util.List;

public record AnalyticsDto(
        String formattedFrom, String formattedTo,
        StatsDto summary,
        List<DailyRow> daily,
        List<DefectRow> defects,
        List<LineRow> lines
) {
    public record DailyRow(String date, Long total, Long defects, Double defectRate) {}
    public record DefectRow(DefectType type, String displayName, Long count, Double percentage) {}
    public record LineRow(String lineId, Long total, Long defects, Double defectRate) {}
}
