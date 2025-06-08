package uz.consortgroup.payment_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import uz.consortgroup.payment_service.topic.KafkaTopic;

import java.util.List;

@Component
@Slf4j
public class CoursePurchasedProducer extends AbstractProducer {
    private final KafkaTopic kafkaTopic;

    public CoursePurchasedProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopic kafkaTopic) {
        super(kafkaTemplate);
        this.kafkaTopic = kafkaTopic;
    }

    public void sendCoursePurchasedEvents(List<Object> messages) {
        log.info("Sending {} course events to topic '{}'", messages.size(), getTopic());
        sendEventToTopic(getTopic(), messages);
    }

    @Override
    protected String getTopic() {
        return kafkaTopic.getCoursePurchasedTopic();
    }
}

