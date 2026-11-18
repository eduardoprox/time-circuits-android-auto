package com.eduardoprox.timecircuits

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.WindowInsets
import java.util.Calendar

/** O painel na tela do celular. No carro, quem desenha e a ClockScreen. */
class CircuitsView(context: Context) : View(context) {

    private val panel = PanelRenderer()
    private val insets = Rect()

    fun update(now: Calendar) {
        panel.setTime(now)
        invalidate()
    }

    override fun onApplyWindowInsets(windowInsets: WindowInsets): WindowInsets {
        val i = windowInsets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
        insets.set(i.left, i.top, i.right, i.bottom)
        invalidate()
        return windowInsets
    }

    override fun onDraw(canvas: Canvas) {
        panel.setArea(width, height, Rect(insets.left, insets.top, width - insets.right, height - insets.bottom))
        panel.draw(canvas)
    }
}
