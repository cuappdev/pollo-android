package com.cornellappdev.android.pollo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

object ExceptionHelper {
    fun getExceptionHandler(context: Context, errorMessage: String): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, exception ->
            Log.d("CoroutineException", "Exception $exception thrown. Error message: $errorMessage")
            (context as Activity).runOnUiThread {
                AlertDialog.Builder(context)
                        .setTitle(errorMessage)
                        .setMessage("Please try again later.")
                        .setNeutralButton(android.R.string.ok, null)
                        .show()
            }
        }
    }
}