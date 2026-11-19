# Architektura QwantSYS

## 1. Założenia

QwantSYS rozpoczyna jako ROM oparty na Android Open Source Project (AOSP). Warstwa Androida zapewnia kompatybilność aplikacji i obsługę urządzeń, natomiast komponenty Qwant dostarczają własne usługi, interfejs i funkcje.

Projekt nie zakłada zastąpienia Linux Kernel własnym kernelem w pierwszym wydaniu.

## 2. Warstwy systemu

```text
Qwant Experience
  Qwant Launcher / System UI / Settings
                  |
Qwant Services
  Core / Memory / AI / Quantum / Game
                  |
Android Framework + System Services
                  |
HAL + Vendor Interfaces + Native Libraries
                  |
Linux Kernel + Device Tree + Drivers
                  |
REDMAGIC hardware
```

## 3. Moduły

### Qwant Core

Pierwszy moduł systemowy. W początkowej fazie jest zwykłą, nieuprzywilejowaną aplikacją platformową z lokalnym interfejsem usługi. Nie może samodzielnie sterować częstotliwościami CPU/GPU, wentylatorem, modemem ani innymi zasobami niskiego poziomu.

Dostęp do uprzywilejowanych funkcji może zostać dodany wyłącznie przez jasno zdefiniowane API, poprawne uprawnienia i audyt bezpieczeństwa.

### Qwant UI

Launcher, panel systemowy, ustawienia i widżety. Zmiany interfejsu nie mogą blokować podstawowych funkcji Androida ani ekranów awaryjnych.

### Qwant AI

Warstwa integracyjna dla modeli lokalnych lub zdalnych. Ma działać opcjonalnie, z kontrolą dostępu do mikrofonu, kamery, plików i sieci.

### Qwant Memory

Warstwa pamięci aplikacyjnej: dane kontekstowe, indeksy i pamięć agenta. Dane osobiste muszą być chronione szyfrowaniem platformowym i mieć funkcję usuwania.

### Qwant Quantum

Klasyczny symulator obwodów kwantowych i zestaw algorytmów. Wykonuje obliczenia na CPU/GPU telefonu lub deleguje je do zewnętrznej usługi. Nie jest fizycznym procesorem kwantowym.

### Qwant Game

Opcjonalne profile wydajności. W pierwszej wersji tylko odczyt monitorowanych danych i bezpieczne ustawienia dostępne przez publiczne API Androida. Sterowanie sprzętem wymaga potwierdzonego interfejsu producenta.

## 4. Reguły projektowe

- Najpierw kompatybilność sprzętowa, potem funkcje dodatkowe.
- Żaden moduł aplikacyjny nie może zakładać dostępu root.
- Brak nieudokumentowanych zapisów do sysfs lub partycji.
- SELinux pozostaje w trybie enforcing w wydaniach stabilnych.
- Moduły AI, Memory i Quantum muszą być możliwe do wyłączenia.
- Aktualizacje muszą być podpisane i weryfikowane.
- Sekrety, klucze podpisywania i prywatne dane nie mogą trafić do repozytorium.

## 5. Granica odpowiedzialności

Qwant Core koordynuje funkcje Qwant. Nie zastępuje Android Init, frameworku, HAL ani kernela. Zmiany w tych warstwach wymagają osobnych komponentów i testów integracyjnych.
