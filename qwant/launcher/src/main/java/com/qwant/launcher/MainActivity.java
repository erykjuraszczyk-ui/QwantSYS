package com.qwant.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.usage.StorageStatsManager;
import android.os.StatFs;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 13, 24);
    private static final int PANEL = Color.rgb(17, 27, 45);
    private static final int PANEL_ALT = Color.rgb(20, 34, 54);
    private static final int LINE = Color.rgb(37, 57, 79);
    private static final int CYAN = Color.rgb(79, 220, 235);
    private static final int WHITE = Color.rgb(239, 246, 255);
    private static final int MUTED = Color.rgb(148, 166, 190);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView clockView;
    private TextView cpuValue;
    private TextView ramValue;
    private TextView batteryValue;
    private TextView thermalValue;
    private TextView storageValue;
    private TextView deviceValue;
    private TextView aiValue;
    private TextView detailValue;

    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            refreshMetrics();
            handler.postDelayed(this, 4000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildInterface();
        refreshMetrics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(refreshTask);
        handler.post(refreshTask);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(refreshTask);
        super.onPause();
    }

    private void buildInterface() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(page, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);

        TextView logo = text("Q", 25, CYAN, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(round(PANEL_ALT, CYAN, 18));
        top.addView(logo, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(dp(13), 0, 0, 0);
        TextView name = text("QWANT", 22, WHITE, true);
        name.setLetterSpacing(0.12f);
        TextView version = text("SYSTEM INTERFACE  /  0.1.0", 10, MUTED, true);
        version.setLetterSpacing(0.08f);
        brand.addView(name);
        brand.addView(version);
        top.addView(brand, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        clockView = text("--:--", 20, WHITE, true);
        clockView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        top.addView(clockView);
        page.addView(top);
        space(page, 25);

        TextView eyebrow = text("PERSONAL DEVICE ENVIRONMENT", 10, CYAN, true);
        eyebrow.setLetterSpacing(0.13f);
        page.addView(eyebrow);
        TextView heading = text("Twój pulpit.", 30, WHITE, true);
        heading.setPadding(0, dp(5), 0, 0);
        page.addView(heading);
        TextView subtitle = text("REDMAGIC 7  •  QwantSYS Foundation", 13, MUTED, false);
        subtitle.setPadding(0, dp(4), 0, 0);
        page.addView(subtitle);
        space(page, 22);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.VERTICAL);
        status.setPadding(dp(17), dp(15), dp(17), dp(15));
        status.setBackground(round(PANEL_ALT, LINE, 18));
        TextView statusTitle = text("QWANT CORE", 11, CYAN, true);
        statusTitle.setLetterSpacing(0.12f);
        status.addView(statusTitle);
        TextView statusBody = text("Warstwa interfejsu działa lokalnie. Android pozostaje systemem bazowym.", 13, WHITE, false);
        statusBody.setPadding(0, dp(7), 0, 0);
        statusBody.setLineSpacing(dp(3), 1.0f);
        status.addView(statusBody);
        page.addView(status, fullWidth());

        section(page, "MONITOR SPRZĘTU", "Odświeżanie co 4 sekundy");
        LinearLayout rowOne = metricRow();
        rowOne.addView(metricCard("CPU", "Odczyt...", "Częstotliwość / klastry", (v) -> cpuValue = v), weightParams());
        rowOne.addView(metricCard("PAMIĘĆ RAM", "Odczyt...", "Dostępna pamięć", (v) -> ramValue = v), weightParams());
        page.addView(rowOne);
        space(page, 10);

        LinearLayout rowTwo = metricRow();
        rowTwo.addView(metricCard("BATERIA", "Odczyt...", "Poziom naładowania", (v) -> batteryValue = v), weightParams());
        rowTwo.addView(metricCard("TERMika", "Odczyt...", "Temperatura / stan", (v) -> thermalValue = v), weightParams());
        page.addView(rowTwo);
        space(page, 10);

        LinearLayout rowThree = metricRow();
        rowThree.addView(metricCard("PAMIĘĆ URZĄDZENIA", "Odczyt...", "Wolne miejsce /data", (v) -> storageValue = v), weightParams());
        rowThree.addView(metricCard("URZĄDZENIE", "REDMAGIC 7", "Model i wersja Androida", (v) -> deviceValue = v), weightParams());
        page.addView(rowThree);

        section(page, "CENTRUM FUNKCJI", "Skróty do Androida i aplikacji");
        LinearLayout actionsA = metricRow();
        actionsA.addView(actionCard("▦", "Aplikacje", "Otwórz listę aplikacji", this::openAppDrawer), weightParams());
        actionsA.addView(actionCard("⚙", "Ustawienia", "Konfiguracja telefonu", () -> openSettings(Settings.ACTION_SETTINGS)), weightParams());
        page.addView(actionsA);
        space(page, 10);

        LinearLayout actionsB = metricRow();
        actionsB.addView(actionCard("◉", "Wyświetlacz", "Ekran i jasność", () -> openSettings(Settings.ACTION_DISPLAY_SETTINGS)), weightParams());
        actionsB.addView(actionCard("⌂", "Domyślny pulpit", "Ustaw Qwant jako Home", this::openHomeSettings), weightParams());
        page.addView(actionsB);

        section(page, "WARSTWA OBLICZENIOWA", "Wykrywanie możliwości urządzenia");
        LinearLayout aiCard = new LinearLayout(this);
        aiCard.setOrientation(LinearLayout.VERTICAL);
        aiCard.setPadding(dp(17), dp(15), dp(17), dp(15));
        aiCard.setBackground(round(PANEL, LINE, 18));
        aiValue = text("Android NNAPI", 18, WHITE, true);
        aiCard.addView(aiValue);
        TextView aiNote = text("Akceleratory Qualcomm są widoczne w diagnostyce systemowej. Dostęp aplikacji do poszczególnych sterowników wymaga osobnej weryfikacji.", 12, MUTED, false);
        aiNote.setPadding(0, dp(6), 0, 0);
        aiNote.setLineSpacing(dp(3), 1.0f);
        aiCard.addView(aiNote);
        page.addView(aiCard, fullWidth());

        section(page, "INFORMACJE", "QwantSYS 0.1.0 • wydanie testowe");
        detailValue = text("Brak dodatkowych danych.", 12, MUTED, false);
        detailValue.setPadding(dp(15), dp(13), dp(15), dp(13));
        detailValue.setBackground(round(PANEL, LINE, 16));
        page.addView(detailValue, fullWidth());

        TextView footer = text("QWANT  •  ZBUDOWANY NA ANDROIDZIE  •  NX679J", 9, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        footer.setLetterSpacing(0.08f);
        footer.setPadding(0, dp(25), 0, dp(5));
        page.addView(footer);

        setContentView(scroll);
    }

    private interface ValueSetter {
        void set(TextView view);
    }

    private LinearLayout metricCard(String title, String initial, String hint, ValueSetter setter) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(12), dp(14));
        card.setMinimumHeight(dp(112));
        card.setBackground(round(PANEL, LINE, 17));

        TextView titleView = text(title, 9, MUTED, true);
        titleView.setLetterSpacing(0.08f);
        TextView value = text(initial, 20, WHITE, true);
        value.setPadding(0, dp(12), 0, dp(4));
        TextView hintView = text(hint, 10, MUTED, false);

        card.addView(titleView);
        card.addView(value);
        card.addView(hintView);
        setter.set(value);
        return card;
    }

    private LinearLayout actionCard(String symbol, String title, String hint, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(12), dp(14));
        card.setMinimumHeight(dp(92));
        card.setBackground(round(PANEL_ALT, LINE, 17));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> action.run());

        TextView icon = text(symbol, 21, CYAN, true);
        TextView label = text(title, 14, WHITE, true);
        label.setPadding(0, dp(8), 0, dp(3));
        TextView desc = text(hint, 10, MUTED, false);
        card.addView(icon);
        card.addView(label);
        card.addView(desc);
        return card;
    }

    private void section(LinearLayout page, String title, String subtitle) {
        LinearLayout block = new LinearLayout(this);
        block.setGravity(Gravity.CENTER_VERTICAL);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(0, dp(23), 0, dp(11));
        TextView heading = text(title, 11, CYAN, true);
        heading.setLetterSpacing(0.10f);
        TextView note = text(subtitle, 10, MUTED, false);
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void space(LinearLayout parent, int heightDp) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(heightDp)));
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private GradientDrawable round(int color, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void refreshMetrics() {
        if (isFinishing()) return;
        clockView.setText(new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date()));

        cpuValue.setText(cpuSummary());

        ActivityManager.MemoryInfo memory = new ActivityManager.MemoryInfo();
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (manager != null) {
            manager.getMemoryInfo(memory);
            ramValue.setText(formatGb(memory.availMem));
            ramValue.setTextSize(21);
        }

        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int percent = (level >= 0 && scale > 0) ? Math.round(level * 100f / scale) : -1;
            int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            String suffix = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL ? " • ładowanie" : "";
            batteryValue.setText(percent >= 0 ? percent + "%" : "—");
            batteryValue.setTextSize(21);
            batteryValue.setContentDescription("Bateria " + percent + " procent" + suffix);
        } else {
            batteryValue.setText("—");
        }

        String skin = readThermal("skin-msm-therm");
        String batteryTemp = readThermal("battery");
        String thermal = skin != null ? skin : systemThermalStatus();
        if (thermal != null && batteryTemp != null) {
            thermalValue.setText(thermal + " / " + batteryTemp);
            thermalValue.setTextSize(16);
        } else {
            thermalValue.setText(thermal != null ? thermal : "Niedostępne");
            thermalValue.setTextSize(16);
        }

        try {
            StatFs stats = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            storageValue.setText(formatGb(stats.getAvailableBytes()));
            storageValue.setTextSize(21);
        } catch (RuntimeException ex) {
            storageValue.setText("—");
        }

        deviceValue.setText("Android " + Build.VERSION.RELEASE);
        deviceValue.setTextSize(17);

        int sensorCount = 0;
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
            sensorCount = sensors == null ? 0 : sensors.size();
        }

        int cores = Runtime.getRuntime().availableProcessors();
        String hardware = Build.HARDWARE == null ? "nieznany" : Build.HARDWARE;
        detailValue.setText(
                "Model: " + Build.MODEL + " (" + Build.DEVICE + ")\n"
                + "Android: " + Build.VERSION.RELEASE + " / API " + Build.VERSION.SDK_INT + "\n"
                + "ABI: " + Build.SUPPORTED_ABIS[0] + "\n"
                + "CPU widziane przez aplikację: " + cores + "\n"
                + "Warstwa sprzętowa: " + hardware + "\n"
                + "Czujniki dostępne przez SensorManager: " + sensorCount + "\n"
                + "Odświeżanie panelu: 4 s"
        );
        aiValue.setText(Build.VERSION.SDK_INT >= 27 ? "Android NNAPI • dostępność platformy" : "NNAPI niedostępne");
    }

    private String cpuSummary() {
        File base = new File("/sys/devices/system/cpu/cpufreq");
        File[] policies = base.listFiles((dir, name) -> name.startsWith("policy"));
        if (policies == null || policies.length == 0) {
            return Runtime.getRuntime().availableProcessors() + " rdzeni";
        }
        Arrays.sort(policies, Comparator.comparing(File::getName));
        List<String> values = new ArrayList<>();
        for (File policy : policies) {
            String raw = readText(new File(policy, "scaling_cur_freq"));
            if (raw == null) raw = readText(new File(policy, "cpuinfo_max_freq"));
            try {
                double ghz = Long.parseLong(raw) / 1_000_000.0;
                values.add(String.format(Locale.US, "%.2f", ghz));
            } catch (Exception ignored) {
                // This policy is not readable in the app sandbox.
            }
        }
        if (values.isEmpty()) return "Dane ograniczone";
        return String.join(" / ", values) + " GHz";
    }

    private String readThermal(String sensorName) {
        File thermalRoot = new File("/sys/class/thermal");
        File[] zones = thermalRoot.listFiles((dir, name) -> name.startsWith("thermal_zone"));
        if (zones == null) return null;
        for (File zone : zones) {
            String name = readText(new File(zone, "type"));
            if (sensorName.equals(name)) {
                String raw = readText(new File(zone, "temp"));
                try {
                    int value = Integer.parseInt(raw);
                    if (value <= 0 || value > 150000) return null;
                    return String.format(Locale.getDefault(), "%.1f°C", value / 1000.0);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String systemThermalStatus() {
        if (Build.VERSION.SDK_INT < 29) return null;
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        if (power == null) return null;
        try {
            switch (power.getCurrentThermalStatus()) {
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
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
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
        List<ResolveInfo> apps = new ArrayList<>();
        for (ResolveInfo info : found) {
            if (info.activityInfo != null && !getPackageName().equals(info.activityInfo.packageName)) {
                apps.add(info);
            }
        }
        apps.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
                String.valueOf(a.loadLabel(getPackageManager())),
                String.valueOf(b.loadLabel(getPackageManager()))
        ));
        if (apps.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Aplikacje")
                    .setMessage("Android nie udostępnił listy aplikacji.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        CharSequence[] labels = new CharSequence[apps.size()];
        for (int i = 0; i < apps.size(); i++) {
            labels[i] = apps.get(i).loadLabel(getPackageManager());
        }
        new AlertDialog.Builder(this)
                .setTitle("Aplikacje")
                .setItems(labels, (dialog, which) -> {
                    ResolveInfo selected = apps.get(which);
                    ComponentName component = new ComponentName(
                            selected.activityInfo.packageName,
                            selected.activityInfo.name
                    );
                    Intent launch = new Intent(Intent.ACTION_MAIN);
                    launch.addCategory(Intent.CATEGORY_LAUNCHER);
                    launch.setComponent(component);
                    try {
                        startActivity(launch);
                    } catch (RuntimeException ignored) {
                        // The selected application may have been removed.
                    }
                })
                .setNegativeButton("Zamknij", null)
                .show();
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (RuntimeException ex) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (RuntimeException ignored) {
                // Leave the launcher available if Settings cannot be opened.
            }
        }
    }

    private void openHomeSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
        } catch (RuntimeException ex) {
            openSettings(Settings.ACTION_SETTINGS);
        }
    }
}
