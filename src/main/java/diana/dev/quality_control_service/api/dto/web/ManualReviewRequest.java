package diana.dev.quality_control_service.api.dto.web;

import diana.dev.quality_control_service.api.dto.common.MeasurementsDto;
import diana.dev.quality_control_service.domain.enums.DefectType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ManualReviewRequest(
        @NotNull Boolean approved,
        @Size(max = 500) String comment,
        DefectType defectType,
        MeasurementsDto measurements
) {
}
