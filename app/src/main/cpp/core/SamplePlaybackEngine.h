#pragma once
#include <vector>
#include <string>
#include <memory>
#include <cmath>
#include <algorithm>
#include <unordered_map>
#include <atomic>
#include <mutex>
#include <cmath>
#include "AudioGraphEvaluator.h"
#include "../interpreter/InstrumentPersonality.h"

namespace SurMaya {

// High-fidelity PCM Sample data container
struct SampleData {
    std::string instrument;
    uint32_t rootPitch = 60; // MIDI Note
    int velocityMin = 0;
    int velocityMax = 127;
    int roundRobinIndex = 0;
    float sampleRate = 48000.0f;
    std::vector<float> pcmBuffer;
};

// Cache-aware manager for pre-loading, storing, and accessing samples without real-time allocations
class CacheAwareSampleManager {
private:
    std::vector<SampleData> mSampleCache;
    std::mutex mCacheMutex;

public:
    static CacheAwareSampleManager& GetInstance() {
        static CacheAwareSampleManager instance;
        return instance;
    }

    CacheAwareSampleManager() {
        // Pre-generate rich multi-sampled virtual instruments on startup
        PreGenerateVirtualRagaInstruments();
    }

    const SampleData* FindSample(const std::string& instrument, int midiNote, int velocity, int roundRobin) {
        std::lock_guard<std::mutex> lock(mCacheMutex);
        
        const SampleData* bestMatch = nullptr;
        int closestPitchDiff = 999;

        for (const auto& sample : mSampleCache) {
            if (sample.instrument == instrument) {
                // Check velocity layer matching
                if (velocity >= sample.velocityMin && velocity <= sample.velocityMax) {
                    // Check round robin match
                    if (sample.roundRobinIndex == roundRobin) {
                        int diff = std::abs(static_cast<int>(sample.rootPitch) - midiNote);
                        if (diff < closestPitchDiff) {
                            closestPitchDiff = diff;
                            bestMatch = &sample;
                        }
                    }
                }
            }
        }

        // Fallback to any round robin or velocity if strict matching fails
        if (!bestMatch) {
            for (const auto& sample : mSampleCache) {
                if (sample.instrument == instrument) {
                    int diff = std::abs(static_cast<int>(sample.rootPitch) - midiNote);
                    if (diff < closestPitchDiff) {
                        closestPitchDiff = diff;
                        bestMatch = &sample;
                    }
                }
            }
        }

        return bestMatch;
    }

private:
    // Generate organic virtual raga instrument samples mathematically to guarantee zero latency and no file I/O
    void PreGenerateVirtualRagaInstruments() {
        std::lock_guard<std::mutex> lock(mCacheMutex);
        mSampleCache.clear();

        // Instruments list
        std::vector<std::string> instruments = { "Sitar", "Santoor", "Bansuri_Flute", "Raga_Violin", "Tabla" };
        
        // Let's generate multiple pitch roots per instrument (e.g., C3, G3, C4, G4, C5)
        std::vector<uint32_t> rootPitches = { 48, 55, 60, 67, 72 };
        
        // 2 Velocity Layers (Soft/Hard) and 2 Round Robin cycles
        for (const auto& inst : instruments) {
            for (auto root : rootPitches) {
                for (int velLayer = 0; velLayer < 2; ++velLayer) {
                    int velMin = (velLayer == 0) ? 0 : 64;
                    int velMax = (velLayer == 0) ? 63 : 127;
                    
                    for (int rr = 0; rr < 2; ++rr) {
                        SampleData sample;
                        sample.instrument = inst;
                        sample.rootPitch = root;
                        sample.velocityMin = velMin;
                        sample.velocityMax = velMax;
                        sample.roundRobinIndex = rr;
                        sample.sampleRate = 48000.0f;
                        
                        // Generate PCM: 1.5 seconds per sample at 48kHz
                        size_t numSamples = static_cast<size_t>(48000 * 1.5);
                        sample.pcmBuffer.resize(numSamples, 0.0f);
                        
                        float rootFreq = 440.0f * powf(2.0f, (static_cast<float>(root) - 69.0f) / 12.0f);
                        float velocityScale = (velLayer == 0) ? 0.45f : 0.85f;
                        
                        // Unique Acoustic synthesis for each raga instrument
                        for (size_t t = 0; t < numSamples; ++t) {
                            float timeSec = static_cast<float>(t) / 48000.0f;
                            float val = 0.0f;
                            
                            if (inst == "Sitar") {
                                // Sitar: Sharp metallic pluck with long sympathetic string buzz
                                float decay = expf(-timeSec * 3.5f);
                                float sympatheticBuzz = sinf(2.0f * M_PI * rootFreq * 1.005f * timeSec) * 0.12f * expf(-timeSec * 0.8f);
                                
                                val = sinf(2.0f * M_PI * rootFreq * timeSec) * 0.6f +
                                      sinf(2.0f * M_PI * rootFreq * 2.0f * timeSec) * 0.3f * expf(-timeSec * 6.0f) + // Pluck brightness
                                      sinf(2.0f * M_PI * rootFreq * 3.0f * timeSec) * 0.15f * expf(-timeSec * 8.0f) +
                                      sympatheticBuzz;
                                
                                val *= decay;
                            } 
                            else if (inst == "Santoor") {
                                // Santoor: Brilliant mallet strike, ringing resonance, multi-string unison
                                float decay = expf(-timeSec * 2.2f);
                                // Unison detuning (chorus-like)
                                val = sinf(2.0f * M_PI * rootFreq * 0.999f * timeSec) * 0.4f +
                                      sinf(2.0f * M_PI * rootFreq * 1.001f * timeSec) * 0.4f +
                                      sinf(2.0f * M_PI * rootFreq * 3.0f * timeSec) * 0.2f * expf(-timeSec * 12.0f); // High hammer chime
                                
                                val *= decay;
                            } 
                            else if (inst == "Bansuri_Flute") {
                                // Bansuri Flute: Breathy, soft attack, gentle organic vibrato, rich 2nd harmonic
                                float attack = std::min(1.0f, timeSec / 0.08f);
                                float decay = expf(-timeSec * 0.5f); // Sustained until released
                                
                                // Organic vibrato at 6Hz
                                float vibrato = 1.0f + 0.015f * sinf(2.0f * M_PI * 6.0f * timeSec);
                                float freqMod = rootFreq * vibrato;
                                
                                // Random breath noise simulation
                                float breathNoise = ((rand() % 1000) / 500.0f - 1.0f) * 0.06f;
                                
                                val = (sinf(2.0f * M_PI * freqMod * timeSec) * 0.7f +
                                       sinf(2.0f * M_PI * freqMod * 2.0f * timeSec) * 0.2f) * attack + breathNoise * (1.0f - attack * 0.5f);
                                
                                val *= decay;
                            } 
                            else if (inst == "Raga_Violin") {
                                // Raga Violin: Sawtooth-like bowed timber, slow swell
                                float attack = std::min(1.0f, timeSec / 0.12f);
                                float decay = expf(-timeSec * 0.3f);
                                float vibrato = 1.0f + 0.022f * sinf(2.0f * M_PI * 5.5f * timeSec);
                                float freqMod = rootFreq * vibrato;

                                // Sum of odd and even harmonics to shape bow friction
                                val = sinf(2.0f * M_PI * freqMod * timeSec) * 0.5f +
                                      sinf(2.0f * M_PI * freqMod * 2.0f * timeSec) * 0.3f +
                                      sinf(2.0f * M_PI * freqMod * 3.0f * timeSec) * 0.15f;
                                
                                val *= (attack * decay);
                            } 
                            else { // "Tabla"
                                // Tabla: Deep resonant bayan slide or high ringing dayan stroke
                                if (root < 54) { // Bayan (Bass) - Pitch slide downwards
                                    float slideFreq = rootFreq * (1.0f + 0.25f * expf(-timeSec * 15.0f));
                                    float decay = expf(-timeSec * 4.0f);
                                    val = sinf(2.0f * M_PI * slideFreq * timeSec) * decay;
                                } else { // Dayan (High ring)
                                    float decay = expf(-timeSec * 2.5f);
                                    val = sinf(2.0f * M_PI * rootFreq * timeSec) * 0.5f * decay +
                                          sinf(2.0f * M_PI * rootFreq * 2.89f * timeSec) * 0.2f * expf(-timeSec * 10.0f); // Harmonic chime
                                }
                            }
                            
                            // Apply slight variation for round robin cycles
                            if (rr == 1) {
                                val *= (0.95f + 0.05f * sinf(100.0f * timeSec));
                            }
                            
                            sample.pcmBuffer[t] = val * velocityScale * 0.35f;
                        }
                        
                        mSampleCache.push_back(std::move(sample));
                    }
                }
            }
        }
    }
};

// Voice structure representation representing an active sampler voice
struct Voice {
    bool active = false;
    uint32_t id = 0;
    int noteNumber = 0;
    float velocity = 0.0f;
    double currentPlaybackIndex = 0.0;
    double pitchRatio = 1.0f;
    const SampleData* sample = nullptr;
    
