# Qwant Launcher 0.5.0

Testowa nakładka Qwant dla REDMAGIC 7 (NX679J) / Android 13.

## Nowości 0.5.0 — Cortana AI Interface

- Osobna aktywność rozmowy „Cortana”.
- Lokalna historia rozmowy (ostatnie 40 wiadomości) i lokalne notatki (maksymalnie 30).
- Komenda `zapamiętaj: treść`, odczyt notatek poleceniem `pamięć` i czyszczenie z potwierdzeniem.
- Lokalny zestaw komend: pomoc, status urządzenia, ustawienia, Wi-Fi, Bluetooth, bateria i powrót na pulpit Qwant.
- Wejście głosowe przez standardową aktywność rozpoznawania mowy Androida (`pl-PL`); wynik trafia do pola tekstowego przed wysłaniem.
- Przycisk do usunięcia historii rozmowy niezależnie od notatek.
- Bez połączenia sieciowego i bez dodatkowych uprawnień w tym module.

## Użycie

1. Otwórz Qwant Launcher i wybierz „Uruchom Cortanę”.
2. Wpisz `pomoc`, aby sprawdzić listę komend.
3. Wpisz np. `zapamiętaj: projekt QwantSYS`, a następnie `pamięć`.
4. Przycisk „Mów” wywołuje usługę rozpoznawania mowy dostępną na Androidzie. Dostępność, język i to, czy rozpoznawanie odbywa się lokalnie, zależą od zainstalowanej usługi.

## Granice wersji 0.5.0

To lokalny interfejs komend z pamięcią notatek, a nie pełny model językowy, AGI ani autonomiczny agent. Nie generuje swobodnych odpowiedzi LLM, nie steruje telefonem poza wyraźnie obsługiwanymi skrótami i nie wysyła danych do backendu. Rozpoznawanie mowy jest obsługiwane przez zewnętrzną usługę Androida, jeśli jest dostępna; polityka prywatności tej usługi zależy od jej dostawcy.

Historia i notatki są przechowywane w prywatnych preferencjach aplikacji. Usunięcie aplikacji usuwa jej prywatne dane zgodnie z zachowaniem Androida.

Qwant Launcher nadal jest nakładką użytkownika, nie ROM-em ani aplikacją systemową. Nie wymaga roota i nie omija zabezpieczeń Androida.

## Budowanie

Workflow używa AAPT2, javac, D8, zipalign i apksigner.
Artefakt: `QwantSYS-REDMAGIC7-Launcher-0.5.0.apk`.
Build jest podpisywany tymczasowym kluczem debug CI; może wymagać odinstalowania poprzedniego builda przed aktualizacją.
