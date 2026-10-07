package diana.dev.quality_control_service.api.dto.kafka;

import diana.dev.quality_control_service.api.dto.common.MeasurementsDto;
import diana.dev.quality_control_service.domain.enums.DefectType;
import diana.dev.quality_control_service.domain.enums.FlangeType;
import diana.dev.quality_control_service.domain.enums.Verdict;

import java.time.LocalDateTime;

public record FlangeCheckResultDto(

        String flangeId,
        String url,
        String lineId,
        FlangeType flangeType,
        LocalDateTime timestamp,

        Verdict mlVerdict,
        DefectType mlDefectType,
        Double confidence,

        MeasurementsDto measurements
) { }
