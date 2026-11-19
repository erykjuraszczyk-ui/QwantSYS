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
