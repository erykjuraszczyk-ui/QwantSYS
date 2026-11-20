# Qwant Launcher 0.4.0

Testowa nakładka Qwant dla REDMAGIC 7 (NX679J) / Android 13.

## Nowości 0.4.0

- Natywny widżet ekranu głównego Androida „Qwant Glance”: zegar 12/24 h, data, status baterii i wejście do launchera.
- Widżet pobiera kolor akcentu i format zegara z preferencji Qwant.
- Widżet odświeża status baterii przy aktualizacji systemowej i przy wejściu do aplikacji; TextClock aktualizuje czas systemowo.
- Wbudowany podgląd Qwant Glance z datą, zegarem i poziomem baterii.
- Skrót do ustawień zabezpieczeń Androida.
- Zachowane funkcje: dock maksymalnie czterech ulubionych aplikacji, Qwant Control Center, szuflada aplikacji, monitor sprzętu i personalizacja.

## Dodawanie widżetu

1. Ustaw Qwant Launcher jako domyślny pulpit.
2. Przytrzymaj puste miejsce na pulpicie.
3. Otwórz „Widżety”, znajdź „Qwant Glance” i przeciągnij widżet na ekran.
4. Dotknięcie widżetu otwiera Qwant Launcher.

Dostępność i sposób otwierania listy widżetów zależą od wersji REDMAGIC OS.

## Granice techniczne

Qwant Glance jest widżetem pulpitu, a nie zamiennikiem systemowego ekranu blokady. Zabezpieczony ekran blokady, PIN, hasło, odcisk palca i pozostałe mechanizmy uwierzytelniania nadal należą do Androida / REDMAGIC OS. Aplikacja nie przechwytuje odblokowywania, nie wyświetla własnego ekranu nad ekranem zabezpieczeń i nie żąda uprawnień do przechwytywania powiadomień.

To aplikacja użytkownika pełniąca rolę launchera, a nie ROM ani uprzywilejowana aplikacja systemowa. Nie ma uprawnień root, nie zapisuje do sysfs, nie zmienia częstotliwości CPU/GPU i nie wyłącza zabezpieczeń Androida. Częstotliwości CPU i temperatury mogą być niedostępne w sandboxie.

## Budowanie

Workflow używa AAPT2, javac, D8, zipalign i apksigner.
Artefakt: `QwantSYS-REDMAGIC7-Launcher-0.4.0.apk`.
Aplikacja jest podpisywana tymczasowym kluczem debug CI; przy instalowaniu nowego builda może być konieczne odinstalowanie poprzedniej wersji.
