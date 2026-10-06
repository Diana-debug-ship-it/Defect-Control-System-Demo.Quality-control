package diana.dev.quality_control_service.api.dto.web;

import java.util.List;

public record SpcDto(
        String parameterName,
        Double mean,
        Double ucl,
        Double lcl,
        Double cpk,
        List<SpcPoint> points
) {
    public record SpcPoint(
            String time,
            Double value,
            boolean outOfControl
    ) {}
}
