package com.sqshq.facets.parser

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.sqshq.facets.config.BenchmarkConfig
import com.sqshq.facets.model.Document

import java.nio.file.Files
import java.nio.file.Path

class Parser {

    private val gson = Gson()

    fun sample(config: BenchmarkConfig, limit: Int): List<Document> {
        val reader = JsonReader(Files.newBufferedReader(Path.of(config.resourceFilePath)))
        reader.beginArray()

        val sample = mutableListOf<Document>()

        while (reader.hasNext() && sample.size < limit) {
            val document = gson.fromJson<Document>(reader, Document::class.java)
            sample.add(document)
        }

        return sample
    }

    fun stream(config: BenchmarkConfig, action: (Document) -> Unit) {

        val reader = JsonReader(Files.newBufferedReader(Path.of(config.resourceFilePath)))
        reader.beginArray()

        while (reader.hasNext()) {
            val document = gson.fromJson<Document>(reader, Document::class.java)
            action.invoke(document)
        }

        reader.endArray()
        reader.close()
    }

    fun config(path: Path): BenchmarkConfig {
        return gson.fromJson(Files.newBufferedReader(path), BenchmarkConfig::class.java)
    }
}