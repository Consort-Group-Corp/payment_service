package uz.consortgroup.payment_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    protected void sendEventToTopic(String topic, List<Object> messages) {
        messages.forEach(message -> {
            try {
                log.info("Sending message to Kafka topic '{}' : {}", topic, message);
                kafkaTemplate.send(topic, message);
            } catch (Exception ex) {
                log.error("Failed to send message to Kafka topic '{}'", topic, ex);
                throw ex;
            }
        });
    }

    protected abstract String getTopic();
}
