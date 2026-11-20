package com.qwant.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

/** Read-only local diagnostics for the QwantOS Android Home Shell. */
public final class QwantSystemDiagnostics {
    private static final String QWEN_FILE = "qwen2.5-0.5b-instruct-q4_k_m.gguf";
    private static final String E5_FILE = "multilingual-e5-small-q8_0.gguf";
    private static final long QWEN_MIN_BYTES = 450_000_000L;
    private static final long E5_MIN_BYTES = 125_000_000L;

    private QwantSystemDiagnostics() { }

    public static void show(Activity activity) {
        final String report = createReport(activity);
        TextView body = new TextView(activity);
        body.setText(report);
        body.setTextSize(12);
        body.setTextColor(0xFFEFF6FF);
        body.setTextIsSelectable(true);
        int padding = dp(activity, 14);
        body.setPadding(padding, padding, padding, padding);

        ScrollView scroll = new ScrollView(activity);
        scroll.addView(body, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("QwantOS Diagnostics • 1.0.0")
                .setView(scroll)
                .setNeutralButton("Kopiuj raport", (d, which) -> {
                    ClipboardManager clipboard =
                            (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("QwantOS diagnostics", report));
                        Toast.makeText(activity, "Raport skopiowany do schowka.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "Schowek jest niedostępny.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Zamknij", null)
                .create();
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    Math.min(activity.getResources().getDisplayMetrics().widthPixels - dp(activity, 32),
                            dp(activity, 520)),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private static String createReport(Activity activity) {
        StringBuilder out = new StringBuilder(1400);
        out.append("QWANTOS SYSTEM DIAGNOSTICS 1.0.0\n");
        out.append("Raport lokalny • bez automatycznej wysyłki\n\n");
        out.append("[URZĄDZENIE]\n");
        out.append("Model: ").append(Build.MODEL).append("\n");
        out.append("Device: ").append(Build.DEVICE).append("\n");
        out.append("Android: ").append(Build.VERSION.RELEASE)
                .append(" / API ").append(Build.VERSION.SDK_INT).append("\n");
        out.append("ABI: ").append(Build.SUPPORTED_ABIS.length > 0
                ? Build.SUPPORTED_ABIS[0] : "unknown").append("\n");
        out.append("Procesory logiczne: ").append(Runtime.getRuntime().availableProcessors()).append("\n");
        out.append("HOME role: ").append(isQwantDefaultHome(activity) ? "QwantOS aktywny" : "QwantOS nie jest domyślnym pulpitem").append("\n\n");

        out.append("[SILNIK NATYWNY]\n");
        boolean jni = LocalLlamaEngine.isNativeAvailable();
        out.append("llama.cpp JNI / arm64: ").append(jni ? "dostępny" : "niedostępny").append("\n");
        out.append("Qwen runtime: ").append(LocalLlamaEngine.isLoaded() ? "model załadowany" : "model niezaładowany").append("\n");
        out.append("Embedding runtime: ").append(LocalEmbeddingEngine.isNativeAvailable() ? "JNI dostępny" : "JNI niedostępny").append("\n");
        out.append("E5 runtime: ").append(LocalEmbeddingEngine.isLoaded() ? "model załadowany" : "model niezaładowany").append("\n\n");

        out.append("[MODELE W PAMIĘCI APLIKACJI]\n");
        File qwen = new File(activity.getFilesDir(), QWEN_FILE);
        File e5 = new File(activity.getFilesDir(), E5_FILE);
        out.append("Qwen 0.5B: ").append(fileState(qwen, QWEN_MIN_BYTES)).append("\n");
        out.append("Multilingual E5: ").append(fileState(e5, E5_MIN_BYTES)).append("\n\n");

        out.append("[PAMIĘĆ QWANT]\n");
        LocalMemoryStore store = null;
        try {
            store = new LocalMemoryStore(activity);
            out.append("Rekordy: ").append(store.count()).append("\n");
            out.append("Wektory 384D: ").append(store.embeddedCount()).append("\n");
            out.append("Do indeksowania: ").append(store.unembeddedCount()).append("\n");
        } catch (RuntimeException ex) {
            out.append("Baza pamięci: błąd odczytu (").append(ex.getClass().getSimpleName()).append(")\n");
        } finally {
            if (store != null) store.close();
        }

        out.append("\n[ZASOBY ANDROIDA]\n");
        ActivityManager manager = (ActivityManager) activity.getSystemService(Activity.ACTIVITY_SERVICE);
        if (manager != null) {
            ActivityManager.MemoryInfo memory = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(memory);
            out.append("RAM dostępny: ").append(formatBytes(memory.availMem)).append("\n");
            out.append("RAM całkowity wg Androida: ").append(formatBytes(memory.totalMem)).append("\n");
        } else {
            out.append("RAM: niedostępny\n");
        }
        try {
            StatFs stats = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            out.append("Wolne miejsce /data: ").append(formatBytes(stats.getAvailableBytes())).append("\n");
        } catch (RuntimeException ex) {
            out.append("Wolne miejsce /data: niedostępne\n");
        }
        out.append("\n[INTERPRETACJA]\n");
        out.append("Dostępność JNI, obecność pliku modelu i załadowanie modelu to trzy osobne stany. ")
                .append("Ten raport nie potwierdza jakości inferencji ani wydajności na urządzeniu.\n");
        out.append("Raport nie zawiera treści notatek ani historii rozmowy.");
        return out.toString();
    }

    private static boolean isQwantDefaultHome(Activity activity) {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager roles = (RoleManager) activity.getSystemService(Activity.ROLE_SERVICE);
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roles.isRoleHeld(RoleManager.ROLE_HOME);
            }
        }
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        android.content.pm.ResolveInfo resolved = activity.getPackageManager().resolveActivity(
                home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
        return resolved != null && resolved.activityInfo != null
                && activity.getPackageName().equals(resolved.activityInfo.packageName);
    }

    private static String fileState(File file, long minimumBytes) {
        if (!file.exists()) return "niepobrany";
        long size = file.length();
        return (size >= minimumBytes ? "plik obecny / próg rozmiaru spełniony" : "plik niekompletny")
                + " • " + formatBytes(size);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) return "—";
        double value = bytes / (1024.0 * 1024.0 * 1024.0);
        return String.format(Locale.getDefault(), "%.2f GB", value);
    }

    private static int dp(Activity activity, float value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
