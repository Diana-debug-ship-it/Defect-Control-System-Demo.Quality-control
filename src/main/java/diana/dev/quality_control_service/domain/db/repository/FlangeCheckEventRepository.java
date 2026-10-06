package diana.dev.quality_control_service.domain.db.repository;

import diana.dev.quality_control_service.domain.db.entity.FlangeCheckEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlangeCheckEventRepository extends JpaRepository<FlangeCheckEventEntity, Long> {
}
