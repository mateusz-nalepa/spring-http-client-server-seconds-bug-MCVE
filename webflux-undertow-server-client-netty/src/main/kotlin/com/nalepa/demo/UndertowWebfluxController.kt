package com.nalepa.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.math.sqrt

data class SomeResponse(
    val text: String,
)

const val DUMMY_INDEX = "DUMMY_INDEX"

@RestController
class UndertowWebfluxController(
    private val undertowAppWebClientProvider: UndertowAppWebClientProvider,
) {

    private val webClient = undertowAppWebClientProvider.createWebClient()

    @GetMapping("/endpoint/{index}/{mockDelaySeconds}/{cpuOperationDelaySeconds}")
    fun endpoint(
        @PathVariable index: String,
        @PathVariable mockDelaySeconds: Long,
        @PathVariable cpuOperationDelaySeconds: Long,
    ): Mono<ResponseEntity<SomeResponse>> {
        return getData(index, mockDelaySeconds)
            .doOnNext { someHeavyCpuOperation(cpuOperationDelaySeconds) }
            .map { ResponseEntity.ok(it) }
    }

    private fun getData(index: String, mockDelaySeconds: Long): Mono<SomeResponse> {
        val startTime = System.nanoTime()

        return webClient
            .get()
            .uri("http://localhost:8083/mock/$index/$mockDelaySeconds")
            .header(DUMMY_INDEX, index)
            .retrieve()
            .bodyToMono(SomeResponse::class.java)
            .doOnNext {
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                UndertowAppLogger.log(this, "Index: $index. Got response from webClient after: $duration")
            }
    }

    private fun someHeavyCpuOperation(cpuOperationDelaySeconds: Long) {
        val startTime = System.nanoTime()
        var iterations = 0L
        while (Duration.ofNanos(System.nanoTime() - startTime).seconds < cpuOperationDelaySeconds) {
            sqrt(iterations.toDouble())
            iterations++
        }
    }

}