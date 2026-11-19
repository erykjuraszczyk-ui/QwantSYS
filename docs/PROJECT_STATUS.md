# QwantSYS — status projektu

**Wersja planowana:** 0.1.0 Foundation  
**Status:** prace koncepcyjne i szkielet repozytorium  
**Gałąź robocza:** `qwantSYS-0.1.0-foundation`

## Co istnieje

- Repozytorium GitHub z podstawowym README.
- Dokumentacja architektury i procesu bring-up.
- Minimalny, nieuprzywilejowany szkielet aplikacji systemowej Qwant Core.
- Plik wymagań dla stanowiska kompilacyjnego.

## Czego jeszcze nie ma

- kompletnego drzewa urządzenia dla REDMAGIC 7;
- potwierdzonej konfiguracji BoardConfig;
- dopasowanego źródła kernela i konfiguracji defconfig;
- zweryfikowanych komponentów vendor/HAL;
- finalnego manifestu AOSP dla wybranej bazy;
- skonfigurowanego produktu ROM;
- obrazu recovery lub ROM;
- podpisanego OTA;
- procedury instalacji sprawdzonej na urządzeniu.

## Blokery przed kompilacją ROM-u

1. Potwierdzenie dokładnego modelu urządzenia, nie tylko nazwy handlowej.
2. Odczyt fingerprintu firmware i wersji Androida.
3. Ustalenie układu partycji, slotów i sposobu uruchamiania.
4. Ustalenie kompatybilnej bazy AOSP.
5. Audyt źródeł kernela oraz komponentów vendor.
6. Potwierdzenie możliwości odzyskania urządzenia do oryginalnego firmware.

## Definicja ukończenia 0.1.0

- [ ] Urządzenie zidentyfikowane na podstawie właściwości systemowych i firmware.
- [ ] Zebrane dane o partycjach bez modyfikowania urządzenia.
- [ ] Wskazana konkretna baza AOSP wraz z rewizją.
- [ ] Wskazane źródła kernel/device/vendor z potwierdzoną zgodnością.
- [ ] Minimalny Qwant Core kompiluje się jako komponent platformy.
- [ ] CI sprawdza podstawową poprawność plików projektu.

**Nie oznaczaj wersji jako ROM alpha, dopóki nie powstanie i nie zostanie uruchomiony rzeczywisty obraz dla potwierdzonego wariantu telefonu.**
