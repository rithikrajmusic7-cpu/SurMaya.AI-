package com.example.core.audio

import kotlin.math.max

class TimelineCoordinator(
    val sampleRate: Int = 48000,
    val ticksPerQuarterNote: Int = 480
) {
    private var elapsedSamples = 0L
    private var bpm = 105.0f

    fun reset() {
        elapsedSamples = 0L
    }

    fun setTempo(tempoBpm: Float) {
        bpm = max(20f, tempoBpm)
    }

    fun advanceSamples(count: Int) {
        elapsedSamples += count
    }

    fun getElapsedSamples(): Long = elapsedSamples

    fun getElapsedTimeSeconds(): Double {
        return elapsedSamples.toDouble() / sampleRate.toDouble()
    }

    // Convert elapsed time to MIDI musical ticks
    fun getElapsedTicks(): Long {
        val secondsPerBeat = 60.0 / bpm
        val samplesPerBeat = sampleRate * secondsPerBeat
        val ticksPerSample = ticksPerQuarterNote.toDouble() / samplesPerBeat
        return (elapsedSamples * ticksPerSample).toLong()
    }

    // Convert a given tick number back to seconds
    fun tickToSeconds(tick: Long): Double {
        val secondsPerQuarter = 60.0 / bpm
        val secondsPerTick = secondsPerQuarter / ticksPerQuarterNote.toDouble()
        return tick.toDouble() * secondsPerTick
    }

    // Convert seconds to ticks
    fun secondsToTick(seconds: Double): Long {
        val secondsPerQuarter = 60.0 / bpm
        val secondsPerTick = secondsPerQuarter / ticksPerQuarterNote.toDouble()
        return (seconds / secondsPerTick).toLong()
    }
}
