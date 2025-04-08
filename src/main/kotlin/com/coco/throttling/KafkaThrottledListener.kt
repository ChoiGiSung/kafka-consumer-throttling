package com.coco.throttling

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ListenerContainerPauseService
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.messaging.handler.annotation.Payload
import java.lang.RuntimeException
import java.time.Duration

class KafkaThrottledListener(
    private val pauser: ListenerContainerPauseService,
    private val delayTimeCalculator: DelayTimeCalculator,
    private val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry,
) {

    companion object {
        private const val topic = "throttled-topic"
        private const val listenerId = "$topic-listener"
    }

    @KafkaListener(
        id = listenerId,
        topics = [topic],
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