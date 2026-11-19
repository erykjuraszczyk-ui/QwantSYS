# Device bring-up — REDMAGIC 7 / kandydat NX679J

## Cel

Przed rozpoczęciem portowania trzeba potwierdzić dokładną odmianę telefonu. Nazwa „REDMAGIC 7”, „7+” lub „7 Pro” nie wystarcza do dobrania obrazu systemu.

**Potwierdzony egzemplarz użytkownika:** ekran „Informacje o telefonie” pokazuje model NX679J oraz nazwę REDMAGIC 7. To potwierdza identyfikator dla tego konkretnego telefonu. Nie oznacza zgodności z REDMAGIC 7 Pro, 7S ani innymi wariantami.

## 1. Dane już potwierdzone ze zrzutów ekranu

Poniższe dane przepisano z dwóch zrzutów ekranu udostępnionych w rozmowie. Są to dane widoczne w ekranie „Informacje o telefonie”, nie wynik odczytu ADB.

| Pole | Odczyt |
|---|---|
| Nazwa urządzenia | REDMAGIC 7 |
| Model | NX679J |
| Hardware version | NX679J_V1AMB |
| SoC | Qualcomm Snapdragon 8 Gen 1 |
| RAM | 12 GB |
| Pamięć wewnętrzna | 128 GB |
| System | REDMAGIC OS 6.0 |
| Android | 13 |
| Aktualizacja zabezpieczeń | 1 września 2024 |
| Numer kompilacji | NX679J_EUCommon_V7.12 |
| Kernel | 5.10.101-android-9-00001-gf4c0e37dbcde-ab8596533 |
| Data kompilacji kernela widoczna na ekranie | Mon May 16 05:21:23 UTC 2022 |

### Wniosek

Początkowym celem portu QwantSYS jest **REDMAGIC 7 / NX679J, wariant z kompilacją EUCommon V7.12 i sprzętem NX679J_V1AMB**. Nie należy traktować go jako uniwersalnego obrazu dla wszystkich telefonów z rodziny REDMAGIC 7.

Zrzuty nie pokazują fingerprintu systemowego, wartości slotu A/B, stanu Verified Boot, mapy partycji ani wersji vendor fingerprint. Te dane nadal wymagają bezpiecznego odczytu przez ADB.

## 2. Odczyt danych — bez modyfikowania telefonu

Włącz debugowanie USB, podłącz telefon i zaakceptuj zaufanie do komputera. Następnie wykonaj:

```bash
adb devices
adb shell getprop ro.product.device
adb shell getprop ro.product.model
adb shell getprop ro.product.name
adb shell getprop ro.build.fingerprint
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.boot.slot_suffix
adb shell getprop ro.boot.verifiedbootstate
adb shell cat /proc/partitions
```

Wyniki zapisz lokalnie. Przed publikacją usuń numery seryjne, identyfikatory kont, adresy MAC i inne dane prywatne.

Jeśli ADB nie jest dostępne, nie próbuj obchodzić blokad. Najpierw sprawdź model oraz wersję oprogramowania w ustawieniach telefonu.

## 3. Automatyczne zebranie danych

W repozytorium znajduje się skrypt tylko do odczytu:

```bash
bash scripts/collect-device-info.sh
```

Skrypt wymaga ADB, debugowania USB i dokładnie jednego autoryzowanego urządzenia. Zapisuje raport do pliku `qwant-device-info.txt`. Nie wykonuje poleceń flashowania, kasowania ani modyfikacji partycji. Przed wysłaniem raportu sprawdź go i usuń dane prywatne.

## 4. Informacje do zebrania

- pełny numer modelu z ustawień i etykiety urządzenia;
- fingerprint kompilacji;
- wersja Androida i poziom API;
- wersja firmware/baseband;
- architektura i nazwa SoC potwierdzone z systemu;
- schemat partycji i informacja o A/B;
- wersja bootloadera i recovery;
- dostępne źródła kernela dla dokładnej odmiany.

## 5. Źródła wymagane do portu

Port nie może być uznany za gotowy bez:

1. device tree dopasowanego do dokładnego wariantu;
2. kernel source i właściwej konfiguracji;
3. zgodnych komponentów vendor i firmware;
4. zgodnych definicji HAL;
5. konfiguracji AVB, dynamic partitions i boot image;
6. znanej procedury przywracania oryginalnego oprogramowania.

Nie kopiuj BoardConfig ani vendor blobs z innego modelu tylko dlatego, że urządzenia mają podobny procesor.

## 6. Zasady bezpieczeństwa

