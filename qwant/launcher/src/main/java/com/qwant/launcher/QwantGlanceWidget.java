package com.qwant.launcher;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Home-screen glance widget. It never replaces or overlays the secure Android lock screen. */
public final class QwantGlanceWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, manager, appWidgetId);
        }
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, QwantGlanceWidget.class);
        int[] ids = manager.getAppWidgetIds(provider);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("qwant_launcher", Context.MODE_PRIVATE);
        boolean clock24 = prefs.getBoolean("clock24", true);
        int accent = prefs.getInt("accent", Color.rgb(79, 220, 235));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.qwant_glance_widget);
        views.setTextViewText(R.id.widget_date,
                new SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(new Date()));
        views.setTextColor(R.id.widget_caption, accent);
        views.setTextColor(R.id.widget_battery, accent);
        String clockFormat = clock24 ? "HH:mm" : "h:mm a";
        views.setCharSequence(R.id.widget_clock, "setFormat24Hour", clockFormat);
        views.setCharSequence(R.id.widget_clock, "setFormat12Hour", clockFormat);

        Intent battery = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        String batteryLabel = "BATERIA  —";
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int status = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if (level >= 0 && scale > 0) {
                int percent = Math.round(level * 100f / scale);
                boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL;
                batteryLabel = (charging ? "ŁADOWANIE " : "BATERIA ") + percent + "%";
            }
        }
        views.setTextViewText(R.id.widget_battery, batteryLabel);

        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending = PendingIntent.getActivity(context, 7404, launch, flags);
        views.setOnClickPendingIntent(R.id.widget_root, pending);

        manager.updateAppWidget(appWidgetId, views);
    }
}
