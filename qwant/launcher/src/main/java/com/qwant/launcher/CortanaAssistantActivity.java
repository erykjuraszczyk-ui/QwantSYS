package com.qwant.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public final class CortanaAssistantActivity extends Activity {
    private static final int BG = Color.rgb(8, 13, 24);
    private static final int PANEL = Color.rgb(17, 27, 45);
    private static final int PANEL_ALT = Color.rgb(20, 34, 54);
    private static final int LINE = Color.rgb(37, 57, 79);
    private static final int WHITE = Color.rgb(239, 246, 255);
    private static final int MUTED = Color.rgb(148, 166, 190);
    private static final int REQUEST_SPEECH = 411;
    private static final int MAX_HISTORY = 40;
    private static final int MAX_NOTES = 30;

    private SharedPreferences preferences;
    private int accent;
    private LinearLayout transcript;
    private ScrollView transcriptScroll;
    private EditText composer;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences("qwant_cortana", MODE_PRIVATE);
        accent = getSharedPreferences("qwant_launcher", MODE_PRIVATE)
                .getInt("accent", Color.rgb(79, 220, 235));
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildScreen();
        loadHistory();
    }

    private void buildScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(BG);
        page.setPadding(dp(16), dp(14), dp(16), dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("CORTANA", 21, WHITE, true);
        title.setLetterSpacing(0.12f);
        TextView subtitle = text("QWANT LOCAL ASSISTANT  /  0.5.0", 9, accent, true);
        subtitle.setLetterSpacing(0.07f);
        titleBlock.addView(title);
        titleBlock.addView(subtitle);
        header.addView(titleBlock, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        header.addView(smallButton("Pamięć", this::showMemory));
        header.addView(smallButton("Wyczyść", this::confirmClearChat));
        page.addView(header);
        space(page, 12);

        TextView info = text(
                "Tryb lokalny: komendy i pamięć działają na telefonie. Pełny model językowy nie jest jeszcze podłączony.",
                11, MUTED, false);
        info.setLineSpacing(dp(3), 1f);
        info.setPadding(dp(12), dp(10), dp(12), dp(10));
        info.setBackground(round(PANEL_ALT, LINE, 14));
        page.addView(info);
        space(page, 10);

        transcriptScroll = new ScrollView(this);
        transcriptScroll.setFillViewport(false);
        transcriptScroll.setBackground(round(PANEL, LINE, 15));
        transcript = new LinearLayout(this);
        transcript.setOrientation(LinearLayout.VERTICAL);
        transcript.setPadding(dp(10), dp(12), dp(10), dp(12));
        transcriptScroll.addView(transcript, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        page.addView(transcriptScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        space(page, 10);
        LinearLayout composeRow = new LinearLayout(this);
        composeRow.setOrientation(LinearLayout.HORIZONTAL);
        composeRow.setGravity(Gravity.CENTER_VERTICAL);

        composer = new EditText(this);
        composer.setSingleLine(false);
        composer.setMinLines(1);
        composer.setMaxLines(4);
        composer.setTextColor(WHITE);
        composer.setHintTextColor(MUTED);
        composer.setHint("Napisz do Cortany…");
        composer.setTextSize(14);
        composer.setPadding(dp(12), dp(10), dp(12), dp(10));
        composer.setBackground(round(PANEL_ALT, LINE, 14));
        composer.setImeOptions(EditorInfo.IME_ACTION_SEND);
        composer.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        composer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentMessage();
                return true;
            }
            return false;
        });
        composeRow.addView(composer, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button voice = smallButton("Mów", this::startVoiceInput);
        LinearLayout.LayoutParams voiceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        voiceParams.setMargins(dp(6), 0, dp(6), 0);
        composeRow.addView(voice, voiceParams);
        Button send = smallButton("Wyślij", this::sendCurrentMessage);
        composeRow.addView(send);
        page.addView(composeRow);

        TextView privacy = text("Rozmowy i notatki są zapisane lokalnie w prywatnych danych aplikacji. Nie są wysyłane przez ten moduł.", 9, MUTED, false);
        privacy.setPadding(dp(2), dp(8), dp(2), 0);
        page.addView(privacy);

        setContentView(page);
    }

    private Button smallButton(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(10);
        button.setTextColor(accent);
        button.setAllCaps(false);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(round(PANEL_ALT, LINE, 12));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private void sendCurrentMessage() {
        String message = composer.getText().toString().trim();
        if (message.isEmpty()) return;
        composer.setText("");
        appendMessage("user", message, true);
        String response = processMessage(message);
        appendMessage("assistant", response, true);
    }

    private String processMessage(String raw) {
        String command = raw.trim().toLowerCase(Locale.ROOT);

        if (command.startsWith("zapamiętaj:") || command.startsWith("zapamietaj:")) {
            int colon = raw.indexOf(':');
            String note = colon >= 0 ? raw.substring(colon + 1).trim() : "";
            if (note.isEmpty()) return "Podaj treść notatki po dwukropku. Przykład: zapamiętaj: projekt Qwant.";
            return saveNote(note)
                    ? "Zapisano notatkę w lokalnej pamięci Qwant."
                    : "Pamięć jest pełna (" + MAX_NOTES + " notatek). Usuń część wpisów i spróbuj ponownie.";
        }

        if (command.equals("pamięć") || command.equals("pamiec")
                || command.contains("pokaż pamięć") || command.contains("pokaz pamiec")
                || command.contains("moje notatki")) {
            return formatNotes();
        }

        if (command.contains("wyczyść pamięć") || command.contains("wyczysc pamiec")
                || command.contains("usuń pamięć") || command.contains("usun pamiec")) {
            confirmClearMemory();
            return "Otworzyłam potwierdzenie usunięcia notatek. Niczego nie kasuję bez Twojej zgody.";
        }

        if (command.equals("pomoc") || command.equals("help") || command.contains("co potrafisz")) {
            return "Dostępne polecenia:\n"
                    + "• zapamiętaj: treść notatki\n"
                    + "• pamięć — pokaż zapisane notatki\n"
                    + "• status — bateria i dostępna pamięć RAM\n"
                    + "• otwórz ustawienia / wifi / bluetooth / baterię\n"
                    + "• otwórz Qwant — wróć do pulpitu\n"
                    + "• wyczyść pamięć — poprosi o potwierdzenie.\n"
                    + "To lokalny zestaw komend, nie pełny LLM.";
        }

        if (command.equals("status") || command.contains("status telefonu") || command.contains("stan telefonu")) {
            return getDeviceStatus();
        }

        if (command.contains("otwórz ustawienia") || command.contains("otworz ustawienia")) {
            openSettings(Settings.ACTION_SETTINGS);
            return "Otwieram ustawienia Androida.";
        }
        if (command.contains("otwórz wi-fi") || command.contains("otworz wi-fi")
                || command.contains("otwórz wifi") || command.contains("otworz wifi")) {
            openSettings(Settings.ACTION_WIFI_SETTINGS);
            return "Otwieram ustawienia Wi-Fi.";
        }
        if (command.contains("otwórz bluetooth") || command.contains("otworz bluetooth")) {
            openSettings(Settings.ACTION_BLUETOOTH_SETTINGS);
            return "Otwieram ustawienia Bluetooth.";
        }
        if (command.contains("otwórz baterię") || command.contains("otworz baterie")
                || command.contains("otwórz baterie")) {
            openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS);
            return "Otwieram ustawienia baterii.";
        }
        if (command.contains("otwórz qwant") || command.contains("wróć na pulpit")
                || command.contains("wroc na pulpit")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return "Wracamy na pulpit Qwant.";
        }
        if (command.equals("cześć") || command.equals("czesc") || command.equals("hej")
                || command.equals("witaj") || command.equals("dzień dobry")) {
            return "Cześć. Jestem Cortana w lokalnym trybie Qwant. Wpisz „pomoc”, aby zobaczyć komendy.";
        }

        return "Odebrałam wiadomość, ale ten build nie ma jeszcze podłączonego modelu językowego. "
                + "Mogę wykonywać lokalne komendy i przechowywać notatki. Wpisz „pomoc”, aby zobaczyć listę.";
    }

    private boolean saveNote(String note) {
        JSONArray notes = readArray("notes");
        if (notes.length() >= MAX_NOTES) return false;
        notes.put(note);
        preferences.edit().putString("notes", notes.toString()).apply();
        return true;
    }

    private String formatNotes() {
        JSONArray notes = readArray("notes");
        if (notes.length() == 0) return "Pamięć jest pusta. Zapisz wpis poleceniem: zapamiętaj: treść.";
        StringBuilder result = new StringBuilder("Lokalna pamięć Qwant (").append(notes.length()).append("):");
        for (int i = 0; i < notes.length(); i++) {
            result.append("\n").append(i + 1).append(". ").append(notes.optString(i, ""));
        }
        return result.toString();
    }

    private void showMemory() {
        new AlertDialog.Builder(this)
                .setTitle("Pamięć lokalna")
                .setMessage(formatNotes())
                .setPositiveButton("Wyczyść pamięć", (d, w) -> confirmClearMemory())
                .setNegativeButton("Zamknij", null)
                .show();
    }

    private void confirmClearMemory() {
        new AlertDialog.Builder(this)
                .setTitle("Usunąć zapisane notatki?")
                .setMessage("Ta operacja usuwa wyłącznie lokalne notatki Cortany. Historia rozmowy pozostanie bez zmian.")
                .setPositiveButton("Usuń", (d, w) -> {
                    preferences.edit().remove("notes").apply();
                    appendMessage("assistant", "Lokalne notatki zostały usunięte.", true);
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void confirmClearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Wyczyścić historię rozmowy?")
                .setMessage("Usunie to historię tekstową przechowywaną przez Cortanę. Notatki z pamięci pozostaną.")
                .setPositiveButton("Wyczyść", (d, w) -> {
                    preferences.edit().remove("history").apply();
                    transcript.removeAllViews();
                    appendMessage("assistant", "Historia wyczyszczona. Pamięć notatek pozostała bez zmian.", false);
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private JSONArray readArray(String key) {
        try {
            return new JSONArray(preferences.getString(key, "[]"));
        } catch (Exception ex) {
            return new JSONArray();
        }
    }

    private void loadHistory() {
        JSONArray history = readArray("history");
        if (history.length() == 0) {
            appendMessage("assistant",
                    "Witaj. Jestem Cortana — lokalny interfejs Qwant. Na tym etapie obsługuję kilka komend, notatki i wejście głosowe Androida. Wpisz „pomoc”.",
                    false);
            return;
        }
        for (int i = 0; i < history.length(); i++) {
            JSONObject item = history.optJSONObject(i);
            if (item != null) appendMessage(item.optString("role", "assistant"),
                    item.optString("text", ""), false);
        }
        scrollToBottom();
    }

    private void appendMessage(String role, String value, boolean persist) {
        boolean user = "user".equals(role);
        TextView bubble = new TextView(this);
        bubble.setText(value);
        bubble.setTextSize(13);
        bubble.setTextColor(WHITE);
        bubble.setTypeface(Typeface.create("sans-serif", user ? Typeface.BOLD : Typeface.NORMAL));
        bubble.setLineSpacing(dp(3), 1f);
        bubble.setPadding(dp(12), dp(10), dp(12), dp(10));
        bubble.setBackground(round(user ? Color.rgb(24, 57, 77) : PANEL_ALT,
                user ? accent : LINE, 14));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(user ? 34 : 0), dp(4), dp(user ? 0 : 34), dp(4));
        params.gravity = user ? Gravity.RIGHT : Gravity.LEFT;
        transcript.addView(bubble, params);
        if (persist) {
            JSONArray history = readArray("history");
            JSONObject item = new JSONObject();
            try {
                item.put("role", role);
                item.put("text", value);
                history.put(item);
            } catch (Exception ignored) { }
            while (history.length() > MAX_HISTORY) history.remove(0);
            preferences.edit().putString("history", history.toString()).apply();
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (transcriptScroll != null) {
            transcriptScroll.post(() -> transcriptScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private String getDeviceStatus() {
        String batteryLabel = "brak danych";
        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            if (level >= 0 && scale > 0) batteryLabel = Math.round(level * 100f / scale) + "%";
        }
        ActivityManager.MemoryInfo memory = new ActivityManager.MemoryInfo();
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        String ram = "brak danych";
        if (manager != null) {
            manager.getMemoryInfo(memory);
            ram = String.format(Locale.getDefault(), "%.1f GB", memory.availMem / (1024.0 * 1024.0 * 1024.0));
        }
        return "Urządzenie: " + android.os.Build.MODEL
                + "\nAndroid: " + android.os.Build.VERSION.RELEASE
                + "\nBateria: " + batteryLabel
                + "\nDostępny RAM: " + ram;
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Powiedz polecenie do Cortany");
        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (RuntimeException ex) {
            Toast.makeText(this, "Brak zainstalowanej usługi rozpoznawania mowy.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                composer.setText(results.get(0));
                composer.setSelection(composer.length());
            }
        }
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (RuntimeException ex) {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
            catch (RuntimeException ignored) { }
        }
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private GradientDrawable round(int color, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        shape.setStroke(dp(1), stroke);
        return shape;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void space(LinearLayout parent, int height) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(height)));
    }
}
