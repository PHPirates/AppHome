package com.abbyberkers.apphome.util

import android.app.Activity
import android.widget.Toast

/**
 * Display a toast with the given text.
 */
fun Activity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}