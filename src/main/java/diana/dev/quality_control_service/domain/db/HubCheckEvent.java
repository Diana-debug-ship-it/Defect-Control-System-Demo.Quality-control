package diana.dev.quality_control_service.domain.db;

import diana.dev.quality_control_service.domain.CheckEventType;
import diana.dev.quality_control_service.domain.CheckStatus;
import diana.dev.quality_control_service.domain.MLVerdict;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class HubCheckEvent {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "hub_check_event_seq"
    )
    @SequenceGenerator(
            name = "hub_check_event_seq",
            sequenceName = "hub_check_event_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "hub_check_id", nullable = false)
    private String hubCheckId;

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
