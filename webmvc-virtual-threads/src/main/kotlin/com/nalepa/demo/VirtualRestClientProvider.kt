package com.nalepa.demo

import org.springframework.http.client.ReactorClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient


@Component
class VirtualRestClientProvider(
    private val restClientBuilder: RestClient.Builder,
) {

    fun createRestClient(): RestClient {
        return restClientBuilder
            .requestFactory(ReactorClientHttpRequestFactory())
            .build()
    }

}