package com.brightsight.joker.ui.safetynet

import android.content.Context

interface SafetyNetHelper {

    val version: Int

    fun attest(context: Context, nonce: ByteArray, callback: Callback)

    interface Callback {
        fun onResponse(response: String?)
    }
}
