package com.arar.muslim.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arar.muslim.data.dao.AsmaulHusnaDao
import com.arar.muslim.data.dao.AyahDao
import com.arar.muslim.data.dao.DoaDao
import com.arar.muslim.data.dao.SurahDao
import com.arar.muslim.data.dao.UserDataDao
import com.arar.muslim.data.entity.AsmaulHusnaEntity
import com.arar.muslim.data.entity.AudioMetadataEntity
import com.arar.muslim.data.entity.AyahEntity
import com.arar.muslim.data.entity.BookmarkEntity
import com.arar.muslim.data.entity.DoaEntity
import com.arar.muslim.data.entity.LastReadEntity
import com.arar.muslim.data.entity.ReciterEntity
import com.arar.muslim.data.entity.SurahEntity

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        AsmaulHusnaEntity::class,
        DoaEntity::class,
        ReciterEntity::class,
        AudioMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuranMasterDatabase : RoomDatabase() {

    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun asmaulHusnaDao(): AsmaulHusnaDao
    abstract fun doaDao(): DoaDao

    companion object {
        @Volatile
        private var INSTANCE: QuranMasterDatabase? = null

        fun getInstance(context: Context): QuranMasterDatabase {
            return INSTANCE ?: synchronized(this) {
                // Verifikasi integritas hash SHA-256
                val isIntegrityValid = DatabaseIntegrityValidator.verifyAssetIntegrity(
                    context,
                    "databases/quran_master.db"
                )
                if (!isIntegrityValid) {
                    throw SecurityException(
                        "Database integrity check failed! Checksum mismatch on quran_master.db. Bootstrapping aborted."
                    )
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranMasterDatabase::class.java,
                    "quran_master.db"
                )
                    .createFromAsset("databases/quran_master.db")
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Menjamin database suci berstatus read-only (immutable)
                            db.query("PRAGMA query_only = ON;").close()
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

@Database(
    entities = [
        BookmarkEntity::class,
        LastReadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_data.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
