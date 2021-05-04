package com.sqshq.facets.index

import org.apache.lucene.facet.FacetResult
import java.io.Closeable

interface Reader: Closeable {

    fun matchAll(facetLimitPerDimension: Int): List<FacetResult>
}