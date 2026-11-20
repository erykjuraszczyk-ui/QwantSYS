# Qwant Launcher 0.3.0

Testowa nakładka Qwant dla REDMAGIC 7 (NX679J) / Android 13.

## Nowości 0.3.0

- Dock ulubionych aplikacji: przypnij do czterech aplikacji, uruchamiaj je jednym dotknięciem i zmieniaj zestaw w dowolnej chwili.
- Ikony i nazwy aplikacji w docku są pobierane z PackageManager; zapisane wybory są przechowywane lokalnie w SharedPreferences.
- Qwant Control Center z kafelkami Internet, Wi-Fi, Bluetooth, Dźwięk, Ekran, Bateria, Ustawienia i Pulpit.
- Kafelki uruchamiają udokumentowane panele Androida lub odpowiednie ekrany ustawień z bezpiecznym fallbackiem.
- Zachowane funkcje 0.2.0: monitor sprzętu, przeszukiwalna szuflada aplikacji, personalizacja kolorów, zegar 24 h i układ kompaktowy.

## Granice techniczne

Qwant Control Center jest własnym panelem skrótów aplikacji. Nie zastępuje systemowego panelu powiadomień ani Quick Settings i nie przełącza bezpośrednio chronionych przełączników systemowych. Przekazuje użytkownika do oficjalnych paneli Androida.

To aplikacja użytkownika pełniąca rolę launchera, a nie ROM ani uprzywilejowana aplikacja systemowa. Nie ma uprawnień root, nie zapisuje do sysfs, nie zmienia częstotliwości CPU/GPU i nie wyłącza zabezpieczeń Androida. Odczyty temperatur i częstotliwości mogą być niedostępne w sandboxie.

## Budowanie

Workflow używa AAPT2, javac, D8, zipalign i apksigner.
Artefakt: `QwantSYS-REDMAGIC7-Launcher-0.3.0.apk`.
Aplikacja jest podpisywana tymczasowym kluczem debug CI; nowy build może wymagać odinstalowania poprzedniej wersji przed instalacją.
