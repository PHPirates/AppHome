package com.abbyberkers.apphome.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.ViewManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.abbyberkers.apphome.R
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.wrapContent

/**
 * Spinner that has a function to set its items.
 */
class StringSpinner : Spinner {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        adapter = ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, arrayOf(" "))
        layoutParams = LayoutParams(wrapContent, matchParent)
    }

    /**
     * Set the items of the spinner.
     * @param items The items to be set on the spinner.
     */
    fun setItems(vararg items: String) {
        adapter = ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, items)
    }
}

fun ViewManager.stringSpinner(theme: Int = 0) = stringSpinner(theme) { }
inline fun ViewManager.stringSpinner(theme: Int = 0, init: (StringSpinner) -> Unit) = ankoView(::StringSpinner, theme, init)