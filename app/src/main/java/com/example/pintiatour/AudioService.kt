package com.example.pintiatour

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.IBinder

class AudioService : Service() {
    private var backgroundPlayer: MediaPlayer? = null
    private var guidePlayer: MediaPlayer? = null
    // Receptores para escuchar cuando la pantalla se apaga o se enciende
    private val screenOffReceiver = ScreenOffReceiver()
    private val screenOnReceiver = ScreenOnReceiver()

    /**
     * Este método se llama cuando el servicio es creado.
     * Registra los receptores para detectar cuando la pantalla se apaga y se enciende.
     */
    override fun onCreate() {
        super.onCreate()
        // Registra el receptor para detectar cuando la pantalla se apaga
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
        val filterOn = IntentFilter(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenOnReceiver, filterOn)
    }

    /**
     * Este método se llama cuando el servicio recibe un intento de inicio (startService).
     * Dependiendo de la acción proporcionada en el Intent, realiza la acción correspondiente
     * como reproducir música de fondo, reproducir la audioguía, pausar o reanudar la música.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("ACTION")

        when (action) {

            "PLAY_BACKGROUND" -> {
                val audioResId = intent.getIntExtra("AUDIO_RES_ID", -1)
                val isLooping = intent.getBooleanExtra("IS_LOOPING", true)
                if (audioResId != -1) {
                    backgroundPlayer?.release()
                    backgroundPlayer = MediaPlayer.create(this, audioResId).apply {
                        this.isLooping = isLooping
                        setVolume(1.0f, 1.0f)
                        start()
                    }
                }
            }

            "PLAY_GUIDE" -> {
                val guideResId = intent.getIntExtra("AUDIO_RES_ID", -1)
                if (guideResId != -1) {
                    guidePlayer?.release()
                    guidePlayer = MediaPlayer.create(this, guideResId).apply {
                        setOnCompletionListener {
                            // Restaura el volumen de la música de fondo cuando termine la audioguía
                            backgroundPlayer?.setVolume(1.0f, 1.0f)
                        }
                        setVolume(1.0f, 1.0f)
                        start()
                    }
                    // Baja el volumen de la música de fondo mientras se reproduce la audioguía
                    backgroundPlayer?.setVolume(0.3f, 0.3f)
                }
            }
            "PAUSE" -> {
                backgroundPlayer?.pause()
                guidePlayer?.pause()
            }
            "RESUME" -> {
                backgroundPlayer?.start()
                guidePlayer?.start()
            }
            "STOP" -> {
                backgroundPlayer?.release()
                guidePlayer?.release()
                stopSelf()
            }
        }

        return START_STICKY
    }

    /**
     * Este método se llama cuando el servicio es destruido.
     * Se asegura de liberar los recursos de los reproductores de audio para evitar fugas de memoria.
     */
    override fun onDestroy() {
        super.onDestroy()
        backgroundPlayer?.release()
        guidePlayer?.release()
    }

    /**
     * Este método se llama cuando el servicio es enlazado con una actividad (en este caso no se usa).
     * Retorna null ya que no se necesita una interfaz de enlace en este servicio.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Receptor de difusión para detectar cuando la pantalla se apaga.
     * Pausa la música de fondo y la audioguía cuando la pantalla se apaga.
     */
    inner class ScreenOffReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                // Pausar la música si la pantalla se apaga
                backgroundPlayer?.pause()
                guidePlayer?.pause()
            }
        }
    }

    /**
     * Receptor de difusión para detectar cuando la pantalla se enciende.
     * Reanuda la música de fondo y la audioguía cuando la pantalla se enciende.
     */
    inner class ScreenOnReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                // Reanudar la música si la pantalla se enciende
                backgroundPlayer?.start()
                guidePlayer?.start()
            }
        }
    }
}