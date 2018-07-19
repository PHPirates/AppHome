package com.abbyberkers.apphome.ui

import android.view.View
import com.abbyberkers.apphome.MainAct
import org.jetbrains.anko.*

class MainUI : AnkoComponent<MainAct> {
    override fun createView(ui: AnkoContext<MainAct>): View = with(ui) {
        tableLayout {
            padding = 16

            tableRow {
                textView {
                    hint = "Hi from Anko?"
                }.lparams(height = wrapContent, width = matchParent)
            }

        }
    }
}