    // ADSR State Machine
    enum class EnvStage { Off, Attack, Decay, Sustain, Release } envStage = EnvStage::Off;
    float envCurrentGain = 0.0f;
    uint64_t envSampleCounter = 0;
    uint64_t envSamplesTotalInStage = 0;
    
    ADSRConfig adsr;
    float lastReleaseGain = 0.0f;
    uint64_t age = 0; // Tracking for voice-stealing

    void TriggerNoteOn(uint32_t voiceId, int note, int vel, const SampleData* sampleData, const ADSRConfig& config, float sampleRate) {
        id = voiceId;
        noteNumber = note;
        velocity = static_cast<float>(vel) / 127.0f;
        sample = sampleData;
        currentPlaybackIndex = 0.0;
        active = true;
        adsr = config;
        age = 0;

        // Calculate pitch transposition ratio
        if (sample) {
            float rootFreq = 440.0f * powf(2.0f, (static_cast<float>(sample->rootPitch) - 69.0f) / 12.0f);
            float targetFreq = 440.0f * powf(2.0f, (static_cast<float>(note) - 69.0f) / 12.0f);
            pitchRatio = targetFreq / rootFreq;
        } else {
            pitchRatio = 1.0f;
        }

        // Start Envelope at Attack Stage
        envStage = EnvStage::Attack;
        envCurrentGain = 0.0f;
        envSampleCounter = 0;
        envSamplesTotalInStage = static_cast<uint64_t>(adsr.attackSec * sampleRate);
    }

