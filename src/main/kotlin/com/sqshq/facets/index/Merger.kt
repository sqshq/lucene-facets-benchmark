package com.sqshq.facets.index

import java.io.Closeable

interface Merger: Closeable {

    fun merge()

}