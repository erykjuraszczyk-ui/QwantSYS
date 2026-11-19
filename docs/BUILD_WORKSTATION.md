# Stanowisko kompilacyjne AOSP

## Oficjalne wymagania

Aktualna dokumentacja AOSP opisuje stanowisko 64-bit x86 Linux z co najmniej 400 GB wolnego miejsca oraz 64 GB RAM dla typowego pełnego checkoutu i kompilacji. Wymagania zależą od gałęzi Androida i konfiguracji.

Źródło: https://source.android.com/docs/setup/start/requirements

## Wpływ na sprzęt deweloperski

Laptop z 16 GB RAM i dyskiem 512 GB nie jest dobrym stanowiskiem do pełnej kompilacji współczesnego AOSP: wolne miejsce będzie ograniczone, a pamięć RAM znacząco poniżej zalecenia. Można na nim rozwijać dokumentację, aplikacje Qwant, wykonywać analizę repozytorium i lekkie testy.

Zalecane opcje:

- dedykowana stacja robocza z większą ilością RAM i dyskiem NVMe;
- zaufany serwer kompilacyjny Linux;
- odrębny katalog `OUT_DIR` na szybkim dysku z odpowiednią ilością miejsca.

Pendrive nie powinien być używany jako główny nośnik źródeł i wyników kompilacji.

## Pakiety hosta

Dokładną listę pakietów należy dobrać do wybranej gałęzi AOSP. Dla Ubuntu dokumentacja AOSP opisuje m.in. Git, GnuPG, Flex, Bison, Build Essential, Curl, narzędzia XML, Unzip i Fontconfig.

Nie instaluj w ciemno przypadkowych wersji JDK. Wybierz wersję wymaganą przez wybraną gałąź AOSP; część narzędzi jest dostarczana w samym drzewie źródeł.

## Pobranie źródeł — dopiero po wyborze bazy

Przykład z bieżącej dokumentacji AOSP:

```bash
mkdir -p ~/work/aosp
cd ~/work/aosp

repo init --partial-clone --no-use-superproject \
  -b android-latest-release \
  -u https://android.googlesource.com/platform/manifest
```

**Nie wykonuj jeszcze `repo sync` dla QwantSYS.** Najpierw trzeba wybrać konkretną wersję Androida zgodną z firmware telefonu. Najnowsza gałąź AOSP nie musi być kompatybilna z vendor stackiem REDMAGIC 7.

Po zatwierdzeniu bazy i wymagań urządzenia:

```bash
repo sync -c -j8
```

Źródła:
- https://source.android.com/docs/setup/start/requirements
- https://source.android.com/docs/setup/start
- https://source.android.com/docs/setup/download
