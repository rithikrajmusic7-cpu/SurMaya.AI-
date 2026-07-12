#pragma once
#include <vector>
#include <cmath>
#include <algorithm>

namespace SurMaya {

class DelayEngine {
private:
    std::vector<float> mBufferL;
    std::vector<float> mBufferR;
    size_t mBufferSize = 0;
    
    size_t mWriteIndex = 0;
    float mDelayTimeMs = 350.0f;
    float mFeedback = 0.4f;
    float mWet = 0.3f;
    float mSampleRate = 48000.0f;
    bool mPingPong = true;

    // Keep feedback signals
    float mFeedbackL = 0.0f;
    float mFeedbackR = 0.0f;

public:
    DelayEngine() {
        Configure(350.0f, 0.4f, 0.3f, true, 48000.0f);
    }

    void Configure(float delayTimeMs, float feedback, float wet, bool pingPong, float sampleRate) {
        mDelayTimeMs = delayTimeMs;
        mFeedback = feedback;
        mWet = wet;
        mPingPong = pingPong;
        mSampleRate = sampleRate;

        // Allocate maximum buffer size (e.g., 2.0 seconds maximum delay)
        size_t maxDelaySamples = static_cast<size_t>(2.0f * mSampleRate);
        mBufferSize = maxDelaySamples;

        mBufferL.assign(mBufferSize, 0.0f);
        mBufferR.assign(mBufferSize, 0.0f);
        mWriteIndex = 0;
        mFeedbackL = 0.0f;
        mFeedbackR = 0.0f;
    }

    void SetDelayTime(float delayTimeMs) {
        mDelayTimeMs = std::max(10.0f, std::min(delayTimeMs, 1900.0f));
    }

    void SetFeedback(float feedback) {
        mFeedback = std::max(0.0f, std::min(feedback, 0.95f));
    }

    void SetWet(float wet) {
        mWet = std::max(0.0f, std::min(wet, 1.0f));
    }

    void SetPingPong(bool pingPong) {
        mPingPong = pingPong;
    }

    // Stereo in, stereo out
    inline void Process(float inputL, float inputR, float& outputL, float& outputR) {
        // Calculate delay read index
        float delaySamples = (mDelayTimeMs / 1000.0f) * mSampleRate;
        size_t delayOffset = static_cast<size_t>(delaySamples);
        if (delayOffset >= mBufferSize) delayOffset = mBufferSize - 1;

        size_t readIndex = (mWriteIndex + mBufferSize - delayOffset) % mBufferSize;

        float delayedL = mBufferL[readIndex];
        float delayedR = mBufferR[readIndex];

        float dryL = inputL;
        float dryR = inputR;

        if (mPingPong) {
            // In ping-pong mode, delayed left feeds into right write buffer with feedback,
            // and delayed right feeds into left write buffer.
            mBufferL[mWriteIndex] = inputL + (delayedR * mFeedback);
            mBufferR[mWriteIndex] = inputR + (delayedL * mFeedback);
        } else {
            // Standard stereo delay
            mBufferL[mWriteIndex] = inputL + (delayedL * mFeedback);
            mBufferR[mWriteIndex] = inputR + (delayedR * mFeedback);
        }

        mWriteIndex = (mWriteIndex + 1) % mBufferSize;

        outputL = dryL * (1.0f - mWet) + delayedL * mWet;
        outputR = dryR * (1.0f - mWet) + delayedR * mWet;
    }

    void Reset() {
        std::fill(mBufferL.begin(), mBufferL.end(), 0.0f);
        std::fill(mBufferR.begin(), mBufferR.end(), 0.0f);
        mWriteIndex = 0;
        mFeedbackL = 0.0f;
        mFeedbackR = 0.0f;
    }
};

} // namespace SurMaya
