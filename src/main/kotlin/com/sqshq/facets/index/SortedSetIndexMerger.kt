package com.sqshq.facets.index

import com.sqshq.facets.config.MergerConfig
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path

class SortedSetIndexMerger(mergerConfig: MergerConfig) : Merger {

    private val targetWriter: IndexWriter
    private val sourceDirectory: Directory

    init {
        val writerConfig = IndexWriterConfig()
            .setOpenMode(IndexWriterConfig.OpenMode.APPEND)
            .setRAMBufferSizeMB(16.0)
            .setCommitOnClose(true)

        this.targetWriter = IndexWriter(FSDirectory.open(Path.of(mergerConfig.targetIndexPath)), writerConfig)
        this.sourceDirectory = FSDirectory.open(Path.of(mergerConfig.sourceIndexPath))
    }

    override fun merge() {
        this.targetWriter.addIndexes(this.sourceDirectory)
    }

    override fun close() {
        this.targetWriter.close()
        this.sourceDirectory.close()
    }
}