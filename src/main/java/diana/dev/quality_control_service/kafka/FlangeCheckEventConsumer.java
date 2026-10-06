package diana.dev.quality_control_service.kafka;

import diana.dev.quality_control_service.api.dto.kafka.FlangeCheckEvent;
import diana.dev.quality_control_service.domain.FlangeCheckProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlangeCheckEventConsumer {

    private final FlangeCheckProcessor flangeCheckProcessor;

    @KafkaListener(
            topics = "${flange-check-event-topic}",
            groupId = "quality-control-group",
            containerFactory = "flangeCheckEventListenerFactory"
    )
    public void listen(FlangeCheckEvent event) {
        log.info("Received flange check event: delivery={}", event);
        flangeCheckProcessor.processFlangeCheckEvent(event);
    }

}
