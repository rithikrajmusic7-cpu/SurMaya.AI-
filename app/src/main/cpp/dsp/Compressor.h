#pragma once
#include <cmath>
#include <algorithm>

class Compressor {
private:
    float mThresholdDb = -12.0f;
    float mRatio = 4.0f;
    float mAttackSec = 0.010f;
    float mReleaseSec = 0.100f;
    float mMakeupGainDb = 0.0f;
    
    float mEnvelope = 0.0f;
    float mSampleRate = 48000.0f;

public:
    Compressor() = default;

    void Configure(float thresholdDb, float ratio, float attackMs, float releaseMs, float makeupGainDb, float sampleRate) {
        mThresholdDb = thresholdDb;
        mRatio = ratio;
        mAttackSec = attackMs / 1000.0f;
        mReleaseSec = releaseMs / 1000.0f;
        mMakeupGainDb = makeupGainDb;
        mSampleRate = sampleRate;
    }

    inline float Process(float input) {
        // Simple sidechain envelope detector (RMS style or Absolute value)
        float absInput = std::abs(input);
        
        // Attack/Release filter coefficients
        float attackCoef = expf(-1.0f / (mSampleRate * mAttackSec));
        float releaseCoef = expf(-1.0f / (mSampleRate * mReleaseSec));
        
        if (absInput > mEnvelope) {
            mEnvelope = attackCoef * mEnvelope + (1.0f - attackCoef) * absInput;
        } else {
            mEnvelope = releaseCoef * mEnvelope + (1.0f - releaseCoef) * absInput;
        }

        // Convert envelope level to Decibels
        float envDb = 20.0f * log10f(std::max(1e-5f, mEnvelope));
        
        // Gain reduction calculation (Threshold / Ratio)
        float gainReductionDb = 0.0f;
        if (envDb > mThresholdDb) {
            gainReductionDb = (mThresholdDb - envDb) * (1.0f - 1.0f / mRatio);
        }
        
        // Convert gain reduction back to linear multiplier
        float attenuation = powf(10.0f, gainReductionDb / 20.0f);
        
        // Apply attenuation and makeup gain
        float makeupGainLinear = powf(10.0f, mMakeupGainDb / 20.0f);
        return input * attenuation * makeupGainLinear;
    }

    void Reset() {
        mEnvelope = 0.0f;
    }
};
