package com.coco.throttling

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
@KafkaThrottling
@KafkaListener(
    id = "throttled-topic3-listener",
    topics = [KafkaThrottledListener3.TOPIC_3],
    concurrency = "3",
)
class KafkaThrottledListener3(
) {

    companion object {
        const val TOPIC_3 = "throttled-topic3"
        private const val listenerId = "$TOPIC_3-listener"
    }

    @KafkaHandler
    fun listen(
        record: ConsumerRecord<String, String>,
        @Payload message: String,
    ) {
        println("listen - $message")
    }

}