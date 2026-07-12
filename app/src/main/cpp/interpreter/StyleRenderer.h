#pragma once
#include <string>
#include <algorithm>

namespace SurMaya {

struct StyleProfile {
    std::string name;
    
    // Equalizer defaults for the master chain
    float eqLowGainDb = 0.0f;
    float eqMidGainDb = 0.0f;
    float eqHighGainDb = 0.0f;
    
    // Compressor parameters
    float compressionThresholdDb = -12.0f;
    float compressionRatio = 3.5f;
    
    // Reverb decay times
    float reverbDecaySec = 1.5f;
    float reverbMixLevel = 0.15f;
    
    // Vocal styling flags
    bool microtonalPitchGlide = false;
    bool emphasizeTraditionalInstruments = false;
};

class StyleRenderer {
private:
    std::string mActiveStyle = "Odia_Classical";
    StyleProfile mProfile;

public:
    StyleRenderer() {
        LoadStyleProfile("Odia_Classical");
    }

    void LoadStyleProfile(const std::string& styleName) {
        mActiveStyle = styleName;

        if (styleName == "Odia_Classical" || styleName == "Odia_Traditional") {
            mProfile = {
                "Odia_Classical",
                1.5f,  // eqLowGainDb (boost tabla and pakhawaj resonance)
                0.5f,  // eqMidGainDb
                -1.0f, // eqHighGainDb (smoother regional flutes)
                -15.0f,// compressionThresholdDb
                2.5f,  // compressionRatio (preserve organic acoustic peaks)
                1.8f,  // reverbDecaySec (warm temple room space)
                0.18f, // reverbMixLevel
                true,  // microtonalPitchGlide (enable raga meend/glides)
                true   // emphasizeTraditionalInstruments (sitar, bansuri, pakhawaj)
            };
        } else if (styleName == "Bollywood") {
            mProfile = {
                "Bollywood",
                2.0f,   // eqLowGainDb (punchy cinematic low end)
                -1.5f,  // eqMidGainDb (scooped mids for modern presence)
                2.5f,   // eqHighGainDb (sparkling brilliant vocals)
                -18.0f, // compressionThresholdDb
                4.5f,   // compressionRatio (tighter pop dynamics)
                2.4f,   // reverbDecaySec (lush large studio hall)
                0.22f,  // reverbMixLevel
                false,  // microtonalPitchGlide
                false
            };
        } else if (styleName == "Bhajan" || styleName == "Devotional") {
            mProfile = {
                "Bhajan",
                -1.0f,  // eqLowGainDb (tamed low end rumble)
                2.0f,   // eqMidGainDb (strong vocal intelligibility)
                1.0f,   // eqHighGainDb
                -10.0f, // compressionThresholdDb
                2.0f,   // compressionRatio (very natural, wide performance)
                3.2f,   // reverbDecaySec (massive temple hall echo)
                0.30f,  // reverbMixLevel (lush spiritual atmosphere)
                true,   // microtonalPitchGlide
                true
            };
        } else {
            // Standard / Pop Defaults
            mProfile = {
                "Default_Pop",
                0.0f, 0.0f, 0.0f,
                -12.0f, 3.5f,
                1.5f, 0.15f,
                false, false
            };
        }
    }

    StyleProfile GetActiveProfile() const { return mProfile; }
    std::string GetActiveStyleName() const { return mActiveStyle; }
};

} // namespace SurMaya
