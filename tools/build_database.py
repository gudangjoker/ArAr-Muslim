#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ArAr Muslim Database Builder & Asset Extractor
Compiles Tanzil Uthmani, Kemenag Indonesian translation, Quran metadata,
Asmaul Husna, and Dzikr into a production-grade SQLite database (quran_master.db).
Also extracts LPMQ Isep Misbah font and computes SHA-256 hash.
"""

import os
import re
import sys
import json
import sqlite3
import hashlib
import zipfile
import xml.etree.ElementTree as ET

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS_DIR = os.path.join(BASE_DIR, "app", "src", "main", "assets", "databases")
FONT_DIR = os.path.join(BASE_DIR, "app", "src", "main", "res", "font")
DB_PATH = os.path.join(ASSETS_DIR, "quran_master.db")

SURAH_NAMES_ID = {
    1: "Pembukaan", 2: "Sapi Betina", 3: "Keluarga Imran", 4: "Wanita", 5: "Jamuan (Hidangan)",
    6: "Binatang Ternak", 7: "Tempat Tertinggi", 8: "Rampasan Perang", 9: "Pengampunan", 10: "Yunus",
    11: "Hud", 12: "Yusuf", 13: "Guruh (Petir)", 14: "Ibrahim", 15: "Bukit Al Hijr",
    16: "Lebah", 17: "Memperjalankan di Malam Hari", 18: "Gua", 19: "Maryam", 20: "Ta Ha",
    21: "Para Nabi", 22: "Haji", 23: "Orang-Orang Mukmin", 24: "Cahaya", 25: "Pembeda",
    26: "Para Penyair", 27: "Semut", 28: "Kisah-Kisah", 29: "Laba-Laba", 30: "Bangsa Romawi",
    31: "Luqman", 32: "Sajdah", 33: "Golongan yang Bersekutu", 34: "Kaum Saba'", 35: "Pencipta",
    36: "Ya Sin", 37: "Barisan-Barisan", 38: "Shad", 39: "Rombongan-Rombongan", 40: "Yang Mengampuni",
    41: "Yang Dijelaskan", 42: "Musyawarah", 43: "Perhiasan", 44: "Kabut", 45: "Yang Berlutut",
    46: "Bukit Pasir", 47: "Muhammad", 48: "Kemenangan", 49: "Kamar-Kamar", 50: "Qaf",
    51: "Angin yang Menerbangkan", 52: "Bukit Tursina", 53: "Bintang", 54: "Bulan", 55: "Yang Maha Pengasih",
    56: "Hari Kiamat", 57: "Besi", 58: "Gugatan", 59: "Pengusiran", 60: "Wanita yang Diuji",
    61: "Barisan", 62: "Hari Jum'at", 63: "Orang-Orang Munafik", 64: "Hari Ditampakkan Kesalahan", 65: "Talak",
    66: "Mengharamkan", 67: "Kerajaan", 68: "Pena", 69: "Hari Kiamat", 70: "Tempat-Tempat Naik",
    71: "Nuh", 72: "Jin", 73: "Orang yang Berselimut", 74: "Orang yang Berkemul", 75: "Hari Berbangkit",
    76: "Manusia", 77: "Malaikat yang Diutus", 78: "Berita Besar", 79: "Malaikat yang Mencabut", 80: "Ia Bermuka Masam",
    81: "Menggulung", 82: "Terbelah", 83: "Orang-Orang Curang", 84: "Terbelah", 85: "Gugusan Bintang",
    86: "Yang Datang di Malam Hari", 87: "Yang Maha Tinggi", 88: "Hari Pembalasan", 89: "Fajar", 90: "Negeri",
    91: "Matahari", 92: "Malam", 93: "Waktu Dhuha", 94: "Melapangkan", 95: "Buah Tin",
    96: "Segumpal Darah", 97: "Kemuliaan", 98: "Bukti Nyata", 99: "Keguncangan", 100: "Kuda Perang",
    101: "Hari Kiamat", 102: "Bermegah-Megahan", 103: "Masa", 104: "Pengumpat", 105: "Gajah",
    106: "Kaum Quraisy", 107: "Barang yang Berguna", 108: "Nikmat yang Berlimpah", 109: "Orang-Orang Kafir", 110: "Pertolongan",
    111: "Gejolak Api", 112: "Keesaan Allah", 113: "Waktu Subuh", 114: "Manusia"
}

DAILY_PRAYERS = [
    {
        "category": "Harian",
        "title": "Doa Sebelum Tidur",
        "arabic": "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
        "transliteration": "Bismikallahumma amuutu wa ahyaa",
        "translation_id": "Dengan nama-Mu ya Allah, aku mati dan aku hidup.",
        "reference": "HR. Bukhari no. 6324"
    },
    {
        "category": "Harian",
        "title": "Doa Bangun Tidur",
        "arabic": "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
        "transliteration": "Alhamdulillahilladzi ahyaana ba'da maa amaatana wa ilaihin nusyuur",
        "translation_id": "Segala puji bagi Allah yang menghidupkan kami kembali setelah mematikan kami dan kepada-Nya tempat kembali.",
        "reference": "HR. Bukhari no. 6312"
    },
    {
        "category": "Harian",
        "title": "Doa Sebelum Makan",
        "arabic": "بِسْمِ اللَّهِ",
        "transliteration": "Bismillah",
        "translation_id": "Dengan nama Allah.",
        "reference": "HR. Abu Dawud no. 3767, At-Tirmidzi no. 1858"
    },
    {
        "category": "Harian",
        "title": "Doa Sesudah Makan",
        "arabic": "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
        "transliteration": "Alhamdulillahilladzi ath'amanii hadza wa razaqaniihi min ghairi haulin minnii wa laa quwwah",
        "translation_id": "Segala puji bagi Allah yang memberi makan ini kepadaku dan mengaruniakannya kepadaku tanpa daya dan kekuatan dariku.",
        "reference": "HR. Abu Dawud no. 4023, At-Tirmidzi no. 3458"
    },
    {
        "category": "Harian",
        "title": "Doa Masuk Masjid",
        "arabic": "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
        "transliteration": "Allahummaftah lii abwaaba rahmatik",
        "translation_id": "Ya Allah, bukakanlah untukku pintu-pintu rahmat-Mu.",
        "reference": "HR. Muslim no. 713"
    },
    {
        "category": "Harian",
        "title": "Doa Keluar Masjid",
        "arabic": "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
        "transliteration": "Allahumma innii as-aluka min fadhlik",
        "translation_id": "Ya Allah, sesungguhnya aku memohon keutamaan dari-Mu.",
        "reference": "HR. Muslim no. 713"
    },
    {
        "category": "Harian",
        "title": "Doa Masuk Kamar Mandi",
        "arabic": "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
        "transliteration": "Allahumma innii a'uudzu bika minal khubutsi wal khabaa-its",
        "translation_id": "Ya Allah, aku berlindung kepada-Mu dari godaan setan laki-laki dan setan perempuan.",
        "reference": "HR. Bukhari no. 142, Muslim no. 375"
    },
    {
        "category": "Harian",
        "title": "Doa Keluar Kamar Mandi",
        "arabic": "غُفْرَانَكَ",
        "transliteration": "Ghufranaka",
        "translation_id": "Aku memohon ampunan-Mu.",
        "reference": "HR. Abu Dawud no. 17, At-Tirmidzi no. 7"
    },
    {
        "category": "Harian",
        "title": "Doa Keluar Rumah",
        "arabic": "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
        "transliteration": "Bismillahi tawakkaltu 'alallah, laa hawla wa laa quwwata illa billah",
        "translation_id": "Dengan nama Allah, aku bertawakal kepada Allah. Tiada daya dan kekuatan kecuali dengan pertolongan Allah.",
        "reference": "HR. Abu Dawud no. 5095, At-Tirmidzi no. 3426"
    },
    {
        "category": "Harian",
        "title": "Doa Naik Kendaraan",
        "arabic": "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
        "transliteration": "Subhaanalladzi sakh-khara lana hadza wa ma kunna lahu muqriniin, wa innaa ilaa rabbina lamunqalibuun",
        "translation_id": "Maha Suci Allah yang telah menundukkan semua ini bagi kami padahal kami sebelumnya tidak mampu menguasainya, dan sesungguhnya kami akan kembali kepada Tuhan kami.",
        "reference": "HR. Abu Dawud no. 2602, At-Tirmidzi no. 3446"
    },
    {
        "category": "Sholat",
        "title": "Doa Iftitah",
        "arabic": "اللَّهُمَّ بَاعِدْ بَيْنِي وَبَيْنَ خَطَايَايَ كَمَا بَاعَدْتَ بَيْنَ الْمَشْرِقِ وَالْمَغْرِبِ، اللَّهُمَّ نَقِّنِي مِنْ خَطَايَايَ كَمَا يُنَقَّى الثَّوْبُ الأَبْيَضُ مِنَ الدَّنَسِ، اللَّهُمَّ اغْسِلْنِي مِنْ خَطَايَايَ بِالثَّلْجِ وَالْمَاءِ وَالْبَرَدِ",
        "transliteration": "Allahumma baa'id baynii wa bayna khathaayaaya kama baa'adta baynal masyriqi wal maghrib...",
        "translation_id": "Ya Allah, jauhkanlah antara diriku dan kesalahan-kesalahanku sebagaimana Engkau menjauhkan antara timur dan barat. Ya Allah, bersihkanlah aku dari kesalahan-kesalahanku sebagaimana kain putih dibersihkan dari kotoran. Ya Allah, cucilah aku dari kesalahan-kesalahanku dengan salju, air, dan embun.",
        "reference": "HR. Bukhari no. 744, Muslim no. 598"
    },
    {
        "category": "Sholat",
        "title": "Doa Ruku'",
        "arabic": "سُبْحَانَ رَبِّيَ الْعَظِيمِ وَبِحَمْدِهِ",
        "transliteration": "Subhaana rabbiyal 'azhiimi wa bihamdih",
        "translation_id": "Maha Suci Tuhanku Yang Maha Agung dan dengan memuji-Nya (3x).",
        "reference": "HR. Abu Dawud no. 870"
    },
    {
        "category": "Sholat",
        "title": "Doa I'tidal",
        "arabic": "رَبَّنَا وَلَكَ الْحَمْدُ حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ",
        "transliteration": "Rabbanaa wa lakal hamdu hamdan katsiiran thayyiban mubaarakan fiih",
        "translation_id": "Wahai Tuhan kami, bagi-Mu segala puji, pujian yang banyak, baik dan penuh berkah.",
        "reference": "HR. Bukhari no. 799"
    },
    {
        "category": "Sholat",
        "title": "Doa Sujud",
        "arabic": "سُبْحَانَ رَبِّيَ الأَعْلَى وَبِحَمْدِهِ",
        "transliteration": "Subhaana rabbiyal a'laa wa bihamdih",
        "translation_id": "Maha Suci Tuhanku Yang Maha Tinggi dan dengan memuji-Nya (3x).",
        "reference": "HR. Abu Dawud no. 870"
    },
    {
        "category": "Sholat",
        "title": "Doa Duduk Antara Dua Sujud",
        "arabic": "رَبِّ اغْفِرْ لِي وَارْحَمْنِي وَاجْبُرْنِي وَارْفَعْنِي وَارْزُقْنِي وَاهْدِنِي وَعَافِنِي",
        "transliteration": "Rabbighfir lii warhamnii wajburnii warfa'nii warzuqnii wahdinii wa 'aafinii",
        "translation_id": "Ya Tuhanku, ampunilah aku, sayangilah aku, cukupkanlah kekuranganku, tinggikanlah derajatku, berilah aku rezeki, berilah aku petunjuk, dan berilah aku kesehatan.",
        "reference": "HR. Abu Dawud no. 850, At-Tirmidzi no. 284"
    },
    {
        "category": "Harian",
        "title": "Doa Kedua Orang Tua",
        "arabic": "رَبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
        "transliteration": "Rabbighfir lii wa liwaalidayya warhamhumaa kamaa rabbayaanii shaghiiraa",
        "translation_id": "Wahai Tuhanku, ampunilah aku dan kedua orang tuaku, dan sayangilah keduanya sebagaimana mereka mendidikku saat kecil.",
        "reference": "QS. Al-Isra': 24"
    },
    {
        "category": "Harian",
        "title": "Doa Kebaikan Dunia dan Akhirat (Sapu Jagad)",
        "arabic": "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "transliteration": "Rabbanaa aatinaa fid dunyaa hasanah wa fil aakhirati hasanah wa qinaa 'adzaaban naar",
        "translation_id": "Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, dan lindungilah kami dari siksa neraka.",
        "reference": "QS. Al-Baqarah: 201"
    }
]

def extract_font():
    os.makedirs(FONT_DIR, exist_ok=True)
    zip_path = os.path.join(BASE_DIR, "LPMQ IsepMisbah.zip")
    out_font = os.path.join(FONT_DIR, "lpmq_isep_misbah.ttf")
    if os.path.exists(zip_path):
        with zipfile.ZipFile(zip_path, 'r') as z:
            for name in z.namelist():
                if name.endswith('.ttf') or name.endswith('.otf'):
                    data = z.read(name)
                    with open(out_font, 'wb') as f:
                        f.write(data)
                    print(f"[OK] Font extracted: {out_font} ({len(data)} bytes)")
                    return
    print("[WARN] LPMQ IsepMisbah.zip not found or font not found in zip")

def parse_metadata():
    xml_path = os.path.join(BASE_DIR, "quran-data.xml")
    tree = ET.parse(xml_path)
    root = tree.getroot()

    surahs = []
    suras_el = root.find("suras")
    for s in suras_el.findall("sura"):
        idx = int(s.attrib["index"])
        surahs.append({
            "number": idx,
            "name_arabic": s.attrib["name"],
            "name_latin": s.attrib["tname"],
            "name_translation": SURAH_NAMES_ID.get(idx, s.attrib["ename"]),
            "revelation_type": s.attrib["type"],
            "total_ayah": int(s.attrib["ayas"]),
            "order": int(s.attrib["order"])
        })

    juzs = []
    juzs_el = root.find("juzs")
    for j in juzs_el.findall("juz"):
        juzs.append((int(j.attrib["index"]), int(j.attrib["sura"]), int(j.attrib["aya"])))

    pages = []
    pages_el = root.find("pages")
    for p in pages_el.findall("page"):
        pages.append((int(p.attrib["index"]), int(p.attrib["sura"]), int(p.attrib["aya"])))

    manzils = []
    manzils_el = root.find("manzils")
    for m in manzils_el.findall("manzil"):
        manzils.append((int(m.attrib["index"]), int(m.attrib["sura"]), int(m.attrib["aya"])))

    sajdas = set()
    sajdas_el = root.find("sajdas")
    if sajdas_el is not None:
        for sj in sajdas_el.findall("sajda"):
            sajdas.add((int(sj.attrib["sura"]), int(sj.attrib["aya"])))

    return surahs, juzs, pages, manzils, sajdas

def get_mapping_val(boundary_list, sura, aya):
    # boundary_list is sorted: [(index, sura, aya), ...]
    current_val = boundary_list[0][0]
    for idx, s, a in boundary_list:
        if (sura > s) or (sura == s and aya >= a):
            current_val = idx
        else:
            break
    return current_val

def parse_sql_values(file_path):
    # Extracts tuples: (index, sura, aya, text)
    result = {}
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Regex matching (index, sura, aya, 'text')
    # Text can contain escaped quotes \'
    pattern = re.compile(r"\((\d+),\s*(\d+),\s*(\d+),\s*'((?:''|\\'|[^'])*)'\)", re.UNICODE)
    matches = pattern.findall(content)
    for idx_str, sura_str, aya_str, text in matches:
        idx = int(idx_str)
        sura = int(sura_str)
        aya = int(aya_str)
        clean_text = text.replace("''", "'").replace("\\'", "'")
        result[(sura, aya)] = clean_text

    print(f"[OK] Parsed {len(result)} verses from {os.path.basename(file_path)}")
    return result

def parse_asmaul_husna():
    json_path = os.path.join(BASE_DIR, "asmaul-husna.json")
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    items = []
    for item in data:
        items.append({
            "number": int(item["urutan"]),
            "arabic": item["arab"].strip(),
            "latin": item["latin"].strip(),
            "translation_id": item["arti"].strip(),
            "meaning_detail": item["arti"].strip()
        })
    print(f"[OK] Parsed {len(items)} Asmaul Husna items")
    return items

def parse_dzikr():
    json_path = os.path.join(BASE_DIR, "dzikr.json")
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    items = []
    for d in data.get("pagi", []):
        items.append({
            "category": "Pagi-Petang",
            "title": f"[Pagi] {d['judul']}",
            "arabic": d.get("arab", "").strip(),
            "transliteration": d.get("latin") or "",
            "translation_id": d.get("arti", "").strip(),
            "reference": d.get("dalil") or "Dzikir Pagi"
        })
    for d in data.get("petang", []):
        items.append({
            "category": "Pagi-Petang",
            "title": f"[Petang] {d['judul']}",
            "arabic": d.get("arab", "").strip(),
            "transliteration": d.get("latin") or "",
            "translation_id": d.get("arti", "").strip(),
            "reference": d.get("dalil") or "Dzikir Petang"
        })
    for d in DAILY_PRAYERS:
        items.append(d)
    print(f"[OK] Parsed {len(items)} Doa & Dzikir items (including daily & prayer)")
    return items

def build_database():
    os.makedirs(ASSETS_DIR, exist_ok=True)
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)

    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    cur.execute("PRAGMA page_size = 4096;")
    cur.execute("PRAGMA foreign_keys = ON;")

    # 1. TABEL SURAH
    cur.execute("""
    CREATE TABLE surah (
        id INTEGER PRIMARY KEY,
        number INTEGER NOT NULL UNIQUE,
        name_arabic TEXT NOT NULL,
        name_latin TEXT NOT NULL,
        name_translation TEXT NOT NULL,
        revelation_type TEXT NOT NULL,
        total_ayah INTEGER NOT NULL,
        page_number INTEGER NOT NULL
    );
    """)

    # 2. TABEL AYAT
    cur.execute("""
    CREATE TABLE ayah (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        surah_id INTEGER NOT NULL,
        ayah_number INTEGER NOT NULL,
        text_uthmani TEXT NOT NULL,
        translation_id TEXT NOT NULL,
        juz INTEGER NOT NULL,
        page INTEGER NOT NULL,
        manzil INTEGER NOT NULL,
        sajdah INTEGER NOT NULL DEFAULT 0,
        FOREIGN KEY(surah_id) REFERENCES surah(id)
    );
    """)

    cur.execute("CREATE INDEX idx_ayah_surah ON ayah(surah_id);")
    cur.execute("CREATE INDEX idx_ayah_juz ON ayah(juz);")
    cur.execute("CREATE INDEX idx_ayah_page ON ayah(page);")

    # 3. TABEL ASMAUL HUSNA
    cur.execute("""
    CREATE TABLE asmaul_husna (
        id INTEGER PRIMARY KEY,
        number INTEGER NOT NULL UNIQUE,
        arabic TEXT NOT NULL,
        latin TEXT NOT NULL,
        translation_id TEXT NOT NULL,
        meaning_detail TEXT NOT NULL
    );
    """)

    # 4. TABEL KUMPULAN DOA
    cur.execute("""
    CREATE TABLE doa (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        category TEXT NOT NULL,
        title TEXT NOT NULL,
        arabic TEXT NOT NULL,
        transliteration TEXT NOT NULL,
        translation_id TEXT NOT NULL,
        reference TEXT NOT NULL
    );
    """)

    cur.execute("CREATE INDEX idx_doa_category ON doa(category);")

    # 5. TABEL RECITER & AUDIO METADATA
    cur.execute("""
    CREATE TABLE reciter (
        id INTEGER PRIMARY KEY,
        name TEXT NOT NULL,
        subfolder_path TEXT NOT NULL
    );
    """)

    cur.execute("""
    CREATE TABLE audio_metadata (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        ayah_id INTEGER NOT NULL,
        reciter_id INTEGER NOT NULL,
        remote_url TEXT NOT NULL,
        local_path TEXT,
        duration_ms INTEGER DEFAULT 0,
        file_size_bytes INTEGER DEFAULT 0,
        download_status INTEGER DEFAULT 0,
        FOREIGN KEY(ayah_id) REFERENCES ayah(id),
        FOREIGN KEY(reciter_id) REFERENCES reciter(id)
    );
    """)

    cur.execute("CREATE UNIQUE INDEX idx_audio_ayah_reciter ON audio_metadata(ayah_id, reciter_id);")

    # Parse data sources
    surahs_meta, juzs_meta, pages_meta, manzils_meta, sajdas_meta = parse_metadata()
    uthmani_dict = parse_sql_values(os.path.join(BASE_DIR, "quran-uthmani.sql"))
    trans_dict = parse_sql_values(os.path.join(BASE_DIR, "id.indonesian.sql"))
    asmaul_husna_items = parse_asmaul_husna()
    doa_items = parse_dzikr()

    # Insert surahs
    for s in surahs_meta:
        surah_page = get_mapping_val(pages_meta, s["number"], 1)
        cur.execute("""
        INSERT INTO surah (id, number, name_arabic, name_latin, name_translation, revelation_type, total_ayah, page_number)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            s["number"],
            s["number"],
            s["name_arabic"],
            s["name_latin"],
            s["name_translation"],
            s["revelation_type"],
            s["total_ayah"],
            surah_page
        ))

    # Insert ayahs
    ayah_rows = []
    audio_rows = []
    ayah_id_counter = 1
    for s in surahs_meta:
        s_num = s["number"]
        for a_num in range(1, s["total_ayah"] + 1):
            key = (s_num, a_num)
            uthmani = uthmani_dict.get(key, "")
            trans = trans_dict.get(key, "")
            juz = get_mapping_val(juzs_meta, s_num, a_num)
            page = get_mapping_val(pages_meta, s_num, a_num)
            manzil = get_mapping_val(manzils_meta, s_num, a_num)
            is_sajdah = 1 if key in sajdas_meta else 0

            ayah_rows.append((
                ayah_id_counter,
                s_num,
                a_num,
                uthmani,
                trans,
                juz,
                page,
                manzil,
                is_sajdah
            ))

            # Sample audio url for Mishary Rashid Alafasy
            # EveryAyah format: 001001.mp3
            audio_url = f"https://everyayah.com/data/Alafasy_128kbps/{s_num:03d}{a_num:03d}.mp3"
            audio_rows.append((
                ayah_id_counter,
                1, # reciter_id 1
                audio_url,
                None,
                0,
                0,
                0
            ))

            ayah_id_counter += 1

    cur.executemany("""
    INSERT INTO ayah (id, surah_id, ayah_number, text_uthmani, translation_id, juz, page, manzil, sajdah)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, ayah_rows)

    print(f"[OK] Inserted {len(ayah_rows)} verses into ayah table")

    # Insert reciters
    reciters = [
        (1, "Misyari Rasyid Al-Afasy", "mishary_alafasy"),
        (2, "Abdul Basit Abdul Samad", "abdul_basit"),
        (3, "Mahmoud Khalil Al-Husary", "al_husary")
    ]
    cur.executemany("INSERT INTO reciter (id, name, subfolder_path) VALUES (?, ?, ?)", reciters)

    # Insert audio_metadata
    cur.executemany("""
    INSERT INTO audio_metadata (ayah_id, reciter_id, remote_url, local_path, duration_ms, file_size_bytes, download_status)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    """, audio_rows)

    # Insert Asmaul Husna
    cur.executemany("""
    INSERT INTO asmaul_husna (id, number, arabic, latin, translation_id, meaning_detail)
    VALUES (?, ?, ?, ?, ?, ?)
    """, [(item["number"], item["number"], item["arabic"], item["latin"], item["translation_id"], item["meaning_detail"]) for item in asmaul_husna_items])

    # Insert Doa
    cur.executemany("""
    INSERT INTO doa (category, title, arabic, transliteration, translation_id, reference)
    VALUES (?, ?, ?, ?, ?, ?)
    """, [(d["category"], d["title"], d["arabic"], d["transliteration"], d["translation_id"], d["reference"]) for d in doa_items])

    conn.commit()

    # Optimize database
    cur.execute("PRAGMA optimize;")
    cur.execute("VACUUM;")
    cur.execute("ANALYZE;")
    conn.commit()
    conn.close()

    # Calculate SHA-256
    hasher = hashlib.sha256()
    with open(DB_PATH, "rb") as f:
        while chunk := f.read(65536):
            hasher.update(chunk)
    db_hash = hasher.hexdigest().upper()
    db_size = os.path.getsize(DB_PATH)

    print(f"==================================================")
    print(f"[SUCCESS] Database created: {DB_PATH}")
    print(f"File Size: {db_size} bytes ({db_size / (1024*1024):.2f} MB)")
    print(f"SHA-256 Hash: {db_hash}")
    print(f"==================================================")
    return db_hash

if __name__ == "__main__":
    extract_font()
    db_hash = build_database()
    # Save hash to a small metadata text file for verification
    meta_path = os.path.join(ASSETS_DIR, "db_checksum.txt")
    with open(meta_path, "w", encoding="utf-8") as f:
        f.write(db_hash)
    print(f"Hash written to {meta_path}")
