package com.utb.scuffedviruschecker.model

data class ScanResult(
    val data: Data,

)

data class Data(
    val type: String,
    val id: String,
    val links: Links
)

data class Links(
    val self: String
)