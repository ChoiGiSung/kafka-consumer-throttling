package com.coco.throttling

import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.ListenerContainerPauseService
import java.time.Duration

class KafkaThrottlingInterceptor : ConsumerInterceptor<String, String> {

    companion object {
        const val PAUSE_SERVICE_CONFIG_KEY = "pauseService"
        const val PAUSE_TIME_CALCULATOR_CONFIG_KEY = "pauseTimeCalculator"
        const val KAFKA_LISTENER_ENDPOINT_REGISTRY_CONFIG_KEY = "listenerRegistry"
    }

    private lateinit var pauser: ListenerContainerPauseService
    private lateinit var registry: KafkaListenerEndpointRegistry
    private lateinit var delayTimeCalculator: DelayTimeCalculator

    override fun configure(configs: MutableMap<String, *>) {
        this.pauser = configs[PAUSE_SERVICE_CONFIG_KEY] as ListenerContainerPauseService
        this.delayTimeCalculator = configs[PAUSE_TIME_CALCULATOR_CONFIG_KEY] as DelayTimeCalculator
        this.registry = configs[KAFKA_LISTENER_ENDPOINT_REGISTRY_CONFIG_KEY] as KafkaListenerEndpointRegistry
    }

    override fun close() {
    }

    override fun onConsume(records: ConsumerRecords<String, String>): ConsumerRecords<String, String> {
        return records
    }

    override fun onCommit(offsets: MutableMap<TopicPartition, OffsetAndMetadata>) {
        //각 컨슈머 별로 commit하지 않나? 왜 파티션이 여러개일까
        val targetContainers = findTargetContainers(offsets.keys, findTargetContainers())

        targetContainers.forEach { container ->
            val pauseTime = delayTimeCalculator.calculateDelayTime()
            if (pauseTime > 0) {
                pauser.pause(container, Duration.ofMillis(pauseTime))
            }
        }
    }

    private fun findTargetContainers(): List<ConcurrentMessageListenerContainer<String, String>> {
        return registry.listenerContainers
            .filterIsInstance<ConcurrentMessageListenerContainer<String, String>>()
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