package diana.dev.quality_control_service.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hub_checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HubCheckEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "hub_check_seq"
    )
    @SequenceGenerator(
            name = "hub_check_seq",
            sequenceName = "hub_check_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "ml_verdict", nullable = false)
    private MLVerdict mlVerdict;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CheckStatus status;

    @Column(name = "defect_type", nullable = true)
    private String defectType;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

}
