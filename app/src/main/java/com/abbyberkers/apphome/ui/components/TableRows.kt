package com.abbyberkers.apphome.ui.components

import android.view.View
import android.view.ViewManager
import org.jetbrains.anko.tableRow
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

/**
 * Table row with a number of text cells.
 *
 * @param texts The tetxs to be displayed in the table row.
 * @param alignment The alignment of the texts in their table cell. Centered by default.
 */
fun ViewManager.textRow(vararg texts: String, alignment: Int = View.TEXT_ALIGNMENT_CENTER) {
    tableRow {
        texts.map {
            textView {
                hint = it
                textAlignment = alignment
            }.lparams(height = wrapContent, width = 0, initWeight = 1f)
        }
    }
}