    void TriggerNoteOff(float sampleRate) {
        if (!active || envStage == EnvStage::Release) return;
        
        envStage = EnvStage::Release;
        lastReleaseGain = envCurrentGain;
        envSampleCounter = 0;
        envSamplesTotalInStage = static_cast<uint64_t>(adsr.releaseSec * sampleRate);
    }

    float EvaluateEnvelope(float sampleRate) {
        if (!active) return 0.0f;

        envSampleCounter++;

        switch (envStage) {
            case EnvStage::Attack: {
                if (envSamplesTotalInStage == 0) {
                    envCurrentGain = 1.0f;
                    TransitionToDecay(sampleRate);
                } else {
                    envCurrentGain = static_cast<float>(envSampleCounter) / static_cast<float>(envSamplesTotalInStage);
                    if (envSampleCounter >= envSamplesTotalInStage) {
                        envCurrentGain = 1.0f;
                        TransitionToDecay(sampleRate);
                    }
                }
                break;
            }
            case EnvStage::Decay: {
                if (envSamplesTotalInStage == 0) {
                    envCurrentGain = adsr.sustainLevel;
                    envStage = EnvStage::Sustain;
                } else {
                    float ratio = static_cast<float>(envSampleCounter) / static_cast<float>(envSamplesTotalInStage);
                    envCurrentGain = 1.0f - (1.0f - adsr.sustainLevel) * ratio;
                    if (envSampleCounter >= envSamplesTotalInStage) {
                        envCurrentGain = adsr.sustainLevel;
                        envStage = EnvStage::Sustain;
                    }
                }
                break;
            }
            case EnvStage::Sustain: {
                envCurrentGain = adsr.sustainLevel;
                break;
            }
            case EnvStage::Release: {
                if (envSamplesTotalInStage == 0) {
                    envCurrentGain = 0.0f;
                    active = false;
                    envStage = EnvStage::Off;
                } else {
                    float ratio = static_cast<float>(envSampleCounter) / static_cast<float>(envSamplesTotalInStage);
                    envCurrentGain = lastReleaseGain * (1.0f - ratio);
                    if (envSampleCounter >= envSamplesTotalInStage) {
                        envCurrentGain = 0.0f;
                        active = false;
                        envStage = EnvStage::Off;
                    }
                }
                break;
            }
            default:
                envCurrentGain = 0.0f;
                active = false;
                break;
        }

        return envCurrentGain;
    }

private:
    void TransitionToDecay(float sampleRate) {
        envStage = EnvStage::Decay;
        envSampleCounter = 0;
        envSamplesTotalInStage = static_cast<uint64_t>(adsr.decaySec * sampleRate);
    }
};

// Polyphonic voice allocation manager & synthesizer engine node
class SamplePlaybackEngine : public AudioNode {
private:
    static constexpr int MAX_VOICES = 32;
    Voice mVoices[MAX_VOICES];
    std::atomic<uint32_t> mVoiceIdCounter{1};
    float mSampleRate = 48000.0f;
    
