# Qwant device target — NX679J (placeholder)

Ten katalog jest miejscem na przyszłą konfigurację urządzenia. **Nie jest kompletnym device tree i nie może być używany do budowania ani flashowania ROM-u.**

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
