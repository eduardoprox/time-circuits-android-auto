package com.eduardoprox.timecircuits

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import java.util.Calendar
import kotlin.math.min

/**
 * Desenha o painel de tres linhas em qualquer Canvas, nas coordenadas do SVG (954 x 636).
 *
 * O conjunto mantem as proporcoes do original e fica centralizado. Em areas mais largas que
 * 3:2, a sobra vira laterais cinza, e as linhas pretas vao de ponta a ponta.
 *
 * Tudo fica pre-desenhado num Bitmap, refeito so quando o minuto muda. O unico elemento que
 * muda a cada segundo sao os dois pontos das tres linhas.
 */
class PanelRenderer {

    private data class Moment(
        val month: String, val day: Int, val year: Int,
        val hour: Int, val minute: Int, val pm: Boolean,
    )

    private class Field(val key: Char, val base: Float, val w: Float, val n: Int, val label: String, val labelW: Float)

    private val dest = Moment("OCT", 26, 1985, 1, 21, pm = false)
    private val last = Moment("OCT", 26, 1985, 1, 20, pm = false)
    private var present: Moment? = null
    private var colonLit = true

    /** Escurece o painel inteiro para dirigir a noite. */
    var night = false

    private val fields = listOf(
        Field('M', 24f, 164f, 3, "MONTH", 104f),
        Field('D', 228f, 112f, 2, "DAY", 72f),
        Field('Y', 380f, 216f, 4, "YEAR", 84f),
        Field('H', 668f, 112f, 2, "HOUR", 88f),
        Field('N', 822f, 112f, 2, "MIN", 70f),
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        maskFilter = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val dim = Paint().apply { color = Color.argb(150, 0, 0, 0) }

    private var frame: Bitmap? = null
    private var width = 0
    private var height = 0
    private val safe = Rect()
    private var scale = 1f
    private var originX = 0f
    private var originY = 0f

    /** Tamanho total e a parte dele que nenhum elemento do sistema cobre. */
    fun setArea(w: Int, h: Int, safeArea: Rect) {
        if (w == width && h == height && safeArea == safe) return
        width = w
        height = h
        safe.set(safeArea)
        frame = null
    }

    fun setTime(now: Calendar) {
        val h = now.get(Calendar.HOUR_OF_DAY)
        val m = Moment(
            MONTHS[now.get(Calendar.MONTH)], now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.YEAR),
            if (h % 12 == 0) 12 else h % 12, now.get(Calendar.MINUTE), h >= 12,
        )
        if (m != present) {
            present = m
            frame = null
        }
        colonLit = now.get(Calendar.SECOND) % 2 == 0
    }

