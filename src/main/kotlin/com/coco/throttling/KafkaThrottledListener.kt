package com.coco.throttling

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ListenerContainerPauseService
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.time.Duration

@Component
class KafkaThrottledListener(
) {

    companion object {
        const val TOPIC = "throttled-topic1"
        private const val listenerId = "$TOPIC-listener"

        const val TOPIC_2 = "throttled-topic2"
        private const val listenerId2 = "$TOPIC_2-listener"
    }

    @KafkaThrottling
    @KafkaListener(
        id = listenerId,
        topics = [TOPIC],
        concurrency = "3",
    )
    fun listenfff(
        record: ConsumerRecord<String, String>,
        @Payload message: String,
    ) {
        println("listen - $message")
    }


    @KafkaListener(
        id = listenerId2,
        topics = [TOPIC_2],
        concurrency = "3",
    )
    fun listen2(
        record: ConsumerRecord<String, String>,
        @Payload message: String,
    ) {
        println("listen - $message")
    }

}