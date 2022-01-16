package com.brightsight.joker.core.tasks

import android.net.Uri
import androidx.core.net.toFile
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.utils.MediaStoreUtils.displayName
import com.brightsight.joker.core.utils.MediaStoreUtils.inputStream
import com.brightsight.joker.core.utils.unzip
import com.brightsight.joker.di.AppContext
import com.brightsight.joker.ktx.writeTo
import com.brightsight.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

open class FlashZip(
    private val mUri: Uri,
    private val console: MutableList<String>,
    private val logs: MutableList<String>
) {

    private val installDir = File(AppContext.cacheDir, "flash")
    private lateinit var zipFile: File

    @Throws(IOException::class)
    private fun flash(): Boolean {
        installDir.deleteRecursively()
        installDir.mkdirs()

        zipFile = if (mUri.scheme == "file") {
            mUri.toFile()
        } else {
            File(installDir, "install.zip").also {
                console.add("- Copying zip to temp directory")
                try {
                    mUri.inputStream().writeTo(it)
                } catch (e: IOException) {
                    when (e) {
                        is FileNotFoundException -> console.add("! Invalid Uri")
                        else -> console.add("! Cannot copy to cache")
                    }
                    throw e
                }
            }
        }

        val isValid = runCatching {
            zipFile.unzip(installDir, "META-INF/com/google/android", true)
            val script = File(installDir, "updater-script")
            script.readText().contains("#MAGISK")
        }.getOrElse {
            console.add("! Unzip error")
            throw it
        }

        if (!isValid) {
            console.add("! This zip is not a Magisk module!")
            return false
        }

        console.add("- Installing ${mUri.displayName}")

        return Shell.jojo("sh $installDir/update-binary dummy 1 \'$zipFile\'")
            .to(console, logs).exec().isSuccess
    }

    open suspend fun exec() = withContext(Dispatchers.IO) {
        try {
            if (!flash()) {
                console.add("! Installation failed")
                false
            } else {
                true
            }
        } catch (e: IOException) {
            Timber.e(e)
            false
        } finally {
            Shell.jojo("cd /", "rm -rf $installDir ${Const.TMPDIR}").submit()
        }
    }
}
