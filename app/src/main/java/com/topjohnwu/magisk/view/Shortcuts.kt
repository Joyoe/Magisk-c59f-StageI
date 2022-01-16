package com.brightsight.joker.view

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.brightsight.joker.R
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.Info
import com.brightsight.joker.ktx.getBitmap
import com.brightsight.joker.utils.Utils

object Shortcuts {

    fun setupDynamic(context: Context) {
        if (Build.VERSION.SDK_INT >= 25) {
            val manager = context.getSystemService<ShortcutManager>() ?: return
            manager.dynamicShortcuts = getShortCuts(context)
        }
    }

    fun addHomeIcon(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        val info = ShortcutInfoCompat.Builder(context, Const.Nav.HOME)
            .setShortLabel(context.getString(R.string.magisk))
            .setIntent(intent)
            .setIcon(context.getIconCompat(R.drawable.ic_launcher))
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    private fun Context.getIconCompat(id: Int): IconCompat {
        return if (Build.VERSION.SDK_INT >= 26)
            IconCompat.createWithAdaptiveBitmap(getBitmap(id))
        else
            IconCompat.createWithBitmap(getBitmap(id))
    }

    @RequiresApi(api = 23)
    private fun Context.getIcon(id: Int) = getIconCompat(id).toIcon(this)

    @RequiresApi(api = 25)
    private fun getShortCuts(context: Context): List<ShortcutInfo> {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return emptyList()

        val shortCuts = mutableListOf<ShortcutInfo>()

        if (Utils.showSuperUser()) {
            shortCuts.add(
                ShortcutInfo.Builder(context, Const.Nav.SUPERUSER)
                    .setShortLabel(context.getString(R.string.superuser))
                    .setIntent(
                        Intent(intent).putExtra(Const.Key.OPEN_SECTION, Const.Nav.SUPERUSER)
                    )
                    .setIcon(context.getIcon(R.drawable.sc_superuser))
                    .setRank(0)
                    .build()
            )
        }
        if (Info.env.jokerHide) {
            shortCuts.add(
                ShortcutInfo.Builder(context, Const.Nav.HIDE)
                    .setShortLabel(context.getString(R.string.jokerhide))
                    .setIntent(
                        Intent(intent).putExtra(Const.Key.OPEN_SECTION, Const.Nav.HIDE)
                    )
                    .setIcon(context.getIcon(R.drawable.sc_jokerhide))
                    .setRank(1)
                    .build()
            )
        }
        if (Info.env.isActive) {
            shortCuts.add(
                ShortcutInfo.Builder(context, Const.Nav.MODULES)
                    .setShortLabel(context.getString(R.string.modules))
                    .setIntent(
                        Intent(intent).putExtra(Const.Key.OPEN_SECTION, Const.Nav.MODULES)
                    )
                    .setIcon(context.getIcon(R.drawable.sc_extension))
                    .setRank(2)
                    .build()
            )
        }
        return shortCuts
    }
}
