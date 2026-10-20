package com.otalab.packageservice.entity

import com.otalab.packageservice.domain.SoftwareComponent
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
public class SoftwarePackage () {
    @Id
    var id: String? = null;
    var variant: String = "";
    var version: String = "";
    @OneToMany
    var softwareComponent: List<SoftwareComponent> = emptyList<SoftwareComponent>();
    
}