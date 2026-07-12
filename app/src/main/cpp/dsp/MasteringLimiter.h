#pragma once
#include <vector>
#include <cmath>
#include <algorithm>

class MasteringLimiter {
private:
    float mThresholdDb = -0.1f;
    float mCeilingDb = -0.2f;
    float mReleaseSec = 0.050f;
    float mSampleRate = 48000.0f;
    
    // Look-ahead buffer
    std::vector<float> mDelayBuffer;
    size_t mDelayLength = 0;
    size_t mWriteIndex = 0;
    
    float mGainReduction = 1.0f;

public:
    MasteringLimiter() {
        Configure(-0.1f, -0.2f, 0.050f, 48000.0f);
    }

    void Configure(float thresholdDb, float ceilingDb, float releaseSec, float sampleRate) {
        mThresholdDb = thresholdDb;
        mCeilingDb = ceilingDb;
        mReleaseSec = releaseSec;
        mSampleRate = sampleRate;
        
        // 2ms look-ahead delay
        float lookAheadMs = 2.0f;
        mDelayLength = static_cast<size_t>((lookAheadMs / 1000.0f) * mSampleRate);
        if (mDelayLength < 1) mDelayLength = 1;
        
        mDelayBuffer.assign(mDelayLength, 0.0f);
        mWriteIndex = 0;
        mGainReduction = 1.0f;
    }

    inline float Process(float input) {
        // Store current input in delay line
        float delayedInput = mDelayBuffer[mWriteIndex];
        mDelayBuffer[mWriteIndex] = input;
        
        // Increment look-ahead pointer
        mWriteIndex = (mWriteIndex + 1) % mDelayLength;
        
        // Detect peak on the incoming look-ahead sample
        float inputPeak = std::abs(input);
        float thresholdLinear = powf(10.0f, mThresholdDb / 20.0f);
        float ceilingLinear = powf(10.0f, mCeilingDb / 20.0f);
        
        float targetGain = 1.0f;
        if (inputPeak > thresholdLinear) {
            targetGain = thresholdLinear / inputPeak;
        }
        
        // Smoothing gain reduction (instant attack, exponential release)
        float releaseCoef = expf(-1.0f / (mSampleRate * mReleaseSec));
        if (targetGain < mGainReduction) {
            mGainReduction = targetGain; // Instant Attack
        } else {
            mGainReduction = releaseCoef * mGainReduction + (1.0f - releaseCoef) * targetGain; // Smooth Release
        }
        
        // Output attenuated delayed sample multiplied by ceiling level
        return delayedInput * mGainReduction * ceilingLinear;
    }

    void Reset() {
        std::fill(mDelayBuffer.begin(), mDelayBuffer.end(), 0.0f);
        mGainReduction = 1.0f;
        mWriteIndex = 0;
    }
};
