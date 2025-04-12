package com.coco.throttling.monitoring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class PrometheusClientConfig {

    @Bean
    fun prometheusWebClient(): WebClient =
        WebClient.builder()
            .baseUrl("http://localhost:9090")
            .build()
}
