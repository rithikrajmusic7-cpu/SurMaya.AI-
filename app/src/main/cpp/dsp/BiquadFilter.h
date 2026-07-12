#pragma once
#include <cmath>

class BiquadFilter {
public:
    enum class FilterType {
        LowPass,
        HighPass,
        PeakingEQ,
        LowShelf,
        HighShelf
    };

private:
    float mType = 0.0f;
    float mSampleRate = 48000.0f;
    
    // Coefficients
    float b0 = 1.0f, b1 = 0.0f, b2 = 0.0f;
    float a1 = 0.0f, a2 = 0.0f;
    
    // Delay lines (history)
    float x1 = 0.0f, x2 = 0.0f;
    float y1 = 0.0f, y2 = 0.0f;

public:
    BiquadFilter() = default;

    void Configure(FilterType type, float frequencyHz, float Q, float gainDb, float sampleRate) {
        mSampleRate = sampleRate;
        float w0 = 2.0f * M_PI * frequencyHz / mSampleRate;
        float alpha = sinf(w0) / (2.0f * Q);
        float A = powf(10.0f, gainDb / 40.0f);
        
        float a0 = 1.0f;

        switch (type) {
            case FilterType::LowPass:
                b0 = (1.0f - cosf(w0)) / 2.0f;
                b1 = 1.0f - cosf(w0);
                b2 = (1.0f - cosf(w0)) / 2.0f;
                a0 = 1.0f + alpha;
                a1 = -2.0f * cosf(w0);
                a2 = 1.0f - alpha;
                break;
            case FilterType::HighPass:
                b0 = (1.0f + cosf(w0)) / 2.0f;
                b1 = -(1.0f + cosf(w0));
                b2 = (1.0f + cosf(w0)) / 2.0f;
                a0 = 1.0f + alpha;
                a1 = -2.0f * cosf(w0);
                a2 = 1.0f - alpha;
                break;
            case FilterType::PeakingEQ:
                b0 = 1.0f + alpha * A;
                b1 = -2.0f * cosf(w0);
                b2 = 1.0f - alpha * A;
                a0 = 1.0f + alpha / A;
                a1 = -2.0f * cosf(w0);
                a2 = 1.0f - alpha / A;
                break;
            case FilterType::LowShelf: {
                float sa = sinf(w0);
                float beta = sqrtf(A) / Q;
                b0 = A * ((A + 1.0f) - (A - 1.0f) * cosf(w0) + beta * sa);
                b1 = 2.0f * A * ((A - 1.0f) - (A + 1.0f) * cosf(w0));
                b2 = A * ((A + 1.0f) - (A - 1.0f) * cosf(w0) - beta * sa);
                a0 = (A + 1.0f) + (A - 1.0f) * cosf(w0) + beta * sa;
                a1 = -2.0f * ((A - 1.0f) + (A + 1.0f) * cosf(w0));
                a2 = (A + 1.0f) + (A - 1.0f) * cosf(w0) - beta * sa;
                break;
            }
            case FilterType::HighShelf: {
                float sa = sinf(w0);
                float beta = sqrtf(A) / Q;
                b0 = A * ((A + 1.0f) + (A - 1.0f) * cosf(w0) + beta * sa);
                b1 = -2.0f * A * ((A - 1.0f) + (A + 1.0f) * cosf(w0));
                b2 = A * ((A + 1.0f) + (A - 1.0f) * cosf(w0) - beta * sa);
                a0 = (A + 1.0f) - (A - 1.0f) * cosf(w0) + beta * sa;
                a1 = 2.0f * ((A - 1.0f) - (A + 1.0f) * cosf(w0));
                a2 = (A + 1.0f) - (A - 1.0f) * cosf(w0) - beta * sa;
                break;
            }
        }

        // Normalize coefficients
        b0 /= a0;
        b1 /= a0;
        b2 /= a0;
        a1 /= a0;
        a2 /= a0;
    }

    inline float Process(float input) {
        float output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
        
        // Update delay line
        x2 = x1;
        x1 = input;
        y2 = y1;
        y1 = output;
        
        // Prevent denormals
        if (std::abs(y1) < 1e-15f) y1 = 0.0f;
        if (std::abs(y2) < 1e-15f) y2 = 0.0f;
        
        return output;
    }

    void Reset() {
        x1 = x2 = y1 = y2 = 0.0f;
    }
};
