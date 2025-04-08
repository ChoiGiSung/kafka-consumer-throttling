package com.coco.throttling

import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

class KafkaThrottlingConfig {
    // 쓰로틀링 대상이 되는 MessageListenerContainer의 개수(ConcurrentMessageListenerContainer 내부 포함)를
    // 넘어서는 스레드가 사용되지 않으므로 고려해서 크기를 결정한다.
    private val throttlingPoolSize = 3

    @Bean
    fun throttlingTaskScheduler(): TaskScheduler {
        return ThreadPoolTaskSchedulerBuilder()
            .poolSize(throttlingPoolSize)
            .build()
    }
}