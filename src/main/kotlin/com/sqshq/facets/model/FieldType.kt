package com.sqshq.facets.model

import org.apache.lucene.facet.FacetField
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField
import org.apache.lucene.index.IndexableField

enum class FieldType {

    TAXONOMY, SSDVFF;

    fun create(dimension: String, label: String?): IndexableField {
        val labelOrDefault = label ?: "default"
        return when (this) {
            TAXONOMY -> FacetField(dimension, labelOrDefault)
            SSDVFF -> SortedSetDocValuesFacetField(dimension, labelOrDefault)
        }
    }
}