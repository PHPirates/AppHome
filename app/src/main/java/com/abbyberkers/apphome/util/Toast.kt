package com.abbyberkers.apphome.util

import android.content.Context
import android.widget.Toast

/**
 * Display a toast with the given message.
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}