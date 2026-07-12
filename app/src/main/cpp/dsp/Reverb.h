#pragma once
#include <vector>
#include <cmath>
#include <algorithm>

namespace SurMaya {

class CombFilter {
private:
    std::vector<float> mBuffer;
    size_t mSize = 0;
    size_t mWriteIndex = 0;
    float mFeedback = 0.5f;
    float mFilterStore = 0.0f;
    float mDamp = 0.2f;

public:
    CombFilter() = default;

    void Configure(size_t size, float feedback, float damp) {
        mSize = size;
        mBuffer.assign(mSize, 0.0f);
        mWriteIndex = 0;
        mFeedback = feedback;
        mDamp = damp;
        mFilterStore = 0.0f;
    }

    inline float Process(float input) {
        float output = mBuffer[mWriteIndex];
        
        // Low-pass damp filter in the feedback loop
        mFilterStore = (output * (1.0f - mDamp)) + (mFilterStore * mDamp);
        
        float inputWithFeedback = input + (mFilterStore * mFeedback);
        mBuffer[mWriteIndex] = inputWithFeedback;
        
        mWriteIndex = (mWriteIndex + 1) % mSize;
        return output;
    }

    void Reset() {
        std::fill(mBuffer.begin(), mBuffer.end(), 0.0f);
        mWriteIndex = 0;
        mFilterStore = 0.0f;
    }
};

class AllpassFilter {
private:
    std::vector<float> mBuffer;
    size_t mSize = 0;
    size_t mWriteIndex = 0;
    float mFeedback = 0.5f;

public:
    AllpassFilter() = default;

    void Configure(size_t size, float feedback) {
        mSize = size;
        mBuffer.assign(mSize, 0.0f);
        mWriteIndex = 0;
        mFeedback = feedback;
    }

    inline float Process(float input) {
        float bufOut = mBuffer[mWriteIndex];
        float output = -input + bufOut;
        mBuffer[mWriteIndex] = input + (bufOut * mFeedback);
        
        mWriteIndex = (mWriteIndex + 1) % mSize;
        return output;
    }

    void Reset() {
        std::fill(mBuffer.begin(), mBuffer.end(), 0.0f);
        mWriteIndex = 0;
    }
};

class ReverbEngine {
private:
    // 8 Comb Filters per channel
    CombFilter mCombL[8];
    CombFilter mCombR[8];
    
    // 4 Allpass Filters per channel
    AllpassFilter mAllpassL[4];
    AllpassFilter mAllpassR[4];

    float mRoomSize = 0.8f;
    float mDamp = 0.2f;
    float mWet = 0.33f;
    float mDry = 1.0f;
    float mWidth = 1.0f;
    float mSampleRate = 48000.0f;

    // Fixed delay times for 44.1kHz / 48kHz scaling
    const int combTuningL[8] = { 1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617 };
    const int combTuningR[8] = { 1116 + 23, 1188 + 23, 1277 + 23, 1356 + 23, 1422 + 23, 1491 + 23, 1557 + 23, 1617 + 23 };
    const int allpassTuningL[4] = { 556, 441, 341, 225 };
    const int allpassTuningR[4] = { 556 + 23, 441 + 23, 341 + 23, 225 + 23 };

public:
    ReverbEngine() {
        Configure(0.8f, 0.2f, 0.3f, 48000.0f);
    }

    void Configure(float roomSize, float damp, float wet, float sampleRate) {
        mRoomSize = roomSize;
        mDamp = damp;
        mWet = wet;
        mSampleRate = sampleRate;

        float scale = mSampleRate / 44100.0f;

        // Comb feedback factor is scaled by room size
        float combFeedback = mRoomSize * 0.28f + 0.7f;

        for (int i = 0; i < 8; ++i) {
            size_t sizeL = static_cast<size_t>(combTuningL[i] * scale);
            size_t sizeR = static_cast<size_t>(combTuningR[i] * scale);
            mCombL[i].Configure(sizeL, combFeedback, mDamp);
            mCombR[i].Configure(sizeR, combFeedback, mDamp);
        }

        for (int i = 0; i < 4; ++i) {
            size_t sizeL = static_cast<size_t>(allpassTuningL[i] * scale);
            size_t sizeR = static_cast<size_t>(allpassTuningR[i] * scale);
            mAllpassL[i].Configure(sizeL, 0.5f);
            mAllpassR[i].Configure(sizeR, 0.5f);
        }
    }

    void SetWetDry(float wet) {
        mWet = wet;
        mDry = 1.0f - wet;
    }

    // Input mono, output stereo left and right
    inline void Process(float input, float& outputL, float& outputR) {
        float outL = 0.0f;
        float outR = 0.0f;

        // Process comb filters in parallel
        for (int i = 0; i < 8; ++i) {
            outL += mCombL[i].Process(input * 0.015f);
            outR += mCombR[i].Process(input * 0.015f);
        }

        // Process allpass filters in series
        for (int i = 0; i < 4; ++i) {
            outL = mAllpassL[i].Process(outL);
            outR = mAllpassR[i].Process(outR);
        }

        // Apply wet/dry mix
        outputL = input * mDry + outL * mWet;
        outputR = input * mDry + outR * mWet;
    }

    void Reset() {
        for (int i = 0; i < 8; ++i) {
            mCombL[i].Reset();
            mCombR[i].Reset();
        }
        for (int i = 0; i < 4; ++i) {
            mAllpassL[i].Reset();
            mAllpassR[i].Reset();
        }
    }
};

} // namespace SurMaya
