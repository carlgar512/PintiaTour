package com.example.pintiatour

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable

// Actividad que muestra la pantalla de fin de la visita, permite interactuar con los botones para
// finalizar o regresar y maneja un temporizador que cuenta el tiempo restante.
class EndOfVisitActivity : AppCompatActivity() {

    // Declaración de variables para los elementos de la interfaz gráfica
    private lateinit var textoTipoVisita: TextView // Muestra el tipo de visita (express o personalizada)
    private lateinit var textoFinVisita: TextView // Texto que indica el fin de la visita

    private lateinit var textoContador: TextView // Muestra el tiempo restante en la visita
    private lateinit var btnVolver: Button // Botón para regresar al cuestionario
    private lateinit var btnFin: Button // Botón para finalizar la visita
    private lateinit var btnAudio: FloatingActionButton
    private lateinit var btnSalir: FloatingActionButton

    // Variables para manejar datos de la sesión y estado
    private var idiomaSeleccionado: String? = "" // Idioma seleccionado por el usuario
    private var visitaExpress: String? = "" // Identifica si es una visita express
    private var visitaPersonalizada: String? = "" // Identifica si es una visita personalizada
    private var tiempoVisita: Long? = 0 // Tiempo restante en segundos
    private var timerJob: Job? = null // Tarea de corrutina para manejar el temporizador

    // Datos relacionados con el tema de la visita
    private var cuestionarioElegido: Int? = 0 // Índice del cuestionario actual
    private var temasSeleccionados = BooleanArray(5) { false } // Array de temas seleccionados
    private var temaActual: String? = "" // Tema actual de la visita
    private var coleccionPantallas = mutableListOf<Pantalla>()
    private var posicionArrayPantallas: Int? = 0
    private var numPantallasContenidoTematica: Int? = 0

    private lateinit var mp: MediaPlayer
    private lateinit var mp1: MediaPlayer

    /**
     * Método llamado al crear la actividad.
     *
     * Este método se ejecuta cuando la actividad se crea. Inicializa los componentes de la interfaz gráfica,
     * recupera los datos de la sesión, configura el idioma y los eventos de los botones, y empieza el temporizador
     * para mostrar el tiempo restante de la visita. Además, ajusta los márgenes de la interfaz para asegurar
     * que el diseño respete las barras del sistema (notificación y navegación).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mp1 = MediaPlayer.create(this, R.raw.end1)
        mp1.start()
        mp1.isLooping=true
        enableEdgeToEdge() // Habilita un diseño "edge-to-edge" en la pantalla
        setContentView(R.layout.activity_end_of_visit) // Asigna el diseño de la actividad
        getSessionData() // Obtiene los datos de la sesión desde el Intent
        initComponents() // Inicializa los componentes de la interfaz
        changeLanguage() // Cambia el idioma de la interfaz según el seleccionado
        initListeners() // Configura los eventos de los botones
        updateTimer() // Inicia el temporizador para mostrar el tiempo restante

        // Ajusta los márgenes de la vista para respetar las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Libera los recursos del MediaPlayer cuando la actividad es destruida.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Libera los recursos del MediaPlayer al cerrar la actividad
        mp.release()
        mp1.release()
    }

    /**
     * Detiene la reproducción y libera los recursos del MediaPlayer cuando la actividad pasa a segundo plano.
     */
    override fun onPause() {
        super.onPause()
        if (::mp.isInitialized && mp.isPlaying) {
            mp.stop()       // Detiene la reproducción
            mp.release()    // Libera los recursos del MediaPlayer
        }
        mp1.release()
    }

    /**
     * Recupera los datos de la sesión desde los extras del Intent que inició la actividad.
     *
     * Este método extrae información sobre el idioma, tema, cuestionario seleccionado,
     * tipo de visita (personalizada o express), tiempo restante de la visita, y una colección de pantallas
     * asociadas a la temática. Los datos se obtienen del Intent y se asignan a las variables correspondientes
     * para ser utilizados en la actividad actual. Además, actualiza el tema actual basado en la pantalla seleccionada.
     */
    private fun getSessionData(){
        this.idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")
        this.temaActual = intent.extras?.getString("temaActual")
        this.cuestionarioElegido = intent.extras?.getInt("cuestionarioActual")
        if(!intent.extras?.getString("visitaPersonalizada").isNullOrEmpty()){
            // Si es una visita personalizada
            this.visitaPersonalizada = intent.extras?.getString("visitaPersonalizada")
        }
        else{
            // Si es una visita express
            this.visitaExpress = intent.extras?.getString("visitaExpress")
        }
        this.temasSeleccionados = intent.extras?.getBooleanArray("temasSeleccionados") as BooleanArray
        this.tiempoVisita = intent.extras?.getLong("tiempoVisita")
        coleccionPantallas = (intent.getSerializableExtra("coleccionPantallas") as? MutableList<Pantalla>)!!
        posicionArrayPantallas = intent.extras?.getInt("posicionArrayPantallas")
        Log.i("positionArr", posicionArrayPantallas.toString())
        this.temaActual = coleccionPantallas[posicionArrayPantallas as Int].temaActual
        numPantallasContenidoTematica = intent.extras?.getInt("numPantallasContenidoTematica")
    }

