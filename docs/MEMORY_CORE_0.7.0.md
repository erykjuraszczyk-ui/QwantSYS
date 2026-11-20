# QwantSYS Memory Core 0.7.0

## Zakres

Memory Core dodaje lokalną bazę SQLite do prototypu Qwant Launcher / Cortana. Nie jest to ROM Androida ani uprzywilejowana usługa systemowa.

## Przechowywanie

- Baza: `qwant_memory.db`
- Tabela: `memories(_id, category, body, created_at)`
- Maksymalnie 500 wpisów; przy dodaniu kolejnego najstarsze są usuwane.
- Treść jednego wpisu: maksymalnie 4 000 znaków.
- Notatki z 0.6.0 zapisane w `SharedPreferences` są migrowane jednokrotnie.
- Dane pozostają w prywatnym sandboxie aplikacji; nie są synchronizowane z serwerem.

## Operacje

- Dodawanie: `zapamiętaj: treść` lub panel Pamięć → Dodaj wpis.
- Lista: panel Pamięć.
- Wyszukiwanie: `szukaj pamięci: fraza` lub panel Pamięć → Szukaj.
- Usuwanie wpisu: otwarcie wpisu i opcja Usuń wpis.
- Czyszczenie całości: Pamięć → Wyczyść → potwierdzenie. Historia rozmowy pozostaje.

## Przywoływanie kontekstu

Podczas generowania odpowiedzi aplikacja dopasowuje ostatnią wiadomość użytkownika do maksymalnie 500 wpisów i dodaje pięć najwyżej ocenionych wyników do promptu (limit kontekstu pamięci: 1 400 znaków). Wyszukiwanie normalizuje znaki diakrytyczne i ocenia dopasowania fraz oraz tokenów.

Jest to tekstowe wyszukiwanie leksykalne, a nie embeddings, vector search ani wyszukiwanie semantyczne. Może nie znaleźć parafrazy, która używa innych słów.

## Granice walidacji

Sukces GitHub Actions potwierdza kompilację i podpisanie APK. Migracja danych, działanie wyszukiwania i stabilność inferencji wymagają testu na fizycznym REDMAGIC 7.
