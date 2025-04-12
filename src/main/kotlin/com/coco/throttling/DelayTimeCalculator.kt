package com.coco.throttling

import com.coco.throttling.monitoring.CpuUsageMonitorClient
import kotlin.math.pow

interface DelayTimeCalculator {
    fun calculateDelayTime(): Long

    class CpuMonitoringDelayTimeCalculator(
        private val cpuUsageMonitorClient: CpuUsageMonitorClient
    ) : DelayTimeCalculator {
        private val allowableCpuUsageLimit = 50 // 쓰로틀링을 최소로 유지하는 CPU 사용률(%)
        private val cpuUsageLimit = 80 // 쓰로틀링을 최대로 하는 CPU 사용률(%)
        private val minDelayTime = 0L // 최소 쓰로틀링일 때 지연 시간 (ms)
        private val maxDelayTime = 500L // 최대 쓰로틀링일 때 지연 시간 (ms)


        override fun calculateDelayTime(): Long {
            val cpuUsage = cpuUsageMonitorClient.getCpuUsage()

            if (cpuUsage > cpuUsageLimit) return maxDelayTime
            if (cpuUsage < allowableCpuUsageLimit) return minDelayTime
            return quadratic(
                allowableCpuUsageLimit,
                minDelayTime,
                cpuUsageLimit,
                maxDelayTime,
                cpuUsage
            )
        }

        /**
         * (x0, y0), (x1, y1) 좌표를 지나는 2차함수
         * y = r(x-x0)^2 + y0
         */
        private fun quadratic(
            x0: Int,
            y0: Long,
            x1: Int,
            y1: Long,
            x: Int
        ): Long {
            val r = (y1 - y0) / (x1 - x0).toDouble().pow(2)
            return (r * (x - x0).toDouble().pow(2) + y0).toLong()
        }
    }
}