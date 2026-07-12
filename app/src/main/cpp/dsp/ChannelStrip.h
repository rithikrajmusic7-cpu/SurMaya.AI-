#pragma once
#include "ParametricEQ.h"
#include "Compressor.h"
#include <algorithm>

namespace SurMaya {

class ChannelStrip {
private:
    float mInputGainDb = 0.0f;
    float mFaderLevelDb = 0.0f;
    float mPanning = 0.0f; // -1.0 (Left) to +1.0 (Right)
    bool mMuted = false;
    bool mSoloed = false;

    // Dedicated processing blocks
    ParametricEQ mEQ;
    Compressor mCompressor;

    // Aux Send Levels (0.0 to 1.0 linear or Db)
    float mReverbSendDb = -12.0f;
    float mDelaySendDb = -12.0f;

    float mSampleRate = 48000.0f;

public:
    ChannelStrip() {
        Configure(48000.0f);
    }

    void Configure(float sampleRate) {
        mSampleRate = sampleRate;
        mEQ.Configure(mSampleRate);
        mCompressor.Configure(-12.0f, 3.5f, 10.0f, 100.0f, 0.0f, mSampleRate);
    }

    // Accessors for processing blocks
    ParametricEQ& GetEQ() { return mEQ; }
    Compressor& GetCompressor() { return mCompressor; }

    // Channel parameters
    void SetInputGain(float gainDb) { mInputGainDb = gainDb; }
    float GetInputGain() const { return mInputGainDb; }

    void SetFaderLevel(float levelDb) { mFaderLevelDb = levelDb; }
    float GetFaderLevel() const { return mFaderLevelDb; }

    void SetPanning(float panning) { mPanning = std::max(-1.0f, std::min(panning, 1.0f)); }
    float GetPanning() const { return mPanning; }

    void SetMute(bool mute) { mMuted = mute; }
    bool IsMuted() const { return mMuted; }

    void SetSolo(bool solo) { mSoloed = solo; }
    bool IsSoloed() const { return mSoloed; }

    void SetReverbSend(float sendDb) { mReverbSendDb = sendDb; }
    float GetReverbSend() const { return mReverbSendDb; }

    void SetDelaySend(float sendDb) { mDelaySendDb = sendDb; }
    float GetDelaySend() const { return mDelaySendDb; }

    // Process mono input to stereo, returning direct main stereo output,
    // plus aux send levels.
    inline void Process(float input, 
                        float& outMainL, float& outMainR, 
                        float& outReverbSend, float& outDelaySend) {
        if (mMuted) {
            outMainL = 0.0f;
            outMainR = 0.0f;
            outReverbSend = 0.0f;
            outDelaySend = 0.0f;
            return;
        }

        // Apply input trim gain
        float inputGainLinear = powf(10.0f, mInputGainDb / 20.0f);
        float x = input * inputGainLinear;

        // Apply channel parametric EQ
        x = mEQ.Process(x);

        // Apply channel dynamics/compressor
        x = mCompressor.Process(x);

        // Apply channel fader level
        float faderLinear = powf(10.0f, mFaderLevelDb / 20.0f);
        x *= faderLinear;

        // Extract Aux Send feeds (pre-panning, post-fader for standard aux routing)
        float reverbSendLinear = powf(10.0f, mReverbSendDb / 20.0f);
        float delaySendLinear = powf(10.0f, mDelaySendDb / 20.0f);
        outReverbSend = x * reverbSendLinear;
        outDelaySend = x * delaySendLinear;

        // Apply equal power panning
        float angleRad = (mPanning + 1.0f) * (M_PI / 4.0f);
        float panL = cosf(angleRad);
        float panR = sinf(angleRad);

        outMainL = x * panL;
        outMainR = x * panR;
    }

    void Reset() {
        mEQ.Reset();
        mCompressor.Reset();
    }
};

} // namespace SurMaya
