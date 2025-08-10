package com.nalepa.demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalTime

object RequestSenderConfig {
    val availableThreads = Runtime.getRuntime().availableProcessors()
    val numberOfRequestsToBeSent = availableThreads * 2
}

@RestController
class RequestSenderEndpoint(
    private val webClientBuilder: WebClient.Builder,
) {

    private val webClient = webClientBuilder.build()

    @GetMapping("/warmup/{appServerType}")
    fun warmup(
        @PathVariable appServerType: String,
    ): Mono<String> =
        sendRequest(-1, 0, 0, appServerType)
            .map {
                RequestSenderLogger.log(this, "Warmup DONE")
                "Warmup DONE"
            }

    @GetMapping("/send-requests-default/{appServerType}")
    fun simulateDefaultBehaviour(
        @PathVariable appServerType: String,
    ): Mono<String> =
        executeSimulation("DEFAULT", appServerType)

    private fun executeSimulation(path: String, appServerType: String): Mono<String> {
        val startTime = System.nanoTime()

        RequestSenderLogger.log(this, "Start simulation for path: $path")

        return step1SendBatchOfRequestsAndDontWaitForResponse(
            mockAppDelaySeconds = 0,
            appCpuDelaySeconds = 10,
            appServerType = appServerType,
        )
            .flatMap {
                step2DelayToMakeSureThatRequestsAreBeingProcessed(requestSenderDelaySeconds = 2)
            }
            .flatMap {
                step3SendBatchOfRequestsAndWaitForResponse(
                    mockAppDelaySeconds = 7,
                    appCpuDelaySeconds = 10,
                    appServerType = appServerType,
                )
            }
            .map {
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                val message = "End simulation for path: $path. Took: $duration"
                RequestSenderLogger.log(this, message)
                message
            }

    }

    private fun step1SendBatchOfRequestsAndDontWaitForResponse(
        mockAppDelaySeconds: Int,
        appCpuDelaySeconds: Int,
        appServerType: String,
    ): Mono<List<String>> {
        RequestSenderLogger.log(this, "Start step1SendBatchOfRequestsAndDontWaitForResponse")

        return Flux
            .range(0, RequestSenderConfig.availableThreads)
            .flatMap { index ->
                sendRequest(index, mockAppDelaySeconds, appCpuDelaySeconds, appServerType)
                    // we are NOT waiting for response
                    .subscribe()
                Mono.just("dummy")
            }
            .collectList() // to be sure, that we sent all requests
    }

    private fun step2DelayToMakeSureThatRequestsAreBeingProcessed(requestSenderDelaySeconds: Int): Mono<String> {
        return Mono.just("XD")
            .doOnNext {
                RequestSenderLogger.log(this, "Start step2DelayToMakeSureThatRequestsAreBeingProcessed")
            }
            .delayElement(Duration.ofSeconds(requestSenderDelaySeconds.toLong()))
            .doOnNext {
                RequestSenderLogger.log(this, "End step2DelayToMakeSureThatRequestsAreBeingProcessed")
            }
    }

    private fun step3SendBatchOfRequestsAndWaitForResponse(
        mockAppDelaySeconds: Int,
        appCpuDelaySeconds: Int,
        appServerType: String,
    ): Mono<List<String>> {
        RequestSenderLogger.log(this, "Start step3SendBatchOfRequestsAndWaitForResponse")

        return Flux
            .range(
                RequestSenderConfig.availableThreads,
                RequestSenderConfig.numberOfRequestsToBeSent - RequestSenderConfig.availableThreads,
            )
            .flatMap { index ->
                sendRequest(index, mockAppDelaySeconds, appCpuDelaySeconds, appServerType)
            }
            .collectList() // to be sure, that we sent all requests
    }

    private fun sendRequest(
        index: Int,
        mockDelaySeconds: Int,
        appCpuOperationDelaySeconds: Int,
        appServerType: String
    ): Mono<String> {

        val port =
            when (appServerType) {
                "undertow" -> "8082"
                "virtual" -> "8085"
                else -> throw RuntimeException("not supported server: $appServerType")
            }

        return webClient
            .get()
            .uri("http://localhost:$port/endpoint/$index/$mockDelaySeconds/$appCpuOperationDelaySeconds")
            .retrieve()
            .bodyToMono(String::class.java)
    }
}

object RequestSenderLogger {
    fun log(caller: Any, message: String) {
        println("${LocalTime.now()} : ${caller.javaClass.simpleName} : ${Thread.currentThread().name} ### $message")
    }
}


