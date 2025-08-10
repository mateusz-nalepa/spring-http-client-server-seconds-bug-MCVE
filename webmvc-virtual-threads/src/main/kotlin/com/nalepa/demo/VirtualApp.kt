package com.nalepa.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VirtualApp

fun main(args: Array<String>) {
	runApplication<VirtualApp>(*args)
}
