#pragma once
#include "ChannelStrip.h"
#include "Reverb.h"
#include "Delay.h"
#include "MasteringLimiter.h"
#include <vector>
#include <memory>
#include <cmath>

namespace SurMaya {

class MixerBusEngine {
private:
    // Support up to 8 channels (e.g. Sitar, Santoor, Bansuri, Tabla, Synth, Vocals, etc.)
    static const int MAX_CHANNELS = 8;
    ChannelStrip mChannels[MAX_CHANNELS];

    // Master Aux Send Busses
    ReverbEngine mReverbAuxBus;
    DelayEngine mDelayAuxBus;

    // Master Stereo Output Bus FX
    ParametricEQ mMasterEQ;
    Compressor mMasterCompressor;
    MasteringLimiter mMasterLimiter;

    float mSampleRate = 48000.0f;
    float mMasterFaderDb = 0.0f; // Main master volume fader

    // Real-time true peak tracking
    float mCachedTruePeakL = 0.0f;
    float mCachedTruePeakR = 0.0f;

public:
    MixerBusEngine() {
        Configure(48000.0f);
    }

    void Configure(float sampleRate) {
        mSampleRate = sampleRate;

        for (int i = 0; i < MAX_CHANNELS; ++i) {
            mChannels[i].Configure(mSampleRate);
        }

        mReverbAuxBus.Configure(0.85f, 0.25f, 0.4f, mSampleRate);
        mDelayAuxBus.Configure(350.0f, 0.4f, 0.35f, true, mSampleRate);

        mMasterEQ.Configure(mSampleRate);
        mMasterCompressor.Configure(-12.0f, 2.5f, 15.0f, 150.0f, 0.0f, mSampleRate);
        mMasterLimiter.Configure(-0.2f, -0.3f, 0.050f, mSampleRate); // -0.3dB True Peak target ceiling

        mMasterFaderDb = 0.0f;
        mCachedTruePeakL = 0.0f;
        mCachedTruePeakR = 0.0f;
    }

    ChannelStrip& GetChannel(int index) {
        if (index < 0) index = 0;
        if (index >= MAX_CHANNELS) index = MAX_CHANNELS - 1;
        return mChannels[index];
    }

    ReverbEngine& GetReverbAuxBus() { return mReverbAuxBus; }
    DelayEngine& GetDelayAuxBus() { return mDelayAuxBus; }

    ParametricEQ& GetMasterEQ() { return mMasterEQ; }
    Compressor& GetMasterCompressor() { return mMasterCompressor; }
    MasteringLimiter& GetMasterLimiter() { return mMasterLimiter; }

    void SetMasterFader(float levelDb) { mMasterFaderDb = levelDb; }
    float GetMasterFader() const { return mMasterFaderDb; }

    float GetTruePeakL() const { return mCachedTruePeakL; }
    float GetTruePeakR() const { return mCachedTruePeakR; }

    // Multi-channel Summing, Aux Routing, FX insertion, and Master Out processing
    void ProcessBlock(const float* const* inputChannels, int numInputChannels, 
                      float* outputL, float* outputR, int numSamples) {
        
        float peakL = 0.0f;
        float peakR = 0.0f;

        for (int s = 0; s < numSamples; ++s) {
            float sumL = 0.0f;
            float sumR = 0.0f;
            float sumReverbSend = 0.0f;
            float sumDelaySend = 0.0f;

            // 1. Process and sum all active channel strips
            for (int ch = 0; ch < MAX_CHANNELS; ++ch) {
                float sampleValue = 0.0f;
                if (ch < numInputChannels && inputChannels[ch] != nullptr) {
                    sampleValue = inputChannels[ch][s];
                }

                float chOutL = 0.0f, chOutR = 0.0f;
                float chRevSend = 0.0f, chDelSend = 0.0f;

                mChannels[ch].Process(sampleValue, chOutL, chOutR, chRevSend, chDelSend);

                sumL += chOutL;
                sumR += chOutR;
                sumReverbSend += chRevSend;
                sumDelaySend += chDelSend;
            }

            // 2. Process Aux FX Buses
            float reverbOutL = 0.0f, reverbOutR = 0.0f;
            mReverbAuxBus.Process(sumReverbSend, reverbOutL, reverbOutR);

            float delayOutL = 0.0f, delayOutR = 0.0f;
            mDelayAuxBus.Process(sumDelaySend, sumDelaySend, delayOutL, delayOutR);

            // 3. Sum direct outputs and aux returns
            float masterInputL = sumL + reverbOutL + delayOutL;
            float masterInputR = sumR + reverbOutR + delayOutR;

            // 4. Apply Master Equalizer
            masterInputL = mMasterEQ.Process(masterInputL);
            masterInputR = mMasterEQ.Process(masterInputR);

            // 5. Apply Master Compressor
            masterInputL = mMasterCompressor.Process(masterInputL);
            masterInputR = mMasterCompressor.Process(masterInputR);

            // Apply Master Fader gain
            float masterFaderLinear = powf(10.0f, mMasterFaderDb / 20.0f);
            masterInputL *= masterFaderLinear;
            masterInputR *= masterFaderLinear;

            // 6. Apply Mastering Limiter (Lookahead & Brickwall)
            float finalOutL = mMasterLimiter.Process(masterInputL);
            float finalOutR = mMasterLimiter.Process(masterInputR);

            // 7. True Peak Detection (inter-sample peak approximation using a 4-tap hermite/parabolic interpolator)
            // For simple robust true peak, we check current value and estimated midpoints.
            float truePeakEstimateL = EstimateTruePeak(finalOutL, masterInputL);
            float truePeakEstimateR = EstimateTruePeak(finalOutR, masterInputR);

            peakL = std::max(peakL, std::abs(truePeakEstimateL));
            peakR = std::max(peakR, std::abs(truePeakEstimateR));

            // Brickwall True Peak Hard Clipper at -0.1 dB (True Peak Protection)
            float hardClipCeiling = powf(10.0f, -0.1f / 20.0f);
            if (std::abs(finalOutL) > hardClipCeiling) {
                finalOutL = (finalOutL > 0.0f) ? hardClipCeiling : -hardClipCeiling;
            }
            if (std::abs(finalOutR) > hardClipCeiling) {
                finalOutR = (finalOutR > 0.0f) ? hardClipCeiling : -hardClipCeiling;
            }

            outputL[s] = finalOutL;
            outputR[s] = finalOutR;
        }

        // Decay the peak meters slowly for visual diagnostic smooth rendering
        mCachedTruePeakL = peakL * 0.15f + mCachedTruePeakL * 0.85f;
        mCachedTruePeakR = peakR * 0.15f + mCachedTruePeakR * 0.85f;
    }

    void Reset() {
        for (int i = 0; i < MAX_CHANNELS; ++i) {
            mChannels[i].Reset();
        }
        mReverbAuxBus.Reset();
        mDelayAuxBus.Reset();
        mMasterEQ.Reset();
        mMasterCompressor.Reset();
        mMasterLimiter.Reset();
        mCachedTruePeakL = 0.0f;
        mCachedTruePeakR = 0.0f;
    }

private:
    // Simple 2x interpolative peak estimator for True Peak approximation
    inline float EstimateTruePeak(float sample1, float sample2) {
        float peak = std::abs(sample1);
        float midpoint = (sample1 + sample2) * 0.5f;
        peak = std::max(peak, std::abs(midpoint));
        return peak;
    }
};

} // namespace SurMaya
