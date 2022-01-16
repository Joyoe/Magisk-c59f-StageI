package com.brightsight.joker.core

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.brightsight.joker.BuildConfig.APPLICATION_ID
import com.brightsight.joker.R
import com.brightsight.joker.core.base.BaseActivity
import com.brightsight.joker.core.tasks.HideAPK
import com.brightsight.joker.di.ServiceLocator
import com.brightsight.joker.ui.MainActivity
import com.brightsight.joker.view.MagiskDialog
import com.brightsight.joker.view.Notifications
import com.brightsight.joker.view.Shortcuts
import com.brightsight.superuser.Shell
import java.util.concurrent.CountDownLatch

open class SplashActivity : BaseActivity() {

    private val latch = CountDownLatch(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        // Pre-initialize root shell
        Shell.getShell(null) { initAndStart() }
    }

    private fun handleRepackage(pkg: String?) {
        if (packageName != APPLICATION_ID) {
            runCatching {
                // Hidden, remove com.brightsight.joker if exist as it could be malware
                packageManager.getApplicationInfo(APPLICATION_ID, 0)
                Shell.jojo("(pm uninstall $APPLICATION_ID)& >/dev/null 2>&1").exec()
            }
        } else {
            if (Config.suManager.isNotEmpty())
                Config.suManager = ""
            pkg ?: return
            if (!Shell.jojo("(pm uninstall $pkg)& >/dev/null 2>&1").exec().isSuccess)
                uninstallApp(pkg)
        }
    }

    private fun initAndStart() {
        if (isRunningAsStub && !Shell.rootAccess()) {
            runOnUiThread {
                MagiskDialog(this)
                    .applyTitle(R.string.unsupport_nonroot_stub_title)
                    .applyMessage(R.string.unsupport_nonroot_stub_msg)
                    .applyButton(MagiskDialog.ButtonType.POSITIVE) {
                        titleRes = R.string.install
                        onClick { HideAPK.restore(this@SplashActivity) }
                    }
                    .cancellable(false)
                    .reveal()
            }
            return
        }

        val prevPkg = intent.getStringExtra(Const.Key.PREV_PKG)

        Config.load(prevPkg)
        handleRepackage(prevPkg)
        Notifications.setup(this)
        UpdateCheckService.schedule(this)
        Shortcuts.setupDynamic(this)

        // Pre-fetch network services
        ServiceLocator.networkService

        DONE = true
        startActivity(redirect<MainActivity>())
        finish()
    }

    @Suppress("DEPRECATION")
    private fun uninstallApp(pkg: String) {
        val uri = Uri.Builder().scheme("package").opaquePart(pkg).build()
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri)
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        startActivityForResult(intent) { _, _ ->
            latch.countDown()
        }
        latch.await()
    }

    companion object {
        var DONE = false
    }
}
