package com.example.domain.mastering

import com.example.domain.model.mastering.HarmonicExciterSettings

interface IHarmonicExciter {
    fun exciteHarmonics(genreStyle: String, intensity: Float = 0.5f): HarmonicExciterSettings
}
