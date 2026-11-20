package com.qwant.launcher;

/** Thin JNI facade for the app-private llama.cpp CPU runtime. */
public final class LocalLlamaEngine {
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

    private LocalLlamaEngine() { }

    public static boolean isNativeAvailable() {
        return NATIVE_AVAILABLE;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static synchronized String load(String modelPath, int contextSize, int threads) {
        if (!NATIVE_AVAILABLE) return "Biblioteka JNI nie jest dostępna.";
        if (loaded) return "";
        String result = nativeLoad(modelPath, contextSize, threads);
        loaded = result == null || result.isEmpty();
        return result == null ? "Silnik nie zwrócił statusu." : result;
    }

    public static synchronized String generate(String prompt, int maxTokens) {
        if (!NATIVE_AVAILABLE || !loaded) return "Model nie jest załadowany.";
        return nativeGenerate(prompt, maxTokens);
    }

    public static synchronized void release() {
        if (NATIVE_AVAILABLE) {
            nativeRelease();
        }
        loaded = false;
    }

    private static native String nativeLoad(String modelPath, int contextSize, int threads);
    private static native String nativeGenerate(String prompt, int maxTokens);
    private static native void nativeRelease();
}
