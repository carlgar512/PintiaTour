package com.example.pintiatour

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

// Actividad que muestra una pantalla de carga con un GIF animado antes de navegar a la siguiente actividad
class LoadingAppActivity : AppCompatActivity() {
    private lateinit var mp: MediaPlayer
    /**
     * Esta actividad configura y muestra una pantalla de carga con un diseño "edge-to-edge",
     * aprovechando toda el área disponible de la pantalla. Inicializa los componentes gráficos
     * necesarios, carga una animación GIF para indicar que la aplicación está cargando,
     * y ajusta los márgenes de la vista principal para respetar las barras del sistema, como
     * la barra de estado y la barra de navegación. Después de un retraso de 4 segundos,
     * navega automáticamente a la siguiente actividad, proporcionando una experiencia fluida
     * de transición para el usuario.
     * Con un audio inicial en mp
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mp= MediaPlayer.create(this,R.raw.initialsong)
        mp.start()
        enableEdgeToEdge() // Habilita un diseño "edge-to-edge" para aprovechar al máximo la pantalla
        setContentView(R.layout.activity_loadingapp) // Asigna el diseño XML a esta actividad
        initComponents() // Inicializa los componentes de la interfaz

        // Ajusta márgenes de las vistas para respetar las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Libera los recursos del MediaPlayer cuando la actividad es destruida.
     * Este método se llama cuando la actividad está a punto de ser destruida, asegurándose de que
     * los recursos del MediaPlayer sean liberados correctamente para evitar fugas de memoria.
     */
    override fun onDestroy() {
        super.onDestroy()
        mp.release() // Libera los recursos del MediaPlayer
    }

    /**
     * Detiene la reproducción y libera los recursos del MediaPlayer cuando la actividad pasa a segundo plano.
     * Este método se llama cuando la actividad entra en pausa, asegurándose de que el MediaPlayer se detenga
     * y libere sus recursos si estaba en uso.
     */
    override fun onPause() {
        super.onPause()
        if (::mp.isInitialized && mp.isPlaying) {
            mp.stop()      // Detiene la reproducción de audio
            mp.release()   // Libera los recursos del MediaPlayer
        }
    }

    /**
     * Inicializa los componentes gráficos de la pantalla y configura la lógica para la pantalla de carga.
     * Este método se ejecuta cuando se crea la actividad, mostrando una animación de carga (GIF),
     * y después de un retraso de 4 segundos, navega a la siguiente pantalla.
     */
    private fun initComponents() {
        val gifImageView = findViewById<ImageView>(R.id.imagen_spinner_loading)

        // Usa la biblioteca Glide para cargar un GIF animado en un ImageView
        Glide.with(this)
            .asGif() // Especifica que la imagen es un GIF
            .load(R.drawable.animacion_spinner_carga) // Reemplaza con el nombre de tu GIF en res/drawable
            .into(gifImageView) // Carga el GIF en el ImageView

        // Configura un retraso de 4 segundos antes de navegar a la siguiente pantalla
        Handler(Looper.getMainLooper()).postDelayed({
            // Crea un Intent para iniciar la actividad SelectVisitActivity
            val siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            startActivity(siguientePantalla) // Navega a la siguiente actividad
        }, 7000) // Tiempo de retraso en milisegundos (7 segundos) para audio completo
    }
}
