package diana.dev.quality_control_service.domain.db.repository;

import diana.dev.quality_control_service.domain.db.entity.FlangeCheckEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlangeCheckEventRepository extends JpaRepository<FlangeCheckEvent, Long> {
}
