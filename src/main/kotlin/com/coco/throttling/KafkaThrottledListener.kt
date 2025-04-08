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
    private val pauser: ListenerContainerPauseService,
    private val delayTimeCalculator: DelayTimeCalculator,
    private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry,
) {

    companion object {
        const val TOPIC = "throttled-topic"
        private const val listenerId = "$TOPIC-listener"
    }

    @KafkaListener(
        id = listenerId,
        topics = [TOPIC],
    )
    fun listen(
        record: ConsumerRecord<String, String>,
        @Payload message: String,
//        ack?
    ) {
        val container = getContainer(record)

        val delayTime = delayTimeCalculator.calculateDelayTime()
        if (delayTime > 0) {
            pauser.pause(container, Duration.ofMillis(delayTime))
        }

    }

    private fun getContainer(
        record: ConsumerRecord<String, String>,
    ): MessageListenerContainer {
        val container = kafkaListenerEndpointRegistry.getListenerContainer(listenerId)
        return container?.getContainerFor(record.topic(), record.partition()) ?: throw RuntimeException("container not found")
    }
}