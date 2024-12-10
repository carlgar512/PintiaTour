package com.example.pintiatour

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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
import kotlin.random.Random

class ShowInitialVisitPointActivity : AppCompatActivity() {

    private lateinit var textoTipoVisita: TextView
    private lateinit var textoPuntoInicial: TextView
    private lateinit var textoContador: TextView
    private lateinit var btnVolver: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnSalir: FloatingActionButton
    private lateinit var btnAudio: FloatingActionButton
    private var idiomaSeleccionado: String? = ""
    private var visitaExpress: String? = ""
    private var visitaPersonalizada: String? = ""
    private var tiempoVisita: Long? = 3600
    private var temasSeleccionados = BooleanArray(5) { false }
    private var timerJob: Job? = null
    private lateinit var audioIntent: Intent
    private lateinit var audioIntent1: Intent
    private var coleccionPantallas = mutableListOf<Pantalla>()
    private var posicionArrayPantallas: Int? = 0
    private var numPantallasContenidoTematica: Int? = 0


    /**
     * Este método es llamado cuando la actividad es creada. Se encarga de inicializar todos los componentes necesarios
     * para la pantalla de la visita inicial. Esto incluye configurar la interfaz de usuario, obtener los datos de la sesión,
     * cambiar el idioma, configurar los escuchadores de eventos, y actualizar el temporizador.
     *
     * - `enableEdgeToEdge()`: Activa el modo edge-to-edge para que el contenido se extienda a lo largo de toda la pantalla.
     * - `setContentView(R.layout.activity_show_initial_visit_point)`: Establece el layout de la actividad.
     * - `getSessionData()`: Obtiene los datos de la sesión desde el Intent.
     * - `initComponents()`: Inicializa todos los componentes de la interfaz de usuario.
     * - `changeLanguage()`: Cambia el idioma de la interfaz de acuerdo al idioma seleccionado.
     * - `initListeners()`: Establece los escuchadores de eventos para los botones y otras interacciones de la UI.
     * - `updateTimer()`: Inicia o actualiza el temporizador que muestra el tiempo de la visita restante.
     * - `ViewCompat.setOnApplyWindowInsetsListener()`: Ajusta los márgenes de la vista principal para que no se superpongan con las barras de sistema (como la barra de estado o de navegación).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Crea y reproduce el MediaPlayer
        audioIntent = Intent(this, AudioService::class.java)
        audioIntent1 = Intent(this, AudioService::class.java)
        audioIntent.putExtra("AUDIO_RES_ID", R.raw.flute) // Recurso de audio
        audioIntent.putExtra("ACTION", "PLAY_BACKGROUND")// Indica que debe reproducirse en bucle
        audioIntent.putExtra("IS_LOOPING", true)
        startService(audioIntent)
        enableEdgeToEdge()
        setContentView(R.layout.activity_show_initial_visit_point)
        getSessionData()
        initComponents()
        changeLanguage()
        initListeners()
        updateTimer()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Libera los recursos del MediaPlayer al destruir la actividad.
     * Este método asegura que el MediaPlayer se libere correctamente cuando la actividad se destruye
     * para evitar fugas de memoria.
     */
    override fun onDestroy() {
        super.onDestroy()
        audioIntent.putExtra("ACTION", "STOP")
        audioIntent1.putExtra("ACTION", "STOP")
        startService(audioIntent)
        startService(audioIntent1)
    }

    /**
     * Detiene la reproducción del audio y libera los recursos del MediaPlayer al pausar la actividad.
     * Este método asegura que el audio se detenga y se liberen los recursos utilizados por el MediaPlayer
     * cuando la actividad pasa a segundo plano.
     */
    override fun onPause() {
        super.onPause()
        audioIntent.putExtra("ACTION", "PAUSE")
        audioIntent1.putExtra("ACTION", "PAUSE")
        startService(audioIntent)
        startService(audioIntent1)
    }

    /**
     * Reanuda la reproducción del audio cuando la actividad vuelve a primer plano.
     * Verifica si el reproductor no está reproduciendo y lo inicia.
     */
    override fun onResume() {
        super.onResume()
        audioIntent.putExtra("ACTION", "RESUME")
        audioIntent1.putExtra("ACTION", "RESUME")
        startService(audioIntent)
        startService(audioIntent1)
    }

