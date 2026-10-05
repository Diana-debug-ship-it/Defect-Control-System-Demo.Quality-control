package diana.dev.quality_control_service.domain.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flange_measurements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlangeMeasurementEntity {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "flange_measurement_seq"
    )
    @SequenceGenerator(
            name = "flange_measurement_seq",
            sequenceName = "flange_measurement_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "flange_check_id", nullable = false)
    private String flangeCheckId;

    @Column(name = "outer_diameter", nullable = false)
    private Double outerDiameter;

    @Column(name = "inner_diameter", nullable = false)
    private Double innerDiameterMm;

    @Column(name = "bolt_hole_count", nullable = false)
    private Integer boltHoleCount;

    @Column(name = "bolt_hole_diameter", nullable = false)
    private Double boltHoleDiameterMm;

    @Column(name = "bolt_circle_diameter", nullable = false)
    private Double boltCircleDiameterMm;

    @Column(name = "ovality", nullable = false)
    private Double ovalityMm;
}
