package com.sqshq.facets.parser

import com.google.gson.Gson
import com.sqshq.facets.config.MergerConfig
import java.nio.file.Files
import java.nio.file.Path

class Parser {

    private val gson = Gson()

    fun config(path: Path): MergerConfig {
        return gson.fromJson(Files.newBufferedReader(path), MergerConfig::class.java)
    }
}