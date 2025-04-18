package com.coco.throttling

import com.coco.throttling.monitoring.CpuUsageMonitorClient.MockCpuUsageMonitorClient
import com.coco.throttling.DelayTimeCalculator.CpuMonitoringDelayTimeCalculator
import com.coco.throttling.monitoring.CpuUsageMonitorClient
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.ListenerContainerPauseService
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class KafkaThrottlingConfig(
    private val registry: KafkaListenerEndpointRegistry,
    private val webClient: WebClient
) {
    // 쓰로틀링 대상이 되는 MessageListenerContainer의 개수(ConcurrentMessageListenerContainer 내부 포함)를
    // 넘어서는 스레드가 사용되지 않으므로 고려해서 크기를 결정한다.
    private val throttlingPoolSize = 3

    @Bean
    fun throttlingTaskScheduler(): TaskScheduler {
        return ThreadPoolTaskSchedulerBuilder().poolSize(throttlingPoolSize).build()
    }

    @Bean
    fun listenerContainerPauseService(): ListenerContainerPauseService {
        return ListenerContainerPauseService(registry, throttlingTaskScheduler())
    }

    @Bean
    fun cpuMonitoringDelayTimeCalculator(): DelayTimeCalculator {
//        return CpuMonitoringDelayTimeCalculator(MockCpuUsageMonitorClient())
        return CpuMonitoringDelayTimeCalculator(CpuUsageMonitorClient.PrometheusCpuUsageMonitorClient(webClient))
    }

}