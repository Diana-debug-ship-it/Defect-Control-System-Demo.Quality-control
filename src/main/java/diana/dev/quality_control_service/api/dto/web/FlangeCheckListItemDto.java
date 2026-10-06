package diana.dev.quality_control_service.api.dto.web;

import diana.dev.quality_control_service.domain.enums.CheckStatus;
import diana.dev.quality_control_service.domain.enums.DefectType;
import diana.dev.quality_control_service.domain.enums.FlangeType;
import diana.dev.quality_control_service.domain.enums.MLVerdict;

public record FlangeCheckListItemDto(
        Long id,
        String flangeId,
        String lineId,
        FlangeType flangeType,
        String formattedTime,
        MLVerdict mlVerdict,
        CheckStatus status,
        DefectType defectType,
        Double confidence
) { }
