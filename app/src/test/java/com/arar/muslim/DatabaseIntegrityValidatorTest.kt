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
            "B3C09E87AB5F4A0899F1CFA7775511C99C3FB5DF503DA3A426DFA0AB2D85B9F3",
            expectedHash
        )
    }
}
