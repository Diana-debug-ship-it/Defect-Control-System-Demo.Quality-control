package diana.dev.quality_control_service.domain.db.repository;

import diana.dev.quality_control_service.domain.db.entity.FlangeCheckEntity;
import diana.dev.quality_control_service.domain.enums.CheckStatus;
import diana.dev.quality_control_service.domain.enums.FlangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FlangeCheckRepository extends JpaRepository<FlangeCheckEntity, Long> {

    Optional<FlangeCheckEntity> findByFlangeId(String flangeId);

    Page<FlangeCheckEntity> findByStatus(CheckStatus status, Pageable pageable);

    Page<FlangeCheckEntity> findByLineId(String lineId, Pageable pageable);

    Long countByStatusIn(Collection<CheckStatus> statuses);

    Long countByFlangeTypeAndStatusIn(FlangeType type, Collection<CheckStatus> statuses);

    List<FlangeCheckEntity> findByStatusOrderByTimestampAsc(CheckStatus status);

    List<FlangeCheckEntity> findByTimestampBetween(LocalDateTime from, LocalDateTime to);

    Long countByTimestampBetween(LocalDateTime from, LocalDateTime to);

    Long countByTimestampBetweenAndFinalVerdict(LocalDateTime from, LocalDateTime to,
                                                diana.dev.quality_control_service.domain.enums.Verdict verdict);

    Long countByTimestampBetweenAndStatusIn(LocalDateTime from, LocalDateTime to,
                                            Collection<CheckStatus> statuses);
}