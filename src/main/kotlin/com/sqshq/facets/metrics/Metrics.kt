package com.sqshq.facets.metrics

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import java.util.concurrent.TimeUnit

fun <T> recordTiming(timer: Timer, function: () -> T): T {
    val context = timer.time()
    val result = function()
    context.stop()
    return result
}

fun reportMetrics(registry: MetricRegistry) {
    ConsoleReporter.forRegistry(registry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build().report()
}