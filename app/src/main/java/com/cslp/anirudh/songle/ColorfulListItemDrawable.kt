package com.cslp.anirudh.songle

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.StateListDrawable



/**
 * Created by anirudh on 07/11/17.
 * Courtesy https://stackoverflow.com/questions/6833844/why-does-calling-setbackgroundcolor-on-a-view-break-its-long-click-color-change
 */

class ColorfulListItemDrawable(color: Int) : StateListDrawable() {
    private val mColor: PaintDrawable

    init {
        mColor = PaintDrawable(color)
        initialize()
    }

    private fun initialize() {
        val color = mColor
        val selected = ColorDrawable(Color.TRANSPARENT)
        addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled,
                android.R.attr.state_window_focused), selected)
        addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled,
                android.R.attr.state_window_focused, android.R.attr.state_selected), selected)
        addState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_window_focused,
                android.R.attr.state_selected), selected)
        addState(intArrayOf(), color)
    }

    fun setColor(color: Int) {
        mColor.paint.color = color
        mColor.invalidateSelf()
    }
}