package com.arar.muslim

import android.app.Application
import com.arar.muslim.core.audio.NoOpQuranAudioService
import com.arar.muslim.core.audio.QuranAudioService
import com.arar.muslim.core.database.QuranMasterDatabase
import com.arar.muslim.core.database.UserDatabase
import com.arar.muslim.core.sensor.CompassEngine
import com.arar.muslim.data.repository.AsmaulHusnaRepositoryImpl
import com.arar.muslim.data.repository.DoaRepositoryImpl
import com.arar.muslim.data.repository.QiblaRepositoryImpl
import com.arar.muslim.data.repository.QuranRepositoryImpl
import com.arar.muslim.data.repository.UserDataRepositoryImpl
import com.arar.muslim.domain.repository.AsmaulHusnaRepository
import com.arar.muslim.domain.repository.DoaRepository
import com.arar.muslim.domain.repository.QiblaRepository
import com.arar.muslim.domain.repository.QuranRepository
import com.arar.muslim.domain.repository.UserDataRepository

class ArArMuslimApp : Application() {

    lateinit var quranMasterDb: QuranMasterDatabase
        private set

    lateinit var userDb: UserDatabase
        private set

    lateinit var compassEngine: CompassEngine
        private set

    lateinit var audioService: QuranAudioService
        private set

    lateinit var quranRepository: QuranRepository
        private set

    lateinit var userDataRepository: UserDataRepository
        private set

    lateinit var asmaulHusnaRepository: AsmaulHusnaRepository
        private set

    lateinit var doaRepository: DoaRepository
        private set

    lateinit var qiblaRepository: QiblaRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Database Master (disertai verifikasi kriptografis SHA-256)
        quranMasterDb = QuranMasterDatabase.getInstance(this)

        // Inisialisasi Database Pengguna (Bookmark & Riwayat baca)
        userDb = UserDatabase.getInstance(this)

        // Inisialisasi Sensor & Audio Subsystem
        compassEngine = CompassEngine(this)
        audioService = NoOpQuranAudioService()

        // Inisialisasi Repositori
        userDataRepository = UserDataRepositoryImpl(userDb.userDataDao())
        quranRepository = QuranRepositoryImpl(
            surahDao = quranMasterDb.surahDao(),
            ayahDao = quranMasterDb.ayahDao(),
            userDataDao = userDb.userDataDao()
        )
        asmaulHusnaRepository = AsmaulHusnaRepositoryImpl(quranMasterDb.asmaulHusnaDao())
        doaRepository = DoaRepositoryImpl(quranMasterDb.doaDao())
        qiblaRepository = QiblaRepositoryImpl(compassEngine)
    }
}
