package com.brightsight.joker.core

import android.os.Build
import androidx.databinding.ObservableBoolean
import com.brightsight.joker.DynAPK
import com.brightsight.joker.core.model.UpdateInfo
import com.brightsight.joker.core.utils.net.NetworkObserver
import com.brightsight.joker.data.repository.NetworkService
import com.brightsight.joker.di.AppContext
import com.brightsight.joker.ktx.getProperty
import com.brightsight.superuser.Shell
import com.brightsight.superuser.ShellUtils.fastCmd
import com.brightsight.superuser.internal.UiThreadHandler

val isRunningAsStub get() = Info.stub != null

object Info {

    var stub: DynAPK.Data? = null

    val EMPTY_REMOTE = UpdateInfo()
    var remote = EMPTY_REMOTE
    suspend fun getRemote(svc: NetworkService): UpdateInfo? {
        return if (remote === EMPTY_REMOTE) {
            svc.fetchUpdate()?.apply { remote = this }
        } else remote
    }

    // Device state
    @JvmStatic val env by lazy { loadState() }
    @JvmField var isSAR = false
    @JvmField var isAB = false
    @JvmField val isVirtualAB = getProperty("ro.virtual_ab.enabled", "false") == "true"
    @JvmStatic val isFDE get() = crypto == "block"
    @JvmField var ramdisk = false
    @JvmField var hasGMS = true
    @JvmField val isPixel = Build.BRAND == "google"
    @JvmField val isEmulator =
        getProperty("ro.kernel.qemu", "0") == "1" ||
        getProperty("ro.boot.qemu", "0") == "1"
    var crypto = ""
    var noDataExec = false

    val isConnected by lazy {
        ObservableBoolean(false).also { field ->
            NetworkObserver.observe(AppContext) {
                UiThreadHandler.run { field.set(it) }
            }
        }
    }

    private fun loadState() = Env(
        fastCmd("magisk -v").split(":".toRegex())[0],
        runCatching { fastCmd("magisk -V").toInt() }.getOrDefault(-1),
        Shell.jojo("jokerhide status").exec().isSuccess
    )

    class Env(
        val magiskVersionString: String = "",
        code: Int = -1,
        hide: Boolean = false
    ) {
        val jokerHide get() = Config.jokerHide
        val magiskVersionCode = when {
            code < Const.Version.MIN_VERCODE -> -1
            else -> if (Shell.rootAccess()) code else -1
        }
        val isUnsupported = code > 0 && code < Const.Version.MIN_VERCODE
        val isActive = magiskVersionCode >= 0

        init {
            Config.jokerHide = hide
        }
    }
}
