package com.eduardoprox.timecircuits

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import java.util.Calendar

/** Tela cheia com o painel. Atualiza uma vez por segundo enquanto estiver visivel. */
class ClockActivity : Activity() {

    private lateinit var circuits: CircuitsView
    private val handler = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() {
            val now = Calendar.getInstance()
            circuits.update(now)
            handler.postDelayed(this, 1000L - now.get(Calendar.MILLISECOND))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        circuits = CircuitsView(this)
        setContentView(circuits)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.insetsController?.let {
            it.hide(WindowInsets.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(tick)
    }

    override fun onStop() {
        handler.removeCallbacks(tick)
        super.onStop()
    }
}
