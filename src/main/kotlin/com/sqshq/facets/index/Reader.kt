package com.sqshq.facets.index

import org.apache.lucene.facet.FacetResult
import java.io.Closeable

interface Reader: Closeable {

    fun matchAll(facetDimensions: Set<String>, facetLimitPerDimension: Int): List<FacetResult>

    fun drillDown(
        queryLabelsPerDimension: Map<String, String>,
        facetDimensions: Set<String>,
        facetLimitPerDimension: Int
    ): List<FacetResult>

    fun drillSideways(
        queryLabelsPerDimension: Map<String, String>,
        facetDimensions: Set<String>,
        facetLimitPerDimension: Int
    ): List<FacetResult>
}