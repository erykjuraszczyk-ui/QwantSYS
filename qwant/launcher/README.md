# Qwant Launcher 0.1.0

Pierwsza testowa nakładka Qwant dla REDMAGIC 7 / Android 13.

## Funkcje

- Ekran główny Androida (HOME launcher) i zwykła ikona aplikacji.
- Panel CPU, pamięci RAM, baterii, temperatury i wolnej przestrzeni.
- Odświeżanie panelu co 4 sekundy.
- Lista aplikacji możliwych do uruchomienia.
- Skróty do Ustawień, ustawień wyświetlacza i wyboru domyślnego launchera.
- Informacje o wersji Androida, ABI i sensorach dostępnych przez publiczne API.

## Ograniczenia

To aplikacja użytkownika, a nie ROM ani uprzywilejowana aplikacja systemowa.
Nie ma uprawnień root, nie zmienia częstotliwości CPU/GPU i nie zapisuje do sysfs.
Dostępność odczytów sysfs zależy od polityki Androida; brak dostępu jest wyświetlany jako brak danych.
NNAPI jest pokazywane jako funkcja platformy, a nie jako potwierdzenie dostępu do każdego akceleratora Qualcomm.

## Budowanie

Wymagania: JDK 17+, Android SDK Platform 35, Android Gradle Plugin 8.13.2, Gradle 8.13.

```bash
gradle --no-daemon :qwant:launcher:assembleDebug
```

APK testowe: `qwant/launcher/build/outputs/apk/debug/launcher-debug.apk`.
