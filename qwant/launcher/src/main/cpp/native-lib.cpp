#include <jni.h>
#include <android/log.h>

#include <algorithm>
#include <cstdint>
#include <cmath>
#include <mutex>
#include <string>
#include <vector>

#include "llama.h"

namespace {
constexpr const char * TAG = "QwantLocalAI";
std::mutex g_mutex;
llama_model * g_model = nullptr;
llama_context * g_context = nullptr;
llama_sampler * g_sampler = nullptr;
llama_model * g_embedding_model = nullptr;
llama_context * g_embedding_context = nullptr;
bool g_backend_ready = false;

void release_backend_if_unused() {
    if (g_model == nullptr && g_embedding_model == nullptr && g_backend_ready) {
        llama_backend_free();
        g_backend_ready = false;
    }
}

void release_locked() {
    if (g_sampler) {
        llama_sampler_free(g_sampler);
        g_sampler = nullptr;
    }
    if (g_context) {
        llama_free(g_context);
        g_context = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    release_backend_if_unused();
}

void release_embedding_locked() {
    if (g_embedding_context) {
        llama_free(g_embedding_context);
        g_embedding_context = nullptr;
    }
    if (g_embedding_model) {
        llama_model_free(g_embedding_model);
        g_embedding_model = nullptr;
    }
    release_backend_if_unused();
}

jstring java_string(JNIEnv * env, const std::string & value) {
    return env->NewStringUTF(value.c_str());
}

std::string token_piece(const llama_vocab * vocab, llama_token token) {
    std::vector<char> buffer(256);
    int32_t count = llama_token_to_piece(vocab, token, buffer.data(),
                                         static_cast<int32_t>(buffer.size()), 0, false);
    if (count < 0) {
        buffer.resize(static_cast<size_t>(-count));
        count = llama_token_to_piece(vocab, token, buffer.data(),
                                     static_cast<int32_t>(buffer.size()), 0, false);
    }
    if (count <= 0) return {};
    return std::string(buffer.data(), static_cast<size_t>(count));
}
} // namespace

extern "C"
JNIEXPORT jstring JNICALL
Java_com_qwant_launcher_LocalLlamaEngine_nativeLoad(
        JNIEnv * env, jclass, jstring model_path, jint context_size, jint threads) {
    if (model_path == nullptr) return java_string(env, "Nie podano ścieżki modelu.");
    const char * path = env->GetStringUTFChars(model_path, nullptr);
    if (path == nullptr) return java_string(env, "Nie można odczytać ścieżki modelu.");

    std::lock_guard<std::mutex> lock(g_mutex);
    release_locked();

    if (!g_backend_ready) {
        llama_backend_init();
        g_backend_ready = true;
    }

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0; // CPU backend only; no unsupported Adreno/DSP assumptions.
    g_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);

    if (g_model == nullptr) {
        release_locked();
        return java_string(env, "llama.cpp nie odczytał pliku GGUF.");
    }

    llama_context_params context_params = llama_context_default_params();
    context_params.n_ctx = static_cast<uint32_t>(std::max(512, std::min(4096, context_size)));
    context_params.n_batch = 256;
    context_params.n_ubatch = 128;
    const int thread_count = std::max(2, std::min(6, static_cast<int>(threads)));
    context_params.n_threads = thread_count;
    context_params.n_threads_batch = thread_count;
    g_context = llama_init_from_model(g_model, context_params);

    if (g_context == nullptr) {
        release_locked();
        return java_string(env, "Nie udało się utworzyć kontekstu modelu.");
    }

