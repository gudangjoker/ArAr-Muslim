package com.arar.muslim.core.database

import android.content.Context
import java.io.InputStream
import java.security.MessageDigest

object DatabaseIntegrityValidator {

    // SHA-256 riil dari quran_master.db yang sudah dikompilasi dari sumber resmi Tanzil & Kemenag RI
    const val EXPECTED_DATABASE_SHA256 = "AD43BF234683C7DFDD729A6ED1FF7EA8FF998D8BDFBF16272E6E051B8E0C08BB"

    /**
     * Memverifikasi checksum SHA-256 dari aset database lokal.
     * Mencegah tampering atau korupsi file mushaf suci Al-Qur'an.
     */
    fun verifyAssetIntegrity(context: Context, assetPath: String = "databases/quran_master.db"): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream: InputStream = context.assets.open(assetPath)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            inputStream.close()
            val checksum = digest.digest().joinToString("") { "%02X".format(it) }
            checksum.equals(EXPECTED_DATABASE_SHA256, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
