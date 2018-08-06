package com.abbyberkers.apphome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.abbyberkers.apphome.ui.MainUI
import org.jetbrains.anko.setContentView

class MainAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainUI().setContentView(this)

        /*
        TODO:
        - check if user configured
         */
    }

    fun hi() {
        val uri = Uri.parse("https://wa.me/?text=hi")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}