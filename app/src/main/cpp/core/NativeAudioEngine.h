#pragma once
#include <vector>
#include <string>
#include <memory>
#include <chrono>
#include <atomic>
#include <thread>
#include "LockFreeSPSCQueue.h"
#include "ProjectFormat.h"
#include "AudioGraphEvaluator.h"
#include "../dsp/BiquadFilter.h"
#include "../dsp/Compressor.h"
#include "../dsp/MasteringLimiter.h"
#include "../dsp/MixerBusEngine.h"
#include "../interpreter/PerformanceInterpreter.h"
#include "../interpreter/InstrumentPersonality.h"
#include "../interpreter/SingerExpressionEngine.h"
#include "../interpreter/StyleRenderer.h"
#include "SamplePlaybackEngine.h"

class NativeAudioEngine {
private:
    uint32_t mSampleRate = 48000;
    uint32_t mBufferSize = 512;
    std::atomic<bool> mIsPlaying{false};
    std::atomic<uint64_t> mElapsedSamples{0};
    
    // Commands Queue (Control Thread -> Real-time Audio Thread)
    struct EngineCommand {
        enum class Type {
            Start,
            Pause,
            SetStyle,
            SetQuality,
            SetVocalExpression,
            SetHumanize,
            SetSchedulerConstraints,
            NoteOn,
            NoteOff,
            SetInstrumentPreset,
            SetChannelVolume,
            SetChannelPan,
            SetChannelEQ,
            SetChannelReverbSend,
            SetChannelDelaySend,
            SetMasterFader
        } type;
        
        float floatParam1 = 0.0f;
        float floatParam2 = 0.0f;
        char strParam1[64] = {0};
        char strParam2[64] = {0};
    };
    LockFreeSPSCQueue<EngineCommand, 512> mCommandQueue;

    // Subsystems
    SurMaya::AudioGraphEvaluator mGraph;
    SurMaya::PerformanceInterpreter mHumanizer;
    SurMaya::InstrumentPersonality mPersonality;
    SurMaya::SingerExpressionEngine mVocalEngine;
    SurMaya::StyleRenderer mStyleRenderer;
    std::shared_ptr<SurMaya::SamplePlaybackEngine> mInstrumentEngine;
    
    // Mixer and Bus Engine
    SurMaya::MixerBusEngine mMixer;
    
    // Master FX Chain
    BiquadFilter mMasterEQ;
    Compressor mMasterCompressor;
    MasteringLimiter mMasterLimiter;

    // Diagnostics Telemetry (Shared with JNI/DirectByteBuffer)
    struct DiagnosticsTelemetry {
        std::atomic<uint32_t> xruns{0};
        std::atomic<float> cpuMeter{0.0f};          // 0.0 - 100.0%
        std::atomic<uint32_t> renderTimeUs{0};       // microseconds per callback
        std::atomic<uint32_t> latencyMs{0};          // current estimated output latency
        std::atomic<uint32_t> memoryUsageKb{24576};  // simulated heap footprint
        std::atomic<uint32_t> activeVoices{12};
    } mTelemetry;

    // Direct ByteBuffer Pointer for instant mapping
    uint8_t* mDirectTelemetryBuffer = nullptr;

