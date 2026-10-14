package com.otalab.packageservice

import com.otalab.packageservice.domain.PackageStatus
import com.otalab.packageservice.domain.SoftwarePackage
import org.springframework.stereotype.Service

@Service
class PackageService {

    fun releasePackage(softwarePackage: SoftwarePackage): SoftwarePackage {
        require(softwarePackage.status == PackageStatus.TESTED) {
            "Package ${softwarePackage.id} must be TESTED before release."
        }

        val invalidVariant = softwarePackage.components
            .firstOrNull { !it.supportedVariants.contains(softwarePackage.variant) }

        require(invalidVariant == null) {
            "Component ${invalidVariant?.ecu} does not support variant ${softwarePackage.variant}."
        }

        return softwarePackage.copy(status = PackageStatus.RELEASED)
    }
}
