package com.qwant.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.StatFs;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 13, 24);
    private static final int PANEL = Color.rgb(17, 27, 45);
    private static final int PANEL_ALT = Color.rgb(20, 34, 54);
    private static final int LINE = Color.rgb(37, 57, 79);
    private static final int WHITE = Color.rgb(239, 246, 255);
    private static final int MUTED = Color.rgb(148, 166, 190);
    private static final int[] ACCENTS = {
            Color.rgb(79, 220, 235),
            Color.rgb(166, 130, 255),
            Color.rgb(83, 220, 157),
            Color.rgb(255, 183, 77),
            Color.rgb(255, 115, 145)
    };
    private static final String[] ACCENT_NAMES = {
            "Quantum Cyan", "Nebula Violet", "Emerald", "Solar Amber", "Pulse Pink"
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences preferences;
    private int accent = ACCENTS[0];
    private boolean compact = false;
    private boolean clock24 = true;

    private TextView clockView;
    private TextView dateView;
    private TextView cpuValue;
    private TextView ramValue;
    private TextView batteryValue;
    private TextView batteryHint;
    private TextView thermalValue;
    private TextView storageValue;
    private TextView deviceValue;
    private TextView statusBody;
    private TextView detailValue;

    private final Runnable refreshTask = new Runnable() {
        @Override public void run() {
            refreshMetrics();
            handler.postDelayed(this, 4000L);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences("qwant_launcher", MODE_PRIVATE);
        accent = preferences.getInt("accent", ACCENTS[0]);
        compact = preferences.getBoolean("compact", false);
        clock24 = preferences.getBoolean("clock24", true);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildInterface();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(refreshTask);
        handler.post(refreshTask);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(refreshTask);
        super.onPause();
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        int outer = compact ? 14 : 20;
        page.setPadding(dp(outer), dp(compact ? 12 : 18), dp(outer), dp(26));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);

        TextView logo = text("Q", 25, accent, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(PANEL_ALT, accent, 18));
        logo.setOnLongClickListener(v -> { openCustomization(); return true; });
        top.addView(logo, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(12), 0, 0, 0);
        TextView name = text("QWANT", 21, WHITE, true);
        name.setLetterSpacing(0.12f);
        TextView version = text("SYSTEM INTERFACE  /  0.2.0", 9, MUTED, true);
        version.setLetterSpacing(0.07f);
        brand.addView(name);
        brand.addView(version);
        top.addView(brand, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout timeBlock = new LinearLayout(this);
        timeBlock.setOrientation(LinearLayout.VERTICAL);
        timeBlock.setGravity(Gravity.RIGHT);
        clockView = text("--:--", 20, WHITE, true);
        dateView = text("--", 10, MUTED, false);
        timeBlock.addView(clockView);
        timeBlock.addView(dateView);
        top.addView(timeBlock);
        page.addView(top);
        space(page, compact ? 17 : 24);

        TextView eyebrow = text("PERSONAL DEVICE ENVIRONMENT", 9, accent, true);
        eyebrow.setLetterSpacing(0.13f);
        page.addView(eyebrow);
        TextView heading = text("Twój pulpit.", compact ? 27 : 30, WHITE, true);
        heading.setPadding(0, dp(4), 0, 0);
        page.addView(heading);
        TextView subtitle = text(Build.MODEL + "  •  QwantSYS Foundation", 12, MUTED, false);
        subtitle.setPadding(0, dp(4), 0, 0);
        page.addView(subtitle);
        space(page, compact ? 14 : 20);

        LinearLayout status = cardContainer(PANEL_ALT, 17);
        TextView statusTitle = text("QWANT CORE  /  LOCAL SESSION", 10, accent, true);
        statusTitle.setLetterSpacing(0.10f);
        status.addView(statusTitle);
        statusBody = text("Warstwa interfejsu działa lokalnie. Android pozostaje systemem bazowym.", 12, WHITE, false);
        statusBody.setPadding(0, dp(7), 0, 0);
        statusBody.setLineSpacing(dp(3), 1.0f);
        status.addView(statusBody);
        page.addView(status, fullWidth());

        section(page, "SZYBKIE SKRÓTY", "Ustawienia telefonu");
        LinearLayout quick = metricRow();
        quick.addView(actionCard("⌁", "Wi-Fi", "Sieci bezprzewodowe", () -> openSettings(Settings.ACTION_WIFI_SETTINGS)), weightParams());
        quick.addView(actionCard("ᛒ", "Bluetooth", "Połączenia", () -> openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)), weightParams());
        quick.addView(actionCard("◉", "Bateria", "Zasilanie", () -> openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS)), weightParams());
        page.addView(quick);

        section(page, "MONITOR SPRZĘTU", "Dane dostępne przez Android API i uprawnione odczyty");
        LinearLayout rowOne = metricRow();
        rowOne.addView(metricCard("CPU", "—", "Rdzenie / częstotliwość", v -> cpuValue = v), weightParams());
        rowOne.addView(metricCard("PAMIĘĆ RAM", "—", "Dostępna pamięć", v -> ramValue = v), weightParams());
        page.addView(rowOne);
        space(page, 8);

        LinearLayout rowTwo = metricRow();
        rowTwo.addView(metricCard("BATERIA", "—", "Poziom naładowania", v -> batteryValue = v, true), weightParams());
        rowTwo.addView(metricCard("TERMika", "—", "Stan termiczny urządzenia", v -> thermalValue = v), weightParams());
        page.addView(rowTwo);
        space(page, 8);

        LinearLayout rowThree = metricRow();
        rowThree.addView(metricCard("WOLNE MIEJSCE", "—", "Pamięć danych", v -> storageValue = v), weightParams());
        rowThree.addView(metricCard("SYSTEM", "Android", "Wersja platformy", v -> deviceValue = v), weightParams());
        page.addView(rowThree);

        section(page, "CENTRUM QWANT", "Aplikacje, diagnostyka i konfiguracja");
        LinearLayout actionsA = metricRow();
        actionsA.addView(actionCard("▦", "Aplikacje", "Szukaj i uruchamiaj", this::openAppDrawer), weightParams());
        actionsA.addView(actionCard("⌁", "Monitor", "Szczegóły sprzętu", this::openHardwareDashboard), weightParams());
        page.addView(actionsA);
        space(page, 8);

        LinearLayout actionsB = metricRow();
        actionsB.addView(actionCard("✧", "Wygląd", "Kolor i układ pulpitu", this::openCustomization), weightParams());
        actionsB.addView(actionCard("⚙", "Ustawienia", "Konfiguracja Androida", () -> openSettings(Settings.ACTION_SETTINGS)), weightParams());
        page.addView(actionsB);
        space(page, 8);

        LinearLayout actionsC = metricRow();
        actionsC.addView(actionCard("⌂", "Domyślny pulpit", "Zarządzaj ekranem Home", this::openHomeSettings), fullWidth());
        page.addView(actionsC);

        section(page, "WARSTWA OBLICZENIOWA", "AI i akceleratory");
        LinearLayout aiCard = cardContainer(PANEL, 17);
        TextView aiTitle = text("ANDROID NNAPI", 15, WHITE, true);
        aiCard.addView(aiTitle);
        TextView aiNote = text("Interfejs Neural Networks jest częścią platformy. Wykrycie usług Qualcomm w systemie nie oznacza automatycznego dostępu aplikacji do DSP lub GPU.", 11, MUTED, false);
        aiNote.setPadding(0, dp(6), 0, 0);
        aiNote.setLineSpacing(dp(3), 1.0f);
        aiCard.addView(aiNote);
        page.addView(aiCard, fullWidth());

        section(page, "INFORMACJE O URZĄDZENIU", "QwantSYS 0.2.0 • testowa nakładka Androida");
        detailValue = text("Ładowanie danych urządzenia…", 11, MUTED, false);
        detailValue.setPadding(dp(14), dp(12), dp(14), dp(12));
        detailValue.setBackground(round(PANEL, LINE, 15));
        page.addView(detailValue, fullWidth());

        TextView footer = text("QWANT  •  ANDROID OVERLAY  •  NX679J", 9, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        footer.setLetterSpacing(0.08f);
        footer.setPadding(0, dp(23), 0, dp(5));
        page.addView(footer);

        setContentView(scroll);
        refreshMetrics();
    }

    private interface ValueSetter { void set(TextView view); }

    private LinearLayout metricCard(String title, String initial, String hint, ValueSetter setter) {
        return metricCard(title, initial, hint, setter, false);
    }

    private LinearLayout metricCard(String title, String initial, String hint, ValueSetter setter, boolean battery) {
        LinearLayout card = cardContainer(PANEL, 15);
        card.setMinimumHeight(dp(compact ? 94 : 106));
        TextView titleView = text(title, 9, MUTED, true);
        titleView.setLetterSpacing(0.07f);
        TextView value = text(initial, 19, WHITE, true);
        value.setPadding(0, dp(compact ? 8 : 11), 0, dp(3));
        TextView hintView = text(hint, 9, MUTED, false);
        card.addView(titleView);
        card.addView(value);
        card.addView(hintView);
        if (battery) batteryHint = hintView;
        setter.set(value);
        return card;
    }

    private LinearLayout actionCard(String symbol, String title, String hint, Runnable action) {
        LinearLayout card = cardContainer(PANEL_ALT, 15);
        card.setMinimumHeight(dp(compact ? 76 : 87));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> action.run());
        TextView icon = text(symbol, 19, accent, true);
        TextView label = text(title, 12, WHITE, true);
        label.setPadding(0, dp(6), 0, dp(2));
        TextView desc = text(hint, 9, MUTED, false);
        card.addView(icon);
        card.addView(label);
        card.addView(desc);
        return card;
    }

    private LinearLayout cardContainer(int color, int radius) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int p = compact ? 11 : 14;
        card.setPadding(dp(p), dp(p), dp(p), dp(p));
        card.setBackground(round(color, LINE, radius));
        return card;
    }

    private void section(LinearLayout page, String title, String subtitle) {
        LinearLayout block = new LinearLayout(this);
        block.setGravity(Gravity.CENTER_VERTICAL);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(0, dp(compact ? 17 : 22), 0, dp(9));
        TextView heading = text(title, 10, accent, true);
        heading.setLetterSpacing(0.09f);
        TextView note = text(subtitle, 9, MUTED, false);
        note.setPadding(0, dp(3), 0, 0);
        block.addView(heading);
        block.addView(note);
        page.addView(block);
    }

    private LinearLayout metricRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private LinearLayout.LayoutParams weightParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(dp(3), 0, dp(3), 0);
        return p;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void space(LinearLayout parent, int h) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return v;
    }

    private GradientDrawable round(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void refreshMetrics() {
        if (isFinishing() || clockView == null) return;
        Date now = new Date();
        clockView.setText(new SimpleDateFormat(clock24 ? "HH:mm" : "h:mm a", Locale.getDefault()).format(now));
        dateView.setText(new SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now));

        cpuValue.setText(cpuSummary());

        ActivityManager.MemoryInfo memory = new ActivityManager.MemoryInfo();
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        String availableRam = "—";
        if (manager != null) {
            manager.getMemoryInfo(memory);
            availableRam = formatGb(memory.availMem);
            ramValue.setText(availableRam);
        }

        String batteryText = "—";
        String chargeDescription = "Dane baterii niedostępne";
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int percent = (level >= 0 && scale > 0) ? Math.round(level * 100f / scale) : -1;
            int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
            batteryText = percent >= 0 ? percent + "%" : "—";
            chargeDescription = charging ? "Ładowanie" : "Praca na baterii";
            if (temp > 0 && temp < 800) chargeDescription += " • " + String.format(Locale.getDefault(), "%.1f°C", temp / 10f);
            batteryValue.setText(batteryText);
        }
        if (batteryHint != null) batteryHint.setText(chargeDescription);

        String skin = readThermal("skin-msm-therm");
        String thermalState = systemThermalStatus();
        if (skin != null) thermalValue.setText(skin);
        else thermalValue.setText(thermalState == null ? "Brak odczytu" : thermalState);

        try {
            StatFs stats = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            storageValue.setText(formatGb(stats.getAvailableBytes()));
        } catch (RuntimeException ex) {
            storageValue.setText("—");
        }
        deviceValue.setText("Android " + Build.VERSION.RELEASE);

        int sensorCount = 0;
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sm != null) {
            List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
            sensorCount = sensors == null ? 0 : sensors.size();
        }
        long totalRam = memory.totalMem;
        detailValue.setText(
                "Model: " + Build.MODEL + " (" + Build.DEVICE + ")\n"
                + "Android: " + Build.VERSION.RELEASE + " / API " + Build.VERSION.SDK_INT + "\n"
                + "ABI: " + (Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "unknown") + "\n"
                + "CPU logiczne: " + Runtime.getRuntime().availableProcessors() + "\n"
                + "RAM fizyczny: " + formatGb(totalRam) + " • dostępny: " + availableRam + "\n"
                + "Czujniki przez SensorManager: " + sensorCount + "\n"
                + "Interwał odświeżania: 4 sekundy"
        );
        statusBody.setText("Sesja lokalna aktywna • bateria " + batteryText
                + " • RAM dostępny " + availableRam + ". Android pozostaje systemem bazowym.");
    }

    private String cpuSummary() {
        File root = new File("/sys/devices/system/cpu/cpufreq");
        File[] policies = root.listFiles((dir, name) -> name.startsWith("policy"));
        if (policies == null || policies.length == 0) {
            return Runtime.getRuntime().availableProcessors() + " rdzeni";
        }
        Arrays.sort(policies, Comparator.comparing(File::getName));
        List<String> values = new ArrayList<>();
        for (File policy : policies) {
            String raw = readText(new File(policy, "scaling_cur_freq"));
            try {
                double ghz = Long.parseLong(raw) / 1_000_000.0;
                values.add(String.format(Locale.US, "%.2f", ghz));
            } catch (Exception ignored) {
                // Sysfs may be hidden from unprivileged applications.
            }
        }
        return values.isEmpty() ? Runtime.getRuntime().availableProcessors() + " rdzeni" : String.join(" / ", values) + " GHz";
    }

    private String readThermal(String wanted) {
        File[] zones = new File("/sys/class/thermal").listFiles((dir, name) -> name.startsWith("thermal_zone"));
        if (zones == null) return null;
        for (File zone : zones) {
            if (!wanted.equals(readText(new File(zone, "type")))) continue;
            try {
                int raw = Integer.parseInt(readText(new File(zone, "temp")));
                if (raw <= 0 || raw > 150000) return null;
                return String.format(Locale.getDefault(), "%.1f°C", raw / 1000.0);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String systemThermalStatus() {
        if (Build.VERSION.SDK_INT < 29) return null;
        PowerManager p = (PowerManager) getSystemService(POWER_SERVICE);
        if (p == null) return null;
        try {
            switch (p.getCurrentThermalStatus()) {
                case PowerManager.THERMAL_STATUS_NONE: return "Normalny";
                case PowerManager.THERMAL_STATUS_LIGHT: return "Niski";
                case PowerManager.THERMAL_STATUS_MODERATE: return "Umiarkowany";
                case PowerManager.THERMAL_STATUS_SEVERE: return "Wysoki";
                case PowerManager.THERMAL_STATUS_CRITICAL: return "Krytyczny";
                case PowerManager.THERMAL_STATUS_EMERGENCY: return "Awaryjny";
                case PowerManager.THERMAL_STATUS_SHUTDOWN: return "Wyłączenie";
                default: return "Nieznany";
            }
        } catch (SecurityException ex) {
            return null;
        }
    }

    private String readText(File file) {
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            return r.readLine();
        } catch (Exception ex) {
            return null;
        }
    }

    private String formatGb(long bytes) {
        return String.format(Locale.getDefault(), "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void openAppDrawer() {
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getPackageManager().queryIntentActivities(query, 0);
        List<AppEntry> entries = new ArrayList<>();
        for (ResolveInfo info : found) {
            if (info.activityInfo == null || getPackageName().equals(info.activityInfo.packageName)) continue;
            entries.add(new AppEntry(info, getPackageManager()));
        }
        entries.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.label, b.label));

        if (entries.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Aplikacje")
                    .setMessage("Android nie udostępnił listy aplikacji.")
                    .setPositiveButton("OK", null).show();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(10), dp(16), dp(4));
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Szukaj aplikacji…");
        search.setTextColor(WHITE);
        search.setHintTextColor(MUTED);
        search.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));
        root.addView(search, fullWidth());

        ListView list = new ListView(this);
        ArrayAdapter<AppEntry> adapter = new ArrayAdapter<AppEntry>(
                this, android.R.layout.simple_list_item_1, new ArrayList<>(entries)) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convertView, parent);
                row.setTextColor(WHITE);
                row.setTextSize(15);
                row.setPadding(dp(12), dp(10), dp(12), dp(10));
                return row;
            }
        };
        list.setAdapter(adapter);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(400));
        root.addView(list, listParams);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Aplikacje")
                .setView(root)
                .setNegativeButton("Zamknij", null)
                .create();
        list.setOnItemClickListener((parent, view, position, id) -> {
            AppEntry selected = adapter.getItem(position);
            if (selected != null) {
                try {
                    Intent launch = new Intent(Intent.ACTION_MAIN);
                    launch.addCategory(Intent.CATEGORY_LAUNCHER);
                    launch.setComponent(new ComponentName(selected.info.activityInfo.packageName,
                            selected.info.activityInfo.name));
                    startActivity(launch);
                    dialog.dismiss();
                } catch (RuntimeException ignored) {
                    // App may have been removed after the drawer was populated.
                }
            }
        });
        dialog.show();
        dialog.getWindow().setLayout(
                Math.min(getResources().getDisplayMetrics().widthPixels - dp(40), dp(500)),
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private static final class AppEntry {
        final ResolveInfo info;
        final String label;
        AppEntry(ResolveInfo info, android.content.pm.PackageManager packageManager) {
            this.info = info;
            this.label = String.valueOf(info.loadLabel(packageManager));
        }
        @Override public String toString() { return label; }
    }

    private void openHardwareDashboard() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(8), dp(18), dp(8));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);

        addMonitorSection(content, "CPU / CZĘSTOTLIWOŚĆ", cpuSummary());
        ActivityManager.MemoryInfo mem = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am != null) am.getMemoryInfo(mem);
        addMonitorSection(content, "PAMIĘĆ RAM", "Całkowita: " + formatGb(mem.totalMem)
                + "\nDostępna: " + formatGb(mem.availMem)
                + "\nSwap nie jest raportowany przez publiczne API aplikacji.");
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) {
            int lvl = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            addMonitorSection(content, "BATERIA",
                    "Poziom: " + (lvl >= 0 && scale > 0 ? Math.round(lvl * 100f / scale) + "%" : "brak danych")
                    + "\nTemperatura: " + (temp > 0 ? String.format(Locale.getDefault(), "%.1f°C", temp / 10f) : "brak danych")
                    + "\nŁadowanie: " + (status == BatteryManager.BATTERY_STATUS_CHARGING ? "tak" : "nie / nieznane"));
        }
        addMonitorSection(content, "TEMPERATURA", "Obudowa / skin: " + valueOrDash(readThermal("skin-msm-therm"))
                + "\nBateria: " + valueOrDash(readThermal("battery"))
                + "\nStan termiczny Androida: " + valueOrDash(systemThermalStatus()));
        try {
            StatFs fs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            addMonitorSection(content, "PAMIĘĆ WEWNĘTRZNA", "Wolne: " + formatGb(fs.getAvailableBytes())
                    + "\nŁącznie: " + formatGb(fs.getTotalBytes()));
        } catch (RuntimeException ignored) {
            addMonitorSection(content, "PAMIĘĆ WEWNĘTRZNA", "Odczyt niedostępny");
        }
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        int sensors = sm == null ? 0 : sm.getSensorList(Sensor.TYPE_ALL).size();
        addMonitorSection(content, "CZUJNIKI", "Liczba czujników widocznych dla aplikacji: " + sensors);
        addMonitorSection(content, "GPU / AI", "GPU: Adreno (identyfikacja z profilu urządzenia)\nNNAPI: interfejs platformy Android. Dostęp do akceleratorów Qualcomm wymaga niezależnego testu.");
        addMonitorSection(content, "OGRANICZENIA", "Aplikacja nie ma uprawnień root ani systemowego DUMP. Część odczytów CPU i temperatur może być niedostępna w sandboxie Androida.");

        new AlertDialog.Builder(this)
                .setTitle("Qwant Hardware Monitor  •  0.2.0")
                .setView(scroll)
                .setPositiveButton("Odśwież pulpit", (d, w) -> refreshMetrics())
                .setNegativeButton("Zamknij", null)
                .show();
    }

    private void addMonitorSection(LinearLayout root, String title, String body) {
        TextView h = text(title, 10, accent, true);
        h.setPadding(0, dp(12), 0, dp(4));
        TextView b = text(body, 12, WHITE, false);
        b.setLineSpacing(dp(3), 1.0f);
        root.addView(h);
        root.addView(b);
    }

    private String valueOrDash(String v) { return v == null ? "brak odczytu" : v; }

    private void openCustomization() {
        int selected = 0;
        for (int i = 0; i < ACCENTS.length; i++) if (ACCENTS[i] == accent) selected = i;
        AlertDialog palette = new AlertDialog.Builder(this)
                .setTitle("Kolor interfejsu")
                .setSingleChoiceItems(ACCENT_NAMES, selected, (d, which) -> {
                    accent = ACCENTS[which];
                    preferences.edit().putInt("accent", accent).apply();
                    d.dismiss();
                    openLayoutOptions();
                })
                .setNeutralButton("Układ i zegar", (d, w) -> openLayoutOptions())
                .setNegativeButton("Gotowe", null)
                .create();
        palette.show();
    }

    private void openLayoutOptions() {
        boolean[] checked = {compact, clock24};
        String[] labels = {"Układ kompaktowy", "Zegar 24-godzinny"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Personalizacja pulpitu")
                .setMultiChoiceItems(labels, checked, (d, which, value) -> {
                    if (which == 0) compact = value;
                    if (which == 1) clock24 = value;
                })
                .setPositiveButton("Zapisz", (d, w) -> {
                    preferences.edit()
                            .putBoolean("compact", compact)
                            .putBoolean("clock24", clock24)
                            .apply();
                    buildInterface();
                })
                .setNegativeButton("Anuluj", null)
                .create();
        dialog.show();
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (RuntimeException ex) {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
            catch (RuntimeException ignored) { }
        }
    }

    private void openHomeSettings() {
        try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
        catch (RuntimeException ex) { openSettings(Settings.ACTION_SETTINGS); }
    }
}
