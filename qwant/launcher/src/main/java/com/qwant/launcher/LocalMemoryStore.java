package com.qwant.launcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LocalMemoryStore extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "qwant_memory.db";
    private static final int DATABASE_VERSION = 1;
    private static final int MAX_RECORDS = 500;
    private static final int MAX_BODY_LENGTH = 4000;

    public static final class Memory {
        public final long id;
        public final String category;
        public final String body;
        public final long createdAt;

        Memory(long id, String category, String body, long createdAt) {
            this.id = id;
            this.category = category;
            this.body = body;
            this.createdAt = createdAt;
        }
    }

    private static final class ScoredMemory {
        final Memory memory;
        final int score;

        ScoredMemory(Memory memory, int score) {
            this.memory = memory;
            this.score = score;
        }
    }

    public LocalMemoryStore(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE memories ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "category TEXT NOT NULL,"
                + "body TEXT NOT NULL,"
                + "created_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX memories_created_at ON memories(created_at DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Future schema upgrades must preserve user records.
    }

    public synchronized long addMemory(String body, String category) {
        if (body == null) return -1L;
        String value = body.trim();
        if (value.isEmpty() || value.length() > MAX_BODY_LENGTH) return -1L;
        String type = category == null ? "notatka" : category.trim();
        if (type.isEmpty()) type = "notatka";
        if (type.length() > 40) type = type.substring(0, 40);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", type);
        values.put("body", value);
        values.put("created_at", System.currentTimeMillis());
        long id = db.insert("memories", null, values);
        db.delete("memories",
                "_id NOT IN (SELECT _id FROM memories ORDER BY created_at DESC, _id DESC LIMIT "
                        + MAX_RECORDS + ")", null);
        return id;
    }

    public synchronized List<Memory> list(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, MAX_RECORDS));
        ArrayList<Memory> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "memories",
                new String[]{"_id", "category", "body", "created_at"},
                null, null, null, null,
                "created_at DESC, _id DESC",
                String.valueOf(limit))) {
            while (cursor.moveToNext()) result.add(readMemory(cursor));
        }
        return result;
    }

    public synchronized List<Memory> search(String query, int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 50));
        String normalizedQuery = normalize(query == null ? "" : query);
        Set<String> queryTokens = tokens(normalizedQuery);
        if (queryTokens.isEmpty()) return Collections.emptyList();

        ArrayList<ScoredMemory> matches = new ArrayList<>();
        for (Memory memory : list(MAX_RECORDS)) {
            String normalizedBody = normalize(memory.body);
            String normalizedCategory = normalize(memory.category);
            int score = normalizedBody.contains(normalizedQuery) ? 5 : 0;
            for (String token : queryTokens) {
                if (normalizedBody.contains(token)) score += 2;
                if (normalizedCategory.contains(token)) score += 1;
            }
            if (score > 0) matches.add(new ScoredMemory(memory, score));
        }

        matches.sort((left, right) -> {
            int byScore = Integer.compare(right.score, left.score);
            return byScore != 0 ? byScore
                    : Long.compare(right.memory.createdAt, left.memory.createdAt);
        });

        ArrayList<Memory> result = new ArrayList<>();
        for (int i = 0; i < matches.size() && i < limit; i++) {
            result.add(matches.get(i).memory);
        }
        return result;
    }

    public synchronized boolean delete(long id) {
        return getWritableDatabase().delete("memories", "_id=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public synchronized void clear() {
        getWritableDatabase().delete("memories", null, null);
    }

    public synchronized int count() {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM memories", null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private Memory readMemory(Cursor cursor) {
        return new Memory(cursor.getLong(0), cursor.getString(1),
                cursor.getString(2), cursor.getLong(3));
    }

    private static Set<String> tokens(String value) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (value == null) return result;
        for (String token : value.split("[^\\p{L}\\p{N}]+")) {
            if (token.length() >= 2) result.add(token);
        }
        return result;
    }

    private static String normalize(String value) {
        String source = value == null ? "" : value;
        String decomposed = Normalizer.normalize(source, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
