cmake_minimum_required(VERSION 3.18.1)

project("surmaya_aire")

# Configure compilation flags for high-performance audio (C++17, NEON vectors, loop unrolling)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(surmaya_aire SHARED
        # JNI Bridge entry point
        jni/AIREJniBridge.cpp
        
        # Audio Engine & Core scheduler
        core/NativeAudioEngine.h
        core/AudioGraphEvaluator.h
        core/LockFreeSPSCQueue.h
        core/ProjectFormat.h
        
        # Digital Signal Processing Units
        dsp/BiquadFilter.h
        dsp/Compressor.h
        dsp/MasteringLimiter.h
        
        # Performance Expression & Genre style engines
        interpreter/PerformanceInterpreter.h
        interpreter/InstrumentPersonality.h
        interpreter/SingerExpressionEngine.h
        interpreter/StyleRenderer.h
)

# Bind standard Android library log features
find_library(log-lib log)

target_link_libraries(surmaya_aire
        ${log-lib}
)
