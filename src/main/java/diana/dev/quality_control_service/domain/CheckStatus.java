package diana.dev.quality_control_service.domain;

public enum CheckStatus {
    PENDING,
    AUTO_PASSED,
    AUTO_DEFECT,
    PENDING_MANUAL,
    MANUAL_PASSED,
    MANUAL_DEFECT
}
