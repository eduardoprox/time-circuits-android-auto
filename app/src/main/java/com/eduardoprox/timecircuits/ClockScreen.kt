package com.eduardoprox.timecircuits

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.graphics.drawable.IconCompat
import java.util.Calendar
import kotlin.math.min

/**
 * Tela do carro: o painel desenhado na superficie do modelo de navegacao.
 * O modelo exige ao menos um botao; o unico e o Noite, que o sistema esconde apos 10 s.
 */
class ClockScreen(carContext: CarContext) : Screen(carContext) {

    private val panel = PanelRenderer()
    private val handler = Handler(Looper.getMainLooper())
    private var surface: Surface? = null
    private var surfaceW = 0
    private var surfaceH = 0
    private var stable: Rect? = null

    private val tick = object : Runnable {
        override fun run() {
            val now = Calendar.getInstance()
            panel.setTime(now)
            render()
            handler.postDelayed(this, 1000L - now.get(Calendar.MILLISECOND))
        }
    }

    private val surfaceCallback = object : SurfaceCallback {
        override fun onSurfaceAvailable(container: SurfaceContainer) {
            surface = container.surface
            surfaceW = container.width
            surfaceH = container.height
            Log.i(TAG, "surface ${container.width}x${container.height} dpi=${container.dpi}")
            handler.removeCallbacks(tick)
            handler.post(tick)
        }

        override fun onStableAreaChanged(area: Rect) {
            Log.i(TAG, "stable=$area")
            stable = Rect(area)
            render()
        }

        override fun onVisibleAreaChanged(area: Rect) {
            Log.i(TAG, "visible=$area")
        }

        override fun onSurfaceDestroyed(container: SurfaceContainer) {
            Log.i(TAG, "surface destruida")
            handler.removeCallbacks(tick)
            surface = null
            container.surface?.release()
        }
    }

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(surfaceCallback)
    }

    private fun render() {
        val s = surface ?: return
        if (!s.isValid) return
        val area = stable?.takeUnless { it.isEmpty }?.let { s2 ->
            // A area estavel reserva o topo para o botao, que some apos 10 s. O painel usa
            // esse espaco; enquanto visivel, o botao fica por cima dele.
            Rect(s2.left, min(s2.top, s2.left), s2.right, s2.bottom)
        } ?: Rect(0, 0, surfaceW, surfaceH)
        panel.setArea(surfaceW, surfaceH, area)
        // Canvas em software: lockHardwareCanvas derruba o processo (RenderThread, "no surface")
        // quando o sistema troca a superficie, ao alternar entre tela cheia e cartao.
        val canvas = try {
            s.lockCanvas(null)
        } catch (e: RuntimeException) {
            Log.w(TAG, "superficie indisponivel: $e")
            return
        }
        try {
            panel.draw(canvas)
        } finally {
            s.unlockCanvasAndPost(canvas)
        }
    }

    override fun onGetTemplate(): Template {
        val night = panel.night
        val toggle = Action.Builder()
            .setTitle(if (night) "Dia" else "Noite")
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(carContext, if (night) R.drawable.ic_day else R.drawable.ic_night),
                ).build(),
            )
            .setOnClickListener {
                panel.night = !panel.night
                render()
                invalidate()
            }
            .build()
        return NavigationTemplate.Builder()
            .setActionStrip(ActionStrip.Builder().addAction(toggle).build())
            .build()
    }

    private companion object {
        const val TAG = "TimeCircuits"
    }
}
