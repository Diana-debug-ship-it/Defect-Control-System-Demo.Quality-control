package diana.dev.quality_control_service.api.dto.web;

import diana.dev.quality_control_service.api.dto.common.MeasurementsDto;
import diana.dev.quality_control_service.domain.enums.CheckStatus;
import diana.dev.quality_control_service.domain.enums.DefectType;
import diana.dev.quality_control_service.domain.enums.FlangeType;
import diana.dev.quality_control_service.domain.enums.Verdict;

import java.util.List;

public record FlangeCheckDetailsDto(
        Long id,
        String flangeId,
        String lineId,
        FlangeType flangeType,
        String formattedTime,
        Verdict mlVerdict,
        Verdict finalVerdict,
        DefectType mlDefectType,
        DefectType finalDefectType,
        Double confidence,
        CheckStatus status,
        String reviewedBy,
        String formattedReviewedAt,
        MeasurementsDto measurements,
        List<FlangeCheckEventDto> history,
        String url
) {
}