    /**
     * Este método obtiene los datos de la sesión desde los extras del intent.
     * Dependiendo de la información disponible, asigna los valores de idioma, tipo de visita,
     * tiempo de visita y los temas seleccionados a las variables correspondientes.
     *
     * - `idiomaSeleccionado`: Obtiene el idioma seleccionado de los extras.
     * - `visitaPersonalizada`: Si está presente en los extras, se asigna como la visita personalizada.
     * - `tiempoVisita`: Si la visita es personalizada, también se obtiene el tiempo de visita.
     * - `temasSeleccionados`: Obtiene un arreglo de booleanos con los temas seleccionados para la visita.
     * - `visitaExpress`: Si la visita no es personalizada, se asigna la visita express.
     */
    private fun getSessionData(){
        this.idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")
        if(intent.extras?.getString("visitaPersonalizada") != null){
            this.visitaPersonalizada = intent.extras?.getString("visitaPersonalizada")
            this.tiempoVisita = intent.extras?.getLong("tiempoVisita")
        }
        else{
            this.visitaExpress = intent.extras?.getString("visitaExpress")
        }
        this.temasSeleccionados = intent.extras?.getBooleanArray("temasSeleccionados") as BooleanArray
        coleccionPantallas = (intent.getSerializableExtra("coleccionPantallas") as? MutableList<Pantalla>)!!
        posicionArrayPantallas = intent.extras?.getInt("posicionArrayPantallas")
        numPantallasContenidoTematica = intent.extras?.getInt("numPantallasContenidoTematica")

    }

    /**
     * Este método inicializa los componentes de la interfaz de usuario de la actividad.
     * Se encarga de asignar las vistas correspondientes a las variables y actualizar
     * el texto de los elementos según el tipo de visita.
     *
     * - `textoTipoVisita`: Muestra el tipo de visita, ya sea "Visita Personalizada" o "Visita Express".
     * - `textoPuntoInicial`: Muestra el texto relacionado con el punto inicial de la visita.
     * - `textoContador`: Muestra el tiempo restante para la visita en formato de contador.
     * - `btnVolver`: Asocia el botón "Volver" a la variable correspondiente.
     * - `btnSiguiente`: Asocia el botón "Siguiente" a la variable correspondiente.
     */
    private fun initComponents() {
        textoTipoVisita = findViewById(R.id.texto_visita_express_o_personalizada_punto_inicial)
        if(!visitaPersonalizada.isNullOrEmpty()){
            textoTipoVisita.text = visitaPersonalizada
        }
        else{
            textoTipoVisita.text = visitaExpress
        }
        textoPuntoInicial = findViewById(R.id.texto_punto_inicial)
        textoContador = findViewById(R.id.texto_contador_tiempo_visita_punto_inicial)
        btnVolver = findViewById(R.id.boton_regresar_punto_inicial)
        btnSiguiente = findViewById(R.id.boton_siguiente_pantalla_punto_inicial)
        btnAudio = findViewById(R.id.boton_audio)
        btnSalir= findViewById(R.id.boton_salir)
    }

    /**
     * Este método inicializa los oyentes de los botones en la interfaz de usuario.
     *
     * - `btnVolver`: Configura un `OnClickListener` para el botón "Volver". Al hacer clic, se detiene la reproducción del audio
     *   y se navega hacia la actividad `QuickAdviseActivity`.
     *
     * - `btnSiguiente`: Configura un `OnClickListener` para el botón "Siguiente". Al hacer clic, se detiene el temporizador,
     *   se detiene la reproducción del audio y luego navega hacia la siguiente pantalla de la colección de pantallas,
     *   determinada por la variable `posicionArrayPantallas`.
     *
     * - `btnAudio`: Configura un `OnClickListener` para el botón "Audio". Al hacer clic, reinicia la reproducción del audio.
     */
    private fun initListeners() {
        btnVolver.setOnClickListener {
            audioIntent.putExtra("ACTION", "STOP")
            audioIntent1.putExtra("ACTION", "STOP")
            startService(audioIntent)
            startService(audioIntent1)
            val siguientePantalla = Intent(this, QuickAdviseActivity::class.java)
            navigateToNextScreen(siguientePantalla)
        }

        btnSiguiente.setOnClickListener {
            stopTimer()
            audioIntent.putExtra("ACTION", "STOP")
            audioIntent1.putExtra("ACTION", "STOP")
            startService(audioIntent)
            startService(audioIntent1)
            posicionArrayPantallas = posicionArrayPantallas!! + 1
            val siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla)
        }

        btnAudio.setOnClickListener{
            audioIntent1.putExtra("ACTION", "PLAY_GUIDE")
            startService(audioIntent1)
        }

