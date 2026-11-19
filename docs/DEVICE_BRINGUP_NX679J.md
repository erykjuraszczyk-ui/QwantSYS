# Device bring-up — REDMAGIC 7 / kandydat NX679J

## Cel

Przed rozpoczęciem portowania trzeba potwierdzić dokładną odmianę telefonu. Nazwa „REDMAGIC 7”, „7+” lub „7 Pro” nie wystarcza do dobrania obrazu systemu.

**NX679J jest w tym repozytorium identyfikatorem-kandydatem, a nie potwierdzonym celem dla każdego telefonu REDMAGIC 7.** Nie używaj obrazu NX679J na innym wariancie.

## 1. Odczyt danych — bez modyfikowania telefonu

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

## 2. Automatyczne zebranie danych

W repozytorium znajduje się skrypt tylko do odczytu:

```bash
bash scripts/collect-device-info.sh
```

Skrypt wymaga ADB, debugowania USB i dokładnie jednego autoryzowanego urządzenia. Zapisuje raport do pliku `qwant-device-info.txt`. Nie wykonuje poleceń flashowania, kasowania ani modyfikacji partycji. Przed wysłaniem raportu sprawdź go i usuń dane prywatne.

## 3. Informacje do zebrania

- pełny numer modelu z ustawień i etykiety urządzenia;
- fingerprint kompilacji;
- wersja Androida i poziom API;
- wersja firmware/baseband;
- architektura i nazwa SoC potwierdzone z systemu;
- schemat partycji i informacja o A/B;
- wersja bootloadera i recovery;
- dostępne źródła kernela dla dokładnej odmiany.

## 4. Źródła wymagane do portu

Port nie może być uznany za gotowy bez:

1. device tree dopasowanego do dokładnego wariantu;
2. kernel source i właściwej konfiguracji;
3. zgodnych komponentów vendor i firmware;
4. zgodnych definicji HAL;
5. konfiguracji AVB, dynamic partitions i boot image;
6. znanej procedury przywracania oryginalnego oprogramowania.

Nie kopiuj BoardConfig ani vendor blobs z innego modelu tylko dlatego, że urządzenia mają podobny procesor.

## 5. Zasady bezpieczeństwa

- Na tym etapie nie odblokowuj bootloadera i nie zapisuj żadnych partycji.
- Nie uruchamiaj poleceń `fastboot flash`, `erase`, `format` ani `set_active`.
- Nie testuj losowych obrazów boot, vendor_boot, init_boot lub recovery.
- Nie kasuj danych i nie zmieniaj schematu partycji.
- Zachowaj oryginalny firmware oraz sprawdzoną ścieżkę odzyskiwania.

## 6. Bramka decyzyjna

Dalsze prace nad BoardConfig i produktem ROM można rozpocząć dopiero wtedy, gdy dane z punktu 2 są znane, a źródła z punktu 3 zostały zweryfikowane. Do tego czasu katalog device zawiera wyłącznie dokumentację i szablony, a nie aktywną konfigurację urządzenia.
