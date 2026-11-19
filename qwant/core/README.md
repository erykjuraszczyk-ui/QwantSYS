# Qwant Core

Pierwszy moduł QwantSYS. W wersji 0.1 jest to minimalna aplikacja platformowa Android z lokalną usługą powiązaną (bound service).

## Zakres 0.1

- uruchomienie procesu aplikacji;
- udostępnienie usługi wyłącznie wewnątrz pakietu;
- zwracanie podstawowego statusu modułu;
- brak dostępu root;
- brak zapisu do sysfs, partycji lub ustawień sprzętowych;
- brak automatycznego startu przy bootowaniu.

## Integracja

Katalog zawiera Android.bp, manifest i kod Java. Moduł jest przeznaczony do umieszczenia w drzewie AOSP po stronie projektu. Nie jest samodzielnym ROM-em i nie definiuje jeszcze polityk privileged permissions.

## Następne kroki

1. Dodać testy jednostkowe i instrumentacyjne.
2. Ustalić kontrakt API dla pozostałych modułów Qwant.
3. Dodać klienta/debug UI do odczytu statusu.
4. Zdefiniować cykl życia i politykę uruchamiania usługi.
5. Dopiero później zaprojektować bezpieczne API dla funkcji systemowych.
