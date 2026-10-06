package diana.dev.quality_control_service.api.dto.web;

import diana.dev.quality_control_service.domain.enums.CheckEventType;
import diana.dev.quality_control_service.domain.enums.CheckStatus;

public record FlangeCheckEventDto(
        String formattedTime,
        String actor,
        CheckStatus fromStatus,                // null для первого события
        CheckStatus toStatus,
        CheckEventType eventType,
        String details
) {
}
