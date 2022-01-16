package com.brightsight.joker.core.download

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toFile
import com.brightsight.joker.arch.BaseUIActivity
import com.brightsight.joker.core.ForegroundTracker
import com.brightsight.joker.core.download.Action.Flash
import com.brightsight.joker.core.download.Subject.Manager
import com.brightsight.joker.core.download.Subject.Module
import com.brightsight.joker.core.intent
import com.brightsight.joker.ui.flash.FlashFragment
import com.brightsight.joker.utils.APKInstall
import com.brightsight.superuser.internal.UiThreadHandler
import kotlin.random.Random.Default.nextInt

open class DownloadService : BaseDownloader() {

    private val context get() = this

    override suspend fun onFinish(subject: Subject, id: Int) = when (subject) {
        is Module -> subject.onFinish(id)
        is Manager -> subject.onFinish(id)
    }

    private fun Module.onFinish(id: Int) = when (action) {
        Flash -> {
            UiThreadHandler.run {
                (ForegroundTracker.foreground as? BaseUIActivity<*, *>)
                    ?.navigation?.navigate(FlashFragment.install(file, id))
            }
        }
        else -> Unit
    }

    private fun Manager.onFinish(id: Int) {
        remove(id)
        APKInstall.install(context, file.toFile())
    }

    // --- Customize finish notification

    override fun Notification.Builder.setIntent(subject: Subject)
    = when (subject) {
        is Module -> setIntent(subject)
        is Manager -> setIntent(subject)
    }

    private fun Notification.Builder.setIntent(subject: Module)
    = when (subject.action) {
        Flash -> setContentIntent(FlashFragment.installIntent(context, subject.file))
        else -> setContentIntent(Intent())
    }

    private fun Notification.Builder.setIntent(subject: Manager)
    = setContentIntent(APKInstall.installIntent(context, subject.file.toFile()))

    private fun Notification.Builder.setContentIntent(intent: Intent) =
        setContentIntent(
            PendingIntent.getActivity(context, nextInt(), intent, PendingIntent.FLAG_ONE_SHOT)
        )

    // ---

    companion object {

        private fun intent(context: Context, subject: Subject) =
            context.intent<DownloadService>().putExtra(ACTION_KEY, subject)

        fun pendingIntent(context: Context, subject: Subject): PendingIntent {
            return if (Build.VERSION.SDK_INT >= 26) {
                PendingIntent.getForegroundService(context, nextInt(),
                    intent(context, subject), PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                PendingIntent.getService(context, nextInt(),
                    intent(context, subject), PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        fun start(context: Context, subject: Subject) {
            val app = context.applicationContext
            if (Build.VERSION.SDK_INT >= 26) {
                app.startForegroundService(intent(app, subject))
            } else {
                app.startService(intent(app, subject))
            }
        }
    }

}
