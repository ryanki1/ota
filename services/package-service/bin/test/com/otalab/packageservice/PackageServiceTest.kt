package com.otalab.packageservice

import com.otalab.packageservice.domain.PackageStatus
import com.otalab.packageservice.domain.SoftwareComponent
import com.otalab.packageservice.domain.SoftwarePackage
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PackageServiceTest {

    @Test
    fun `release is rejected when package is not tested`() {
        val packageService = PackageService()
        val packageEntity = SoftwarePackage(
            id = "pkg-1",
            variant = "sedan",
            components = listOf(SoftwareComponent("ecu-1", "1.0.0", "abc123", setOf("sedan"))),
            status = PackageStatus.DRAFT
        )

        assertThrows(IllegalArgumentException::class.java) {
            packageService.releasePackage(packageEntity)
        }
    }

    @Test
    fun `release is rejected when a component does not support the package variant`() {
        val packageService = PackageService()
        val packageEntity = SoftwarePackage(
            id = "pkg-2",
            variant = "truck",
            components = listOf(SoftwareComponent("ecu-1", "1.0.0", "abc123", setOf("sedan"))),
            status = PackageStatus.TESTED
        )

        assertThrows(IllegalArgumentException::class.java) {
            packageService.releasePackage(packageEntity)
        }
    }
}
