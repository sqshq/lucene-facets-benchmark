package com.sqshq.facets.config

data class MergerConfig(
    val indexType: IndexType,
    val sourceIndexPath: String,
    val targetIndexPath: String,
    val printAllFacets: Boolean
)

enum class IndexType {
    TAXONOMY, SSDVFF
}