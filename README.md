# Aplikasi SPK Pemilihan Vendor IT (AHP + TOPSIS)

Aplikasi desktop berbasis JavaFX untuk Sistem Pendukung Keputusan (SPK) pemilihan vendor IT. Aplikasi ini mengkombinasikan metode **Analytical Hierarchy Process (AHP)** untuk menentukan bobot kriteria secara konsisten dan **Technique for Others Preference by Similarity to Ideal Solution (TOPSIS)** untuk memberikan perankingan alternatif vendor terbaik.

Aplikasi dibangun menggunakan prinsip **Clean Architecture** (tanpa framework besar seperti Spring) dan dibalut dengan desain antarmuka modern bertema **Neumorphism**.

## ✨ Scope Fitur

### 🧑‍💻 Role: Administrator

1. **Data Kriteria**
   * Menambah, mengedit, dan menghapus kriteria.
   * Menentukan tipe kriteria: *Benefit* (semakin besar semakin baik) atau *Cost* (semakin kecil semakin baik).
2. **Data Alternatif (Vendor)**
   * Menambah, mengedit, dan menghapus data vendor beserta alamatnya.
3. **Data Penilaian**
   * Menginput nilai masing-masing vendor terhadap setiap kriteria.
   * Dilengkapi validasi ketat: tidak boleh ada nilai kosong dan semua vendor harus memiliki nilai untuk semua kriteria.
4. **Data Perhitungan (AHP)**
   * Input *pairwise comparison* (perbandingan berpasangan) antar kriteria.
   * Generate matriks AHP secara otomatis.
   * Kalkulasi normalisasi matriks, bobot kriteria, dan nilai *Consistency Ratio* (CR).
   * Validasi konsistensi matriks (CR $\le 0.1$).
5. **Data Hasil Akhir (TOPSIS)**
   * Generate ranking vendor secara otomatis.
   * Proses kalkulasi meliputi: mengambil bobot kriteria dari AHP, normalisasi matriks keputusan, menghitung matriks solusi ideal positif/negatif, menghitung jarak ideal, dan menghitung skor preferensi akhir.
6. **Data User**
   * Manajemen data pengguna sistem (CRUD).
   * Pengaturan akses *role* (Admin/User).
7. **Data Profile**
   * Mengubah profil dan mengganti password.

### 👤 Role: User

1. **Data Hasil Akhir**
   * Melihat hasil akhir ranking vendor.
   * Melihat detail nilai dan skor preferensi secara transparan.
2. **Data Profile**
   * Mengubah profil dan mengganti password.

## 🛠️ Teknologi yang Digunakan

* **Bahasa**: Java 17+
* **GUI Framework**: JavaFX 21 (`javafx-controls`, `javafx-fxml`)
* **Build Tool**: Maven
* **Database**: SQLite (via JDBC, tanpa ORM untuk performa yang lebih ringan)
* **Keamanan**: BCrypt (hashing password)
* **Arsitektur Code**: Clean Architecture (Domain, UseCase, Repository, Presentation)

## 🏗️ Struktur Arsitektur

* `com.spk.domain`: Berisi entitas utama (Criteria, Vendor, User, Score, AHPResult, TOPSISResult).
* `com.spk.usecase`: Berisi inti *business logic* dan algoritma perhitungan AHP & TOPSIS.
* `com.spk.repository`: Mengatur koneksi langsung ke SQLite database (JDBC).
* `com.spk.presentation`: Mengatur antarmuka pengguna (UI) menggunakan komponen JavaFX.

## 🚀 Cara Menjalankan Aplikasi

Pastikan sistem Anda sudah terinstal **Java (JDK 17 atau lebih baru)** dan **Maven**.

1. Clone repositori ini atau buka melalui IDE Anda.
2. Buka terminal/Command Prompt dan arahkan ke direktori root proyek ini.
3. Jalankan perintah berikut untuk mengunduh dependensi dan menjalankan aplikasi:

```bash
mvn clean compile javafx:run
```

Atau cukup gunakan satu perintah (jika sudah dicompile sebelumnya):

```bash
mvn javafx:run
```

**Catatan Login Pertama Kali:**
Sistem akan otomatis membuat akun admin *default* saat pertama kali dijalankan.
* **Username**: `admin`
* **Password**: `admin123`

## 🔒 Keamanan & Performa
* Semua password dienkripsi secara aman di database menggunakan algoritma hashing BCrypt.
* Validasi ketat pada setiap layer (UI & UseCase) memastikan tidak ada *NullPointerException* atau data nilai/matriks yang terlewat sebelum kalkulasi dilakukan.
* Tidak perlu konfigurasi server database karena SQLite tersimpan lokal secara portabel di `spk_ahp_topsis.db`.