- Na tym etapie nie odblokowuj bootloadera i nie zapisuj żadnych partycji.
- Nie uruchamiaj poleceń `fastboot flash`, `erase`, `format` ani `set_active`.
- Nie testuj losowych obrazów boot, vendor_boot, init_boot lub recovery.
- Nie kasuj danych i nie zmieniaj schematu partycji.
- Zachowaj oryginalny firmware oraz sprawdzoną ścieżkę odzyskiwania.

## 7. Bramka decyzyjna

Dalsze prace nad BoardConfig i produktem ROM można rozpocząć dopiero wtedy, gdy dane z punktu 2 są znane, a źródła z punktu 3 zostały zweryfikowane. Do tego czasu katalog device zawiera wyłącznie dokumentację i szablony, a nie aktywną konfigurację urządzenia.


## 8. Wyniki diagnostyki Termux — raport użytkownika

Raport został wygenerowany lokalnie w Termux na urządzeniu docelowym.

| Właściwość | Odczyt |
|---|---|
| `ro.product.model` | `NX679J` |
| `ro.product.device` | `NX679J-EEA` |
| `ro.hardware` | `qcom` |
| `ro.boot.hardware` | `qcom` |
| `ro.build.version.release` | `13` |
| `ro.build.version.sdk` | `33` |
| `ro.build.fingerprint` | `nubia/NX679J-EEA/NX679J-EEA:13/TKQ1.221013.002/eng.nubia.20241108.103936:user/release-keys` |
| `ro.vendor.build.fingerprint` | `nubia/NX679J-EEA/NX679J-EEA:12/SKQ1.211113.001/eng.nubia.20241108.105108:user/release-keys` |
| `ro.boot.slot_suffix` | `_a` |
| `ro.boot.verifiedbootstate` | `green` |
| `uname -a` | `Linux localhost 5.10.101-android12-9-00001-gf4c0e37dbcde-ab8596533 #1 SMP PREEMPT Mon May 16 05:21:23 UTC 2022 aarch64 Android` |
| `/proc/partitions` | brak widocznych wpisów w raporcie |

### Interpretacja i ograniczenia

- Wariant urządzenia `NX679J-EEA` jest potwierdzony przez właściwości systemowe.
- System raportuje Android 13 / API 33.
- System raportuje aktywny slot `_a`; pełny schemat A/B nie został jeszcze potwierdzony.
- Stan Verified Boot `green` oznacza raportowany stan zweryfikowanego rozruchu; nie określa sam w sobie możliwości odblokowania bootloadera.
- Kernel ma oznaczenie `android12`, mimo że userspace raportuje Android 13. To nie jest samo w sobie dowodem niezgodności.
- Fingerprint vendor wskazuje bazę Android 12, podczas gdy fingerprint systemu wskazuje Android 13. Wymaga to sprawdzenia zgodności vendor interface, VINTF i HAL; nie wolno z tego wyciągać wniosku o błędzie bez dalszych danych.
- Brak wpisów z `/proc/partitions` oznacza, że raport nie dostarczył mapy partycji. Nie wolno zgadywać nazw ani rozmiarów partycji.

### Następny bezpieczny odczyt

Zebrać dodatkowe właściwości bootloadera, vendor i partycji zgodnie z poleceniami w dalszej części dokumentacji. Wszystkie polecenia diagnostyczne mają być tylko do odczytu. Nie używać `fastboot flash`, `erase`, `format`, `set_active` ani poleceń wymagających root.


## 9. Wyniki diagnostyki boot/vendor — raport Termux

Drugi raport został przekazany przez użytkownika. Dane są odczytami właściwości Androida i listingu VINTF; dostęp do katalogów blokowych został odmówiony przez system.

| Właściwość | Odczyt |
|---|---|
| `ro.boot.flash.locked` | `1` |
| `ro.boot.vbmeta.device_state` | `locked` |
| `ro.boot.veritymode` | `enforcing` |
| `ro.boot.slot_suffix` | `_a` |
| `ro.boot.verifiedbootstate` | `green` |
| `ro.boot.dynamic_partitions` | `true` |
| `ro.boot.super_partition` | pusty |
| `ro.build.type` | `user` |
| `ro.build.tags` | `release-keys` |
| `ro.vendor.build.version.release` | `12` |
| `ro.vendor.build.version.sdk` | `31` |
| `ro.product.cpu.abi` | `arm64-v8a` |

### Interpretacja

- Bootloader jest obecnie zgłaszany jako zablokowany (`flash.locked=1`, `vbmeta.device_state=locked`).
- Verified Boot raportuje stan `green`, a dm-verity tryb `enforcing`.
- System raportuje dynamic partitions. Wartość pustej właściwości `ro.boot.super_partition` nie dowodzi braku partycji `super`; wymaga osobnej weryfikacji.
- Aktywny slot raportowany jest jako `_a`. Nie jest to pełny opis mapowania partycji ani dowód wszystkich szczegółów implementacji A/B.
- Android userspace raportuje API 33, a vendor API 31 / Android 12. To istotny punkt do audytu zgodności framework/vendor i VINTF, ale sam raport nie dowodzi niezgodności.
- `build.type=user` i `release-keys` opisują typ kompilacji i tagi kluczy; nie potwierdzają możliwości instalowania alternatywnego ROM-u.

