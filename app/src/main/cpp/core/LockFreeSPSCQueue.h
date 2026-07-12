#pragma once
#include <atomic>
#include <vector>
#include <cstddef>

template <typename T, size_t Capacity = 1024>
class LockFreeSPSCQueue {
private:
    static_assert((Capacity & (Capacity - 1)) == 0, "Capacity must be a power of 2");
    
    T mRingBuffer[Capacity];
    std::atomic<size_t> mHead{0};
    std::atomic<size_t> mTail{0};

public:
    LockFreeSPSCQueue() = default;
    ~LockFreeSPSCQueue() = default;

    // Push item to queue. Called from Producer thread (Kotlin / Control Thread).
    bool Push(const T& item) {
        size_t currentTail = mTail.load(std::memory_order_relaxed);
        size_t currentHead = mHead.load(std::memory_order_acquire);
        
        if ((currentTail - currentHead) >= Capacity) {
            return false; // Queue is full
        }
        
        mRingBuffer[currentTail & (Capacity - 1)] = item;
        mTail.store(currentTail + 1, std::memory_order_release);
        return true;
    }

    // Pop item from queue. Called from Consumer thread (Real-Time Audio Thread).
    bool Pop(T& item) {
        size_t currentHead = mHead.load(std::memory_order_relaxed);
        size_t currentTail = mTail.load(std::memory_order_acquire);
        
        if (currentHead == currentTail) {
            return false; // Queue is empty
        }
        
        item = mRingBuffer[currentHead & (Capacity - 1)];
        mHead.store(currentHead + 1, std::memory_order_release);
        return true;
    }

    bool IsEmpty() const {
        return mHead.load(std::memory_order_relaxed) == mTail.load(std::memory_order_relaxed);
    }

    size_t Size() const {
        size_t head = mHead.load(std::memory_order_relaxed);
        size_t tail = mTail.load(std::memory_order_relaxed);
        return (tail >= head) ? (tail - head) : 0;
    }
};