    /**
     * Inicializa los componentes de la interfaz gráfica de la pantalla de fin de visita.
     *
     * Este método se encarga de:
     * - Configurar el texto que muestra el tipo de visita (personalizada o express).
     * - Inicializar los elementos gráficos como los botones y el contador de tiempo.
     */
    private fun initComponents() {
        textoTipoVisita = findViewById(R.id.texto_visita_express_o_personalizada_fin_visita)
        // Configura el texto del tipo de visita según el caso
        if(visitaPersonalizada != "" && visitaPersonalizada != null){
            textoTipoVisita.text = visitaPersonalizada
        } else {
            textoTipoVisita.text = visitaExpress
        }
        textoFinVisita = findViewById(R.id.texto_fin_visita)
        textoContador = findViewById(R.id.texto_contador_tiempo_fin_visita)
        btnVolver = findViewById(R.id.boton_regresar_fin_visita)
        btnFin = findViewById(R.id.boton_fin_visita)
        btnAudio = findViewById(R.id.boton_audio)
        btnSalir= findViewById(R.id.boton_salir)
    }

    /**
     * Configura los listeners para los botones de la interfaz.
     *
     * Este método asigna las funciones a tres botones:
     * - **btnVolver**: Al hacer clic, detiene la reproducción de audio, detiene el temporizador,
     *   y navega a la actividad de la pantalla anterior (cuestionario anterior).
     * - **btnFin**: Al hacer clic, detiene la reproducción de audio y finaliza la visita,
     *   regresando a la actividad de selección de visita.
     * - **btnAudio**: Al hacer clic, inicia o reanuda la reproducción de audio.
     */
    private fun initListeners() {
        btnVolver.setOnClickListener {
            mp1.stop()
            mp.stop() // Detiene la reproducción de audio
            stopTimer() // Detiene el temporizador
            posicionArrayPantallas = posicionArrayPantallas!! - 1
            var siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla) // Navega al cuestionario anterior
        }

