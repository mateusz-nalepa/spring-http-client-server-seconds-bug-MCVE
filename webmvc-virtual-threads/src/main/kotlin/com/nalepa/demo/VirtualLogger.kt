package com.nalepa.demo

import java.time.LocalTime

object VirtualLogger {
    fun log(caller: Any, message: String) {
        println("${LocalTime.now()} : ${caller.javaClass.simpleName} : ${Thread.currentThread().name} ### $message")
    }
}