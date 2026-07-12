package com.example.domain.mastering

import com.example.domain.model.mastering.StereoMetrics

interface IStereoEnhancer {
    fun enhanceStereo(genreStyle: String): StereoMetrics
}
