package com.aviator.hoc.update

/** Release tags look like "build-17"; the number is the app's versionCode. */
fun versionCodeFromTag(tag: String?): Int? =
    tag?.let { Regex("""(\d+)\s*$""").find(it.trim())?.groupValues?.get(1)?.toIntOrNull() }

fun isNewer(remote: Int?, local: Int): Boolean = remote != null && remote > local

/** First few non-empty lines of the release notes, for the update card. */
fun shortNotes(body: String?, maxLines: Int = 4): String =
    body.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }.take(maxLines).joinToString("\n")

data class UpdateInfo(
    val versionCode: Int,
    val name: String,
    val notes: String,
    val apkUrl: String,
    val sizeBytes: Long,
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateState
    data class Ready(val info: UpdateInfo) : UpdateState
    /** Android needs the user to allow "install unknown apps" for this app first. */
    data class NeedsPermission(val info: UpdateInfo) : UpdateState
    data class Error(val message: String) : UpdateState
}
