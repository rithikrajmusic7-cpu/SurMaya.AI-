#pragma once
#include <string>
#include <algorithm>

namespace SurMaya {

struct ADSRConfig {
    float attackSec = 0.002f;
    float decaySec = 0.150f;
    float sustainLevel = 0.70f;
    float releaseSec = 0.250f;
};

class InstrumentPersonality {
private:
    std::string mPresetName = "Concert_Grand";
    ADSRConfig mEnvelope;
    float mFilterCutoffHz = 20000.0f;
    float mFilterResonance = 0.707f;
    float mChorusingLevel = 0.0f;

public:
    InstrumentPersonality() {
        LoadPreset("Concert_Grand");
    }

    void LoadPreset(const std::string& name) {
        mPresetName = name;
        
        if (name == "Soft_Piano") {
            mEnvelope = { 0.050f, 0.350f, 0.40f, 0.600f };
            mFilterCutoffHz = 1200.0f; // Warm, muted highs
            mFilterResonance = 0.5f;
            mChorusingLevel = 0.15f;
        } else if (name == "Vintage_Upright") {
            mEnvelope = { 0.005f, 0.200f, 0.65f, 0.180f };
            mFilterCutoffHz = 3500.0f; // Nostalgic, mid-focused presence
            mFilterResonance = 0.9f;
            mChorusingLevel = 0.35f; // Slight detuning vibe
        } else if (name == "Film_Noir_Steinway") {
            mEnvelope = { 0.080f, 0.500f, 0.30f, 0.850f }; // Lush long tails
            mFilterCutoffHz = 850.0f;
            mFilterResonance = 0.6f;
            mChorusingLevel = 0.10f;
        } else if (name == "Bright_Studio_Grand") {
            mEnvelope = { 0.001f, 0.100f, 0.85f, 0.200f }; // Crisp and punchy attack
            mFilterCutoffHz = 18000.0f;
            mFilterResonance = 0.707f;
            mChorusingLevel = 0.0f;
        } else {
            // "Concert_Grand" (Default)
            mEnvelope = { 0.002f, 0.150f, 0.70f, 0.250f };
            mFilterCutoffHz = 16000.0f;
            mFilterResonance = 0.707f;
            mChorusingLevel = 0.05f;
        }
    }

    ADSRConfig GetEnvelope() const { return mEnvelope; }
    float GetFilterCutoff() const { return mFilterCutoffHz; }
    float GetFilterResonance() const { return mFilterResonance; }
    float GetChorusing() const { return mChorusingLevel; }
    std::string GetPresetName() const { return mPresetName; }
};

} // namespace SurMaya
