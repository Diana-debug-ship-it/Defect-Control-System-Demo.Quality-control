package diana.dev.quality_control_service.domain.db.entity;


import diana.dev.quality_control_service.domain.enums.CheckStatus;
import diana.dev.quality_control_service.domain.enums.MLVerdict;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "flange_checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlangeCheckEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "flange_check_seq"
    )
    @SequenceGenerator(
            name = "flange_check_seq",
            sequenceName = "flange_check_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "flange_id", nullable = false)
    private String flangeId;


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

    @Column(name = "geometry_metrics", columnDefinition = "jsonb", nullable = true)
    private String geometryMetrics;

}
