package com.sqshq.facets

import com.codahale.metrics.MetricRegistry
import com.sqshq.facets.config.*
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

    private fun index(config: BenchmarkConfig) {

        println("Started indexing.")

        val parser = Parser()
        val sampleDocument = parser.sample(config, 1).first()
        val facetsConfig = FacetsConfig().forBenchmark(config, sampleDocument)
        val meterRegistry = MetricRegistry()

        val writer = when (config.indexType) {
            IndexType.TAXONOMY -> TaxonomyIndexWriter(config, facetsConfig, meterRegistry)
            IndexType.SSDVFF -> SortedSetIndexWriter(config, facetsConfig, meterRegistry)
        }

        // using classic Java concurrency, since all Lucene calls are blocking
        val executor = Executors.newFixedThreadPool(config.concurrency)
        val elapsedMs = measureTimeMillis {
            parser.stream(config) {
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

    private fun query(config: BenchmarkConfig) {

        println("Started querying.")

        // load some documents in memory beforehand
        val samples = Parser().sample(config, config.querySampleSize)
        val facetsConfig = FacetsConfig().forBenchmark(config, samples.first())
        val meterRegistry = MetricRegistry()

        val reader = when (config.indexType) {
            IndexType.TAXONOMY -> TaxonomyIndexReader(config, facetsConfig, meterRegistry)
            IndexType.SSDVFF -> SortedSetIndexReader(config, facetsConfig, meterRegistry)
        }

        // using classic Java concurrency, since all Lucene calls are blocking
        val executor = Executors.newFixedThreadPool(config.concurrency)

        val elapsedMs = measureTimeMillis {

            val dimensions =
                samples.first().getArrayFields().keys + samples.first().getStringFields().keys

            samples.forEachIndexed { index, sample ->

                sample.getStringFields().entries
                    .filterNot { it.value.isNullOrBlank() }
                    .sortedBy { (_, label) -> label }
                    .chunked(1)
                    .subList(0, 4)
                    .forEachIndexed { subindex, chunk ->
                        val query = chunk.associate { entry -> entry.key to entry.value }.toMutableMap()
                        config.mandatoryQueryDimensions.forEach {
                            query[it] = sample.getStringFields().getOrDefault(it, "default")
                        }
                        if (config.queryTypes.contains(QueryType.DRILLDOWN)) {
                            executor.submit { reader.drillDown(query, getSubset(dimensions, subindex, 5), 10) }
                        }
                        if (config.queryTypes.contains(QueryType.DRILLSIDEWAYS)) {
                            executor.submit { reader.drillSideways(query, getSubset(dimensions, subindex, 5), 10) }
                        }
                    }

                sample.getArrayFields()
                    .flatMap { (dimension, labels) -> labels.map { Pair(dimension, it) } }
                    .filterNot { it.second.isNullOrBlank() }
                    .sortedByDescending { (_, label) -> label }
                    .chunked(1)
                    .subList(0, 4)
                    .forEachIndexed { subindex, chunk ->
                        val query = chunk.associate { entry -> entry.first to entry.second }.toMutableMap()
                        config.mandatoryQueryDimensions.forEach {
                            query[it] = sample.getStringFields().getOrDefault(it, "default")
                        }
                        if (config.queryTypes.contains(QueryType.DRILLDOWN)) {
                            executor.submit { reader.drillDown(query, getSubset(dimensions, subindex, 5), 10) }
                        }
                        if (config.queryTypes.contains(QueryType.DRILLSIDEWAYS)) {
                            executor.submit { reader.drillSideways(query, getSubset(dimensions, subindex, 5), 10) }
                        }
                    }

                if (config.queryTypes.contains(QueryType.MATCHALL)) {
                    executor.submit { reader.matchAll(getSubset(dimensions, index, 5), 10) }
                }
            }

            // wait for querying to complete
            executor.shutdown()
            executor.awaitTermination(Long.MAX_VALUE, SECONDS)
            reader.close()
        }

        reportMetrics(meterRegistry)

        println("Finished querying. Time elapsed: ${MILLISECONDS.toSeconds(elapsedMs)} sec.")
    }

    // get reproducable "random" subset of [allValues]
    private fun getSubset(allValues: Set<String>, start: Int, count: Int): Set<String> {
        val subsetStartIndex = (start % (allValues.size - count))
        return allValues.toList()
            .subList(subsetStartIndex, subsetStartIndex + count)
            .toSet()
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Expected path to json config as a single argument")
        return
    }
    Benchmark(Path.of(args.first())).run()
}
