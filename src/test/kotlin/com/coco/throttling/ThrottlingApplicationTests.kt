package com.coco.throttling

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka


@EnableAutoConfiguration
@SpringBootTest
@EmbeddedKafka(
    partitions = 6,
    topics = [KafkaThrottledListener.TOPIC],
    brokerProperties = [
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092",
        "max.poll.records=5",
        "auto.create.topics.enable=true",
    ]
)
class ThrottlingApplicationTests {
    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Test
    fun consumerThrottlingTest() {
        (0..100).forEach { sendMessage(it) }
        Thread.sleep(10000)
    }


    private fun sendMessage(i: Int) {
        try {
            val result = kafkaTemplate.send(KafkaThrottledListener.TOPIC, i.toString(), "message$i").get()
            val partition = result.recordMetadata.partition()
        } catch (_: Exception) {
        }
    }
}
