package com.sqshq.facets.index

import com.codahale.metrics.MetricRegistry
import com.sqshq.facets.config.BenchmarkConfig
import com.sqshq.facets.metrics.recordTiming
import com.sqshq.facets.model.Converter
import com.sqshq.facets.model.Document
import com.sqshq.facets.model.FieldType
import org.apache.commons.io.FileUtils
import org.apache.lucene.facet.FacetsConfig
import org.apache.lucene.facet.taxonomy.TaxonomyWriter
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.NoMergeScheduler
import org.apache.lucene.store.FSDirectory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

class TaxonomyIndexWriter(
    private val benchmarkConfig: BenchmarkConfig,
    private val facetsConfig: FacetsConfig,
    private val meterRegistry: MetricRegistry
) :
    Writer {

    private val bufferCount = AtomicInteger()
    private val indexWriter: IndexWriter
    private val facetWriter: TaxonomyWriter

    init {
        val facetPath = Path.of(benchmarkConfig.indexPath).resolve("facets")

        FileUtils.deleteDirectory(Path.of(benchmarkConfig.indexPath).toFile())
        Files.createDirectories(Path.of(benchmarkConfig.indexPath))
        Files.createDirectories(facetPath)

        val writerConfig = IndexWriterConfig().setOpenMode(IndexWriterConfig.OpenMode.CREATE)
        if (!benchmarkConfig.enableMergeScheduler) {
            writerConfig.mergeScheduler = NoMergeScheduler.INSTANCE
        }

        this.indexWriter = IndexWriter(FSDirectory.open(Path.of(benchmarkConfig.indexPath)), writerConfig)
        this.facetWriter = DirectoryTaxonomyWriter(FSDirectory.open(facetPath))

        this.indexWriter.commit()
        this.facetWriter.commit()
    }

    private val writeTimer = meterRegistry.timer("taxonomy.write")

    override fun write(document: Document) = recordTiming(writeTimer) {
        val luceneDocument = Converter.convertToTaxonomy(document, FieldType.TAXONOMY)
        indexWriter.addDocument(facetsConfig.build(facetWriter, luceneDocument))

        if (bufferCount.incrementAndGet() >= benchmarkConfig.commitBuffer) {
            commit()
            bufferCount.set(0)
        }
    }

    override fun commit() {
        facetWriter.commit()
        indexWriter.commit()
    }

    override fun close() {
        facetWriter.close()
        indexWriter.close()
    }

    override fun getSizeInBytes() = FileUtils.sizeOfDirectory(Path.of(benchmarkConfig.indexPath).toFile())
}