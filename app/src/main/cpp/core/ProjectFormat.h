#pragma once
#include <string>
#include <vector>
#include <cstdint>

namespace SurMaya {

struct ProjectMetadata {
    std::string title;
    std::string artist;
    uint64_t createdAt;
};

struct GlobalSettings {
    uint32_t targetSampleRate = 48000;
    std::string qualityProfile = "Studio";
    std::string styleProfile = "Odia_Classical";
};

struct TempoMapEntry {
    uint64_t tick;
    float bpm;
    std::string timeSignature;
};

struct Note {
    uint64_t tick;
    uint32_t duration;
    uint8_t noteNumber;
    uint8_t velocity;
    std::string phonemes;
};

enum class TrackType {
    Vocal,
    Instrument,
    Percussion
};

struct ChannelSettings {
    float volume = 0.8f;
    float pan = 0.5f; // 0.0 = Left, 1.0 = Right
    float eqLowGainDb = 0.0f;
    float eqMidGainDb = 0.0f;
    float eqHighGainDb = 0.0f;
};

struct Track {
    std::string trackId;
    std::string name;
    TrackType type = TrackType::Instrument;
    std::string vocalModelId;
    std::string emotionState = "Normal";
    float vibratoDepth = 0.35f;
    float breathGain = 0.1f;
    std::string instrumentPreset;
    std::string soundfontAsset;
    ChannelSettings channel;
    std::vector<Note> notes;
};

struct SongBlueprint {
    std::string projectId;
    ProjectMetadata metadata;
    GlobalSettings settings;
    std::vector<TempoMapEntry> tempoMap;
    std::vector<Track> tracks;
};

} // namespace SurMaya
