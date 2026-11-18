package com.eduardoprox.timecircuits

import android.graphics.Path

/** Geometria dos segmentos numa celula de 44 x 78. Digitos usam 7 tracos; os meses, 14. */
object Segments {
    const val CELL_W = 44f
    const val CELL_H = 78f

    private const val T = 7.5f
    private const val G = 1.6f
    private const val L = T / 2
    private const val R = CELL_W - T / 2
    private const val C = CELL_W / 2
    private const val TOP = T / 2
    private const val M = CELL_H / 2
    private const val B = CELL_H - T / 2

    private fun poly(vararg p: Float) = Path().apply {
        moveTo(p[0], p[1])
        for (i in 2 until p.size step 2) lineTo(p[i], p[i + 1])
        close()
    }

    private fun hbar(a: Float, b: Float, y: Float): Path {
        val t = T / 2
        return poly(a, y, a + t, y - t, b - t, y - t, b, y, b - t, y + t, a + t, y + t)
    }

    private fun vbar(x: Float, a: Float, b: Float): Path {
        val t = T / 2
        return poly(x, a, x + t, a + t, x + t, b - t, x, b, x - t, b - t, x - t, a + t)
    }

    private fun diag(x1: Float, y1: Float, x2: Float, y2: Float): Path {
        val d = T * .42f
        return poly(x1 - d, y1, x1 + d, y1, x2 + d, y2, x2 - d, y2)
    }

    private val strokes = mapOf(
        'a' to hbar(L + G, R - G, TOP),
        'b' to vbar(R, TOP + G, M - G),
        'c' to vbar(R, M + G, B - G),
        'd' to hbar(L + G, R - G, B),
        'e' to vbar(L, M + G, B - G),
        'f' to vbar(L, TOP + G, M - G),
        'g' to hbar(L + G, R - G, M),
        // Tracos extras, usados so pelas letras dos meses.
        'p' to hbar(L + G, C - G, M),
        'q' to hbar(C + G, R - G, M),
        'h' to diag(L + T * .95f, TOP + T * .85f, C - T * .7f, M - T * .75f),
        'i' to vbar(C, TOP + T * .7f, M - T * .7f),
        'j' to diag(R - T * .95f, TOP + T * .85f, C + T * .7f, M - T * .75f),
        'k' to diag(C + T * .7f, M + T * .75f, R - T * .95f, B - T * .85f),
        'l' to vbar(C, M + T * .7f, B - T * .7f),
        'm' to diag(C - T * .7f, M + T * .75f, L + T * .95f, B - T * .85f),
    )

    private val glyphs = mapOf(
        '0' to "abcdef", '1' to "bc", '2' to "abdeg", '3' to "abcdg", '4' to "bcfg",
        '5' to "acdfg", '6' to "acdefg", '7' to "abc", '8' to "abcdefg", '9' to "abcdfg",
        'A' to "abcefpq", 'B' to "abcdqil", 'C' to "adef", 'D' to "abcdil", 'E' to "adefpq",
        'F' to "aefp", 'G' to "acdefq", 'J' to "bcde", 'L' to "def", 'M' to "bcefhj",
        'N' to "bcefhk", 'O' to "abcdef", 'P' to "abefpq", 'R' to "abefpqk", 'S' to "acdfpq",
        'T' to "ail", 'U' to "bcdef", 'V' to "efjm", 'Y' to "hjl",
    )

    private fun join(names: String) = Path().apply {
        names.forEach { addPath(strokes.getValue(it)) }
    }

    private val cache = glyphs.mapValues { join(it.value) }

    /** Contorno aceso de um caractere. */
    fun glyph(ch: Char): Path = cache.getValue(ch)

    /** Todos os tracos, para o fantasma dos segmentos apagados. */
    val allDigit: Path = join("abcdefg")
    val allLetter: Path = join("abcdefpqhijklm")
}
