package diana.dev.quality_control_service.domain;

import diana.dev.quality_control_service.api.dto.kafka.FlangeCheckResultDto;
import diana.dev.quality_control_service.domain.db.repository.FlangeCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlangeCheckProcessor {

    private final FlangeCheckRepository flangeCheckRepository;

    private String formatTime(LocalDateTime time) {
        return null;
    }

    public void processFlangeCheckResult(FlangeCheckResultDto event) {
    }
}