    // Cycle alternates for raga round robin sequence
    std::unordered_map<std::string, int> mInstrumentRoundRobinCycle;
    std::mutex mEngineMutex;

public:
    SamplePlaybackEngine(std::string id, float sampleRate) 
        : AudioNode(std::move(id)), mSampleRate(sampleRate) {
        // Voice array initialization
        for (int i = 0; i < MAX_VOICES; ++i) {
            mVoices[i].active = false;
        }
    }

    void NoteOn(int note, int velocity, const std::string& instrument, const InstrumentPersonality& personality) {
        std::lock_guard<std::mutex> lock(mEngineMutex);

        // Calculate Round Robin Index
        int rrIndex = mInstrumentRoundRobinCycle[instrument];
        mInstrumentRoundRobinCycle[instrument] = (rrIndex + 1) % 2;

        const SampleData* sample = CacheAwareSampleManager::GetInstance().FindSample(instrument, note, velocity, rrIndex);
        if (!sample) return;

        // Find available voice slot or steal the oldest active voice
        int slot = -1;
        uint64_t oldestAge = 0;
        int oldestSlot = -1;
        
        for (int i = 0; i < MAX_VOICES; ++i) {
            if (!mVoices[i].active) {
                slot = i;
                break;
            }
            if (mVoices[i].age > oldestAge) {
                oldestAge = mVoices[i].age;
                oldestSlot = i;
            }
        }

        // Voice Stealing System
        if (slot == -1) {
            slot = oldestSlot;
        }

        if (slot >= 0 && slot < MAX_VOICES) {
            uint32_t nextId = mVoiceIdCounter.fetch_add(1);
            mVoices[slot].TriggerNoteOn(nextId, note, velocity, sample, personality.GetEnvelope(), mSampleRate);
        }
    }

    void NoteOff(int note) {
        std::lock_guard<std::mutex> lock(mEngineMutex);
        for (int i = 0; i < MAX_VOICES; ++i) {
            if (mVoices[i].active && mVoices[i].noteNumber == note) {
                mVoices[i].TriggerNoteOff(mSampleRate);
            }
        }
    }

    void StopAllNotes() {
        std::lock_guard<std::mutex> lock(mEngineMutex);
        for (int i = 0; i < MAX_VOICES; ++i) {
            if (mVoices[i].active) {
                mVoices[i].TriggerNoteOff(mSampleRate);
            }
        }
    }

    int GetActiveVoiceCount() {
        int count = 0;
        for (int i = 0; i < MAX_VOICES; ++i) {
            if (mVoices[i].active) {
                count++;
            }
        }
        return count;
    }

    // Process real-time block, calculating voice mixing & linear interpolation
    void ProcessBlock(float* outputBuffer, size_t numSamples) override {
        std::fill(outputBuffer, outputBuffer + numSamples, 0.0f);
        
        std::lock_guard<std::mutex> lock(mEngineMutex);

        for (int v = 0; v < MAX_VOICES; ++v) {
            Voice& voice = mVoices[v];
            if (!voice.active) continue;

            voice.age += numSamples;

            for (size_t s = 0; s < numSamples; ++s) {
                if (!voice.sample || voice.currentPlaybackIndex >= voice.sample->pcmBuffer.size() - 1) {
                    voice.active = false;
                    break;
                }

                // Linear Interpolation for Sample Playback Pitch shifting
                double idx = voice.currentPlaybackIndex;
                size_t idx0 = static_cast<size_t>(idx);
                size_t idx1 = idx0 + 1;
                
                float val0 = voice.sample->pcmBuffer[idx0];
                float val1 = (idx1 < voice.sample->pcmBuffer.size()) ? voice.sample->pcmBuffer[idx1] : 0.0f;
                float frac = static_cast<float>(idx - idx0);
                
                // Interpolated value
                float interpolatedSample = val0 + frac * (val1 - val0);
                
                // Evaluate and apply ADSR gain envelope
                float envelopeGain = voice.EvaluateEnvelope(mSampleRate);
                
                // Accumulate to output buffer with stereo/mono scale
                outputBuffer[s] += interpolatedSample * envelopeGain * voice.velocity;
                
                // Advance playback pointer based on pitch transposition factor
                voice.currentPlaybackIndex += voice.pitchRatio;
            }
        }
    }
};

} // namespace SurMaya
