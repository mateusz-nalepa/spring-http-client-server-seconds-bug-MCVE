package com.nalepa.demo

import org.apache.coyote.ProtocolHandler
import org.apache.tomcat.util.threads.VirtualThreadExecutor
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading
import org.springframework.boot.autoconfigure.thread.Threading
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.Executor

@Component
@ConditionalOnThreading(Threading.VIRTUAL)
class CustomTomcatVirtualThreadsWebServerCustomizer : WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory>,
    Ordered {


    // modified code from: TomcatVirtualThreadsWebServerFactoryCustomizer
    override fun customize(factory: ConfigurableTomcatWebServerFactory) {
        factory.addProtocolHandlerCustomizers(
            TomcatProtocolHandlerCustomizer { protocolHandler: ProtocolHandler ->
                protocolHandler.executor = MonitoredExecutor(
                    VirtualThreadExecutor("tomcat-handler-")
                )
            })
    }

    override fun getOrder(): Int {
//        TomcatWebServerFactoryCustomizer.ORDER == 0
        return 0 + 1
    }


    class MonitoredExecutor internal constructor(private val delegate: Executor) : Executor {
        override fun execute(runnable: Runnable) {
            delegate.execute(MonitoredRunnable(runnable))
        }
    }

    class MonitoredRunnable internal constructor(private val delegate: Runnable) : Runnable {
        private val startTime = System.nanoTime()

        override fun run() {
            val duration = Duration.ofNanos(System.nanoTime() - startTime)
            VirtualLogger.log(this, "WORKAROUND Http server pending request took: $duration")

            delegate.run()
        }
    }

}