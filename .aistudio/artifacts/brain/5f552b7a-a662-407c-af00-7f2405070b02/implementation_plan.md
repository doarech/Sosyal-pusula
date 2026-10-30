# Sosyal Pusula: Yapay Zeka Destekli Yalnızlık Ölçümü ve Yaşam Rehberi

Sosyal Pusula, kullanıcıların yalnızlık düzeyini standart statik testler yerine yapay zekanın dinamik ve empatik sorularıyla ölçen, ekran süresiyle ilişkisini analiz eden ve kişiye özel günlük eylem görevleri ile motivasyon kartları sunan modern bir Android sağlık ve refah uygulamasıdır.

## User Review & Critical Decisions

> [!IMPORTANT]
> Yapay zeka ve kullanıcı tercihleri doğrultusunda belirlenen kritik tasarım ve mimari kararlar aşağıda özetlenmiştir:

- **Doğrulanmış Karar 1 (Yapay Zeka Destekli Ölçüm)**: Standart 20 maddelik sabit UCLA testi yerine, yapay zekanın kullanıcının ruh halini, sosyal hislerini ve önceki yanıtlarını dikkate alarak dinamik ve akıcı sorular sorduğu uyarlanabilir (adaptive) bir ölçüm akışı kurgulanacaktır.
- **Doğrulanmış Karar 2 (Yapay Zeka Önerileri ve Görevler)**: Öneriler salt metin tavsiyeler yerine, tamamlanabilir günlük kişisel mikro-görevler (social challenges, mindful screen-free tasks) ve ilham verici motivasyon kartları olarak sunulacaktır.
- **Doğrulanmış Karar 3 (Tasarım ve Görsel Kimlik)**: Sakinleştirici, yumuşak tonlara sahip modern açık ve koyu tema destekli Material 3 arayüzü; dairesel skor halkaları, ekran süresi korelasyon grafikleri ve akıcı geçiş animasyonlarıyla donatılacaktır.

---

## 1. Overview & Core Concept

- **What It Does**: Uygulama, kullanıcının haftalık ve günlük sosyal doyumunu, yalnızlık algısını ve akıllı telefon ekran süresini değerlendirir. Gemini AI destekli dinamik soru motoru ile kullanıcıyı yormayan, empatik bir değerlendirme yapar. Ölçüm sonrası, kullanıcının durumuna özel üretilen 3-4 adet günlük mikro-görev ve motivasyon kartı sunar; haftalık ilerlemeyi ve korelasyonu grafiklerle görselleştirir.
- **Target Audience / Persona**: Dijital dünyada yoğun zaman geçirip gerçek hayatta yalnızlık hisseden, sosyal bağlarını güçlendirmek ve ekran bağımlılığı ile yalnızlık arasındaki ilişkiyi somut verilerle görerek adım adım iyileşmek isteyen herkes.
- **Key Value**: Yargılayıcı olmayan, bilimsel temellere dayalı ancak yapay zekayla kişiselleştirilmiş bir rehberlik; ekran süresini düşürüp yüz yüze veya kaliteli sosyal etkileşimleri artıran uygulanabilir küçük adımlar.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Ana Ekran (Dashboard & Bugünün Pusulası)**:
   - Mevcut haftanın yalnızlık durumu, son ölçüm skoru ve dairesel ilerleme halkası.
   - Bugünün yapay zeka tarafından oluşturulmuş **Kişisel Görevleri** (örneğin "Eski bir dostuna hal hatır mesajı at", "Akşam yemeğinde telefonu başka odaya bırak"). Görevler tamamlandıkça işaretlenir ve kutlama mikro-animasyonu oynatılır.
   - Günün motivasyon kartı (yapay zeka tarafından her güne özel üretilmiş ilham verici alıntı ve telkin).
   - "Yeni Değerlendirme Başlat" birincil eylem butonu.

2. **Dinamik Yapay Zeka Ölçüm Akışı (Adaptive Check-In)**:
   - Adım adım ilerleyen, şık kart tabanlı soru ekranı.
   - Yapay zeka tarafından kullanıcının verdiği yanıtlara göre bağlamsal olarak yönlendirilen 5-7 arası dinamik soru + haftalık ekran süresi girişi.
   - Kullanıcı dilerse serbest metinle hissettiği duyguyu da belirtebilir; yapay zeka bu girdiyi puanlama ve tavsiye matrisinde derinleştirir.
   - Değerlendirme tamamlandığında dairesel analiz animasyonu ve detaylı sonuç paneli açılır.

