package com.sqshq.facets.index

import org.apache.lucene.facet.FacetResult
import org.apache.lucene.facet.FacetsCollector
import org.apache.lucene.facet.FacetsConfig
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path

class SortedSetIndexReader(
    private val indexPath: String,
    private val facetsConfig: FacetsConfig,
) : Reader {

    private val directory = FSDirectory.open(Path.of(indexPath))
    private var indexReader = DirectoryReader.open(directory)
    private val indexSearcher = IndexSearcher(indexReader)
    private val readerState = DefaultSortedSetDocValuesReaderState(indexReader)

    override fun matchAll(
        facetLimitPerDimension: Int
    ): List<FacetResult> {
        val collector = FacetsCollector()
        FacetsCollector.search(
            indexSearcher,
            MatchAllDocsQuery(),
            10,
            collector
        )
        val facetsResults = SortedSetDocValuesFacetCounts(readerState, collector)
        return facetsResults.getAllDims(facetLimitPerDimension)
    }

    override fun close() {
        this.indexReader.close()
        this.directory.close()
    }
}
