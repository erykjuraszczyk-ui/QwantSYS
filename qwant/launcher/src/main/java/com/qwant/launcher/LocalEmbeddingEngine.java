package com.qwant.launcher;

/** Dedicated multilingual-e5-small embedding runtime; shares the native llama.cpp library. */
public final class LocalEmbeddingEngine {
    private static final boolean NATIVE_AVAILABLE;
    private static volatile boolean loaded;

    static {
        boolean available;
        try {
            System.loadLibrary("qwant_llama_jni");
            available = true;
        } catch (UnsatisfiedLinkError | SecurityException ex) {
            available = false;
        }
        NATIVE_AVAILABLE = available;
    }

    private LocalEmbeddingEngine() { }

    public static boolean isNativeAvailable() {
        return NATIVE_AVAILABLE;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static synchronized String load(String modelPath, int threads) {
        if (!NATIVE_AVAILABLE) return "Biblioteka JNI nie jest dostępna.";
        if (loaded) return "";
        String result = nativeLoad(modelPath, threads);
        loaded = result == null || result.isEmpty();
        return result == null ? "Silnik embeddingów nie zwrócił statusu." : result;
    }

    /**
     * Encodes a memory passage or search query.
     * E5 requires "passage: " for indexed records and "query: " for searches.
     */
    public static synchronized float[] embed(String text, boolean query) {
        if (!NATIVE_AVAILABLE || !loaded || text == null || text.trim().isEmpty()) return null;
        return nativeEmbed(text, query);
    }

    public static synchronized void release() {
        if (NATIVE_AVAILABLE) nativeRelease();
        loaded = false;
    }

    private static native String nativeLoad(String modelPath, int threads);
    private static native float[] nativeEmbed(String text, boolean query);
    private static native void nativeRelease();
}
