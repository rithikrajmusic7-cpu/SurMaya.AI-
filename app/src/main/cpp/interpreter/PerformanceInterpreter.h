#pragma once
#include <vector>
#include <cmath>
#include <cstdlib>
#include <algorithm>
#include "../core/ProjectFormat.h"

namespace SurMaya {

class PerformanceInterpreter {
private:
    float mHumanizeAmount = 0.15f;
    float mTempoBpm = 105.0f;

public:
    PerformanceInterpreter() = default;

    void SetHumanizeFactors(float amount, float tempoBpm) {
        mHumanizeAmount = std::max(0.0f, std::min(1.0f, amount));
        mTempoBpm = std::max(20.0f, tempoBpm);
    }

    // Applies micro-timing offset and velocity humanization on raw MIDI notes
    void Humanize(std::vector<Note>& notes) {
        if (mHumanizeAmount <= 1e-4f) return;

        // Peak shift limits in ticks (approx 15ms maximum at typical ticks-per-quarter-note)
        float maxShiftTicks = 12.0f * mHumanizeAmount;

        for (auto& note : notes) {
            // Seeded pseudo-random timing shifts
            int shiftDirection = ((rand() % 100) > 50) ? 1 : -1;
            float shiftVal = static_cast<float>(rand() % 100) / 100.0f * maxShiftTicks * shiftDirection;
            
            // Adjust tick placement with human offset
            if (note.tick > static_cast<uint64_t>(std::abs(shiftVal))) {
                note.tick = static_cast<uint64_t>(note.tick + shiftVal);
            }

            // Humanize velocity with random accents
            int velVariance = static_cast<int>((rand() % 16 - 8) * mHumanizeAmount);
            int newVel = static_cast<int>(note.velocity) + velVariance;
            note.velocity = static_cast<uint8_t>(std::max(1, std::min(127, newVel)));
        }
    }

    // Introduces style-based accents (strong-beat and weak-beat human feel)
    void ApplyDynamicSyncopation(std::vector<Note>& notes, uint32_t ticksPerQuarter = 480) {
        for (auto& note : notes) {
            uint64_t beatIndex = note.tick / ticksPerQuarter;
            bool isDownbeat = (beatIndex % 4 == 0); // Beat 1 of a 4/4 bar
            
            if (isDownbeat) {
                // Emphasize downbeats
                note.velocity = static_cast<uint8_t>(std::min(127, static_cast<int>(note.velocity * 1.10f)));
            } else if (beatIndex % 2 != 0) {
                // Accentuate off-beats slightly for syncopated genres
                note.velocity = static_cast<uint8_t>(std::max(1, static_cast<int>(note.velocity * 0.95f)));
            }
        }
    }
};

} // namespace SurMaya
