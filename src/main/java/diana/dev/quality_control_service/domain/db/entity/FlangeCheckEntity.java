package diana.dev.quality_control_service.domain.db.entity;


import diana.dev.quality_control_service.domain.enums.CheckStatus;
import diana.dev.quality_control_service.domain.enums.DefectType;
import diana.dev.quality_control_service.domain.enums.FlangeType;
import diana.dev.quality_control_service.domain.enums.Verdict;
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

    @Column(name = "line_id", nullable = false)
    private String lineId;

    @Column(name = "flange_type", nullable = false)
    private FlangeType flangeType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "ml_verdict", nullable = false)
    private Verdict mlVerdict;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_verdict")
    private Verdict finalVerdict;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CheckStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "ml_defect_type")
    private DefectType mlDefectType;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_defect_type")
    private DefectType finalDefectType;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "url")
    private String url;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
