package com.example.domain.mastering

import com.example.domain.model.mastering.DitherSettings

interface IDitherEngine {
    fun processDither(bitDepth: String): DitherSettings
}
