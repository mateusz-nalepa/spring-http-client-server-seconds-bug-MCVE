package com.nalepa.demo

import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.ReactorClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient


const val WEB_CLIENT_PENDING_REQUEST_TIME = "WEB_CLIENT_PENDING_REQUEST_TIME"

data class ContextWithStartTime(
    val index: String,
    val startTime: Long,
)

@Component
class VirtualRestClientProvider(
    private val restClientBuilder: RestClient.Builder,
//    private val reactorClientHttpConnectorBuilder: ReactorClientHttpConnectorBuilder,
) {

    fun createRestClient(): RestClient {

//
        // Tworzymy menedżera połączeń z pulą socketów
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.maxTotal = 100 // maksymalna liczba połączeń (socketów)
        connectionManager.defaultMaxPerRoute = 100 // maksymalna liczba połączeń na host


        // Tworzymy klienta HTTP z tym menedżerem
        val httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .build()


        // Tworzymy fabrykę żądań HTTP z klientem
        val requestFactory =
            HttpComponentsClientHttpRequestFactory(httpClient)


        return restClientBuilder
            .requestFactory(ReactorClientHttpRequestFactory()) // dedicated thread pool for http
//            .requestFactory(requestFactory)
//            .requestFactory(HttpComponentsClientHttpRequestFactory())
//                HttpClients.createMinimal(PoolingHttpClientConnectionManager())
//            )) // shared thread pool with server for default tomcat? 34s XDDDD
//            .requestInterceptor(XDDD())
            //            .filter { request, next ->
//                next.exchange(request)
//                    .contextWrite {
//                        // executed on server thread
//                        val index = request.headers()[DUMMY_INDEX]?.first()
//                        if (index != null) {
//                            UndertowAppLogger.log(this, "Index: $index. Start $WEB_CLIENT_PENDING_REQUEST_TIME")
//                            it.put(WEB_CLIENT_PENDING_REQUEST_TIME, ContextWithStartTime(index, System.nanoTime()))
//                        } else {
//                            it
//                        }
//                    }
//            }
//            .clientConnector(
//                reactorClientHttpConnectorBuilder
//                    .withHttpClientCustomizer { httpClient ->
//                        httpClient
//                            .doOnRequest { httpClientRequest, _ ->
//                                // executed on webClient thread
//                                val contextWithStartTime =
//                                    httpClientRequest
//                                        .currentContextView()
//                                        .getOrEmpty<ContextWithStartTime>(WEB_CLIENT_PENDING_REQUEST_TIME)
//                                        .getOrNull()
//                                        ?: return@doOnRequest
//
//                                val duration = Duration.ofNanos(System.nanoTime() - contextWithStartTime.startTime)
//                                UndertowAppLogger.log(
//                                    this,
//                                    "Index: ${contextWithStartTime.index}. End $WEB_CLIENT_PENDING_REQUEST_TIME. Took: $duration"
//                                )
//                            }
//                    }
//                    .build()
//            )
            .build()
    }

}