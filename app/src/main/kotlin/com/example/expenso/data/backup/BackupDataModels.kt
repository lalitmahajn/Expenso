package com.example.expenso.data.backup

data class BackupMetadata(
    val formatVersion: Int = 1,
    val app: String = "Expenso",
    val databaseVersion: Int = 4,
    val exportedAt: Long,
    val exportedAtFormatted: String,
    val currency: String,
    val themeMode: String,
    val accountCount: Int,
    val categoryCount: Int,
    val eventCount: Int,
    val expenseCount: Int
)

sealed class BackupResult {
    data class Success(val message: String, val fileUriString: String? = null) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestorePreviewResult {
    data class Success(val metadata: BackupMetadata, val rawJson: String) : RestorePreviewResult()
    data class Error(val message: String) : RestorePreviewResult()
}

sealed class RestoreResult {
    data class Success(
        val accountCount: Int,
        val categoryCount: Int,
        val eventCount: Int,
        val expenseCount: Int
    ) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}
