package com.sqshq.facets.index

import org.apache.lucene.facet.FacetResult
import org.apache.lucene.facet.FacetsCollector
import org.apache.lucene.facet.FacetsConfig
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path

class TaxonomyIndexReader(
    private val indexPath: String,
    private val facetsConfig: FacetsConfig) : Reader {

    private val indexDirectory = FSDirectory.open(Path.of(indexPath))
    private val facetDirectory =
        FSDirectory.open(Path.of(indexPath).resolve("facets"))
    private val indexReader = DirectoryReader.open(indexDirectory)
    private val facetReader = DirectoryTaxonomyReader(facetDirectory)
    private val indexSearcher = IndexSearcher(indexReader)

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
        val facetsResults = FastTaxonomyFacetCounts(facetReader, facetsConfig, collector)
        return facetsResults.getAllDims(facetLimitPerDimension)
    }

    override fun close() {
        this.facetReader.close()
        this.indexReader.close()
        this.facetDirectory.close()
        this.indexDirectory.close()
    }
}