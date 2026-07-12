#pragma once
#include <cmath>
#include <string>
#include <algorithm>
#include <cstdlib>

namespace SurMaya {

class SingerExpressionEngine {
private:
    std::string mTrackId;
    std::string mEmotion = "Devotional";
    float mVibratoDepth = 0.35f;
    float mBreathGain = 0.15f;
    
    // LFO parameters
    float mVibratoPhase = 0.0f;
    float mVibratoFreqHz = 6.0f; // Human average
    float mSampleRate = 48000.0f;
    
    // Breath generation state
    float mBreathNoiseZ1 = 0.0f;

public:
    SingerExpressionEngine() = default;

    void Configure(const std::string& trackId, const std::string& emotion, float vibratoDepth, float breathGain, float sampleRate = 48000.0f) {
        mTrackId = trackId;
        mEmotion = emotion;
        mVibratoDepth = std::max(0.0f, std::min(1.0f, vibratoDepth));
        mBreathGain = std::max(0.0f, std::min(1.0f, breathGain));
        mSampleRate = sampleRate;

        // Customise vibrato LFO and rate depending on the emotion
        if (emotion == "Sorrowful") {
            mVibratoFreqHz = 4.8f; // Slower, wider tragic vibrato
            mVibratoDepth *= 1.25f;
        } else if (emotion == "Joyful") {
            mVibratoFreqHz = 6.8f; // Rapid, upbeat flutter
            mVibratoDepth *= 0.85f;
        } else if (emotion == "Devotional" || emotion == "Calm") {
            mVibratoFreqHz = 5.5f; // Serene, stable sustain
        } else {
            mVibratoFreqHz = 6.0f;
        }
    }

    // Calculates current pitch modulation factor (multiplier on sampling frequency)
    float GetPitchModFactor() {
        if (mVibratoDepth <= 1e-4f) return 1.0f;
        
        float lfoVal = sinf(mVibratoPhase);
        
        // Modulate phase
        float lfoStep = (2.0f * M_PI * mVibratoFreqHz) / mSampleRate;
        mVibratoPhase += lfoStep;
        if (mVibratoPhase >= 2.0f * M_PI) {
            mVibratoPhase -= 2.0f * M_PI;
        }

        // Convert LFO amplitude to semitone cents (+/- 50 cents max based on depth)
        float semitoneOffset = lfoVal * mVibratoDepth * 0.50f;
        return powf(2.0f, semitoneOffset / 12.0f);
    }

    // Generates high-pass filtered breath noise bursts
    float ProcessBreathNoise() {
        if (mBreathGain <= 1e-4f) return 0.0f;

        // White noise sample (-1.0 to 1.0)
        float rawNoise = ((float)rand() / (float)RAND_MAX) * 2.0f - 1.0f;
        
        // High-pass filter (RC filter style) to remove low rumble, simulating vocal aspiration
        float filteredNoise = rawNoise - mBreathNoiseZ1;
        mBreathNoiseZ1 = rawNoise * 0.95f; // preserve history

        return filteredNoise * mBreathGain * 0.12f;
    }

    // Returns saturation/gain multiplier depending on emotional state
    float GetVocalSaturationDb() const {
        if (mEmotion == "Devotional") return 1.5f;   // Mild rich warm harmonic content
        if (mEmotion == "Aggressive") return 4.5f;   // Saturated, gritty grit
        if (mEmotion == "Sorrowful") return -1.0f;   // Clean, soft, breathy presence
        return 0.0f; // Normal
    }
};

} // namespace SurMaya
