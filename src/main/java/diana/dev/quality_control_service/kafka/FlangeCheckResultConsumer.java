package diana.dev.quality_control_service.kafka;

import diana.dev.quality_control_service.api.dto.kafka.FlangeCheckResultDto;
import diana.dev.quality_control_service.domain.FlangeCheckProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlangeCheckResultConsumer {

    private final FlangeCheckProcessor flangeCheckProcessor;

    @KafkaListener(
            topics = "${flange-check-result-topic}",
            groupId = "quality-control-group",
            containerFactory = "flangeCheckResultListenerFactory"
    )
    public void listen(FlangeCheckResultDto event) {
        log.info("Received flange check result event: {}", event);
        try {
            flangeCheckProcessor.saveResult(event);
            log.info("Successfully processed flange check for flangeId={}", event.flangeId());
        } catch (Exception ex) {
            // логируем и не пробрасываем дальше, чтобы не зациклить consumer
            // (сообщение уже прочитано, повторная обработка не поможет)
            log.error("Failed to process flange check event: {}", event, ex);
        }
    }
}