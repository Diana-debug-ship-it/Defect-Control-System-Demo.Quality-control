package diana.dev.quality_control_service.domain;

public enum CheckEventType {
    RECEIVED_FROM_ML,
    AUTO_PASSED,
    AUTO_DEFECT,
    SENT_TO_MANUAL_REVIEW,
    MANUAL_PASSED,
    MANUAL_DEFECT,
    REOPENED,
    TIMEOUT
}
