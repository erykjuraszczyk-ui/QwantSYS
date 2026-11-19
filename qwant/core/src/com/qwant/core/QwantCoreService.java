package com.qwant.core;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;

/**
 * Minimal local service for the first Qwant Core milestone.
 *
 * This service intentionally exposes no privileged hardware controls.
 * It is not exported and is not started automatically at boot.
 */
public final class QwantCoreService extends Service {
    private final LocalBinder binder = new LocalBinder();
    private long createdAtElapsedRealtime;

    @Override
    public void onCreate() {
        super.onCreate();
        createdAtElapsedRealtime = SystemClock.elapsedRealtime();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public String getCoreStatus() {
        long uptimeMs = Math.max(
                0L,
                SystemClock.elapsedRealtime() - createdAtElapsedRealtime
        );

        return "Qwant Core 0.1 | state=READY | uptime_ms=" + uptimeMs;
    }

    public final class LocalBinder extends Binder {
        public QwantCoreService getService() {
            return QwantCoreService.this;
        }
    }
}
