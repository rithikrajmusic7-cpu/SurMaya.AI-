package com.example.data.remote.gateway.lyrics

interface AILyricistGateway {
    suspend fun generateLyricsStructured(params: LyricsGenerationParams, language: String): Result<LyricProject>
    
    suspend fun generateLyricsChat(
        project: LyricProject, 
        message: String, 
        selectedText: String? = null
    ): Result<LyricProject>
    
    suspend fun analyzeQuality(lyrics: String, language: String): QualityScoreReport
    
    suspend fun getSmartSuggestions(lyrics: String, language: String): SmartSuggestions
    
    suspend fun saveProject(project: LyricProject): Result<Unit>
    
    suspend fun getProject(projectId: String): Result<LyricProject?>
    
    suspend fun getAllProjects(): List<LyricProject>
    
    suspend fun deleteProject(projectId: String): Result<Unit>
}
