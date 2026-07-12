#pragma once
#include "BiquadFilter.h"
#include <algorithm>

namespace SurMaya {

class ParametricEQ {
private:
    BiquadFilter mHighPass;
    BiquadFilter mLowShelf;
    BiquadFilter mPeakingMid;
    BiquadFilter mHighShelf;

    float mHpFreq = 30.0f;
    float mLsFreq = 150.0f;
    float mLsGainDb = 0.0f;
    
    float mPeakFreq = 1000.0f;
    float mPeakQ = 1.0f;
    float mPeakGainDb = 0.0f;
    
    float mHsFreq = 6000.0f;
    float mHsGainDb = 0.0f;

    float mSampleRate = 48000.0f;
    bool mEnabled = true;

public:
    ParametricEQ() {
        Configure(48000.0f);
    }

    void Configure(float sampleRate) {
        mSampleRate = sampleRate;
        UpdateBands();
    }

    void SetEnabled(bool enabled) {
        mEnabled = enabled;
    }

    bool IsEnabled() const { return mEnabled; }

    void SetHPF(float frequencyHz) {
        mHpFreq = std::max(20.0f, std::min(frequencyHz, 500.0f));
        UpdateHP();
    }

    void SetLowShelf(float frequencyHz, float gainDb) {
        mLsFreq = std::max(50.0f, std::min(frequencyHz, 400.0f));
        mLsGainDb = std::max(-15.0f, std::min(gainDb, 15.0f));
        UpdateLS();
    }

    void SetPeakingMid(float frequencyHz, float Q, float gainDb) {
        mPeakFreq = std::max(200.0f, std::min(frequencyHz, 8000.0f));
        mPeakQ = std::max(0.1f, std::min(Q, 10.0f));
        mPeakGainDb = std::max(-15.0f, std::min(gainDb, 15.0f));
        UpdatePeak();
    }

    void SetHighShelf(float frequencyHz, float gainDb) {
        mHsFreq = std::max(4000.0f, std::min(frequencyHz, 18000.0f));
        mHsGainDb = std::max(-15.0f, std::min(gainDb, 15.0f));
        UpdateHS();
    }

    inline float Process(float input) {
        if (!mEnabled) return input;
        
        float x = input;
        x = mHighPass.Process(x);
        x = mLowShelf.Process(x);
        x = mPeakingMid.Process(x);
        x = mHighShelf.Process(x);
        return x;
    }

    void Reset() {
        mHighPass.Reset();
        mLowShelf.Reset();
        mPeakingMid.Reset();
        mHighShelf.Reset();
    }

private:
    void UpdateBands() {
        UpdateHP();
        UpdateLS();
        UpdatePeak();
        UpdateHS();
    }

    void UpdateHP() {
        mHighPass.Configure(BiquadFilter::FilterType::HighPass, mHpFreq, 0.707f, 0.0f, mSampleRate);
    }

    void UpdateLS() {
        mLowShelf.Configure(BiquadFilter::FilterType::LowShelf, mLsFreq, 0.707f, mLsGainDb, mSampleRate);
    }

    void UpdatePeak() {
        mPeakingMid.Configure(BiquadFilter::FilterType::PeakingEQ, mPeakFreq, mPeakQ, mPeakGainDb, mSampleRate);
    }

    void UpdateHS() {
        mHighShelf.Configure(BiquadFilter::FilterType::HighShelf, mHsFreq, 0.707f, mHsGainDb, mSampleRate);
    }
};

} // namespace SurMaya