        btnFin.setOnClickListener {
            mp1.stop()
            mp.stop() // Detiene la reproducción de audio
            var siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla) // Finaliza la visita y vuelve a la selección de visita
        }

        btnAudio.setOnClickListener {
            mp1.setVolume(0.5f, 0.5f)
            mp.start() // Inicia o reanuda la reproducción de audio
        }

        btnSalir.setOnClickListener{
            mp1.stop()
            mp.stop() // Detiene la reproducción de audio
            var siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla) // Finaliza l
        }
    }

    /**
     * Inicia el temporizador para contar el tiempo restante de la visita.
     *
     * Este método utiliza una corrutina para actualizar el temporizador cada segundo. El tiempo restante se convierte
     * a formato hh:mm:ss y se muestra en la interfaz de usuario. Después de cada actualización, se espera un segundo
     * antes de restar 1 segundo al tiempo restante. El temporizador se detiene automáticamente cuando el tiempo alcanza 0.
     */
    private fun updateTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (tiempoVisita as Long > 0) {
                // Convierte el tiempo restante en formato hh:mm:ss
                val hours = (tiempoVisita as Long / 3600).toString().padStart(2, '0')
                val minutes = ((tiempoVisita as Long % 3600) / 60).toString().padStart(2, '0')
                val seconds = (tiempoVisita as Long % 60).toString().padStart(2, '0')
                val timeFormatted = "$hours:$minutes:$seconds"

                // Actualiza el texto del contador en la interfaz
                textoContador.text = timeFormatted

                // Pausa durante un segundo antes de decrementar el tiempo
                delay(1000L)
                var tiempo = tiempoVisita as Long
                tiempo--
                tiempoVisita = tiempo
            }
        }
    }

    /**
     * Detiene el temporizador cancelando la corrutina que lo maneja.
     *
     * Este método cancela la corrutina responsable de la actualización del temporizador y devuelve el tiempo restante de la visita.
     * El valor de `tiempoVisita` se devuelve para su posible uso posterior, por ejemplo, para mostrar el tiempo restante antes de la cancelación.
     *
     * @return El tiempo restante de la visita antes de que se detuviera el temporizador.
     */
    private fun stopTimer(): Long? {
        timerJob?.cancel() // Cancela la corrutina del temporizador
        return tiempoVisita
    }

    /**
     * Navega a la siguiente pantalla enviando los datos necesarios a través del Intent.
     *
     * Este método maneja la navegación entre pantallas, asegurándose de que se envíen los datos relevantes,
     * como el idioma seleccionado, el tema actual, el cuestionario actual, y el tipo de visita (express o personalizada).
     * Además, ajusta el estado de los temas seleccionados según si se está avanzando o retrocediendo en la visita.
     *
     * @param siguientePantalla El Intent que inicia la nueva actividad.
     * @param goBack Indica si la navegación es hacia atrás (true) o hacia adelante (false).
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        // Enviar los datos comunes
        siguientePantalla.apply {
            putExtra("idiomaSeleccionado", idiomaSeleccionado)
            putExtra("tiempoVisita", tiempoVisita)
            putExtra("temaActual", temaActual)
            putExtra("cuestionarioActual", cuestionarioElegido)
            putExtra("coleccionPantallas", coleccionPantallas as Serializable)
            putExtra("posicionArrayPantallas", posicionArrayPantallas)
            putExtra("numPantallasContenidoTematica", numPantallasContenidoTematica)

        }
        // Determina el tipo de visita (express o personalizada)
        if (!visitaExpress.isNullOrEmpty()) {
            siguientePantalla.putExtra("visitaExpress", visitaExpress)
        } else {
            siguientePantalla.putExtra("visitaPersonalizada", visitaPersonalizada)
        }

        val temas = temasSeleccionados

        // Se agregan los temas seleccionados antes de la navegación
        siguientePantalla.putExtra("temasSeleccionados", temas)

        // Inicia la nueva actividad
        startActivity(siguientePantalla)
    }

    /**
     * Cambia el idioma de los textos de la interfaz de usuario según el idioma seleccionado.
     * Dependiendo del valor de la variable 'idiomaSeleccionado', se actualizan los textos
     * de los elementos de la interfaz, como el tipo de visita, el texto de fin de visita,
     * y los botones de acción (Volver y Fin de visita).
     *
     * Los idiomas soportados son:
     * - Español ("esp")
     * - Inglés ("eng")
     * - Alemán ("deu")
     * - Francés ("fra")
     *
     * La función verifica si se está utilizando una visita personalizada o una visita express
     * y selecciona el texto adecuado para cada caso, garantizando que la interfaz se ajuste
     * correctamente al idioma y tipo de visita seleccionados.
     * Además asigna el archivo de audioguía correspondiente
     */
    private fun changeLanguage(){
        when(this.idiomaSeleccionado) {
            "esp" -> { // Español
                textoTipoVisita.text = getString(if (intent.extras?.getString("visitaPersonalizada") != null)
                    R.string.texto_select_visit_visita_personalizada
                else R.string.texto_visita_express)
                textoFinVisita.text = getString(R.string.texto_fin_visita)
                btnVolver.text = getString(R.string.texto_boton_regresar)
                btnFin.text = getString(R.string.texto_boton_fin_visita)
                mp= MediaPlayer.create(this,R.raw.endes)
            }
            "eng" -> { // Inglés
                textoTipoVisita.text = getString(if (intent.extras?.getString("visitaPersonalizada") != null)
                    R.string.texto_select_visit_visita_personalizada_eng
                else R.string.texto_visita_express_eng)
                textoFinVisita.text = getString(R.string.texto_fin_visita_eng)
                btnVolver.text = getString(R.string.texto_boton_regresar_eng)
                btnFin.text = getString(R.string.texto_boton_fin_visita_eng)
                mp= MediaPlayer.create(this,R.raw.enden)
            }
            "deu" -> { // Alemán
                textoTipoVisita.text = getString(if (intent.extras?.getString("visitaPersonalizada") != null)
                    R.string.texto_select_visit_visita_personalizada_deu
                else R.string.texto_visita_express_deu)
                textoFinVisita.text = getString(R.string.texto_fin_visita_deu)
                btnVolver.text = getString(R.string.texto_boton_regresar_deu)
                btnFin.text = getString(R.string.texto_boton_fin_visita_deu)
                mp= MediaPlayer.create(this,R.raw.endal)
            }
            "fra" -> { // Francés
                textoTipoVisita.text = getString(if (intent.extras?.getString("visitaPersonalizada") != null)
                    R.string.texto_select_visit_visita_personalizada_fra
                else R.string.texto_visita_express_fra)
                textoFinVisita.text = getString(R.string.texto_fin_visita_fra)
                btnVolver.text = getString(R.string.texto_boton_regresar_fra)
                btnFin.text = getString(R.string.texto_boton_fin_visita_fra)
                mp= MediaPlayer.create(this,R.raw.endfr)
            }
        }
    }
}