### Ograniczenia odczytu partycji

Termux nie ma dostępu do:
- `/dev/block/by-name`;
- `/dev/block/bootdevice/by-name`;
- `/sys/dev/block`.

Odmowa dostępu jest normalnym ograniczeniem uprawnień Androida w nieuprzywilejowanej aplikacji. Nie próbuj obchodzić jej przez root, zmianę polityki SELinux ani modyfikację urządzenia. Mapa partycji pozostaje nieustalona.

### VINTF manifesty widoczne w raporcie

Raport wskazuje pliki:
- `manifest_cape.xml`;
- `manifest_diwali.xml`;
- `manifest_taro.xml`;
- `manifest/manifest_lahaina_identity.xml`;
- zestaw manifestów usług QTI, display, GNSS, audio, Wi-Fi, power, thermal, sensors, USB, KeyMint i innych.

Są to nazwy plików widoczne w katalogu vendor. Ich obecność sama w sobie nie potwierdza, które wpisy są aktywne, załadowane lub zgodne z wybraną gałęzią AOSP. Potrzebny jest audyt pełnych manifestów i macierzy kompatybilności dla dokładnego firmware.

### Konsekwencja dla projektu

Nie przygotowujemy instrukcji flashowania ani obrazu do instalacji. Bootloader jest obecnie zablokowany, a układ partycji i zgodność vendor/AOSP nie zostały potwierdzone. Następny etap pozostaje nieinwazyjnym audytem fingerprintów, właściwości platformy i źródeł firmware/kernel.


## 10. Wyniki platform/VINTF — raport Termux

Użytkownik udostępnił kolejny odczyt właściwości platformy oraz początkowe 100 linii `manifest_taro.xml`.

| Właściwość | Odczyt |
|---|---|
| `ro.board.platform` | `taro` |
| `ro.soc.model` | `SM8450` |
| `ro.soc.manufacturer` | `QTI` |
| `ro.boot.product.hardware.sku` | `nfc` |
| `ro.vendor.build.fingerprint` | `nubia/NX679J-EEA/NX679J-EEA:12/SKQ1.211113.001/eng.nubia.20241108.105108:user/release-keys` |
| `ro.vndk.version` | `31` |
| `ro.product.first_api_level` | `31` |
| `ro.treble.enabled` | `true` |
| `ro.boot.boot_devices` | `soc/1d84000.ufshc` |

### Wnioski oparte na raporcie

- Platforma zgłasza nazwę `taro`, model SoC `SM8450` i producenta `QTI`.
- Urządzenie raportuje SKU `nfc`.
- Treble jest włączone.
- Vendor VNDK i pierwszy poziom API produktu wskazują wartość 31.
- Ścieżka `ro.boot.boot_devices` wskazuje kontroler UFS `soc/1d84000.ufshc`.
- `manifest_taro.xml` ma nagłówek `version="4.0"`, `type="device"`, `target-level="6"`.
- Udostępniony fragment manifestu deklaruje m.in. HIDL audio 7.0, fingerprint 2.1, Bluetooth 1.0, Bluetooth Audio 2.0, Camera Provider 2.7, Gatekeeper 1.0 i Keymaster 4.1. Jest to tylko początkowy fragment pliku, nie pełny spis usług.

### Ograniczenia i następny audyt

- Nie wyciągamy wniosku, że wszystkie manifesty `cape`, `diwali` i `taro` są aktywne. Sam listing plików nie określa, które manifesty są scalane przez VINTF.
- Nie wyciągamy wniosku, że dowolna gałąź AOSP będzie kompatybilna. Potrzebne są pełne device/framework compatibility matrices, manifesty i dokładna baza firmware.
- Nazwa platformy `taro` i SoC `SM8450` są zapisane jako odczyty właściwości urządzenia; nie zastępują weryfikacji źródeł kernela i device tree dla NX679J-EEA.
- Nie ustalono nadal mapy partycji, zawartości `super`, konfiguracji AVB ani źródła zgodnych vendor blobs.

### Status bring-up

Identyfikacja platformy i podstawowych parametrów Treble/VNDK została uzupełniona. Projekt nadal jest na etapie audytu. Nie twórz jeszcze `BoardConfig.mk`, konfiguracji produktu ani obrazu flashowalnego na podstawie samych tych danych.
