cmake_minimum_required(VERSION 3.22)
project(qwant_llama_jni LANGUAGES C CXX)

if(NOT ANDROID)
    message(FATAL_ERROR "Qwant JNI backend must be cross-compiled with the Android NDK.")
endif()

if(NOT DEFINED LLAMA_CPP_SOURCE_DIR)
    message(FATAL_ERROR "Pass -DLLAMA_CPP_SOURCE_DIR=/path/to/llama.cpp")
endif()

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

set(LLAMA_BUILD_COMMON OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_APP OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_UI OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(LLAMA_SUBPROCESS OFF CACHE BOOL "" FORCE)

set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(GGML_CPU_KLEIDIAI ON CACHE BOOL "" FORCE)

add_subdirectory("${LLAMA_CPP_SOURCE_DIR}" llama.cpp EXCLUDE_FROM_ALL)

add_library(qwant_llama_jni SHARED native-lib.cpp)
target_include_directories(qwant_llama_jni PRIVATE "${LLAMA_CPP_SOURCE_DIR}/include")
target_link_libraries(qwant_llama_jni PRIVATE llama android log)
set_target_properties(qwant_llama_jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/jni")
