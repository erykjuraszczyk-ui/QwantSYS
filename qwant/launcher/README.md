# Qwant Launcher 0.1.0

Pierwsza testowa nakładka Qwant dla REDMAGIC 7 / Android 13.

## Funkcje

- Ekran główny Androida (HOME launcher) oraz ikona aplikacji.
- Panel CPU, pamięci RAM, baterii, stanu termicznego i wolnej przestrzeni.
- Odświeżanie panelu co 4 sekundy.
- Lista aplikacji możliwych do uruchomienia.
- Skróty do Ustawień, ustawień wyświetlacza i wyboru domyślnego launchera.
- Informacje o wersji Androida, ABI i sensorach dostępnych przez publiczne API.

## Ograniczenia

To aplikacja użytkownika, a nie ROM ani uprzywilejowana aplikacja systemowa.
Nie ma uprawnień root, nie zmienia częstotliwości CPU/GPU i nie zapisuje do sysfs.
Dostępność odczytów sysfs zależy od polityki Androida; brak dostępu jest wyświetlany jako brak danych.
NNAPI jest pokazywane jako funkcja platformy, a nie jako potwierdzenie dostępu do każdego akceleratora Qualcomm.

## Budowanie APK

CI używa narzędzi Android SDK: AAPT2, javac, D8, zipalign i apksigner.
Artefakt instalacyjny: `QwantSYS-REDMAGIC7-Launcher-0.1.0.apk`.

Aplikacja jest podpisana testowym kluczem debug. Nie jest to klucz produkcyjny ani wydanie Google Play.