3. **Sonuç ve Analiz Ekranı (AI Insights)**:
   - 0-80 skalasında hesaplanan dinamik Yalnızlık Skoru ve seviye etiketi (Düşük/Sağlıklı, Orta, Yüksek, Şiddetli).
   - Yapay zekanın kullanıcıya yazdığı şefkatli ve empatik özet değerlendirme.
   - Hemen başlatılabilecek yeni günlük mikro-görevlerin listesi.

4. **Dijital Denge ve Korelasyon Ekranı (Screen Time & Correlation)**:
   - Haftalık ekran süresi ile yalnızlık skoru arasındaki ilişkiyi gösteren çift sütunlu karşılaştırma grafiği.
   - Pearson korelasyon katsayısı ve anlaşılır açıklaması ("Ekran süren arttıkça yalnızlık hissin de yükseliyor" vb.).
   - Dijital detoks ipuçları.

5. **Geçmiş ve Gelişim Zaman Tüneli (History)**:
   - Önceki haftaların kayıtları, skor trendi, silme ve detay inceleme opsiyonları.

### Visual Identity & Theme

- **Aesthetic Direction**: Sakinleştirici, güven veren, modern wellness & mindfulness havası. Keskin çizgiler yerine yumuşatılmış yuvarlak köşeler (`RoundedCornerShape(16.dp)`), ferah boşluklar ve hafif gölgeler.
- **Color Palette**:
  - *Light Palette*: Arka plan yumuşak kırık beyaz (`#FAF9F6`), Kart zeminleri (`#FFFFFF`), Birincil Vurgu (`#2D3748` / Koyu Slate), İkincil Vurgu Teal/Mint (`#1D9E75`), Sıcak Amber/Terracotta (`#D85A30`), Dijital Denge İndigo (`#6B7CFF`).
  - *Dark Palette*: Arka plan derin gece grisi (`#12161A`), Yüzeyler (`#1A2026`), Vurgular parlak soft Teal (`#2DD4BF`), Yumuşak Turuncu (`#F97316`) ve İndigo (`#818CF8`).
- **Typography & Hierarchy**: Temiz, modern ve okunabilir sans-serif hiyerarşisi (`titleLarge`, `headlineMedium`, `bodyLarge`, `labelSmall`). Rakamlar ve skorlar için belirgin semi-bold glifler.
- **Interactive Feedback & Motion**: Görevleri tamamlarken checkbox haptic hissiyatı, sonuç ekranına girişte dairesel halka dolum animasyonu (`animateFloatAsState`), kartlar arası yumuşak yatay kayma.

---

## 3. Key Product Decisions & Trade-Offs

- **Dinamik Soru Motoru vs Statik UCLA Testi**:
  - *Seçilen Yaklaşım*: Bilimsel UCLA ölçeğinin ana boyutlarını (sosyal aidiyet, izolasyon, dertleşebilme) temel alan, ancak kullanıcının o anki durumuna göre yapay zeka tarafından üretilen 5-7 adet dinamik soru akışı.
  - *Gerekçe*: Kullanıcı statik 20 soruyu her hafta çözmekten sıkılabilir; dinamik sorular hem daha samimi bir his yaratır hem de bilişsel yükü azaltır.
  - *Güvenilirlik Koruması*: Yapay zekaya prompt seviyesinde standart UCLA değerlendirme kriterleri ve puanlama ağırlıkları verilecek, böylece skor her zaman 20-80 bilimsel aralığında kalacaktır.

- **Kişiselleştirilmiş Günlük Görevler & Motivasyon Kartları**:
  - *Seçilen Yaklaşım*: Yapay zekanın skora ve ekran süresine göre ürettiği 3 pratik mikro-görev (Sosyal, Dijital Detoks, Açık Hava/Aktivite) ve 1 günlük motivasyon kartı. Görevler Room veritabanında saklanarak kullanıcının tamamladığı görevler işaretlenebilecek.
  - *Gerekçe*: Kullanıcı sadece skorunu görmekle kalmaz, yalnızlığı azaltacak eyleme geçilebilir somut adımlar kazanır.

- **Veri Saklama: Offline-First Room Veritabanı**:
  - *Seçilen Yaklaşım*: Kullanıcının hassas duygu ve yalnızlık verilerini tamamen kendi cihazında saklayan Room Database mimarisi.
  - *Gerekçe*: Kullanıcı mahremiyeti bu tip psikolojik ölçümlerde en üst düzey önceliktir. Giriş/hesap zorunluluğu olmadan anında kullanılabilirlik sağlar.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI                       │
