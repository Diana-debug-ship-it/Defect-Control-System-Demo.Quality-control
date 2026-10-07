package diana.dev.quality_control_service.api.dto.web;

import diana.dev.quality_control_service.domain.enums.CheckStatus;
import diana.dev.quality_control_service.domain.enums.DefectType;
import diana.dev.quality_control_service.domain.enums.FlangeType;
import diana.dev.quality_control_service.domain.enums.Verdict;

public record FlangeCheckListItemDto(
        Long id,
        String flangeId,
        String lineId,
        FlangeType flangeType,
        String formattedTime,
        Verdict finalVerdict,
        CheckStatus status,
        DefectType finalDefectType
) { }
