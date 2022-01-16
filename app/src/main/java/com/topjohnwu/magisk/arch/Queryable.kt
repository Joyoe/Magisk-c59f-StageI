package com.brightsight.joker.arch

import android.os.Handler
import androidx.core.os.postDelayed
import com.brightsight.superuser.internal.UiThreadHandler

interface Queryable {

    val queryDelay: Long
    val queryHandler: Handler get() = UiThreadHandler.handler

    fun submitQuery() {
        queryHandler.postDelayed(queryDelay) { query() }
    }

    fun query()
}
