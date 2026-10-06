package diana.dev.quality_control_service.api.dto.web;

public record StatsDto(
        Long totalChecked,
        Long totalDefects,
        Long totalPassed,
        Long totalManualReview,
        Long totalPendingManualReview,
        Double defectRate,
        Double manualReviewRate
) { }
