package com.coco.throttling.monitoring

interface Dto {

    data class PrometheusResponse(
        val status: String,
        val data: PrometheusData
    )

    data class PrometheusData(
        val resultType: String,
        val result: List<PrometheusResult>
    )

    data class PrometheusResult(
        val metric: Map<String, String>,
        val value: List<String> // [timestamp, value]
    )

}