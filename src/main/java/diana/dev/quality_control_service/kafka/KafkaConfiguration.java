package diana.dev.quality_control_service.kafka;

import diana.dev.quality_control_service.api.dto.kafka.FlangeCheckResultDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfiguration {

    @Bean
    public ConsumerFactory<String, FlangeCheckResultDto> flangeCheckResultConsumerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "diana.dev.quality_control_service.api");
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, FlangeCheckResultDto.class.getName());
        // Python не шлёт Kafka type headers — заставляем десериализатор всегда
        // использовать VALUE_DEFAULT_TYPE, а не пытаться читать __TypeId__ из заголовков
        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaListenerContainerFactory<?> flangeCheckResultListenerFactory(
            ConsumerFactory<String, FlangeCheckResultDto> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, FlangeCheckResultDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(false);

        // «битые» сообщения (невалидный JSON, несовпадение структуры) логируем и пропускаем,
        // иначе consumer зациклится на них и топик встанет
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, ex) -> log.error("Skipping bad Kafka record: topic={}, partition={}, offset={}",
                        record.topic(), record.partition(), record.offset(), ex),
                new FixedBackOff(0L, 0L)
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}