│  ┌───────────────┐ ┌────────────────┐ ┌──────────────────┐  │
│  │  HomeScreen   │ │  CheckInScreen │ │  DigitalBalance  │  │
│  │ (Cards/Tasks) │ │ (AI Questions) │ │ (Screen/Corr)    │  │
│  └───────┬───────┘ └────────┬───────┘ └────────┬─────────┘  │
└──────────┼──────────────────┼──────────────────┼────────────┘
           │                  │                  │
┌──────────▼──────────────────▼──────────────────▼────────────┐
│                        ViewModels                           │
│     (CompassViewModel, CheckInViewModel, HistoryViewModel)  │
└──────────┬─────────────────────────────────────┬────────────┘
           │                                     │
┌──────────▼──────────────────┐       ┌──────────▼────────────┐
│      Firebase AI Logic      │       │    Room Database      │
│  ┌───────────────────────┐  │       │  ┌─────────────────┐  │
│  │ Gemini 3.8 Flash      │  │       │  │ CheckInEntity   │  │
│  │ - Dynamic Questions   │  │       │  │ TaskEntity      │  │
│  │ - Loneliness Analysis │  │       │  │ (Offline local) │  │
│  │ - Daily Action Tasks  │  │       │  └─────────────────┘  │
│  └───────────────────────┘  │       └───────────────────────┘
└─────────────────────────────┘
```

### Data Model & Entities

```kotlin
// CheckInEntity (Room):
@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekString: String,      // e.g. "2026-W42"
    val timestamp: Long,
    val score: Int,              // 20 - 80
    val screenHours: Float,      // Daily avg screen hours
    val aiSummary: String,       // AI empathetic analysis
    val levelLabel: String       // e.g. "Orta Seviye"
)

// TaskEntity (Room):
@Entity(tableName = "daily_tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checkInId: Long,
    val title: String,
    val category: String,        // "Sosyal", "Dijital Detoks", "Aktivite"
    val isCompleted: Boolean = false,
    val dateString: String
)
```

### AI Integration Schema & Flow

Gemini AI (model: `gemini-3.8-flash`) iki noktada devreye girer:
1. **Dinamik Soru Üretimi**: Kullanıcı ankete başladığında, yapay zeka 5 adet samimi, empatik ve farklı sosyal boyutları (arkadaşlık, anlaşılma hissi, içe kapanıklık, teknoloji kullanımı) yoklayan çoktan seçmeli soru seti oluşturur.
2. **Skorlama & Görev Üretimi**: Kullanıcının yanıtları ve ekran süresi Gemini'ye gönderilerek yapılandırılmış JSON çıktısı alınır:
   - `score` (20-80 arası normalize UCLA skoru)
   - `summary` (kullanıcıya özel moral verici analiz)
   - `tasks` (3 adet uygulanabilir günlük mikro-eylem)
   - `motivationQuote` (günün ilham verici sözü)

---

## 5. Implementation Steps & Verification Plan

1. **Adım 1 - Firebase AI & App Check Kurulumu**:
   - `MainActivity.kt` üzerinde `configureAppCheck(this, intent)` çağrısının entegre edilmesi.
   - `Firebase.ai` Gemini model yapılandırması ve prompt/schema katmanının hazırlanması.
2. **Adım 2 - Yerel Room Veritabanı ve Repository**:
   - `CheckInEntity`, `TaskEntity`, `CompassDao` ve `CompassDatabase` sınıflarının oluşturulması.
   - Repository üzerinden offline-first veri akışının bağlanması.
3. **Adım 3 - UI Bileşenleri ve Ekranlar**:
   - M3 Teması (`Theme.kt`, `Color.kt`, sakinleştirici açık ve koyu renk şemaları).
   - `HomeScreen`: Özet skor halkası, bugünün görevleri (checkbox & durum takibi), motivasyon kartı.
   - `CheckInScreen`: Yapay zeka dinamik soru kartları, akıcı geçiş animasyonu, ekran süresi seçici.
   - `DigitalBalanceScreen`: Çift çubuklu karşılaştırma grafiği, Pearson korelasyon hesabı ve rehber ipuçları.
   - `HistoryScreen`: Geçmiş haftaların dökümü, istatistik kartları ve kayıt silme.
4. **Adım 4 - Derleme ve Doğrulama**:
   - `compile_applet` ile projenin sıfır hatayla derlendiğinin doğrulanması.
   - Görev tamamlama, dinamik anket akışı ve veri kalıcılığının test edilmesi.
