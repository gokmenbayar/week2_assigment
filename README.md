# DevCluster — Donanım havuzu

**week2_assignment** · Kotlin / Jetpack Compose

Geliştirme ortamındaki bilgisayarların, test cihazlarının ve diğer ekipmanların dağıtımını izleyen küçük bir kaynak yönetim paneli. Bir donanımı **Ayır +** ile ayırınca kalan stok azalıyor; ayrılan toplam cihaz sayısı ve maliyet özeti güncelleniyor. Stok bitince aynı cihazdan yeni bir ayırma yapılamıyor.

## Kullandığım yaklaşım

- Ödevde verilen sekiz ürünün adı, kimliği, kategorisi, maliyeti ve stokları değiştirilmedi.
- **İsim, kategori ya da cihaz koduyla arama** ve yatay kategori filtreleri var.
- Ekran yatay/dikey çevrilince arama metni ve seçilen kategori `rememberSaveable` sayesinde korunuyor.
- Arama sonuçları `remember(searchQuery)` ile, kategori filtresi ise `remember(selectedCategory)` ile önbelleğe alınıyor. Stok değiştiğinde yalnızca görüntülenen stoklar güncelleniyor; arama algoritması gereksiz yere tekrar çalışmıyor.
- Kart listesi `LazyColumn` ve `items(key = { it.id })` kullanıyor. Kategori seçenekleri de `LazyRow` içinde çiziliyor.
- `HardwareItem` sınıfının alanları `val` ve sınıf `@Immutable` işaretli. Stok değişikliği `copy` ile yeni veri üreterek uygulanıyor.
- `HardwareCard` ve `HardwareSearchBar` kendi durumunu tutmuyor. Veriyi üst bileşenden alıp etkileşimleri callback ile iletiyor.
- Kartın tıklanabilir alanında `.clickable()` → `.padding()` sıralaması korunuyor.
- Filtreleme fonksiyonları ayrı bir Kotlin dosyasında. Temel durumlar için birim testleri bulunuyor.

## Android Studio'da açmak

1. Bu depoyu indir ya da klonla. Android Studio'da klasörü **Open** ile aç.
2. Gradle senkronizasyonunu tamamla. İlk seferinde ağ bağlantısı gerekir.
3. Android 8.0 (API 26) veya üzeri emülatör/cihaz seçerek **Run 'app'** de.

**Proje ortamı:** JDK 17, Gradle 8.13, AGP 8.13.2, Kotlin 2.2.20, Compose BOM 2025.12.00; `compileSdk = 36`.

İlk terminal derlemesinde `gradlew` eksik Gradle wrapper bileşenini Gradle'ın GitHub deposundaki resmî sürümünden indirir. İnternet bağlantısı gerekir.

## Telefondan GitHub'a yükleme

GitHub'ın sıradan **Upload files** ekranına ZIP yüklemek, içindeki klasörleri kendiliğinden açmaz. Klasör yapısını korumak için GitHub **Codespaces** kullanılabilir:

1. GitHub'da **week2_assignment** adında **Public** bir depo oluştur. Depo oluştururken **Add a README file** seçeneğini işaretle.
2. Depodaki **Code → Codespaces → Create codespace on main** seçeneğiyle tarayıcıda kod düzenleyicisini aç. Gerekirse Chrome'un **Masaüstü sitesi** modunu kullan.
3. Soldaki Explorer bölümünde **Upload...** ile indirilen `week2_assignment_github.zip` dosyasını proje ana klasörüne yükle.
4. **Terminal → New Terminal** bölümünde sırayla şu komutları çalıştır:

   ```bash
   unzip -oq week2_assignment_github.zip
   rm week2_assignment_github.zip
   git add -A
   git commit -m "DevCluster kaynak yonetim paneli"
   git push origin main
   ```

5. GitHub deposuna geri dön. `app/src/main/java/com/example/week2/MainActivity.kt` dosyasının varlığını kontrol et. **Actions** sekmesindeki *Android build check* işlemi otomatik olarak derleme ve birim testlerini dener. Başarılı olursa aynı çalışmanın *Artifacts* bölümünde debug APK bulunur.

> Codespaces hesabında kullanılamıyorsa, telefonda git komutlarını destekleyen bir araçla dosyaları ZIP'ten çıkarıp klasör yapısını koruyarak push etmek gerekir. ZIP'i tek başına repo dosyası olarak yüklemek, ödevin klasör şartını karşılamaz.

## Dizinler

- `app/src/main/java/com/example/week2/MainActivity.kt`: Compose ekranı, durum yönetimi ve stateless bileşenler
- `app/src/main/java/com/example/week2/HardwareItem.kt`: değişmez donanım modeli
- `app/src/main/java/com/example/week2/HardwareCatalog.kt`: sabit veriler ve filtre fonksiyonları
- `app/src/test/java/com/example/week2/HardwareCatalogTest.kt`: veri ve filtreleme testleri
- `.github/workflows/android.yml`: GitHub'da otomatik derleme ve test

**Not:** Bu bir yerel simülasyon. Gerçek cihaz rezervasyonu veya veri tabanı bağlantısı yok. Ayrılan cihaz sayısı ile toplam maliyet Activity yeniden oluşturulduğunda sıfırlanabilir; ödev gereği arama metni ise ekran döndürme işleminde korunur.
