# QwantSYS Semantic Memory Core 0.8.0

## Scope

0.8.0 adds real dense embeddings and vector similarity search to the Android launcher prototype. It remains an application-level feature, not an Android ROM or a privileged OS service.

## Embedding model

- Base model: `intfloat/multilingual-e5-small`
- GGUF distribution: `cstr/multilingual-e5-small-GGUF`
- Quantization: Q8_0 (about 126 MiB)
- Output dimension: 384
- Runtime: llama.cpp JNI, CPU only
- Model download: on demand, HTTPS, stored in app-private files
- Embedding model file is not packaged inside the APK.

The model is multilingual and includes Polish in the listed language set. Its documented usage requires `passage: ` for records and `query: ` for user searches. The implementation applies those prefixes inside the native embedding call.

## Storage and retrieval

- SQLite schema is upgraded from version 1 to version 2 by adding `memories.embedding BLOB`; existing rows and note text are preserved.
- Each stored vector is 384 float32 values (1,536 bytes) normalized to unit length.
- New records are embedded automatically when E5 is loaded.
- Existing records without vectors are indexed in the background after the embedding model loads.
- Semantic search ranks stored vectors using cosine similarity (dot product over normalized vectors).
- The app falls back to lexical search when E5 is not active or no semantic result is available.
- Current record limit remains 500; the vector BLOB storage for 500 records is approximately 750 KiB, excluding SQLite overhead.

## Runtime boundaries

Qwen and E5 have separate model/context handles in the same llama.cpp JNI library. The native mutex serializes their inference calls so the two model contexts are not executed concurrently. Both models use the CPU backend; this build does not claim Adreno GPU, DSP or NPU acceleration.

After the E5 model has been downloaded, embedding creation and search run locally without a network connection. The initial download requires HTTPS and about 180 MiB of free app-private storage.

## Validation

GitHub Actions compiles the ARM64 JNI library, Java classes and APK, then signs and uploads the APK. This does not constitute a runtime test of the E5 model or SQLite migration on a physical REDMAGIC 7; those still require device testing.
