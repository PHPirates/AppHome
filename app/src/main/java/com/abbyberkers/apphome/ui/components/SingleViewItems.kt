package com.abbyberkers.apphome.ui.components

import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.*
import android.R
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onItemSelectedListener

/**
 * Spinner with listener for selected item.
 *
 * @param items The items to display in the spinner.
 * @param onItemSelected The function to evaluate when an item is being selected.
 *        Takes the position of the item in [items] as an argument.
 */
fun ViewManager.spinnerWithListener(items: Array<*>, onItemSelected: (position: Int) -> Unit) : Spinner {
    return spinner {
            adapter = ArrayAdapter(this.context, R.layout.simple_spinner_dropdown_item, items)
            onItemSelectedListener {
                onItemSelected { adapterView, view, i, l -> onItemSelected(i) }
                onNothingSelected { parent: AdapterView<*>? -> getContext().toast("Please select an item.") }
            }
    }
}

/**
 * A languageSwitch with tags on either side.
 *
 * @param leftText The text on the left side of the languageSwitch. When the languageSwitch is left, its value is false.
 * @param rightText The text on the right side of the languageSwitch. When the languageSwitch is right, its value is true.
 */
fun ViewManager.wordSwitch(leftText: String, rightText: String) : Switch {
    lateinit var switch: Switch
    tableRow {
        textView {
            text = leftText
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }.lparams(height = wrapContent, width = 0, initWeight = 1f)

        linearLayout {
            gravity = Gravity.CENTER
            switch = switch()
        }.lparams(height = wrapContent, width = 0, initWeight = 1f)

        textView {
            text = rightText
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }.lparams(height = wrapContent, width = 0, initWeight = 1f)
    }
    return switch
}