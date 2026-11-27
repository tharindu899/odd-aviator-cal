package com.aviator.hoc.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/** GitHub Releases based updater: check latest release, download the APK, hand it to the installer. */
class Updater(private val context: Context, private val repo: String) {

    /** The newest release, or null when the repo has none yet. */
    suspend fun fetchLatest(): UpdateInfo? = withContext(Dispatchers.IO) {
        val c = open("https://api.github.com/repos/$repo/releases/latest")
        c.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            when (val code = c.responseCode) {
                404 -> return@withContext null
                200 -> Unit
                else -> throw IOException("GitHub answered $code")
            }
            val json = JSONObject(c.inputStream.bufferedReader().use { it.readText() })
            val tag = json.optString("tag_name")
            val version = versionCodeFromTag(tag) ?: return@withContext null
            val assets = json.optJSONArray("assets") ?: return@withContext null

            var url: String? = null
            var size = 0L
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.optString("name").endsWith(".apk", ignoreCase = true)) {
                    url = a.optString("browser_download_url")
                    size = a.optLong("size")
                    break
                }
            }
            if (url.isNullOrBlank()) return@withContext null
            UpdateInfo(version, json.optString("name", tag), shortNotes(json.optString("body")), url, size)
        } finally {
            c.disconnect()
        }
    }

    /** Downloads the APK into the app cache; [onProgress] gets 0..1. */
    suspend fun download(info: UpdateInfo, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val out = File(dir, "update-${info.versionCode}.apk")

        val c = open(info.apkUrl)
        try {
            if (c.responseCode != 200) throw IOException("Download failed (${c.responseCode})")
            val total = c.contentLengthLong.takeIf { it > 0 } ?: info.sizeBytes
            var done = 0L
            c.inputStream.use { input ->
                out.outputStream().use { output ->
                    val buf = ByteArray(32 * 1024)
                    var last = -1
                    while (true) {
                        coroutineContext.ensureActive()
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                        done += n
                        if (total > 0) {
                            val pct = (done * 100 / total).toInt()
                            if (pct != last) {
                                last = pct
                                onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                            }
                        }
                    }
                }
            }
            if (out.length() < 100_000) throw IOException("Downloaded file looks incomplete")
        } catch (e: Throwable) {
            out.delete()
            throw e
        } finally {
            c.disconnect()
        }
        out
    }

    fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    /** Opens Android's "allow from this source" screen for this app. */
    fun openInstallPermissionSettings() {
        val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }

    /** Hands the APK to the system installer (the user confirms, Android replaces the app). */
    fun install(apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val i = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "AviatorHOC-updater")
        }
}