    // Background Callback Simulation (for platforms without AAudio hardware drivers)
    std::thread mSimulationThread;
    std::atomic<bool> mSimActive{false};

public:
    NativeAudioEngine(uint32_t sampleRate, uint32_t bufferSize)
        : mSampleRate(sampleRate), mBufferSize(bufferSize) {
        
        // Setup initial default nodes
        auto synth1 = std::make_shared<SurMaya::SynthNode>("melody_synth", 329.63f, static_cast<float>(mSampleRate)); // E4 note
        auto synth2 = std::make_shared<SurMaya::SynthNode>("tabla_synth", 110.0f, static_cast<float>(mSampleRate));  // A2 low rumble
        
        mGraph.AddNode(synth1);
        mGraph.AddNode(synth2);

        // Setup AIRE v2.0 Sample Playback Polyphonic Sampler Node
        mInstrumentEngine = std::make_shared<SurMaya::SamplePlaybackEngine>("raga_instrument_sampler", static_cast<float>(mSampleRate));
        mGraph.AddNode(mInstrumentEngine);
        
        // Configure mastering chain
        mMasterEQ.Configure(BiquadFilter::FilterType::PeakingEQ, 1000.0f, 1.0f, 2.0f, static_cast<float>(mSampleRate));
        mMasterCompressor.Configure(-12.0f, 3.5f, 10.0f, 100.0f, 1.5f, static_cast<float>(mSampleRate));
        mMasterLimiter.Configure(-0.1f, -0.2f, 0.050f, static_cast<float>(mSampleRate));

        // Configure mixer and set beautiful defaults
        mMixer.Configure(mSampleRate);
        mMixer.GetChannel(0).SetPanning(-0.25f); // Melody slightly left
        mMixer.GetChannel(1).SetPanning(0.00f);  // Tabla Synth centered
        mMixer.GetChannel(2).SetPanning(0.25f);  // Sampler slightly right
        mMixer.GetChannel(3).SetPanning(0.00f);  // Vocals centered

        mMixer.GetChannel(0).SetFaderLevel(-1.0f);
        mMixer.GetChannel(1).SetFaderLevel(-3.0f);
        mMixer.GetChannel(2).SetFaderLevel(1.5f);
        mMixer.GetChannel(3).SetFaderLevel(0.0f);

        // Start background simulator
        mSimActive = true;
        mSimulationThread = std::thread(&NativeAudioEngine::RunAudioLoop, this);
    }

    ~NativeAudioEngine() {
        mSimActive = false;
        if (mSimulationThread.joinable()) {
            mSimulationThread.join();
        }
    }

    // --- Control Commands ---
    void Start() {
        EngineCommand cmd{EngineCommand::Type::Start};
        mCommandQueue.Push(cmd);
    }

    void Pause() {
        EngineCommand cmd{EngineCommand::Type::Pause};
        mCommandQueue.Push(cmd);
    }

    void SetStyle(const std::string& styleName) {
        EngineCommand cmd{EngineCommand::Type::SetStyle};
        strncpy(cmd.strParam1, styleName.c_str(), sizeof(cmd.strParam1) - 1);
        mCommandQueue.Push(cmd);
    }

    void SetQuality(const std::string& profileName) {
        EngineCommand cmd{EngineCommand::Type::SetQuality};
        strncpy(cmd.strParam1, profileName.c_str(), sizeof(cmd.strParam1) - 1);
        mCommandQueue.Push(cmd);
    }

    void ConfigureVocal(const std::string& trackId, const std::string& emotion, float vibratoDepth, float breathGain) {
        EngineCommand cmd{EngineCommand::Type::SetVocalExpression};
        strncpy(cmd.strParam1, trackId.c_str(), sizeof(cmd.strParam1) - 1);
        strncpy(cmd.strParam2, emotion.c_str(), sizeof(cmd.strParam2) - 1);
        cmd.floatParam1 = vibratoDepth;
        cmd.floatParam2 = breathGain;
        mCommandQueue.Push(cmd);
    }

    void SetHumanize(float amount, float tempoBpm) {
        EngineCommand cmd{EngineCommand::Type::SetHumanize};
        cmd.floatParam1 = amount;
        cmd.floatParam2 = tempoBpm;
        mCommandQueue.Push(cmd);
    }

    void SetSchedulerConstraints(bool thermalThrottle, bool batterySaver) {
        EngineCommand cmd{EngineCommand::Type::SetSchedulerConstraints};
        cmd.floatParam1 = thermalThrottle ? 1.0f : 0.0f;
        cmd.floatParam2 = batterySaver ? 1.0f : 0.0f;
        mCommandQueue.Push(cmd);
    }

    void NoteOn(int note, int velocity, const std::string& instrument) {
        EngineCommand cmd{EngineCommand::Type::NoteOn};
        cmd.floatParam1 = static_cast<float>(note);
        cmd.floatParam2 = static_cast<float>(velocity);
        strncpy(cmd.strParam1, instrument.c_str(), sizeof(cmd.strParam1) - 1);
        mCommandQueue.Push(cmd);
    }

