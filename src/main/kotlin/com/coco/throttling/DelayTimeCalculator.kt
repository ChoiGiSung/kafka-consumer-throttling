package com.coco.throttling

interface DelayTimeCalculator {
    fun calculateDelayTime(): Long

    class CpuMonitoringDelayTimeCalculator : DelayTimeCalculator {
        override fun calculateDelayTime(): Long {
            // TODO: implement CPU monitoring delay time calculator
            return 0
        }
    }
}