        btnSalir.setOnClickListener{
            audioIntent.putExtra("ACTION", "STOP")
            audioIntent1.putExtra("ACTION", "STOP")
            startService(audioIntent)
            startService(audioIntent1)
            var siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla) // Finaliza l
        }
    }

    /**
     * Este método inicia un temporizador que actualiza un `TextView` cada segundo,
     * mostrando el tiempo restante en formato `hh:mm:ss`. El temporizador decrementa
     * el valor de `tiempoVisita` en cada ciclo de un segundo hasta que el tiempo llegue a cero.
     *
     * Se ejecuta dentro de un `CoroutineScope` utilizando el despachador `Dispatchers.Main`
     * para actualizar la interfaz de usuario (UI) de manera segura.
     */
    private fun updateTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (tiempoVisita as Long > 0) {
                // Formatear duración en hh:mm:ss
                val hours = (tiempoVisita as Long / 3600).toString().padStart(2, '0')
                val minutes = ((tiempoVisita as Long % 3600) / 60).toString().padStart(2, '0')
                val seconds = (tiempoVisita as Long % 60).toString().padStart(2, '0')
                val timeFormatted = "$hours:$minutes:$seconds"

                // Actualizar el TextView con el tiempo restante
                textoContador.text = timeFormatted

                // Esperar un segundo antes de decrementar
                delay(1000L)
                var tiempo = tiempoVisita as Long
                tiempo--
                tiempoVisita = tiempo
            }
        }
    }

    /**
     * Este método detiene el temporizador cancelando el `Job` que se está ejecutando
     * y devuelve el tiempo de visita actual.
     * Si el temporizador estaba en ejecución, se cancela, y se retorna el valor
     * de `tiempoVisita`.
     *
     * @return El tiempo de visita actual, en milisegundos.
     */
    private fun stopTimer(): Long? {
        timerJob?.cancel()
        return tiempoVisita
    }

    /**
     * Este método navega a la siguiente pantalla y pasa los datos de sesión necesarios
     * a través de un `Intent` para la nueva actividad. Dependiendo de si es una visita
     * express o personalizada, se incluyen diferentes datos en el `Intent`.
     *
     * - El idioma seleccionado, el tiempo de visita y los temas seleccionados siempre se pasan.
     * - Si la visita es express, también se pasa el tipo de visita express.
     * - Si la visita es personalizada, se pasa el tipo de visita personalizada.
     *
     * @param siguientePantalla El `Intent` que se utiliza para iniciar la siguiente actividad.
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        siguientePantalla.putExtra("idiomaSeleccionado", idiomaSeleccionado)
        siguientePantalla.putExtra("tiempoVisita", tiempoVisita)
        siguientePantalla.putExtra("coleccionPantallas", coleccionPantallas as Serializable)
        siguientePantalla.putExtra("posicionArrayPantallas", posicionArrayPantallas)
        siguientePantalla.putExtra("numPantallasContenidoTematica", numPantallasContenidoTematica)

        if(!visitaExpress.isNullOrEmpty()){
            siguientePantalla.putExtra("visitaExpress", visitaExpress)
        }
        else{
            siguientePantalla.putExtra("visitaPersonalizada", visitaPersonalizada)
        }
        siguientePantalla.putExtra("temasSeleccionados", temasSeleccionados)
        startActivity(siguientePantalla)
    }

    /**
     * Este método cambia el idioma de la interfaz de usuario en función del idioma seleccionado
     * y actualiza los textos correspondientes para cada tipo de visita (express o personalizada).
     *
     * Dependiendo del valor de la variable `idiomaSeleccionado`, se asignan los textos correspondientes
     * para los elementos de la pantalla como los botones y los textos de las vistas.
     */
    private fun changeLanguage() {
        when (this.idiomaSeleccionado) {
            "esp" -> {
                textoTipoVisita.text = if (intent.extras?.getString("visitaPersonalizada") != null) {
                    getString(R.string.texto_select_visit_visita_personalizada)
                } else {
                    getString(R.string.texto_visita_express)
                }
                audioIntent1.putExtra("AUDIO_RES_ID", R.raw.inicioes)
                textoPuntoInicial.text = getString(R.string.texto_inicial_punto_inicial)
                btnVolver.text = getString(R.string.texto_boton_regresar)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente)
            }
            "eng" -> {
                textoTipoVisita.text = if (intent.extras?.getString("visitaPersonalizada") != null) {
                    getString(R.string.texto_select_visit_visita_personalizada_eng)
                } else {
                    getString(R.string.texto_visita_express_eng)
                }
                audioIntent1.putExtra("AUDIO_RES_ID", R.raw.inicioen)
                textoPuntoInicial.text = getString(R.string.texto_inicial_punto_inicial_eng)
                btnVolver.text = getString(R.string.texto_boton_regresar_eng)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente_eng)
            }
            "deu" -> {
                textoTipoVisita.text = if (intent.extras?.getString("visitaPersonalizada") != null) {
                    getString(R.string.texto_select_visit_visita_personalizada_deu)
                } else {
                    getString(R.string.texto_visita_express_deu)
                }
                audioIntent1.putExtra("AUDIO_RES_ID", R.raw.inicioal)
                textoPuntoInicial.text = getString(R.string.texto_inicial_punto_inicial_deu)
                btnVolver.text = getString(R.string.texto_boton_regresar_deu)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente_deu)
            }
            "fra" -> {
                textoTipoVisita.text = if (intent.extras?.getString("visitaPersonalizada") != null) {
                    getString(R.string.texto_select_visit_visita_personalizada_fra)
                } else {
                    getString(R.string.texto_visita_express_fra)
                }
                audioIntent1.putExtra("AUDIO_RES_ID", R.raw.iniciofr)
                textoPuntoInicial.text = getString(R.string.texto_inicial_punto_inicial_fra)
                btnVolver.text = getString(R.string.texto_boton_regresar_fra)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente_fra)
            }
        }
    }

}