package com.brightsight.joker.core.utils

import android.content.Context
import com.brightsight.joker.DynAPK
import com.brightsight.joker.R
import com.brightsight.joker.core.*
import com.brightsight.joker.ktx.cachedFile
import com.brightsight.joker.ktx.deviceProtectedContext
import com.brightsight.joker.ktx.rawResource
import com.brightsight.joker.ktx.writeTo
import com.brightsight.superuser.Shell
import com.brightsight.superuser.ShellUtils
import java.io.File
import java.util.jar.JarFile

abstract class BaseShellInit : Shell.Initializer() {
    final override fun onInit(context: Context, shell: Shell): Boolean {
        return init(context.wrap(), shell)
    }

    abstract fun init(context: Context, shell: Shell): Boolean
}


class BusyBoxInit : BaseShellInit() {

    override fun init(context: Context, shell: Shell): Boolean {
        shell.newJob().apply {
            add("export ASH_STANDALONE=1")

            val localBB: File
            if (isRunningAsStub) {
                if (!shell.isRoot)
                    return true
                val jar = JarFile(DynAPK.current(context))
                val bb = jar.getJarEntry("lib/${Const.CPU_ABI}/libbusybox.so")
                localBB = context.deviceProtectedContext.cachedFile("busybox")
                localBB.delete()
                jar.getInputStream(bb).writeTo(localBB)
                localBB.setExecutable(true)
            } else {
                localBB = File(context.applicationInfo.nativeLibraryDir, "libbusybox.so")
            }

            if (shell.isRoot) {
                add("export MAGISKTMP=\$(magisk --path)/.magisk")
                // Test if we can properly execute stuff in /data
                Info.noDataExec = !shell.newJob().add("$localBB true").exec().isSuccess
            }

            if (Info.noDataExec) {
                // Copy it out of /data to workaround Samsung bullshit
                add(
                    "if [ -x \$MAGISKTMP/busybox/busybox ]; then",
                    "  cp -af $localBB \$MAGISKTMP/busybox/busybox",
                    "  exec \$MAGISKTMP/busybox/busybox sh",
                    "else",
                    "  cp -af $localBB /dev/.busybox",
                    "  exec /dev/.busybox sh",
                    "fi"
                )
            } else {
                // Directly execute the file
                add("exec $localBB sh")
            }
        }.exec()
        return true
    }
}

class AppShellInit : BaseShellInit() {

    override fun init(context: Context, shell: Shell): Boolean {

        fun fastCmd(cmd: String) = ShellUtils.fastCmd(shell, cmd)
        fun getVar(name: String) = fastCmd("echo \$$name")
        fun getBool(name: String) = getVar(name).toBoolean()

        shell.newJob().apply {
            add(context.rawResource(R.raw.manager))
            if (shell.isRoot) {
                add(context.assets.open("util_functions.sh"))
            }
            add("app_init")
        }.exec()

        Const.MAGISKTMP = getVar("MAGISKTMP")
        Info.isSAR = getBool("SYSTEM_ROOT")
        Info.ramdisk = getBool("RAMDISKEXIST")
        Info.isAB = getBool("ISAB")
        Info.crypto = getVar("CRYPTOTYPE")

        // Default presets
        Config.recovery = getBool("RECOVERYMODE")
        Config.keepVerity = getBool("KEEPVERITY")
        Config.keepEnc = getBool("KEEPFORCEENCRYPT")

        return true
    }
}
