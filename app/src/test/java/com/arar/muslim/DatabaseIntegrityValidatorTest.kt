package com.arar.muslim

import com.arar.muslim.core.database.DatabaseIntegrityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DatabaseIntegrityValidatorTest {

    @Test
    fun testExpectedSha256ConstantIsValid() {
        val expectedHash = DatabaseIntegrityValidator.EXPECTED_DATABASE_SHA256
        assertNotNull(expectedHash)
        assertEquals("SHA-256 string length must be 64 characters", 64, expectedHash.length)
        assertEquals(
            "Expected hash must match compiled quran_master.db",
            "AD43BF234683C7DFDD729A6ED1FF7EA8FF998D8BDFBF16272E6E051B8E0C08BB",
            expectedHash
        )
    }
}
