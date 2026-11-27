package com.aviator.hoc.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate

/** Writes the JSON to the app cache and opens the Android share sheet (Save to Files, Drive, Telegram...). */
fun shareResultsJson(context: Context, json: String) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "aviator-results-${LocalDate.now()}.json")
    file.writeText(json)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Export results"))
}
