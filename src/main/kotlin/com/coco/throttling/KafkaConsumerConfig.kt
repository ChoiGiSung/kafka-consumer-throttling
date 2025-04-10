package com.coco.throttling

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ListenerContainerPauseService

@Configuration
class KafkaConsumerConfig(
    private val pauser: ListenerContainerPauseService,
    private val registry: KafkaListenerEndpointRegistry,
    private val delayTimeCalculator: DelayTimeCalculator,
    private val throttlingListenerFinder: ThrottlingListenerFinder
) : DefaultKafkaConsumerFactoryCustomizer {

    override fun customize(consumerFactory: DefaultKafkaConsumerFactory<*, *>) {
        val customConfig = mapOf(
            KafkaThrottlingInterceptor.PAUSE_SERVICE_CONFIG_KEY to pauser,
            KafkaThrottlingInterceptor.PAUSE_TIME_CALCULATOR_CONFIG_KEY to delayTimeCalculator,
            KafkaThrottlingInterceptor.KAFKA_LISTENER_ENDPOINT_REGISTRY_CONFIG_KEY to registry,
            ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG to KafkaThrottlingInterceptor::class.java.name,
            KafkaThrottlingInterceptor.KAFKA_THROTTLING_LISTENER_FINDER_CONFIG_KEY to throttlingListenerFinder,
        )
        consumerFactory.updateConfigs(customConfig)
    }

}