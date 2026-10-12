# ArAr Muslim (Al-Qur'an & Qibla Engine 100% Offline)

Aplikasi Android modern berbasis **Kotlin** dan **Jetpack Compose** untuk ibadah harian umat Muslim secara mandiri tanpa memerlukan koneksi internet, dilengkapi database Al-Qur'an tervalidasi kriptografis SHA-256 dan penunjuk arah kiblat presisi tinggi berbasis sensor fusion dengan kompensasi kemiringan (tilt compensation).

---

## 🌟 Fitur Utama

1. **Al-Qur'an 30 Juz (100% Offline):**
   - 114 Surah, 6.236 Ayat teks rasm Uthmani standar *Tanzil Project*.
   - Terjemahan resmi Kementerian Agama Republik Indonesia (Kemenag RI).
   - Tipografi standar Kemenag RI menggunakan font resmi **LPMQ Isep Misbah** dengan text direction RTL dan line-height 2:1 mencegah harakat bertumpuk.
   - Penanda ayat sajdah, penomoran juz, halaman mushaf Madinah/Kemenag, dan penanda akhir ayat (*End of Ayah*).
   - Fitur simpan ayat terakhir dibaca (*Last Read*) dan bookmark ayat.

2. **Qibla Engine Presisi Tinggi:**
   - Kalkulasi astronomis Geometri Bola (*Great-Circle Bearing*) ke Ka'bah (21.422487° N, 39.826206° E).
   - Sensor Fusion `Sensor.TYPE_ROTATION_VECTOR` dengan kompensasi kemiringan 3D.
   - Koreksi deklinasi magnetik bumi ke Kutub Utara Sejati (*True North*) menggunakan model magnetik bumi WMM (`GeomagneticField`).
   - Low-pass filter peredam getaran tangan (alpha 0.15f).
   - Deteksi interferensi magnetik dan dialog panduan kalibrasi angka 8.
   - Haptic feedback (getaran halus) saat ponsel tepat mengarah lurus ke Ka'bah (toleransi ±2.5°).

3. **Asmaul Husna (99 Nama Allah):**
   - Tampilan grid 2 kolom dengan kaligrafi Arab, transliterasi Latin, dan terjemahan resmi.
   - Pencarian instan dan dialog detail makna.

4. **Doa Harian & Dzikir Pagi-Petang:**
   - Kumpulan dzikir pagi dan petang shahih (*Hisnul Muslim*).
   - Doa sholat dan doa harian.
   - Dilengkapi **Tasbih Digital Interaktif** dengan target pengulangan, efek haptic tick, dan tombol reset.

5. **Integritas Data Suci & Kriptografi:**
   - File database SQLite pre-populated (`quran_master.db`) diverifikasi checksum **SHA-256** saat inisialisasi aplikasi.
   - Master database dibuka dalam mode *read-only* (`PRAGMA query_only = ON;`).
   - Riwayat dan data pengguna disimpan terpisah di `user_data.db`.

---

## 🏗️ Arsitektur Sistem

Aplikasi dibangun dengan **Clean Architecture + MVVM** mengikuti standar Google:
- **Presentation Layer:** Jetpack Compose (Material 3), Navigation Compose, StateFlow.
- **Domain Layer:** Plain Kotlin Models, Use Cases, Repository Contracts.
- **Data Layer:** Room Persistence Library (KSP), Repository Implementations.
- **Core Layer:** Sensor Fusion Engine, Astronomical Bearing Calculator, Database Integrity Validator, dan Quran Audio Service contract.

---

## 🛠️ Persyaratan Build

- Android Studio Koala / Ladybug / Meerkat atau Gradle CLI
- Android SDK 35 (Min SDK: 24)
- JDK 17
- Gradle 8.11.1
