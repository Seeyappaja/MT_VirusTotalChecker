package com.utb.scuffedviruschecker.model

data class GetFileInformation(
    val data: FileInfoData
)

data class FileInfoData(
    val id: String,
    val type: String,
    val links: FileInfoLinks,
    val attributes: FileInfoAttributes
)

data class FileInfoLinks(
    val self: String
)

data class FileInfoAttributes(
    val tags: List<String>,
    val type_description: String,
    val size: Int,
    val first_submission_date: Long,
    val unique_sources: Int,
    val last_analysis_results: Map<String, LastAnalysisResult?>
)

data class LastAnalysisResult(
    val method: String,
    val engine_name: String,
    val engine_version: String,
    val engine_update: String,
    val category: String,
    val result: String?
)
