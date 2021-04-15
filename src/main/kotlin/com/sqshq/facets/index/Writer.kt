package com.sqshq.facets.index

import com.sqshq.facets.model.Document
import java.io.Closeable

interface Writer: Closeable {

    fun write(document: Document)
    fun commit()
    fun getSizeInBytes(): Long

}
