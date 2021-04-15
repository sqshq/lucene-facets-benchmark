package com.sqshq.facets.index

import com.codahale.metrics.MetricRegistry
import com.sqshq.facets.config.BenchmarkConfig
import com.sqshq.facets.metrics.recordTiming
import org.apache.lucene.facet.*
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path

class SortedSetIndexReader(
    private val benchmarkConfig: BenchmarkConfig,
    private val facetsConfig: FacetsConfig,
    private val meterRegistry: MetricRegistry
) : Reader {

    private val directory = FSDirectory.open(Path.of(benchmarkConfig.indexPath))
    private var indexReader = DirectoryReader.open(directory)
    private val indexSearcher = IndexSearcher(indexReader)
    private val readerState = DefaultSortedSetDocValuesReaderState(indexReader)

    private val matchAllTimer = meterRegistry.timer("sortedSet.matchAll")

    override fun matchAll(
        facetDimensions: Set<String>,
        facetLimitPerDimension: Int
    ): List<FacetResult> = recordTiming(matchAllTimer) {
        val collector = FacetsCollector()
        FacetsCollector.search(
            indexSearcher,
            MatchAllDocsQuery(),
            benchmarkConfig.queryPageSize,
            collector
        )
        val facetsResults = SortedSetDocValuesFacetCounts(readerState, collector)
        facetDimensions.map { dimension ->
            facetsResults.getTopChildren(facetLimitPerDimension, dimension)
        }
    }

    private val drillDownTimer = meterRegistry.timer("sortedSet.drillDown")

    override fun drillDown(
        queryLabelsPerDimension: Map<String, String>,
        facetDimensions: Set<String>,
        facetLimitPerDimension: Int
    ): List<FacetResult> = recordTiming(drillDownTimer) {
        val collector = FacetsCollector()
        val query = DrillDownQuery(facetsConfig)
        queryLabelsPerDimension.forEach { (dimension, label) -> query.add(dimension, label) }
        FacetsCollector.search(
            indexSearcher,
            query,
            benchmarkConfig.queryPageSize,
            collector
        )
        val facetsResults = SortedSetDocValuesFacetCounts(readerState, collector)
        facetDimensions.map { dimension ->
            facetsResults.getTopChildren(facetLimitPerDimension, dimension)
        }
    }

    private val drillSidewaysTimer = meterRegistry.timer("sortedSet.drillSideways")

    override fun drillSideways(
        queryLabelsPerDimension: Map<String, String>,
        facetDimensions: Set<String>,
        facetLimitPerDimension: Int
    ): List<FacetResult> = recordTiming(drillSidewaysTimer) {
        val query = DrillDownQuery(facetsConfig)
        queryLabelsPerDimension.forEach { (dimension, label) -> query.add(dimension, label) }
        val drillSideways = DrillSideways(indexSearcher, facetsConfig, readerState)
        val facetsResults = drillSideways.search(query, benchmarkConfig.queryPageSize).facets
        facetDimensions.map { dimension ->
            facetsResults.getTopChildren(facetLimitPerDimension, dimension)
        }
    }

    override fun close() {
        this.indexReader.close()
        this.directory.close()
    }
}
