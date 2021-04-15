package com.sqshq.facets.config

import com.sqshq.facets.model.Document
import org.apache.lucene.facet.FacetsConfig

data class BenchmarkConfig(
    val mode: Mode,
    val indexType: IndexType,
    val cycles: Int,
    val concurrency: Int,
    val enableMergeScheduler: Boolean,
    val allFieldsMultivalue: Boolean,
    val commitBuffer: Int,
    val querySampleSize: Int,
    val queryPageSize: Int,
    val resourceFilePath: String,
    val indexPath: String
)

enum class Mode {
    INDEX, QUERY
}

enum class IndexType {
    TAXONOMY, SSDVFF
}

fun FacetsConfig.forBenchmark(benchmarkConfig: BenchmarkConfig, sample: Document): FacetsConfig {
    if (benchmarkConfig.allFieldsMultivalue) {
        sample.getStringFields().forEach { this.setMultiValued(it.key, true) }
    }
    sample.getArrayFields().forEach { this.setMultiValued(it.key, true) }
    return this
}
