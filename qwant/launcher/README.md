# Qwant Launcher 0.2.0

Testowa nakładka Qwant dla REDMAGIC 7 / Android 13.

## Nowości 0.2.0

- Personalizacja koloru akcentu: Cyan, Violet, Emerald, Amber, Pink.
- Trwałe ustawienia układu kompaktowego i zegara 24-godzinnego.
- Wyszukiwalna lista aplikacji zainstalowanych na urządzeniu.
- Osobny Qwant Hardware Monitor z podsumowaniem CPU, RAM, baterii, temperatur, pamięci i czujników.
- Szybkie skróty do Wi-Fi, Bluetooth, baterii i ustawień Androida.
- Rozbudowany pulpit z zegarem, datą i automatycznym odświeżaniem statusu.

## Ograniczenia i bezpieczeństwo

To aplikacja użytkownika pełniąca rolę launchera, a nie ROM ani uprzywilejowana aplikacja systemowa.
Nie ma uprawnień root, nie zmienia częstotliwości CPU/GPU i nie zapisuje do sysfs.
Odczyty temperatur i częstotliwości mogą być niedostępne w sandboxie Androida; w takim przypadku panel pokazuje brak danych lub stan termiczny zwracany przez publiczne API.
NNAPI jest interfejsem platformy; wykrycie usług Qualcomm nie potwierdza bezpośredniego dostępu aplikacji do akceleratorów.

## Budowanie

Workflow używa narzędzi Android SDK: AAPT2, javac, D8, zipalign i apksigner.
Artefakt: `QwantSYS-REDMAGIC7-Launcher-0.2.0.apk`.
Aplikacja jest podpisana testowym kluczem debug, nie kluczem produkcyjnym.