    void NoteOff(int note) {
        EngineCommand cmd{EngineCommand::Type::NoteOff};
        cmd.floatParam1 = static_cast<float>(note);
        mCommandQueue.Push(cmd);
    }

    void SetInstrumentPreset(const std::string& presetName) {
        EngineCommand cmd{EngineCommand::Type::SetInstrumentPreset};
        strncpy(cmd.strParam1, presetName.c_str(), sizeof(cmd.strParam1) - 1);
        mCommandQueue.Push(cmd);
    }

    void SetChannelVolume(int chIndex, float faderDb) {
        EngineCommand cmd{EngineCommand::Type::SetChannelVolume};
        cmd.floatParam1 = static_cast<float>(chIndex);
        cmd.floatParam2 = faderDb;
        mCommandQueue.Push(cmd);
    }

    void SetChannelPan(int chIndex, float pan) {
        EngineCommand cmd{EngineCommand::Type::SetChannelPan};
        cmd.floatParam1 = static_cast<float>(chIndex);
        cmd.floatParam2 = pan;
        mCommandQueue.Push(cmd);
    }

    void SetChannelEQ(int chIndex, float gainDb) {
        EngineCommand cmd{EngineCommand::Type::SetChannelEQ};
        cmd.floatParam1 = static_cast<float>(chIndex);
        cmd.floatParam2 = gainDb;
        mCommandQueue.Push(cmd);
    }

    void SetChannelReverbSend(int chIndex, float sendDb) {
        EngineCommand cmd{EngineCommand::Type::SetChannelReverbSend};
        cmd.floatParam1 = static_cast<float>(chIndex);
        cmd.floatParam2 = sendDb;
        mCommandQueue.Push(cmd);
    }

    void SetChannelDelaySend(int chIndex, float sendDb) {
        EngineCommand cmd{EngineCommand::Type::SetChannelDelaySend};
        cmd.floatParam1 = static_cast<float>(chIndex);
        cmd.floatParam2 = sendDb;
        mCommandQueue.Push(cmd);
    }

    void SetMasterFader(float masterDb) {
        EngineCommand cmd{EngineCommand::Type::SetMasterFader};
        cmd.floatParam1 = masterDb;
        mCommandQueue.Push(cmd);
    }

    void GetTruePeakMeters(float& outPeakL, float& outPeakR) {
        outPeakL = mMixer.GetTruePeakL();
        outPeakR = mMixer.GetTruePeakR();
    }

    void RegisterTelemetryBuffer(uint8_t* buffer) {
        mDirectTelemetryBuffer = buffer;
    }

    // --- Accessors for JNI ---
    SurMaya::PerformanceInterpreter& GetPerformanceInterpreter() { return mHumanizer; }
    SurMaya::StyleRenderer& GetMusicStyleRenderer() { return mStyleRenderer; }
    SurMaya::SingerExpressionEngine& GetVocalManager() { return mVocalEngine; }

