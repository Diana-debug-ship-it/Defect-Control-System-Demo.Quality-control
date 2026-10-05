package diana.dev.quality_control_service.domain.db.repository;

import diana.dev.quality_control_service.domain.db.entity.FlangeMeasurementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlangeMeasurementRepository extends JpaRepository<FlangeMeasurementEntity, Long> {
}
