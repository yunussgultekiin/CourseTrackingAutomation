# Course Tracking Automation - Proje Aktifleştirme Rehberi

Bu doküman, Course Tracking Automation projesini aktifleştirmek için gerekli adımları içermektedir.

## 📋 Gereksinimler

Projeyi çalıştırmadan önce aşağıdaki yazılımların sisteminizde yüklü olması gerekmektedir:

- **Java 17+ (LTS)** - Java 24 kullanılıyor (pom.xml'de belirtilmiş)
- **Maven 3.6+** - Bağımlılık yönetimi için
- **PostgreSQL 15+** - Veritabanı (Docker ile otomatik kurulum mevcut)
- **Docker & Docker Compose** - Veritabanı konteynerizasyonu için (opsiyonel)

## 🚀 Proje Aktifleştirme Adımları

### 1. Projeyi Klonlayın veya İndirin

```bash
cd "C:\Users\HP VICTUS\Desktop\javaproje\CourseTrackingAutomation"
```

### 2. Veritabanını Başlatın

#### Seçenek A: Docker ile (Önerilen)

```bash
docker-compose up -d
```

Bu komut PostgreSQL veritabanını başlatacak ve şu bilgilerle hazır hale getirecektir:
- **Host:** localhost
- **Port:** 5432
- **Database:** university_db
- **Username:** postgres
- **Password:** password123

#### Seçenek B: Manuel PostgreSQL Kurulumu

Eğer Docker kullanmıyorsanız, PostgreSQL'i manuel olarak kurup aşağıdaki veritabanını oluşturun:

```sql
CREATE DATABASE university_db;
```

`application.properties` dosyasındaki bağlantı bilgilerini kendi PostgreSQL ayarlarınıza göre güncelleyin.

### 3. Maven Bağımlılıklarını Yükleyin

```bash
mvn clean install
```

veya IDE'nizde (IntelliJ IDEA, Eclipse, VS Code) Maven projesini otomatik olarak yükleyin.

### 4. Projeyi Derleyin

```bash
mvn compile
```

### 5. Uygulamayı Çalıştırın

#### Seçenek A: Maven ile

```bash
mvn javafx:run
```

veya

```bash
mvn spring-boot:run
```

#### Seçenek B: IDE'den Çalıştırma

1. **IntelliJ IDEA:**
   - `CourseTrackingAutomationApplication.java` dosyasını açın
   - Sağ tıklayıp "Run 'CourseTrackingAutomationApplication.main()'" seçeneğini seçin

2. **Eclipse:**
   - `CourseTrackingAutomationApplication.java` dosyasını açın
   - Sağ tıklayıp "Run As" > "Java Application" seçeneğini seçin

3. **VS Code:**
   - Java Extension Pack'in yüklü olduğundan emin olun
   - `CourseTrackingAutomationApplication.java` dosyasında "Run" butonuna tıklayın

### 6. Uygulamayı Test Edin

Uygulama başladığında:
1. Login ekranı açılmalı
2. Veritabanı bağlantısı kontrol edilmeli
3. Herhangi bir hata mesajı görünmemeli

## 🔧 Yapılandırma

### Veritabanı Ayarları

`src/main/resources/application.properties` dosyasında veritabanı ayarlarını değiştirebilirsiniz:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/university_db
spring.datasource.username=postgres
spring.datasource.password=password123
```

### Loglama Ayarları

Log seviyelerini `application.properties` dosyasında ayarlayabilirsiniz:

```properties
logging.level.org.example.coursetrackingautomation=DEBUG
```

## 🐛 Sorun Giderme

### Veritabanı Bağlantı Hatası

- Docker container'ın çalıştığından emin olun: `docker ps`
- PostgreSQL portunun (5432) kullanılabilir olduğunu kontrol edin
- `application.properties` dosyasındaki bağlantı bilgilerini kontrol edin

### JavaFX Başlatma Hatası

- Java sürümünün 17+ olduğundan emin olun: `java -version`
- Maven bağımlılıklarının yüklendiğinden emin olun: `mvn dependency:resolve`
- JavaFX modül yolunun doğru olduğundan emin olun

### Port Çakışması

- PostgreSQL portu (5432) başka bir uygulama tarafından kullanılıyorsa:
  - Docker Compose dosyasında portu değiştirin
  - `application.properties` dosyasında yeni portu belirtin

## 📝 Notlar

- İlk çalıştırmada JPA/Hibernate otomatik olarak veritabanı şemasını oluşturacaktır (`spring.jpa.hibernate.ddl-auto=update`)
- Uygulama kapatıldığında Spring context otomatik olarak kapanacaktır
- Loglar konsola yazılacaktır (SLF4J kullanılıyor)

## 🎯 Sonraki Adımlar

1. Login ekranından giriş yapın (kullanıcı bilgileri DataSeeder ile oluşturulacak)
2. Admin Dashboard'a erişin
3. Kullanıcı, ders ve kayıt yönetimini test edin

## 📞 Destek

Herhangi bir sorunla karşılaşırsanız:
1. Log dosyalarını kontrol edin
2. Veritabanı bağlantısını test edin
3. Maven bağımlılıklarının güncel olduğundan emin olun

