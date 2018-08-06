package com.abbyberkers.apphome.communication

import android.content.Context
import android.content.Intent


class WhatsappMessage(val text: String = "bloop!",
                      val language: Language = Language.DUTCH) {

    infix fun sendWith(context: Context) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        context.startActivity(intent)
    }

}