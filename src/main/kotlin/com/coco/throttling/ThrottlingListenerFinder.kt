package com.coco.throttling

import org.springframework.aop.support.AopUtils
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ThrottlingListenerFinder(
    private val applicationContext: ApplicationContext
) : ApplicationListener<ApplicationReadyEvent> {

    private val throttlingListenerIds: MutableList<String> = mutableListOf()

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        applicationContext.beanDefinitionNames.forEach { beanName ->
            // 빈을 가져오고 AOP 프록시 여부를 고려하여 실제 클래스를 얻어옵니다.
            val bean = applicationContext.getBean(beanName)
            val targetClass = AopUtils.getTargetClass(bean)

            val classListenerAnn  = AnnotationUtils.findAnnotation(targetClass, KafkaListener::class.java)
            val classThrottlingAnn  = AnnotationUtils.findAnnotation(targetClass, KafkaThrottling::class.java)

            // 클래스에 둘 다 붙어있으면 등록하고 메소드 스캔 생략
            if (classListenerAnn != null && classThrottlingAnn != null) {
                if (classListenerAnn.id.isNotBlank()) {
                    throttlingListenerIds.add(classListenerAnn.id)
                }
                return@forEach
            }

            // 메소드 레벨에서만 검사
            targetClass.declaredMethods.forEach { method ->
                val methodListenerAnn = AnnotationUtils.findAnnotation(method, KafkaListener::class.java)
                val methodThrottlingAnn = AnnotationUtils.findAnnotation(method, KafkaThrottling::class.java)

                if (methodListenerAnn != null && methodThrottlingAnn != null) {
                    if (methodListenerAnn.id.isNotBlank()) {
                        throttlingListenerIds.add(methodListenerAnn.id)
                    }
                }
            }
        }
    }

    fun isThrottlingTarget(listenerId: String): Boolean {
        return throttlingListenerIds.contains(listenerId)
    }

}