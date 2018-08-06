package com.abbyberkers.apphome.ui.components

import android.R
import android.view.View
import android.view.ViewManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
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
