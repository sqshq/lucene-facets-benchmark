package com.sqshq.facets

import com.sqshq.facets.config.IndexType
import com.sqshq.facets.config.MergerConfig
import com.sqshq.facets.index.SortedSetIndexMerger
import com.sqshq.facets.index.SortedSetIndexReader
import com.sqshq.facets.index.TaxonomyIndexMerger
import com.sqshq.facets.index.TaxonomyIndexReader
import com.sqshq.facets.parser.Parser
import org.apache.lucene.facet.FacetsConfig
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class Merger {

    fun merge(config: MergerConfig) {

        if (config.printAllFacets) {
            printFacets(config.indexType, config.sourceIndexPath)
            printFacets(config.indexType, config.targetIndexPath)
        }

        val merger = when (config.indexType) {
            IndexType.TAXONOMY -> TaxonomyIndexMerger(config)
            IndexType.SSDVFF -> SortedSetIndexMerger(config)
        }

        val elapsedMs = measureTimeMillis {
            merger.use {
                it.merge()
            }
        }

        if (config.printAllFacets) {
            printFacets(config.indexType, config.targetIndexPath)
        }

        println("Time spent on merge: ${TimeUnit.MILLISECONDS.toSeconds(elapsedMs)} sec.")
    }

    private fun printFacets(indexType: IndexType, path: String) {
        val reader = when (indexType) {
            IndexType.TAXONOMY -> TaxonomyIndexReader(path, FacetsConfig())
            IndexType.SSDVFF -> SortedSetIndexReader(path, FacetsConfig())
        }
        reader.use {
            val facets = it.matchAll(2)
            println("Index: $path\nTotal dimensions: ${facets.size} \nTop labels per dimension:\n$facets")
        }
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Expected path to json config as a single argument")
        return
    }
    val config = Parser().config(Path.of(args.first()))
    println(config)
    Merger().merge(config)
}
