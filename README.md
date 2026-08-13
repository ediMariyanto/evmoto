# EVmoto Waiting Fee & Cancellation Engine

Backend service untuk menghitung waiting fee dan cancellation fee pada aplikasi
ride-hailing EVmoto.

## 1. How To Run

### Prerequisites

* Java 17
* Gradle

### Run Application

* Linux / macOS: ./gradlew
* Windows: gradlew.bat 

### Run Unit Tests

* Linux / macOS: ./gradlew test
* Windows: gradlew.bat test

### Build
./gradlew clean build

---
## 2. Call API

### Endpoint

POST `/v1/orders/{orderId}/fee-preview`

### Request

```json
{
  "arrivedAt": "2026-08-10T09:00:00+07:00",
  "endedAt": "2026-08-10T09:21:40+07:00",
  "endReason": "CANCELLED_BY_CUSTOMER",
  "pickupPoint": {
    "lat": -6.21462,
    "lng": 106.84513
  },
  "driverPings": [
    {
      "at": "2026-08-10T09:00:00+07:00",
      "lat": -6.21462,
      "lng": 106.84513
    },
    {
      "at": "2026-08-10T09:08:00+07:00",
      "lat": -6.21980,
      "lng": 106.85110
    }
  ]
}
```

---
## 2. Asumsi
Spesifikasi memiliki beberapa kondisi yang belum didefinisikan secara eksplisit, saya butuh bantuan AI untuk menerjemahkan kebutuhan dari spesifikasi yang ada. Berikut asumsi yang digunakan dalam implementasi.

### 2.1 Driver Ping kosong ?
saat ini akan terjadi error pada system ini, karena belum ada proteksi terhadap kondisi tersebut
### 2.2 Bagaimana Bila Ping Pertama sudah lebih dari 100 meter ?
jika ping pertama sudah lebih dari 100 meter system akan menghitungnya sebagai pause time, jadi tidak akan menambah active waiting time driver.
### 2.3 Apakah posisi driver di antara dua ping dianggap tetap di posisi ping sebelumnya?
tidak, untuk system yang sekarang dibuat jika selama ping berikutnya kurang dari 100 meter akan dianggap active waiting. 
### 2.4  Bagaimana kalau endedAt lebih awal dari arrivedAt?
saat ini akan mendaptkan exception, karena berdasarkan logika hal tersebut adalah salah menurut asumsi saya, maka request tersebut akan mendapatkan message reject.
### 2.4  Bagaimana kalau input order Id kosong?
saat ini  akan mendaptkan exception, karena order id merupakan mandatory pada request tersebut.
### 2.5  GPS Distance Calculation
Jarak antara driver dan pickup point dihitung menggunakan Haversine formula.

* Keuntungan:
Tidak membutuhkan external mapping service,Perhitungan sederhana dan deterministic, Sesuai untuk kebutuhan pengecekan radius 100 meter.

### 2.6 Driver Pings secara acak
driverPings tidak harus dikirim dalam urutan waktu.

Service akan mengurutkan ping berdasarkan field at sebelum melakukan perhitungan.

Behaviour ini diuji oleh:
shouldCalculateFeeCorrectlyWhenDriverPingsAreUnordered

### 2.7 Free Waiting
5 menit pertama merupakan free waiting.

Behaviour ini diuji oleh:
- shouldHaveNoFeeWhenWaitingLessThanFreeWaitingTime
- shouldHaveNoFeeWhenWaitingExactlyFiveMinutes
### 2.8 Waiting Fee After Free Waiting
Setelah melewati 5 menit:

paidWaitingMinutes x Rp500

Contoh:
waiting time = 10 minutes
free waiting = 5 minutes
paid waiting = 5 minutes

waiting fee = 5 x Rp500
= Rp2.500

Behaviour ini diuji oleh:
- shouldHaveFeeWhenWaitingFiveMinutesAndOneSecond
- shouldHaveFeeWhenWaitingTenMinutes

### 2.9 Waiting Fee Cap
Waiting fee memiliki maksimum Rp15.000.

Jika hasil perhitungan melebihi nilai tersebut, fee dibatasi menjadi Rp15.000
dan waitingFeeCapped bernilai true.

Behaviour ini diuji oleh:
shouldHaveFeeWhenWaitingFortyMinutes

### 2.10 Customer Cancellation
Jika customer membatalkan setelah free waiting:

cancellationFee = waitingFee + Rp5.000

Contoh:
waiting fee      = Rp2.500
cancellation fee = Rp2.500 + Rp5.000
= Rp7.500

Behaviour ini diuji oleh:
shouldHaveFeeWhenCancelledByCustomerAfterTenMinutes

### 2.11 Customer Cancellation Fee Cap
Cancellation fee memiliki maksimum Rp20.000.

Jika hasil perhitungan melebihi maksimum tersebut, fee dibatasi menjadi Rp20.000
dan cancellationFeeCapped bernilai true.

Behaviour ini diuji oleh:
shouldCapFeeAtMaximumWhenCancelledByCustomer

### 2.12 Customer Cancellation Before Free Waiting
Jika customer membatalkan sebelum free waiting selesai, tidak ada waiting fee
dan cancellation fee.

Behaviour ini diuji oleh:
shouldHaveNoFeeWhenCustomerCancelLessThanFreeWaitingTime

### 2.13 Driver Cancellation
Jika driver membatalkan, customer tidak dikenakan waiting fee maupun
cancellation fee.

Behaviour ini diuji oleh:
shouldHaveNoFeeWhenCancelledByDriver

### 2.14 Active and Paused Driver
Driver dianggap active apabila posisi GPS berada dalam radius 100 meter dari
pickup point.

Driver dianggap paused apabila posisi GPS berada lebih dari 100 meter dari
pickup point.

Behaviour active -> paused -> active diuji oleh:
shouldCalculateActiveAndPausedTimeWhenDriverMovesOutAndBackToPickup

---
## 3. Trade-off:
* Tidak menghitung actual road distance, Tidak mempertimbangkan kondisi jalan atau route kendaraan.
* Tidak menggunakan database, Database tidak digunakan karena requirement berfokus pada calculation engine
  dan REST endpoint, Business logic dibuat tanpa dependency terhadap database. 

----

## 4. jika diberikan waktu lebih dari 2 hari
* Menambahkan integration test untuk REST endpoint.
* Menambahkan validation request yang lebih lengkap.
* Menggunakan custom exception untuk validation error.
* Menambahkan global exception handler untuk REST API.
* Menambahkan CI pipeline untuk menjalankan test dan build secara otomatis.
* Menambahkan test untuk timestamp yang tidak valid.
* Menambahkan OpenAPI/Swagger documentation.
* Memindahkan konfigurasi tarif dan cap dari hardcoded value ke configuration object.
* melakukan tuning terhadap code agar bisa lebih clean
 