    auto sampler_params = llama_sampler_chain_default_params();
    g_sampler = llama_sampler_chain_init(sampler_params);
    if (g_sampler == nullptr) {
        release_locked();
        return java_string(env, "Nie udało się utworzyć sampler chain.");
    }
    llama_sampler_chain_add(g_sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_top_p(0.90f, 1));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_temp(0.70f));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_dist(0x5157414e));

    __android_log_print(ANDROID_LOG_INFO, TAG, "Qwen model loaded on CPU, context=%u, threads=%d",
                        llama_n_ctx(g_context), thread_count);
    return java_string(env, "");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_qwant_launcher_LocalLlamaEngine_nativeGenerate(
        JNIEnv * env, jclass, jstring prompt_text, jint max_tokens) {
    if (prompt_text == nullptr) return java_string(env, "Puste wejście.");
    const char * prompt_chars = env->GetStringUTFChars(prompt_text, nullptr);
    if (prompt_chars == nullptr) return java_string(env, "Nie można odczytać promptu.");
    std::string prompt(prompt_chars);
    env->ReleaseStringUTFChars(prompt_text, prompt_chars);

    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_model == nullptr || g_context == nullptr || g_sampler == nullptr) {
        return java_string(env, "Model nie został załadowany.");
    }

    const llama_vocab * vocab = llama_model_get_vocab(g_model);
    if (vocab == nullptr) return java_string(env, "Model nie zawiera słownika tokenów.");

    // Keep enough context headroom for generation. The Java side sends a short chat history.
    while (prompt.size() > 12000) prompt.erase(0, prompt.size() / 4);

    std::vector<llama_token> tokens(prompt.size() + 32);
    int32_t token_count = llama_tokenize(vocab, prompt.c_str(),
            static_cast<int32_t>(prompt.size()), tokens.data(),
            static_cast<int32_t>(tokens.size()), true, true);
    if (token_count < 0) {
        tokens.resize(static_cast<size_t>(-token_count));
        token_count = llama_tokenize(vocab, prompt.c_str(),
                static_cast<int32_t>(prompt.size()), tokens.data(),
                static_cast<int32_t>(tokens.size()), true, true);
    }
    if (token_count <= 0) return java_string(env, "Nie udało się podzielić promptu na tokeny.");
    tokens.resize(static_cast<size_t>(token_count));

    const uint32_t context_limit = llama_n_ctx(g_context);
    if (tokens.size() >= context_limit - 32) {
        return java_string(env, "Historia jest zbyt długa dla kontekstu 2048 tokenów. Wyczyść historię lub skróć wiadomość.");
    }

    llama_memory_clear(llama_get_memory(g_context), true);
    llama_sampler_reset(g_sampler);

    llama_batch batch = llama_batch_get_one(tokens.data(), token_count);
    const int32_t decode_result = llama_decode(g_context, batch);
    if (decode_result != 0) {
        return java_string(env, "llama_decode nie przetworzył promptu.");
    }

    std::string answer;
    const int limit = std::max(16, std::min(384, static_cast<int>(max_tokens)));
    for (int i = 0; i < limit; ++i) {
        const llama_token token = llama_sampler_sample(g_sampler, g_context, -1);
        if (llama_vocab_is_eog(vocab, token)) break;

        answer += token_piece(vocab, token);
        batch = llama_batch_get_one(const_cast<llama_token *>(&token), 1);
        if (llama_decode(g_context, batch) != 0) break;
    }

    return java_string(env, answer);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_qwant_launcher_LocalLlamaEngine_nativeRelease(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(g_mutex);
    release_locked();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_qwant_launcher_LocalEmbeddingEngine_nativeLoad(
        JNIEnv * env, jclass, jstring model_path, jint threads) {
    if (model_path == nullptr) return java_string(env, "Nie podano ścieżki modelu embeddingów.");
    const char * path = env->GetStringUTFChars(model_path, nullptr);
    if (path == nullptr) return java_string(env, "Nie można odczytać ścieżki modelu embeddingów.");

    std::lock_guard<std::mutex> lock(g_mutex);
    release_embedding_locked();

    if (!g_backend_ready) {
        llama_backend_init();
        g_backend_ready = true;
    }

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;
    g_embedding_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);

    if (g_embedding_model == nullptr) {
        release_embedding_locked();
        return java_string(env, "llama.cpp nie odczytał modelu multilingual-e5-small.");
    }
    if (!llama_model_has_encoder(g_embedding_model)) {
        release_embedding_locked();
        return java_string(env, "Model nie udostępnia architektury enkodera wymaganej do embeddings.");
    }

    llama_context_params context_params = llama_context_default_params();
    context_params.n_ctx = 512;
    context_params.n_batch = 512;
    context_params.n_ubatch = 128;
    const int thread_count = std::max(2, std::min(4, static_cast<int>(threads)));
    context_params.n_threads = thread_count;
    context_params.n_threads_batch = thread_count;
    context_params.embeddings = true;
    context_params.pooling_type = LLAMA_POOLING_TYPE_MEAN;
    context_params.attention_type = LLAMA_ATTENTION_TYPE_NON_CAUSAL;

    g_embedding_context = llama_init_from_model(g_embedding_model, context_params);
    if (g_embedding_context == nullptr) {
        release_embedding_locked();
        return java_string(env, "Nie udało się utworzyć kontekstu embeddingów.");
    }

    __android_log_print(ANDROID_LOG_INFO, TAG,
            "multilingual-e5-small loaded on CPU, dim=%d, threads=%d",
            llama_model_n_embd_out(g_embedding_model), thread_count);
    return java_string(env, "");
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_qwant_launcher_LocalEmbeddingEngine_nativeEmbed(
        JNIEnv * env, jclass, jstring text_value, jboolean is_query) {
    if (text_value == nullptr) return nullptr;
    const char * chars = env->GetStringUTFChars(text_value, nullptr);
    if (chars == nullptr) return nullptr;
    std::string tagged = is_query == JNI_TRUE ? "query: " : "passage: ";
    tagged += chars;
    env->ReleaseStringUTFChars(text_value, chars);

    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_embedding_model == nullptr || g_embedding_context == nullptr) return nullptr;

    const llama_vocab * vocab = llama_model_get_vocab(g_embedding_model);
    if (vocab == nullptr) return nullptr;

    std::vector<llama_token> tokens(512);
    int32_t token_count = llama_tokenize(vocab, tagged.c_str(),
            static_cast<int32_t>(tagged.size()), tokens.data(),
            static_cast<int32_t>(tokens.size()), true, true);
    if (token_count < 0) {
        const size_t required = static_cast<size_t>(-token_count);
        if (required > 512) return nullptr;
        tokens.resize(required);
        token_count = llama_tokenize(vocab, tagged.c_str(),
                static_cast<int32_t>(tagged.size()), tokens.data(),
                static_cast<int32_t>(tokens.size()), true, true);
    }
    if (token_count <= 0 || token_count > 512) return nullptr;
    tokens.resize(static_cast<size_t>(token_count));

    llama_memory_clear(llama_get_memory(g_embedding_context), true);
    llama_batch batch = llama_batch_get_one(tokens.data(), token_count);
    const int32_t encode_result = llama_encode(g_embedding_context, batch);
    if (encode_result != 0) {
        llama_memory_clear(llama_get_memory(g_embedding_context), true);
        __android_log_print(ANDROID_LOG_WARN, TAG, "E5 encode failed, status=%d", encode_result);
        return nullptr;
    }

    float * pooled = llama_get_embeddings_seq(g_embedding_context, 0);
    const int32_t dimensions = llama_model_n_embd_out(g_embedding_model);
    if (pooled == nullptr || dimensions <= 0 || dimensions > 4096) {
        llama_memory_clear(llama_get_memory(g_embedding_context), true);
        return nullptr;
    }

    double norm_squared = 0.0;
    for (int32_t i = 0; i < dimensions; ++i) {
        norm_squared += static_cast<double>(pooled[i]) * pooled[i];
    }
    const double norm = std::sqrt(norm_squared);
    if (!std::isfinite(norm) || norm < 1.0e-12) {
        llama_memory_clear(llama_get_memory(g_embedding_context), true);
        return nullptr;
    }

    std::vector<jfloat> normalized(static_cast<size_t>(dimensions));
    for (int32_t i = 0; i < dimensions; ++i) {
        normalized[static_cast<size_t>(i)] = static_cast<jfloat>(pooled[i] / norm);
    }
    llama_memory_clear(llama_get_memory(g_embedding_context), true);

    jfloatArray result = env->NewFloatArray(dimensions);
    if (result == nullptr) return nullptr;
    env->SetFloatArrayRegion(result, 0, dimensions, normalized.data());
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_qwant_launcher_LocalEmbeddingEngine_nativeRelease(JNIEnv *, jclass) {
    std::lock_guard<std::mutex> lock(g_mutex);
    release_embedding_locked();
}
