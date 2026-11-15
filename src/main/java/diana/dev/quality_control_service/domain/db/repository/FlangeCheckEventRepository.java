package diana.dev.quality_control_service.domain.db.repository;

import diana.dev.quality_control_service.domain.db.entity.FlangeCheckEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlangeCheckEventRepository extends JpaRepository<FlangeCheckEventEntity, Long> {

    List<FlangeCheckEventEntity> findByFlangeCheckIdOrderByOccurredAtAsc(Long flangeCheckId);
}