    fun draw(canvas: Canvas) {
        if (width <= 0 || height <= 0 || safe.isEmpty) return
        val bmp = frame ?: buildFrame().also { frame = it }
        canvas.drawBitmap(bmp, 0f, 0f, null)
        canvas.save()
        applyTransform(canvas)
        for (y in floatArrayOf(ROW_DEST, ROW_NOW, ROW_LAST)) {
            led(canvas, COLON_X, y + 68f, 6.5f, colonLit)
            led(canvas, COLON_X, y + 108f, 6.5f, colonLit)
        }
        canvas.restore()
        if (night) canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dim)
    }

    private fun applyTransform(canvas: Canvas) {
        canvas.translate(originX, originY)
        canvas.scale(scale, scale)
    }

    private fun measure() {
        val safeW = safe.width().toFloat()
        val safeH = safe.height().toFloat()
        scale = min(safeW / W0, safeH / H0)
        originX = safe.left + (safeW - W0 * scale) / 2f
        originY = safe.top + (safeH - H0 * scale) / 2f
    }

    private fun buildFrame(): Bitmap {
        measure()
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(PANEL)
        paint.color = Color.BLACK
        for (y in intArrayOf(200, 410)) {
            val top = originY + y * scale
            c.drawRect(0f, top, width.toFloat(), top + 6f * scale, paint)
        }
        applyTransform(c)
        row(c, ROW_DEST, "DESTINATION TIME", 372f, DEST, dest)
        row(c, ROW_NOW, "PRESENT TIME", 300f, NOW, present ?: dest)
        row(c, ROW_LAST, "LAST TIME DEPARTED", 412f, LAST, last)
        return bmp
    }

    private fun row(c: Canvas, y: Float, title: String, titleW: Float, color: Int, t: Moment) {
        for (f in fields) {
            val x = f.base
            paint.color = LABEL
            c.drawRect(x + (f.w - f.labelW) / 2, y + 2, x + (f.w + f.labelW) / 2, y + 30, paint)
            drawLabel(c, f.label, x + f.w / 2, y + 23, 21f, 0f)
            val value = when (f.key) {
                'M' -> t.month
                'D' -> pad(t.day, 2)
                'Y' -> pad(t.year, 4)
                'H' -> pad(t.hour, 2)
                else -> pad(t.minute, 2)
            }
            display(c, x, y + 36, f.w, value, color, f.key == 'M')
        }
        marker(c, "AM", AMPM_X, y + 40, !t.pm)
        marker(c, "PM", AMPM_X, y + 88, t.pm)
        val mid = W0 / 2
        paint.color = Color.BLACK
        c.drawRect(mid - titleW / 2, y + 150, mid + titleW / 2, y + 184, paint)
        drawLabel(c, title, mid, y + 175, 25f, 0.056f)
    }

    private fun display(c: Canvas, x: Float, y: Float, w: Float, value: String, color: Int, letters: Boolean) {
        paint.color = EDGE
        c.drawRoundRect(x + 2, y + 2, x + w + 3, y + 106, 10f, 10f, paint)
        paint.color = Color.BLACK
        c.drawRoundRect(x, y, x + w, y + 104, 10f, 10f, paint)

        val gap = 8f
        val total = value.length * Segments.CELL_W + (value.length - 1) * gap
        val x0 = x + (w - total) / 2
        val y0 = y + (104 - Segments.CELL_H) / 2
        val ghost = if (letters) Segments.allLetter else Segments.allDigit
        value.forEachIndexed { i, ch ->
            c.save()
            c.translate(x0 + i * (Segments.CELL_W + gap) + 2.7f, y0)
            c.skew(-0.07f, 0f)
            paint.color = color
            paint.alpha = 14
            c.drawPath(ghost, paint)
            paint.alpha = 255
            val lit = Segments.glyph(ch)
            glow.color = color
            glow.alpha = 170
            c.drawPath(lit, glow)
            paint.color = color
            c.drawPath(lit, paint)
            c.restore()
        }
    }

    private fun marker(c: Canvas, name: String, cx: Float, y: Float, on: Boolean) {
        paint.color = LABEL
        c.drawRect(cx - 21, y, cx + 21, y + 18, paint)
        drawLabel(c, name, cx, y + 14, 14f, 0f)
        led(c, cx, y + 31, 7.5f, on)
    }

    private fun led(c: Canvas, x: Float, y: Float, r: Float, on: Boolean) {
        if (on) {
            paint.shader = RadialGradient(x, y, r * 2f, LED_ON, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            c.drawCircle(x, y, r * 2f, paint)
            paint.shader = null
            paint.color = LED_ON
        } else {
            paint.color = LED_OFF
        }
        c.drawCircle(x, y, r, paint)
    }

    private fun drawLabel(c: Canvas, s: String, x: Float, y: Float, size: Float, spacing: Float) {
        text.textSize = size
        text.letterSpacing = spacing
        c.drawText(s, x, y, text)
    }

    private fun pad(n: Int, width: Int) = n.toString().padStart(width, '0')

    private companion object {
        const val W0 = 954f
        const val H0 = 636f
        const val ROW_DEST = 6f
        const val ROW_NOW = 216f
        const val ROW_LAST = 426f
        const val AMPM_X = 632f
        const val COLON_X = 801f

        val MONTHS = arrayOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

        val PANEL = Color.rgb(0x90, 0x90, 0x90)
        val EDGE = Color.rgb(0xEF, 0xEF, 0xEF)
        val LABEL = Color.rgb(0x7A, 0x10, 0x12)
        val LED_OFF = Color.rgb(0x5B, 0x4A, 0x35)
        val LED_ON = Color.rgb(0xF6, 0xF0, 0x8A)
        val DEST = Color.rgb(0xF2, 0x66, 0x1B)
        val NOW = Color.rgb(0x3F, 0xE4, 0x3A)
        val LAST = Color.rgb(0xF4, 0xE7, 0x8C)
    }
}