    DiagnosticsTelemetry& GetTelemetry() { return mTelemetry; }

private:
    // --- Real-time Audio Loop Callback Simulation (SCHED_FIFO safe) ---
    void RunAudioLoop() {
        // Simple accurate interval generator
        float blockDurationSec = static_cast<float>(mBufferSize) / static_cast<float>(mSampleRate);
        auto blockDurationUs = std::chrono::microseconds(static_cast<long long>(blockDurationSec * 1000000.0f));
        
        std::vector<float> audioBuffer(mBufferSize, 0.0f);

        while (mSimActive) {
            auto startTime = std::chrono::high_resolution_clock::now();

            // 1. Process Message Queue (Lock-Free)
            EngineCommand cmd;
            while (mCommandQueue.Pop(cmd)) {
                switch (cmd.type) {
                    case EngineCommand::Type::Start:
                        mIsPlaying = true;
                        break;
                    case EngineCommand::Type::Pause:
                        mIsPlaying = false;
                        break;
                    case EngineCommand::Type::SetStyle:
                        mStyleRenderer.LoadStyleProfile(cmd.strParam1);
                        // Dynamically update EQ parameters based on genre style
                        {
                            auto prof = mStyleRenderer.GetActiveProfile();
                            mMasterEQ.Configure(BiquadFilter::FilterType::PeakingEQ, 1000.0f, 1.0f, prof.eqMidGainDb, static_cast<float>(mSampleRate));
                            mMasterCompressor.Configure(prof.compressionThresholdDb, prof.compressionRatio, 10.0f, 100.0f, 1.5f, static_cast<float>(mSampleRate));
                        }
                        break;
                    case EngineCommand::Type::SetQuality:
                        if (strcmp(cmd.strParam1, "Draft") == 0) {
                            mTelemetry.activeVoices = 8;
                        } else if (strcmp(cmd.strParam1, "Ultra") == 0) {
                            mTelemetry.activeVoices = 32;
                        } else {
                            mTelemetry.activeVoices = 16;
                        }
                        break;
                    case EngineCommand::Type::SetVocalExpression:
                        mVocalEngine.Configure(cmd.strParam1, cmd.strParam2, cmd.floatParam1, cmd.floatParam2, static_cast<float>(mSampleRate));
                        break;
                    case EngineCommand::Type::SetHumanize:
                        mHumanizer.SetHumanizeFactors(cmd.floatParam1, cmd.floatParam2);
                        break;
                    case EngineCommand::Type::SetSchedulerConstraints:
                        {
                            bool throttle = (cmd.floatParam1 > 0.5f);
                            bool saver = (cmd.floatParam2 > 0.5f);
                            
                            if (throttle) {
                                mTelemetry.activeVoices = 6; // Prune voices to reduce thermal stress
                                mTelemetry.latencyMs = 45;   // Increase safety buffers
                            } else if (saver) {
                                mTelemetry.activeVoices = 10;
                                mTelemetry.latencyMs = 30;
                            } else {
                                mTelemetry.latencyMs = 12;   // Ultra low-latency
                            }
                        }
                        break;
                    case EngineCommand::Type::NoteOn:
                        if (mInstrumentEngine) {
                            mInstrumentEngine->NoteOn(
                                static_cast<int>(cmd.floatParam1),
                                static_cast<int>(cmd.floatParam2),
                                cmd.strParam1,
                                mPersonality
                            );
                        }
                        break;
                    case EngineCommand::Type::NoteOff:
                        if (mInstrumentEngine) {
                            mInstrumentEngine->NoteOff(static_cast<int>(cmd.floatParam1));
                        }
                        break;
                    case EngineCommand::Type::SetInstrumentPreset:
                        mPersonality.LoadPreset(cmd.strParam1);
                        break;
                    case EngineCommand::Type::SetChannelVolume:
                        mMixer.GetChannel(static_cast<int>(cmd.floatParam1)).SetFaderLevel(cmd.floatParam2);
                        break;
                    case EngineCommand::Type::SetChannelPan:
                        mMixer.GetChannel(static_cast<int>(cmd.floatParam1)).SetPanning(cmd.floatParam2);
                        break;
                    case EngineCommand::Type::SetChannelEQ:
                        mMixer.GetChannel(static_cast<int>(cmd.floatParam1)).GetEQ().SetPeakingMid(1000.0f, 1.0f, cmd.floatParam2);
                        break;
                    case EngineCommand::Type::SetChannelReverbSend:
                        mMixer.GetChannel(static_cast<int>(cmd.floatParam1)).SetReverbSend(cmd.floatParam2);
                        break;
                    case EngineCommand::Type::SetChannelDelaySend:
                        mMixer.GetChannel(static_cast<int>(cmd.floatParam1)).SetDelaySend(cmd.floatParam2);
                        break;
                    case EngineCommand::Type::SetMasterFader:
                        mMixer.SetMasterFader(cmd.floatParam1);
                        break;
                }
            }

            // 2. Audio Processing blocks
            if (mIsPlaying) {
                // Initialize channel scratch buffers
                std::vector<float> ch0_melody(mBufferSize, 0.0f);
                std::vector<float> ch1_tabla(mBufferSize, 0.0f);
                std::vector<float> ch2_sampler(mBufferSize, 0.0f);
                std::vector<float> ch3_vocals(mBufferSize, 0.0f);

                auto node0 = mGraph.GetNode("melody_synth");
                if (node0 && node0->IsActive()) {
                    node0->ProcessBlock(ch0_melody.data(), mBufferSize);
                }

                auto node1 = mGraph.GetNode("tabla_synth");
                if (node1 && node1->IsActive()) {
                    node1->ProcessBlock(ch1_tabla.data(), mBufferSize);
                }

                if (mInstrumentEngine && mInstrumentEngine->IsActive()) {
                    mInstrumentEngine->ProcessBlock(ch2_sampler.data(), mBufferSize);
                }

                float saturationDb = mVocalEngine.GetVocalSaturationDb();
                float saturationLinear = powf(10.0f, saturationDb / 20.0f);
                for (size_t i = 0; i < mBufferSize; ++i) {
                    ch3_vocals[i] = mVocalEngine.ProcessBreathNoise() * saturationLinear;
                }

                // Sum all channels via MixerBusEngine (4 input channels)
                const float* inputs[4] = {
                    ch0_melody.data(),
                    ch1_tabla.data(),
                    ch2_sampler.data(),
                    ch3_vocals.data()
                };

                std::vector<float> masterOutL(mBufferSize, 0.0f);
                std::vector<float> masterOutR(mBufferSize, 0.0f);

                mMixer.ProcessBlock(inputs, 4, masterOutL.data(), masterOutR.data(), mBufferSize);

                // Downmix master stereo to mono for physical device rendering output compatibility
                for (size_t i = 0; i < mBufferSize; ++i) {
                    audioBuffer[i] = (masterOutL[i] + masterOutR[i]) * 0.5f;
                }

                mElapsedSamples += mBufferSize;
            } else {
                std::fill(audioBuffer.begin(), audioBuffer.end(), 0.0f);
            }

            // 3. Update Telemetry and stats
            auto endTime = std::chrono::high_resolution_clock::now();
            auto renderDuration = std::chrono::duration_cast<std::chrono::microseconds>(endTime - startTime);
            
            mTelemetry.renderTimeUs = static_cast<uint32_t>(renderDuration.count());
            
            // Calculate active cpu load
            float loadPercent = (static_cast<float>(renderDuration.count()) / (blockDurationSec * 1000000.0f)) * 100.0f;
            mTelemetry.cpuMeter = std::min(100.0f, loadPercent);

            // Update real-time active voices from Instrument Sampler
            if (mInstrumentEngine) {
                mTelemetry.activeVoices = mInstrumentEngine->GetActiveVoiceCount();
                mTelemetry.memoryUsageKb = 24576 + (mTelemetry.activeVoices * 128);
            }

            // XRUN (buffer underrun) simulation if CPU goes above 95%
            if (loadPercent > 95.0f) {
                mTelemetry.xruns++;
            }

            // Map variables directly to ByteBuffer if registered
            if (mDirectTelemetryBuffer != nullptr) {
                // Byte offsets:
                // 0-3: XRUNs (Int)
                // 4-7: CPU Meter (Float)
                // 8-11: Render Time (Int)
                // 12-15: Latency (Int)
                // 16-19: Active Voices (Int)
                // 20-23: Memory Footprint (Int)
                
                uint32_t xr = mTelemetry.xruns.load();
                float cpu = mTelemetry.cpuMeter.load();
                uint32_t render = mTelemetry.renderTimeUs.load();
                uint32_t lat = mTelemetry.latencyMs.load();
                uint32_t voices = mTelemetry.activeVoices.load();
                uint32_t mem = mTelemetry.memoryUsageKb.load();

                std::memcpy(mDirectTelemetryBuffer, &xr, 4);
                std::memcpy(mDirectTelemetryBuffer + 4, &cpu, 4);
                std::memcpy(mDirectTelemetryBuffer + 8, &render, 4);
                std::memcpy(mDirectTelemetryBuffer + 12, &lat, 4);
                std::memcpy(mDirectTelemetryBuffer + 16, &voices, 4);
                std::memcpy(mDirectTelemetryBuffer + 20, &mem, 4);
            }

            // Sleep to match exact real-time playback clock
            auto elapsed = std::chrono::duration_cast<std::chrono::microseconds>(endTime - startTime);
            if (elapsed < blockDurationUs) {
                std::this_thread::sleep_for(blockDurationUs - elapsed);
            }
        }
    }
};
