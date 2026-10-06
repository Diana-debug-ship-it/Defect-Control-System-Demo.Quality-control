package diana.dev.quality_control_service.api.dto.common;

public record MeasurementsDto(
        Double outerDiameter,
        Double innerDiameter,
        Integer boltHoleCount,
        Double boltHoleDiameter,
        Double boltCircleDiameter,
        Double ovality
) { }
