package com.brightsight.joker.view

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toIcon
import com.brightsight.joker.R
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.Const.ID.PROGRESS_NOTIFICATION_CHANNEL
import com.brightsight.joker.core.Const.ID.UPDATE_NOTIFICATION_CHANNEL
import com.brightsight.joker.core.download.DownloadService
import com.brightsight.joker.core.download.Subject
import com.brightsight.joker.di.AppContext
import com.brightsight.joker.ktx.getBitmap

@Suppress("DEPRECATION")
object Notifications {

    val mgr by lazy { AppContext.getSystemService<NotificationManager>()!! }

    fun setup(context: Context) {
        if (SDK_INT >= 26) {
            var channel = NotificationChannel(UPDATE_NOTIFICATION_CHANNEL,
                    context.getString(R.string.update_channel), NotificationManager.IMPORTANCE_DEFAULT)
            mgr.createNotificationChannel(channel)
            channel = NotificationChannel(PROGRESS_NOTIFICATION_CHANNEL,
                    context.getString(R.string.progress_channel), NotificationManager.IMPORTANCE_LOW)
            mgr.createNotificationChannel(channel)
        }
    }

    private fun updateBuilder(context: Context): Notification.Builder {
        return Notification.Builder(context).apply {
            val bitmap = context.getBitmap(R.drawable.ic_magisk_outline)
            setLargeIcon(bitmap)
            if (SDK_INT >= 26) {
                setSmallIcon(bitmap.toIcon())
                setChannelId(UPDATE_NOTIFICATION_CHANNEL)
            } else {
                setSmallIcon(R.drawable.ic_magisk_outline)
            }
        }
    }

    fun managerUpdate(context: Context) {
        val intent = DownloadService.pendingIntent(context, Subject.Manager())

        val builder = updateBuilder(context)
            .setContentTitle(context.getString(R.string.magisk_update_title))
            .setContentText(context.getString(R.string.manager_download_install))
            .setAutoCancel(true)
            .setContentIntent(intent)

        mgr.notify(Const.ID.APK_UPDATE_NOTIFICATION_ID, builder.build())
    }

    fun progress(context: Context, title: CharSequence): Notification.Builder {
        val builder = if (SDK_INT >= 26) {
            Notification.Builder(context, PROGRESS_NOTIFICATION_CHANNEL)
        } else {
            Notification.Builder(context).setPriority(Notification.PRIORITY_LOW)
        }
        builder.setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setProgress(0, 0, true)
            .setOngoing(true)
        return builder
    }
}
