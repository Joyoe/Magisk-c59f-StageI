package com.brightsight.joker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.brightsight.joker.R
import com.brightsight.joker.core.Config
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.Info
import com.brightsight.joker.di.AppContext
import com.brightsight.superuser.internal.UiThreadHandler

object Utils {

    fun toast(msg: CharSequence, duration: Int) {
        UiThreadHandler.run { Toast.makeText(AppContext, msg, duration).show() }
    }

    fun toast(resId: Int, duration: Int) {
        UiThreadHandler.run { Toast.makeText(AppContext, resId, duration).show() }
    }

    fun showSuperUser(): Boolean {
        return Info.env.isActive && (Const.USER_ID == 0
                || Config.suMultiuserMode == Config.Value.MULTIUSER_MODE_USER)
    }

    fun openLink(context: Context, link: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            toast(
                R.string.open_link_failed_toast,
                Toast.LENGTH_SHORT
            )
        }
    }
}
