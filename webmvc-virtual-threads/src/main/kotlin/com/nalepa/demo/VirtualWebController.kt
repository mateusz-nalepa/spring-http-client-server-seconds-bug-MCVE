package com.nalepa.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import kotlin.math.sqrt


data class SomeResponse(
    val text: String,
)

const val DUMMY_INDEX = "DUMMY_INDEX"


@RestController
class VirtualWebController(
    private val virtualRestClientProvider: VirtualRestClientProvider,
) {

    private val restClient = virtualRestClientProvider.createRestClient()

    @GetMapping("/endpoint/{index}/{mockDelaySeconds}/{cpuOperationDelaySeconds}")
    fun endpoint(
        @PathVariable index: String,
        @PathVariable mockDelaySeconds: Long,
        @PathVariable cpuOperationDelaySeconds: Long,
    ): ResponseEntity<SomeResponse> {
        VirtualLogger.log(this, "Start endpoint for index: $index")


        return getData(index, mockDelaySeconds)
            .also { someHeavyCpuOperation(cpuOperationDelaySeconds) }
            .let { ResponseEntity.ok(it) }
    }

    private fun getData(index: String, mockDelaySeconds: Long): SomeResponse {
        val startTime = System.nanoTime()

        return restClient
            .get()
            .uri("http://localhost:8083/mock/$index/$mockDelaySeconds")
            .header(DUMMY_INDEX, index)
            .header(WEB_CLIENT_PENDING_REQUEST_TIME, System.nanoTime().toString())
            .retrieve()
            .body(SomeResponse::class.java)!!
            .also {
                val duration = Duration.ofNanos(System.nanoTime() - startTime)
                VirtualLogger.log(this, "Index: $index. Got response from webClient after: $duration")
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
