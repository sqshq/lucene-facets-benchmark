package com.sqshq.facets

import com.codahale.metrics.MetricRegistry
import com.sqshq.facets.config.BenchmarkConfig
import com.sqshq.facets.config.IndexType
import com.sqshq.facets.config.Mode
import com.sqshq.facets.config.forBenchmark
import com.sqshq.facets.index.SortedSetIndexReader
import com.sqshq.facets.index.SortedSetIndexWriter
import com.sqshq.facets.index.TaxonomyIndexReader
import com.sqshq.facets.index.TaxonomyIndexWriter
import com.sqshq.facets.metrics.reportMetrics
import com.sqshq.facets.parser.Parser
import org.apache.commons.io.FileUtils
import org.apache.lucene.facet.FacetsConfig
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.measureTimeMillis

class Benchmark(configPath: Path) {

    private var parser = Parser()
    private val config = parser.config(configPath)

    fun run() {
        println(config)
        repeat(config.cycles) {
            when (config.mode) {
                Mode.INDEX -> index(config)
                Mode.QUERY -> query(config)
            }
        }
    }

    private fun index(benchmarkConfig: BenchmarkConfig) {

        println("Started indexing.")

        val parser = Parser()
        val sampleDocument = parser.sample(benchmarkConfig, 1).first()
        val facetsConfig = FacetsConfig().forBenchmark(benchmarkConfig, sampleDocument)
        val meterRegistry = MetricRegistry()

        val writer = when (benchmarkConfig.indexType) {
            IndexType.TAXONOMY -> TaxonomyIndexWriter(benchmarkConfig, facetsConfig, meterRegistry)
            IndexType.SSDVFF -> SortedSetIndexWriter(benchmarkConfig, facetsConfig, meterRegistry)
        }

        // using classic Java concurrency, since all Lucene calls are blocking
        val executor = Executors.newFixedThreadPool(benchmarkConfig.concurrency)
        val elapsedMs = measureTimeMillis {
            parser.stream(benchmarkConfig) {
                executor.submit {
                    writer.write(it)
                }
            }

            // wait for indexing to complete
            executor.shutdown()
            executor.awaitTermination(Long.MAX_VALUE, SECONDS)

            writer.close()
        }

        reportMetrics(meterRegistry)

        println(
            "Finished indexing. " +
                    "Time elapsed: ${MILLISECONDS.toSeconds(elapsedMs)} sec. " +
                    "Index size: ${FileUtils.byteCountToDisplaySize(writer.getSizeInBytes())}"
        )
    }

    private fun query(benchmarkConfig: BenchmarkConfig) {

        println("Started querying.")

        // load some documents in memory beforehand
        val samples = Parser().sample(benchmarkConfig, benchmarkConfig.querySampleSize)
        val facetsConfig = FacetsConfig().forBenchmark(benchmarkConfig, samples.first())
        val meterRegistry = MetricRegistry()

        val reader = when (benchmarkConfig.indexType) {
            IndexType.TAXONOMY -> TaxonomyIndexReader(benchmarkConfig, facetsConfig, meterRegistry)
            IndexType.SSDVFF -> SortedSetIndexReader(benchmarkConfig, facetsConfig, meterRegistry)
        }

        // using classic Java concurrency, since all Lucene calls are blocking
        val executor = Executors.newFixedThreadPool(benchmarkConfig.concurrency)

        val elapsedMs = measureTimeMillis {

            val allDimensions =
                samples.first().getArrayFields().keys + samples.first().getStringFields().keys

            samples.forEach { sample ->

                sample.getStringFields().entries.filterNot { it.value.isNullOrBlank() }.chunked(4)
                    .forEach { chunk ->
                        val query = chunk.associate { entry -> entry.key to entry.value }
                        executor.submit { reader.drillDown(query, query.keys, 15) }
                        executor.submit { reader.drillSideways(query, query.keys, 30) }
                    }

                sample.getArrayFields()
                    .flatMap { (dimension, labels) -> labels.map { Pair(dimension, it) } }
                    .filterNot { it.second.isNullOrBlank() }
                    .chunked(10)
                    .forEach { chunk ->
                        val query = chunk.associate { entry -> entry.first to entry.second }
                        executor.submit { reader.drillDown(query, query.keys, 10) }
                        executor.submit { reader.drillSideways(query, query.keys, 20) }
                    }

                val matchAllDimensions =
                    allDimensions.toList().subList(0, 1 + samples.size % allDimensions.size).toSet()

                executor.submit { reader.matchAll(matchAllDimensions, 50) }
            }

            // wait for querying to complete
            executor.shutdown()
            executor.awaitTermination(Long.MAX_VALUE, SECONDS)
            reader.close()
        }

        reportMetrics(meterRegistry)

        println("Finished querying. Time elapsed: ${MILLISECONDS.toSeconds(elapsedMs)} sec.")
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Expected path to json config as a single argument")
        return
    }
    Benchmark(Path.of(args.first())).run()
}
