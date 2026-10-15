package com.otalab.packageservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PackageServiceApplication

fun main(args: Array<String>) {
    runApplication<PackageServiceApplication>(*args)
}
