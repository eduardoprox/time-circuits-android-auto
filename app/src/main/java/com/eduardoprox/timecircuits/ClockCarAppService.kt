package com.eduardoprox.timecircuits

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/** Entrada do app no Android Auto. */
class ClockCarAppService : CarAppService() {

    // App de uso pessoal: aceita qualquer host, inclusive o DHU.
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = ClockScreen(carContext)
    }
}
