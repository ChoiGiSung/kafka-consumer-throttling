package com.coco.throttling

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ThrottlingApplication

fun main(args: Array<String>) {
	runApplication<ThrottlingApplication>(*args)
}
