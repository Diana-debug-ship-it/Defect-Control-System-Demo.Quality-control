package diana.dev.quality_control_service.domain.db.entity;

import diana.dev.quality_control_service.domain.enums.CheckEventType;
import diana.dev.quality_control_service.domain.enums.CheckStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class FlangeCheckEventEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "flange_check_event_seq"
    )
    @SequenceGenerator(
            name = "flange_check_event_seq",
            sequenceName = "flange_check_event_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "flange_check_id", nullable = false)
    private String flangeCheckId;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = true)
    private CheckStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private CheckStatus toStatus;

    @Column(name = "actor", nullable = false)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_event_type", nullable = false)
    private CheckEventType checkEventType;

    @Column(name = "details", nullable = true)
    private String details;
}
