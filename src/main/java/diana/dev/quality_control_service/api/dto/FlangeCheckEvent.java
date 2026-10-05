package diana.dev.quality_control_service.api.dto;

import diana.dev.quality_control_service.domain.enums.DefectType;
import diana.dev.quality_control_service.domain.enums.FlangeType;
import diana.dev.quality_control_service.domain.enums.MLVerdict;

import java.time.LocalDateTime;

public record FlangeCheckEvent(

        String hubId,
        String lineId,
        FlangeType flangeType,
        LocalDateTime timestamp,

        MLVerdict mlVerdict,
        DefectType defectType,
        Double confidence,

        MeasurementsDto measurements
) { }
