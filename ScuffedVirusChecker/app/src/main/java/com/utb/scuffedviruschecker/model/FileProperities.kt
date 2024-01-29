package com.utb.scuffedviruschecker.model

data class FileProperties(
    val fileName: String,
    val fileSize: Long,
    val lastModified: String
)