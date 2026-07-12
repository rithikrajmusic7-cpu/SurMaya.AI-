#pragma once
#include <vector>
#include <string>
#include <memory>
#include <algorithm>
#include <queue>
#include <unordered_map>

namespace SurMaya {

// Abstract representation of an Audio Node in the DAG
class AudioNode {
protected:
    std::string mNodeId;
    bool mIsActive = true;

public:
    explicit AudioNode(std::string id) : mNodeId(std::move(id)) {}
    virtual ~AudioNode() = default;

    std::string GetId() const { return mNodeId; }
    void SetActive(bool active) { mIsActive = active; }
    bool IsActive() const { return mIsActive; }

    // Computes buffer blocks (Real-Time callback function)
    virtual void ProcessBlock(float* outputBuffer, size_t numSamples) = 0;
};

// Concrete Synthesizer Node (Generates Sitar/Tabla Waveforms)
class SynthNode : public AudioNode {
private:
    float mFrequency = 220.0f;
    float mPhase = 0.0f;
    float mSampleRate = 48000.0f;

public:
    SynthNode(std::string id, float freq, float sampleRate) 
        : AudioNode(std::move(id)), mFrequency(freq), mSampleRate(sampleRate) {}

    void ProcessBlock(float* outputBuffer, size_t numSamples) override {
        if (!mIsActive) {
            std::fill(outputBuffer, outputBuffer + numSamples, 0.0f);
            return;
        }

        // Sine wave sample generator
        for (size_t i = 0; i < numSamples; ++i) {
            outputBuffer[i] = sinf(mPhase) * 0.25f;
            mPhase += (2.0f * M_PI * mFrequency) / mSampleRate;
            if (mPhase >= 2.0f * M_PI) {
                mPhase -= 2.0f * M_PI;
            }
        }
    }
};

// Topological Router and Evaluator
class AudioGraphEvaluator {
private:
    std::unordered_map<std::string, std::shared_ptr<AudioNode>> mNodes;
    std::unordered_map<std::string, std::vector<std::string>> mAdjacencyList;
    std::vector<std::string> mSortedNodeIds;
    bool mNeedsSorting = true;

public:
    AudioGraphEvaluator() = default;

    void AddNode(const std::shared_ptr<AudioNode>& node) {
        mNodes[node->GetId()] = node;
        mNeedsSorting = true;
    }

    std::shared_ptr<AudioNode> GetNode(const std::string& nodeId) {
        auto it = mNodes.find(nodeId);
        if (it != mNodes.end()) {
            return it->second;
        }
        return nullptr;
    }

    void AddEdge(const std::string& fromNodeId, const std::string& toNodeId) {
        mAdjacencyList[fromNodeId].push_back(toNodeId);
        mNeedsSorting = true;
    }

    void Clear() {
        mNodes.clear();
        mAdjacencyList.clear();
        mSortedNodeIds.clear();
        mNeedsSorting = true;
    }

    // Solves DAG using Kahn's topological sort algorithm
    void RebuildEvaluationRoute() {
        if (!mNeedsSorting) return;

        std::unordered_map<std::string, int> inDegree;
        for (auto const& pair : mNodes) {
            inDegree[pair.first] = 0;
        }

        for (auto const& pair : mAdjacencyList) {
            for (auto const& target : pair.second) {
                inDegree[target]++;
            }
        }

        std::queue<std::string> zeroDegreeQueue;
        for (auto const& pair : inDegree) {
            if (pair.second == 0) {
                zeroDegreeQueue.push(pair.first);
            }
        }

        mSortedNodeIds.clear();
        while (!zeroDegreeQueue.empty()) {
            std::string current = zeroDegreeQueue.front();
            zeroDegreeQueue.pop();
            mSortedNodeIds.push_back(current);

            auto neighbors = mAdjacencyList[current];
            for (auto const& neighbor : neighbors) {
                inDegree[neighbor]--;
                if (inDegree[neighbor] == 0) {
                    zeroDegreeQueue.push(neighbor);
                }
            }
        }

        mNeedsSorting = false;
    }

    // Processes all active nodes in sorted order, writing to output
    void EvaluateBlock(float* outCombinedBuffer, size_t numSamples) {
        RebuildEvaluationRoute();

        // Local scratch buffer
        std::vector<float> scratchBuffer(numSamples, 0.0f);
        std::fill(outCombinedBuffer, outCombinedBuffer + numSamples, 0.0f);

        for (const auto& nodeId : mSortedNodeIds) {
            auto node = mNodes[nodeId];
            if (node && node->IsActive()) {
                node->ProcessBlock(scratchBuffer.data(), numSamples);
                
                // Sum nodes outputs (mixing matrix)
                for (size_t i = 0; i < numSamples; ++i) {
                    outCombinedBuffer[i] += scratchBuffer[i];
                }
            }
        }
    }
};

} // namespace SurMaya
