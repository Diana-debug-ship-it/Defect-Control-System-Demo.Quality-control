package diana.dev.quality_control_service.domain.db.repository;

import diana.dev.quality_control_service.domain.db.entity.FlangeMeasurementEntity;
import diana.dev.quality_control_service.domain.enums.MeasurementSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlangeMeasurementRepository extends JpaRepository<FlangeMeasurementEntity, Long> {

    List<FlangeMeasurementEntity> findByFlangeCheckId(Long flangeCheckId);

    Optional<FlangeMeasurementEntity> findFirstByFlangeCheckIdOrderByIdDesc(Long flangeCheckId);

    List<FlangeMeasurementEntity> findTop30BySourceOrderByIdDesc(MeasurementSource source);
}