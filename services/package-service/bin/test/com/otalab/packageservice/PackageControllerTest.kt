package com.otalab.packageservice

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class PackageControllerTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `creating a package is accepted`() {
        mockMvc.perform(
            post("/packages")
                .contentType("application/json")
                .content(
                    """
                    {
                      "variant": "sedan",
                      "components": [
                        {
                          "ecu": "ecu-1",
                          "version": "1.0.0",
                          "checksum": "abc123",
                          "supportedVariants": ["sedan"]
                        }
                      ]
                    }
                    """.trimIndent()
                )
        ).andExpect(status().isOk)
    }

    @Test
    fun `creating a package with a blank component ecu is rejected`() {
        mockMvc.perform(
            post("/packages")
                .contentType("application/json")
                .content(
                    """
                    {
                      "variant": "sedan",
                      "components": [
                        {
                          "ecu": "",
                          "version": "1.0.0",
                          "checksum": "abc123",
                          "supportedVariants": ["sedan"]
                        }
                      ]
                    }
                    """.trimIndent()
                )
        ).andExpect(status().isBadRequest)
    }
}
