#include <jni.h>
#include <string>
#include <cstring>
#include <android/log.h>
#include "../core/NativeAudioEngine.h"

#define LOG_TAG "AIRE_JNI_v2_0"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_initEngineNative(
        JNIEnv* env, jobject thiz, jint sample_rate, jint buffer_size) {
    auto* engine = new NativeAudioEngine(static_cast<uint32_t>(sample_rate), static_cast<uint32_t>(buffer_size));
    LOGD("AIRE JNI: Native Audio Engine initialized at %d Hz [Buffer: %d]", sample_rate, buffer_size);
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_releaseEngineNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        delete engine;
        LOGD("AIRE JNI: Native Audio Engine released.");
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_startPlaybackNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->Start();
        LOGD("AIRE JNI: Playback started.");
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_pausePlaybackNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->Pause();
        LOGD("AIRE JNI: Playback paused.");
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_registerTelemetryBufferNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jobject byte_buffer) {
    if (engine_ptr && byte_buffer) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        uint8_t* buffer_address = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(byte_buffer));
        if (buffer_address) {
            engine->RegisterTelemetryBuffer(buffer_address);
            LOGD("AIRE JNI: Registered direct ByteBuffer telemetry mapping.");
        } else {
            LOGE("AIRE JNI: Failed to load direct ByteBuffer memory address.");
        }
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setHumanizeParametersNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jfloat amount, jfloat tempo_bpm) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetHumanize(amount, tempo_bpm);
        LOGD("AIRE JNI: Set humanize amount to %.2f @ %.1f BPM", amount, tempo_bpm);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_applyPerformanceInterpretationNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring track_id, jstring notes_json) {
    if (engine_ptr && track_id && notes_json) {
        const char* native_track = env->GetStringUTFChars(track_id, nullptr);
        const char* native_notes = env->GetStringUTFChars(notes_json, nullptr);
        
        LOGD("AIRE JNI: Applying Performance Interpretation on track %s: notesJson size: %zu", 
             native_track, strlen(native_notes));
             
        env->ReleaseStringUTFChars(track_id, native_track);
        env->ReleaseStringUTFChars(notes_json, native_notes);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setMusicStyleProfileNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring style_name) {
    if (engine_ptr && style_name) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_style = env->GetStringUTFChars(style_name, nullptr);
        engine->SetStyle(native_style);
        LOGD("AIRE JNI: Style Profile loaded: %s", native_style);
        env->ReleaseStringUTFChars(style_name, native_style);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_configureInstrumentPersonalityNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring track_id, jstring preset_name) {
    if (engine_ptr && track_id && preset_name) {
        const char* native_track = env->GetStringUTFChars(track_id, nullptr);
        const char* native_preset = env->GetStringUTFChars(preset_name, nullptr);
        
        LOGD("AIRE JNI: Configuring track %s with Instrument Character preset: %s", 
             native_track, native_preset);
             
        env->ReleaseStringUTFChars(track_id, native_track);
        env->ReleaseStringUTFChars(preset_name, native_preset);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_configureSingerExpressionNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring track_id, 
        jstring emotion, jfloat vibrato_depth, jfloat breath_gain) {
    if (engine_ptr && track_id && emotion) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_track = env->GetStringUTFChars(track_id, nullptr);
        const char* native_emotion = env->GetStringUTFChars(emotion, nullptr);
        
        engine->ConfigureVocal(native_track, native_emotion, vibrato_depth, breath_gain);
        
        env->ReleaseStringUTFChars(track_id, native_track);
        env->ReleaseStringUTFChars(emotion, native_emotion);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setQualityProfileNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring profile_name) {
    if (engine_ptr && profile_name) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_profile = env->GetStringUTFChars(profile_name, nullptr);
        engine->SetQuality(native_profile);
        LOGD("AIRE JNI: Quality Profile set: %s", native_profile);
        env->ReleaseStringUTFChars(profile_name, native_profile);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_updateSchedulerConstraintsNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jboolean thermal_throttle, jboolean battery_saver) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetSchedulerConstraints(thermal_throttle, battery_saver);
        LOGD("AIRE JNI: Constraints updated. Thermal throttle: %d, Battery Saver: %d", 
             thermal_throttle, battery_saver);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_noteOnNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jint note, jint velocity, jstring instrument) {
    if (engine_ptr && instrument) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_instrument = env->GetStringUTFChars(instrument, nullptr);
        engine->NoteOn(static_cast<int>(note), static_cast<int>(velocity), native_instrument);
        env->ReleaseStringUTFChars(instrument, native_instrument);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_noteOffNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jint note) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->NoteOff(static_cast<int>(note));
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setInstrumentPresetNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring preset_name) {
    if (engine_ptr && preset_name) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_preset = env->GetStringUTFChars(preset_name, nullptr);
        engine->SetInstrumentPreset(native_preset);
        LOGD("AIRE JNI: Instrument Preset loaded: %s", native_preset);
        env->ReleaseStringUTFChars(preset_name, native_preset);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setChannelVolumeNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jint channel_index, jfloat fader_db) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetChannelVolume(static_cast<int>(channel_index), static_cast<float>(fader_db));
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setChannelPanNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jint channel_index, jfloat pan) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetChannelPan(static_cast<int>(channel_index), static_cast<float>(pan));
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setChannelEQNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jint channel_index, jfloat gain_db) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetChannelEQ(static_cast<int>(channel_index), static_cast<float>(gain_db));
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setChannelAuxSendsNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jint channel_index, jfloat reverb_db, jfloat delay_db) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetChannelReverbSend(static_cast<int>(channel_index), static_cast<float>(reverb_db));
        engine->SetChannelDelaySend(static_cast<int>(channel_index), static_cast<float>(delay_db));
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setMasterFaderNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jfloat master_db) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetMasterFader(static_cast<float>(master_db));
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_getTruePeakMetersNative(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jfloatArray out_peaks) {
    if (engine_ptr && out_peaks) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        float peakL = 0.0f;
        float peakR = 0.0f;
        engine->GetTruePeakMeters(peakL, peakR);
        
        jfloat buffer[2] = { peakL, peakR };
        env->SetFloatArrayRegion(out_peaks, 0, 2, buffer);
    }
}

}
