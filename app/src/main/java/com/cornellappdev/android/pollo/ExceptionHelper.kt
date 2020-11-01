package com.cornellappdev.android.pollo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import java.util.concurrent.atomic.AtomicBoolean

object ExceptionHelper {
    private var alertShown = AtomicBoolean(false)

    fun getThreadExceptionHandler(context: Context, errorTitle: String, callback: ResettableView?): Thread.UncaughtExceptionHandler {
        return Thread.UncaughtExceptionHandler { _, exception ->
            handleException(context, errorTitle, callback, exception)
        }
    }

    fun getCoroutineExceptionHandler(context: Context, errorTitle: String, callback: ResettableView?): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, exception ->
            handleException(context, errorTitle, callback, exception)
        }
    }

    private fun handleException(context: Context, errorMessage: String, callback: ResettableView?, exception: Throwable) {
        Log.d("CoroutineException", "Exception $exception thrown. Error message: $errorMessage")
        if (!alertShown.get()) {
            alertShown.set(true)
            displayAlert(context, errorMessage) { alertShown.set(false) }
        }
        callback?.reset()
    }

    fun displayAlert(context: Context, errorTitle: String, alertClosedAction: () -> Unit) {
        (context as Activity).runOnUiThread {
            AlertDialog.Builder(context)
                    .setTitle(errorTitle)
                    .setMessage("Please try again later.")
                    .setNeutralButton(android.R.string.ok) { _, _ -> alertClosedAction() }
                    .show()
        }
    }

    interface ResettableView {
        fun reset()
    }
}