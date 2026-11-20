# Qwant Launcher 0.6.0

Testowa nakładka Qwant dla REDMAGIC 7 (NX679J) / Android 13.

## Nowości 0.6.0 — Local AI Core

- Natywny silnik llama.cpp w bibliotece JNI dla ABI `arm64-v8a`.
- Lokalny model Qwen2.5-0.5B-Instruct w formacie GGUF Q4_K_M.
- Przycisk pobrania modelu z oficjalnego repozytorium Qwen na Hugging Face (około 491 MB).
- Model jest pobierany do prywatnego katalogu aplikacji; pobieranie ma progress, plik tymczasowy i podstawową kontrolę rozmiaru.
- Po pobraniu model jest ładowany w pamięci procesu i wykonuje inferencję na CPU telefonu.
- Kontekst początkowy 2048 tokenów, maksymalnie 192 tokeny odpowiedzi w aplikacji.
- Pamięć notatek Qwant jest przekazywana do promptu jako kontekst; ostatnie wiadomości rozmowy są formatowane szablonem Qwen.
- Lokalne komendy telefonu pozostają obsługiwane przez aplikację, a swobodne pytania kierowane są do modelu, gdy jest załadowany.
- Możliwość usunięcia pliku modelu i zwolnienia około 491 MB.

## Model i licencja

Model: [Qwen2.5-0.5B-Instruct-GGUF](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF), wariant `Q4_K_M`.
Licencja modelu według karty Hugging Face: Apache-2.0.
Plik: `qwen2.5-0.5b-instruct-q4_k_m.gguf`.

## Użycie

1. Zainstaluj Qwant Launcher 0.6.0 i otwórz Cortanę.
2. W sekcji Local Model wybierz „Pobierz model”. Zalecane jest stabilne Wi-Fi.
3. Po pobraniu naciśnij „Uruchom model” (aplikacja może załadować model automatycznie po pobraniu).
4. Wpisz zwykłe pytanie. Komendy takie jak `status`, `pamięć`, `otwórz wifi` nadal obsługuje lokalny dispatcher.
5. „Usuń model” zwalnia plik GGUF, nie kasując lokalnych notatek ani historii.

Po pobraniu modelu generowanie tekstu nie wymaga połączenia z internetem. Połączenie HTTPS jest potrzebne wyłącznie do pobrania pliku modelu. Wejście głosowe nadal wywołuje usługę rozpoznawania mowy Androida; jej tryb przetwarzania zależy od dostawcy tej usługi.

## Parametry i ograniczenia

- Docelowe ABI w tym buildzie: `arm64-v8a`, zgodne z REDMAGIC 7.
- Backend inferencji: CPU; nie deklarujemy akceleracji Adreno, DSP ani NPU.
- Model ma 0,5 mld parametrów i jest niewielkim modelem instrukcyjnym. Jakość odpowiedzi, polszczyzna i szybkość wymagają testu na urządzeniu; nie jest to model klasy 7B ani AGI.
- Kontekst ustawiony na 2048 tokenów, aby ograniczyć zużycie RAM. Długie rozmowy są ograniczone do ostatnich wiadomości.
- Aplikacja sprawdza wolne miejsce przed pobraniem i wymaga około 800 MB zapasu.
- Telefon może ograniczać szybkość z powodu temperatury lub zarządzania energią. Zalecane jest testowanie na chłodnym urządzeniu i bez intensywnego obciążenia.
- Model jest oddzielnym plikiem i nie jest osadzony w APK.

Qwant Launcher jest nadal nakładką użytkownika, a nie ROM-em ani aplikacją systemową. Nie wymaga roota i nie omija zabezpieczeń Androida.

## Budowanie

GitHub Actions buduje `llama.cpp` jako kod źródłowy z przypiętego commita, kompiluje bibliotekę JNI przez Android NDK dla `arm64-v8a`, a następnie pakuje ją do APK razem z aplikacją Java.
Workflow używa AAPT2, javac, D8, CMake, Android NDK, zipalign i apksigner.
Artefakt: `QwantSYS-REDMAGIC7-Launcher-0.6.0.apk`.
Build jest podpisywany tymczasowym kluczem debug CI; instalacja kolejnego builda może wymagać odinstalowania poprzedniej wersji.
