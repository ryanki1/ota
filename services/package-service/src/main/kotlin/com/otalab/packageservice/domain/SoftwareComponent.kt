package com.otalab.packageservice.domain

data class SoftwareComponent(
    val ecu: String,
    val version: String,
    val checksum: String,
    val supportedVariants: Set<String>
)
