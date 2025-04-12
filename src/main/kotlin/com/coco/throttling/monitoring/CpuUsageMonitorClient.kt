package com.coco.throttling.monitoring

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import org.slf4j.LoggerFactory
interface CpuUsageMonitorClient {
    fun getCpuUsage(): Int

    class MockCpuUsageMonitorClient : CpuUsageMonitorClient {
        private val cpuUsageLimit = 90
        private val cpuUsage = AtomicInteger(0)

        override fun getCpuUsage(): Int {
            return cpuUsage.updateAndGet() { current ->
                (current + 5).coerceAtMost(cpuUsageLimit)
            }
        }

    }

    class PrometheusCpuUsageMonitorClient(
        private val webClient: WebClient
    ) : CpuUsageMonitorClient {
        private val log = LoggerFactory.getLogger(javaClass)

        override fun getCpuUsage(): Int {
            return try {
                val rawQuery = "cpu_usage_idle{cpu=\"cpu-total\"}"
                val uri = URI.create("http://localhost:9090/api/v1/query?query=${URLEncoder.encode(rawQuery, StandardCharsets.UTF_8)}")

                val response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(Dto.PrometheusResponse::class.java)
                    .timeout(Duration.ofSeconds(3))
                    .block() ?: throw IllegalStateException("Prometheus response was null")

                val idleStr = response.data.result.firstOrNull()?.value?.getOrNull(1)
                    ?: throw IllegalStateException("No CPU idle data found")

                val idle = idleStr.toDoubleOrNull()
                    ?: throw IllegalStateException("CPU idle metric not parsable")

                val usage = 100.0 - idle
                usage.toInt().coerceIn(0, 100)
            } catch (e: Exception) {
                // 실패 시 fallback: CPU 사용량 0% 로 간주 (최소 쓰로틀)
                log.warn("Failed to fetch CPU usage from Prometheus, defaulting to 0%. Reason: ${e.message}")
                0
            }
        }

    }

}