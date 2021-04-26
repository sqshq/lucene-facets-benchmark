package com.sqshq.facets.index

import com.codahale.metrics.MetricRegistry
import com.sqshq.facets.config.BenchmarkConfig
import com.sqshq.facets.metrics.recordTiming
import com.sqshq.facets.model.Converter
import com.sqshq.facets.model.Document
import com.sqshq.facets.model.FieldType
import org.apache.commons.io.FileUtils
import org.apache.lucene.facet.FacetsConfig
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.NoMergeScheduler
import org.apache.lucene.store.FSDirectory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

class SortedSetIndexWriter(
    private val benchmarkConfig: BenchmarkConfig,
    private val facetsConfig: FacetsConfig,
    private val meterRegistry: MetricRegistry
) :
    Writer {

    private val bufferCount = AtomicInteger()
    private val indexWriter: IndexWriter

    init {
        FileUtils.deleteDirectory(Path.of(benchmarkConfig.indexPath).toFile())
        Files.createDirectories(Path.of(benchmarkConfig.indexPath))

        val writerConfig = IndexWriterConfig().setOpenMode(IndexWriterConfig.OpenMode.CREATE)
        if (!benchmarkConfig.enableMergeScheduler) {
            writerConfig.mergeScheduler = NoMergeScheduler.INSTANCE
        }

        this.indexWriter = IndexWriter(FSDirectory.open(Path.of(benchmarkConfig.indexPath)), writerConfig)
        this.indexWriter.commit()
    }

    private val writeTimer = meterRegistry.timer("sortedSet.write")

    override fun write(document: Document) = recordTiming(writeTimer) {
        val luceneDocument = Converter.convertToTaxonomy(document, FieldType.SSDVFF)
        indexWriter.addDocument(facetsConfig.build(luceneDocument))

        if (bufferCount.incrementAndGet() == benchmarkConfig.commitBuffer) {
            bufferCount.set(0)
            commit()
        }
    }

    override fun commit() {
        indexWriter.commit()
    }

    override fun close() {
        indexWriter.close()
    }

    override fun getSizeInBytes() = FileUtils.sizeOfDirectory(Path.of(benchmarkConfig.indexPath).toFile())
}