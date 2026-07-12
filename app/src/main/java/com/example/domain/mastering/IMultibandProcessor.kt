package com.example.domain.mastering

import com.example.domain.model.mastering.MultibandBand

interface IMultibandProcessor {
    fun processMultibandDynamics(genreStyle: String, inputLoudnessDb: Float): List<MultibandBand>
}
