package com.example.domain.mastering

import com.example.domain.model.mastering.ReferenceMatchingReport

interface IReferenceMatcher {
    fun matchReference(genreStyle: String, mood: String, language: String): ReferenceMatchingReport
}
