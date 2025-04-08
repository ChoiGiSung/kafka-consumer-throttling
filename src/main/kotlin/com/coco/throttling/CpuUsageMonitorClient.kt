package com.coco.throttling

import java.util.concurrent.atomic.AtomicInteger

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
}