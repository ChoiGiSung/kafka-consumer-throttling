package com.coco.throttling

import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.ListenerContainerPauseService
import org.springframework.kafka.listener.adapter.MessagingMessageListenerAdapter
import org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter
import java.time.Duration

class KafkaThrottlingInterceptor : ConsumerInterceptor<String, String> {

    companion object {
        const val PAUSE_SERVICE_CONFIG_KEY = "pauseService"
        const val PAUSE_TIME_CALCULATOR_CONFIG_KEY = "pauseTimeCalculator"
        const val KAFKA_LISTENER_ENDPOINT_REGISTRY_CONFIG_KEY = "listenerRegistry"
        const val KAFKA_THROTTLING_LISTENER_FINDER_CONFIG_KEY = "KAFKA_THROTTLING_LISTENER_FINDER_CONFIG_KEY"
    }

    private lateinit var pauser: ListenerContainerPauseService
    private lateinit var registry: KafkaListenerEndpointRegistry
    private lateinit var delayTimeCalculator: DelayTimeCalculator
    private lateinit var throttlingListenerFinder: ThrottlingListenerFinder

    override fun configure(configs: MutableMap<String, *>) {
        this.pauser = configs[PAUSE_SERVICE_CONFIG_KEY] as ListenerContainerPauseService
        this.delayTimeCalculator = configs[PAUSE_TIME_CALCULATOR_CONFIG_KEY] as DelayTimeCalculator
        this.registry = configs[KAFKA_LISTENER_ENDPOINT_REGISTRY_CONFIG_KEY] as KafkaListenerEndpointRegistry
        this.throttlingListenerFinder = configs[KAFKA_THROTTLING_LISTENER_FINDER_CONFIG_KEY] as ThrottlingListenerFinder
    }

    override fun close() {
    }

    override fun onConsume(records: ConsumerRecords<String, String>): ConsumerRecords<String, String> {
        return records
    }

    override fun onCommit(offsets: MutableMap<TopicPartition, OffsetAndMetadata>) {
        // TODO: Partition Assignment Strategy 확인
        val targetContainers = findTargetContainers(offsets.keys, findTargetContainers())

        targetContainers
            .forEach { container ->
                val pauseTime = delayTimeCalculator.calculateDelayTime()
                if (pauseTime > 0) {
                    pauser.pause(container, Duration.ofMillis(pauseTime))
                }
            }
    }

    private fun findTargetContainers(): List<ConcurrentMessageListenerContainer<String, String>> {
        return registry.listenerContainers
            .filterIsInstance<ConcurrentMessageListenerContainer<String, String>>()
            .filter { throttlingListenerFinder.isThrottlingTarget(it.listenerId) }
    }

    private fun findTargetContainers(
        topicPartitions: Set<TopicPartition>,
        concurrentContainers: List<ConcurrentMessageListenerContainer<String, String>>
    ): List<KafkaMessageListenerContainer<String, String>> {
        return concurrentContainers
            .flatMap { it.containers }
            .filter { container -> containsAnyTopicPartition(topicPartitions, container) }
    }

    private fun containsAnyTopicPartition(
        topicPartitions: Set<TopicPartition>,
        container: KafkaMessageListenerContainer<String, String>
    ): Boolean {
        return topicPartitions.any { partition -> containsTopicPartition(partition, container) }

    }

    private fun containsTopicPartition(
        partition: TopicPartition,
        container: KafkaMessageListenerContainer<String, String>
    ): Boolean {
        return container.assignedPartitions?.contains(partition) ?: false
    }
}