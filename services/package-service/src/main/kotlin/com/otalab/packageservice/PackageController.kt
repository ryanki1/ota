package com.otalab.packageservice

import com.otalab.packageservice.domain.PackageStatus
import com.otalab.packageservice.domain.SoftwareComponent
import com.otalab.packageservice.domain.SoftwarePackage
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/packages")
class PackageController(
    private val packageService: PackageService = PackageService()
) {

    private val packages = mutableMapOf<String, SoftwarePackage>()

    @PostMapping
    fun createPackage(@Valid @RequestBody request: CreatePackageRequest): SoftwarePackage {
        val packageEntity = SoftwarePackage(
            id = request.id ?: java.util.UUID.randomUUID().toString(),
            variant = request.variant,
            components = request.components.map {
                SoftwareComponent(
                    ecu = it.ecu,
                    version = it.version,
                    checksum = it.checksum,
                    supportedVariants = it.supportedVariants.toSet()
                )
            },
            status = PackageStatus.DRAFT
        )
        packages[packageEntity.id] = packageEntity
        return packageEntity
    }

    @GetMapping("/{id}")
    fun getPackage(@PathVariable id: String): SoftwarePackage? = packages[id]

    @PostMapping("/{id}/test")
    fun testPackage(@PathVariable id: String): SoftwarePackage {
        val current = packages[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found")
        val updated = current.copy(status = PackageStatus.TESTED)
        packages[id] = updated
        return updated
    }

    @PostMapping("/{id}/release")
    fun releasePackage(@PathVariable id: String): SoftwarePackage {
        val current = packages[id] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found")
        val released = packageService.releasePackage(current)
        packages[id] = released
        return released
    }
}

data class CreatePackageRequest(
    val id: String? = null,
    @field:NotBlank val variant: String,
    @field:NotEmpty @field:Valid val components: List<CreateComponentRequest>
)

data class CreateComponentRequest(
    @field:NotBlank val ecu: String,
    @field:NotBlank val version: String,
    @field:NotBlank val checksum: String,
    @field:NotEmpty val supportedVariants: List<String>
)
