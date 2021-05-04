package com.sqshq.facets.index

import com.sqshq.facets.config.MergerConfig
import org.apache.commons.io.IOUtils
import org.apache.lucene.facet.FacetsConfig
import org.apache.lucene.facet.taxonomy.TaxonomyMergeUtils
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter
import org.apache.lucene.facet.taxonomy.writercache.LruTaxonomyWriterCache
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import java.nio.file.Path

class TaxonomyIndexMerger(private val mergerConfig: MergerConfig) : Merger {

    private val targetMainWriter: IndexWriter
    private val targetFacetWriter: DirectoryTaxonomyWriter

    private val targetMainDirectory: Directory
    private val targetFacetDirectory: Directory

    private val sourceMainDirectory: Directory
    private val sourceFacetDirectory: Directory

    init {
        val writerConfig = IndexWriterConfig()
            .setOpenMode(IndexWriterConfig.OpenMode.APPEND)
            .setRAMBufferSizeMB(16.0)
            .setCommitOnClose(true)

        this.targetMainDirectory = FSDirectory.open(Path.of(mergerConfig.targetIndexPath))
        this.targetFacetDirectory =
            FSDirectory.open(Path.of(mergerConfig.targetIndexPath).resolve("facets"))

        this.targetMainWriter = IndexWriter(targetMainDirectory, writerConfig)
        this.targetFacetWriter = DirectoryTaxonomyWriter(
            targetFacetDirectory, IndexWriterConfig.OpenMode.APPEND, LruTaxonomyWriterCache(10_000_000, LruTaxonomyWriterCache.LRUType.LRU_HASHED)
        )

        this.sourceMainDirectory = FSDirectory.open(Path.of(mergerConfig.sourceIndexPath))
        this.sourceFacetDirectory =
            FSDirectory.open(Path.of(mergerConfig.sourceIndexPath).resolve("facets"))
    }

    override fun merge() {
        TaxonomyMergeUtils.merge(
            sourceMainDirectory,
            sourceFacetDirectory,
            DirectoryTaxonomyWriter.MemoryOrdinalMap(),
            targetMainWriter,
            targetFacetWriter,
            FacetsConfig()
        )
    }

    override fun close() {
        IOUtils.close(
            targetFacetWriter,
            targetMainWriter,
            targetFacetDirectory,
            targetMainDirectory,
            sourceFacetDirectory,
            sourceMainDirectory
        )
    }
}