# Qwant device target — NX679J (placeholder)

Ten katalog jest miejscem na przyszłą konfigurację urządzenia. **Nie jest kompletnym device tree i nie może być używany do budowania ani flashowania ROM-u.**

## Potwierdzony cel sprzętowy

Dane odczytane z ekranu „Informacje o telefonie” dla urządzenia użytkownika:

- Nazwa: REDMAGIC 7
- Model: NX679J
- Hardware version: NX679J_V1AMB
- Kompilacja: NX679J_EUCommon_V7.12
- Android: 13 / REDMAGIC OS 6.0
- SoC: Snapdragon 8 Gen 1
- RAM / pamięć: 12 GB / 128 GB

To identyfikuje cel dla tego egzemplarza. Nie potwierdza jeszcze układu partycji, konfiguracji A/B, AVB ani zgodności konkretnych źródeł kernela i vendor.

Przed dodaniem aktywnych plików build systemu należy zweryfikować:

- czy telefon rzeczywiście identyfikuje się jako NX679J;
- wersję firmware i Androida;
- układ partycji oraz schemat A/B;
- kompatybilną gałąź AOSP;
- kernel source i defconfig;
- vendor blobs i interfejsy HAL;
- wymagania boot image, vendor_boot, init_boot, dtbo i AVB.

Nie dodawaj wartości do BoardConfig.mk na podstawie zgadywania. Każda wartość musi pochodzić ze zweryfikowanego źródła dla dokładnego wariantu urządzenia.

Planowana zawartość po zakończeniu audytu:

```text
device/qwant/nx679j/
├── AndroidProducts.mk
├── BoardConfig.mk
├── device.mk
├── qwant_nx679j.mk
├── manifest.xml
├── init/
├── sepolicy/
└── overlay/
```

Do czasu zamknięcia audytu pozostaje tu tylko dokumentacja.
