package com.qwant.launcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LocalMemoryStore extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "qwant_memory.db";
    private static final int DATABASE_VERSION = 2;
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
                + "created_at INTEGER NOT NULL,"
                + "embedding BLOB)");
        db.execSQL("CREATE INDEX memories_created_at ON memories(created_at DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE memories ADD COLUMN embedding BLOB");
        }
    }


    /** Exports memory records for an explicit user-controlled backup. */
    public synchronized JSONArray exportRecords() throws JSONException {
        JSONArray records = new JSONArray();
        try (Cursor cursor = getReadableDatabase().query(
                "memories",
                new String[]{"category", "body", "created_at", "embedding"},
                null, null, null, null,
                "created_at DESC, _id DESC")) {
            while (cursor.moveToNext()) {
                JSONObject item = new JSONObject();
                item.put("category", cursor.getString(0));
                item.put("body", cursor.getString(1));
                item.put("created_at", cursor.getLong(2));
                byte[] embedding = cursor.isNull(3) ? null : cursor.getBlob(3);
                if (embedding != null && embedding.length == 384 * 4) {
                    item.put("embedding_b64", Base64.encodeToString(embedding, Base64.NO_WRAP));
                }
                records.put(item);
            }
        }
        return records;
    }

    /**
     * Merges validated records from a backup. Existing category/body pairs are kept once.
     * Invalid individual entries are skipped; an invalid backup envelope is rejected by the caller.
     */
    public synchronized int importRecords(JSONArray records) {
        if (records == null) return 0;
        SQLiteDatabase db = getWritableDatabase();
        HashSet<String> known = new HashSet<>();
        for (Memory existing : list(MAX_RECORDS)) {
            known.add(existing.category + "\\u0000" + existing.body);
        }

        int inserted = 0;
        db.beginTransaction();
        try {
            int count = Math.min(records.length(), MAX_RECORDS);
            for (int i = 0; i < count; i++) {
                JSONObject item = records.optJSONObject(i);
                if (item == null) continue;
                String body = item.optString("body", "").trim();
                String category = item.optString("category", "notatka").trim();
                if (body.isEmpty() || body.length() > MAX_BODY_LENGTH) continue;
                if (category.isEmpty()) category = "notatka";
                if (category.length() > 40) category = category.substring(0, 40);
                String key = category + "\\u0000" + body;
                if (!known.add(key)) continue;

                long createdAt = item.optLong("created_at", System.currentTimeMillis());
                if (createdAt <= 0L || createdAt > System.currentTimeMillis() + 86_400_000L) {
                    createdAt = System.currentTimeMillis();
                }
                ContentValues values = new ContentValues();
                values.put("category", category);
                values.put("body", body);
                values.put("created_at", createdAt);

                String encoded = item.optString("embedding_b64", "");
                if (!encoded.isEmpty()) {
                    try {
                        byte[] vector = Base64.decode(encoded, Base64.DEFAULT);
                        if (vector.length == 384 * 4 && isFiniteVector(vector)) {
                            values.put("embedding", vector);
                        }
                    } catch (IllegalArgumentException ignored) {
                        // Re-index this record later if the E5 model is available.
                    }
                }
                if (db.insert("memories", null, values) != -1L) inserted++;
            }
            db.delete("memories",
                    "_id NOT IN (SELECT _id FROM memories ORDER BY created_at DESC, _id DESC LIMIT "
                            + MAX_RECORDS + ")", null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return inserted;
    }

    private static boolean isFiniteVector(byte[] raw) {
        if (raw == null || raw.length != 384 * 4) return false;
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        double normSquared = 0.0;
        for (int i = 0; i < 384; i++) {
            float value = buffer.getFloat();
            if (!Float.isFinite(value)) return false;
            normSquared += (double) value * value;
        }
        return Double.isFinite(normSquared) && normSquared > 1.0e-12;
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

    public synchronized List<Memory> listUnembedded(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, MAX_RECORDS));
        ArrayList<Memory> result = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "memories",
                new String[]{"_id", "category", "body", "created_at"},
                "embedding IS NULL", null, null, null,
                "created_at ASC, _id ASC",
                String.valueOf(limit))) {
            while (cursor.moveToNext()) result.add(readMemory(cursor));
        }
        return result;
    }

    public synchronized boolean updateEmbedding(long id, float[] embedding) {
        if (embedding == null || embedding.length != 384) return false;
        double normSquared = 0.0;
        for (float value : embedding) {
            if (!Float.isFinite(value)) return false;
            normSquared += (double) value * value;
        }
        double norm = Math.sqrt(normSquared);
        if (!Double.isFinite(norm) || norm < 1.0e-12) return false;

        ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : embedding) buffer.putFloat((float) (value / norm));
        ContentValues values = new ContentValues();
        values.put("embedding", buffer.array());
        return getWritableDatabase().update("memories", values, "_id=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public synchronized List<Memory> searchSemantic(float[] queryEmbedding, int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 50));
        if (queryEmbedding == null || queryEmbedding.length != 384) return Collections.emptyList();

        double queryNormSquared = 0.0;
        for (float value : queryEmbedding) {
            if (!Float.isFinite(value)) return Collections.emptyList();
            queryNormSquared += (double) value * value;
        }
        double queryNorm = Math.sqrt(queryNormSquared);
        if (!Double.isFinite(queryNorm) || queryNorm < 1.0e-12) return Collections.emptyList();

        ArrayList<SemanticResult> matches = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                "memories",
                new String[]{"_id", "category", "body", "created_at", "embedding"},
                "embedding IS NOT NULL", null, null, null, null)) {
            while (cursor.moveToNext()) {
                byte[] raw = cursor.getBlob(4);
                if (raw == null || raw.length != 384 * 4) continue;
                ByteBuffer vector = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
                double dot = 0.0;
                for (int i = 0; i < 384; i++) {
                    dot += (double) queryEmbedding[i] * vector.getFloat();
                }
                double score = dot / queryNorm;
                if (Double.isFinite(score)) {
                    matches.add(new SemanticResult(readMemory(cursor), score));
                }
            }
        }
        matches.sort((left, right) -> Double.compare(right.score, left.score));

        ArrayList<Memory> result = new ArrayList<>();
        for (int i = 0; i < matches.size() && i < limit; i++) {
            result.add(matches.get(i).memory);
        }
        return result;
    }

    public synchronized int embeddedCount() {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM memories WHERE embedding IS NOT NULL", null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public synchronized int unembeddedCount() {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM memories WHERE embedding IS NULL", null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private static final class SemanticResult {
        final Memory memory;
        final double score;
        SemanticResult(Memory memory, double score) {
            this.memory = memory;
            this.score = score;
        }
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
