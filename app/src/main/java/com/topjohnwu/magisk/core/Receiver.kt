package com.brightsight.joker.core

import android.annotation.SuppressLint
import android.content.ContextWrapper
import android.content.Intent
import com.brightsight.joker.core.base.BaseReceiver
import com.brightsight.joker.di.ServiceLocator
import com.brightsight.joker.view.Shortcuts
import com.brightsight.superuser.Shell
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class Receiver : BaseReceiver() {

    private val policyDB get() = ServiceLocator.policyDB

    @SuppressLint("InlinedApi")
    private fun getPkg(intent: Intent): String? {
        val pkg = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
        return pkg ?: intent.data?.schemeSpecificPart
    }

    private fun getUid(intent: Intent): Int? {
        val uid = intent.getIntExtra(Intent.EXTRA_UID, -1)
        return if (uid == -1) null else uid
    }

    override fun onReceive(context: ContextWrapper, intent: Intent?) {
        intent ?: return

        fun rmPolicy(uid: Int) = GlobalScope.launch {
            policyDB.delete(uid)
        }

        when (intent.action ?: return) {
            Intent.ACTION_PACKAGE_REPLACED -> {
                // This will only work pre-O
                if (Config.suReAuth)
                    getUid(intent)?.let { rmPolicy(it) }
            }
            Intent.ACTION_UID_REMOVED -> {
                getUid(intent)?.let { rmPolicy(it) }
                getPkg(intent)?.let { Shell.jojo("jokerhide rm $it").submit() }
            }
            Intent.ACTION_LOCALE_CHANGED -> Shortcuts.setupDynamic(context)
        }
    }
}
