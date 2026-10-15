package com.otalab.packageservice.domain

data class SoftwarePackage(
    val id: String,
    val variant: String,
    val components: List<SoftwareComponent>,
    val status: PackageStatus
)
