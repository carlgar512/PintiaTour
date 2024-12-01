package com.example.pintiatour


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
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

// Actividad que muestra el contenido del siguiente punto de la visita, con temporizador y navegación
class NextPointContentActivity : AppCompatActivity() {

    // Componentes de la interfaz de usuario
    private lateinit var textoTipoVisita: TextView
    private lateinit var imgPuntoSiguienteContenido: ImageView
    private lateinit var textoTematicaVisita: TextView
    private lateinit var textoContenidoTematicaVisita: TextView
    private lateinit var textoContador: TextView
    private lateinit var btnVolver: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnAudio: FloatingActionButton
    private lateinit var btnSalir: FloatingActionButton

    // Datos para controlar la sesión y la visita
    private var idiomaSeleccionado: String? = ""
    private var visitaExpress: String? = ""
    private var visitaPersonalizada: String? = ""
    private var tiempoVisita: Long? = 0
    private var timerJob: Job? = null
    private lateinit var mp: MediaPlayer
    private lateinit var mp1: MediaPlayer

    // Temas y pantallas seleccionadas
    private var temasSeleccionados = BooleanArray(5) { false }
    private var temaActual: String? = ""
    private var coleccionPantallas = mutableListOf<Pantalla>()
    private var posicionArrayPantallas: Int? = 0
    private var numeroPantallaContenidoActual: Int? = 0
    private var numPantallasContenidoTematica: Int? = 0

    /**
     * Método llamado cuando se crea la actividad.
     *
     * Este es el punto de entrada principal para la actividad. Se encarga de:
     *
     * - Inicializar la interfaz de usuario y configurar el audio de fondo.
     * - Recuperar los datos de la sesión pasados a través del Intent, como la información sobre el idioma y la visita.
     * - Inicializar los componentes gráficos de la actividad, como botones y vistas interactivas.
     * - Configurar los listeners para manejar las interacciones con los elementos de la interfaz.
     * - Modificar los componentes según sea necesario y actualizar el temporizador.
     * - Ajustar los márgenes para tener en cuenta las barras del sistema (e.g. barras de estado y navegación) con un diseño sin bordes.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i: Int = Random.nextInt(1, 16)  // Genera un número aleatorio entre 1 y 9.

        // Construye dinámicamente el identificador del recurso
        val resId = resources.getIdentifier("music$i", "raw", packageName)

        // Crea y reproduce el MediaPlayer
        mp1 = MediaPlayer.create(this, resId)
        mp1.start()
        mp1.isLooping=true
        // Habilita el diseño sin bordes (full-screen) para la actividad
        enableEdgeToEdge()

        // Establece el diseño de la actividad a partir del archivo XML especificado
        setContentView(R.layout.activity_next_point_content)

        // Recupera los datos de la sesión pasados a través del Intent
        getSessionData()

        // Inicializa los componentes gráficos de la interfaz de usuario
        initComponents()

        // Configura los listeners para los botones y otros elementos interactivos de la interfaz
        initListeners()
        modifyComponents()
        updateTimer()

        // Ajusta los márgenes de la actividad para tener en cuenta las barras del sistema (e.g. notificaciones, barra de navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()) // Recupera el insets de las barras del sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) // Establece los márgenes con el insets
            insets // Devuelve el insets para que la vista lo maneje adecuadamente
        }
    }

    /**
     * Método llamado cuando la actividad pasa a primer plano.
     *
     * Se asegura de que la reproducción del audio se reanude cuando el usuario regresa a la actividad,
     * siempre y cuando el audio no se esté reproduciendo ya.
     */
    override fun onResume() {
        super.onResume()
        if (!mp1.isPlaying) {
            mp1.start()  // Reanuda la reproducción del audio
        }
    }

    /**
     * Método llamado cuando la actividad pasa a segundo plano.
     *
     * Detiene temporalmente la reproducción del audio y libera los recursos del MediaPlayer
     * asociado con el audio principal (mp), si es necesario, al salir de la pantalla.
     */
    override fun onPause() {
        super.onPause()
        if (mp1.isPlaying) {
            mp1.pause()  // Pausa la reproducción del audio
        }
        if (::mp.isInitialized && mp.isPlaying) {
            mp.stop()     // Detiene la reproducción del audio principal
            mp.release()  // Libera los recursos del MediaPlayer principal
        }
    }

    /**
     * Método llamado cuando la actividad es destruida.
     *
     * Libera todos los recursos utilizados por los objetos MediaPlayer al cerrar la actividad,
     * asegurando una correcta gestión de la memoria.
     */
    override fun onDestroy() {
        super.onDestroy()
        mp1.release()  // Libera los recursos del MediaPlayer de fondo
        mp.release()   // Libera los recursos del MediaPlayer principal
    }

    /**
     * Recupera los datos de la sesión desde los extras del Intent y establece las variables necesarias.
     *
     * Esta función extrae los datos pasados a través del Intent cuando la actividad es iniciada.
     * Dependiendo de si la visita es personalizada o express, los datos son tratados de manera diferente.
     * Además, gestiona la navegación hacia la siguiente pantalla de acuerdo con el número de pantalla de contenido
     * y el tema actual.
     *
     * - **idiomaSeleccionado**: Se obtiene el idioma que fue seleccionado previamente.
     * - **temaActual**: Se recupera el tema actual de la visita, basado en la pantalla seleccionada.
     * - **visitaPersonalizada / visitaExpress**: Se obtienen los datos relacionados con el tipo de visita (personalizada o express).
     * - **temasSeleccionados**: Se recupera el arreglo de booleanos que indica qué temas han sido seleccionados.
     * - **tiempoVisita**: Se obtiene el tiempo restante de la visita.
     * - **numeroPantallaContenidoActual**: Se recupera el número de la pantalla actual, lo que ayuda a determinar si se debe avanzar o no.
     * - **coleccionPantallas**: Se obtiene la lista de pantallas para la visita, que determina la navegación.
     * - **posicionArrayPantallas**: Se obtiene la posición actual en la lista de pantallas.
     * - **numPantallasContenidoTematica**: Se obtiene el número total de pantallas de contenido para el tema seleccionado.
     *
     * Dependiendo del **temaActual** y del **numeroPantallaContenidoActual**, se puede decidir avanzar
     * a una pantalla de cuestionarios (por ejemplo, `NextPointQuestionaryActivity`) y detener el temporizador.
     */
    private fun getSessionData() {
        this.idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")

        // Recupera los datos dependiendo de si es una visita personalizada o express
        if (!intent.extras?.getString("visitaPersonalizada").isNullOrEmpty()) {
            this.visitaPersonalizada = intent.extras?.getString("visitaPersonalizada")
        } else {
            this.visitaExpress = intent.extras?.getString("visitaExpress")
        }
        this.temasSeleccionados = intent.extras?.getBooleanArray("temasSeleccionados") as BooleanArray
        this.tiempoVisita = intent.extras?.getLong("tiempoVisita")
        this.numeroPantallaContenidoActual = intent.extras?.getInt("numeroPantallaContenido")
        coleccionPantallas = (intent.getSerializableExtra("coleccionPantallas") as? MutableList<Pantalla>)!!
        posicionArrayPantallas = intent.extras?.getInt("posicionArrayPantallas")
        this.temaActual = coleccionPantallas[posicionArrayPantallas as Int].temaActual
        numPantallasContenidoTematica = intent.extras?.getInt("numPantallasContenidoTematica")
    }

    /**
     * Inicializa los componentes de la interfaz de usuario de la actividad.
     *
     * Esta función se encarga de buscar e inicializar todos los componentes visuales de la actividad, como
     * los TextViews, botones e imágenes. Además, configura el texto del tipo de visita (personalizada o express)
     * según los valores proporcionados y asigna las vistas correspondientes a las variables de la clase.
     *
     * - **textoTipoVisita**: Se configura el texto que indica el tipo de visita, ya sea personalizada o express.
     * - **imgPuntoSiguienteContenido**: Inicializa la imagen que se mostrará para el contenido de la visita.
     * - **textoTematicaVisita**: Inicializa el texto que muestra la temática de la visita.
     * - **textoContenidoTematicaVisita**: Inicializa el texto principal que describe el contenido de la temática de la visita.
     * - **textoContador**: Inicializa el contador de tiempo restante para la visita.
     * - **btnVolver**: Inicializa el botón para volver a la pantalla anterior.
     * - **btnSiguiente**: Inicializa el botón para avanzar a la siguiente pantalla.
     */
    private fun initComponents() {
        textoTipoVisita = findViewById(R.id.texto_visita_express_o_personalizada_punto_siguiente_contenido)
        // Define el texto del tipo de visita según si es personalizada o express
        if (!visitaPersonalizada.isNullOrEmpty()) {
            textoTipoVisita.text = visitaPersonalizada
        } else {
            textoTipoVisita.text = visitaExpress
        }
        // Inicializa los demás componentes gráficos
        imgPuntoSiguienteContenido = findViewById(R.id.imagen_contenido_punto_siguiente_contenido)
        textoTematicaVisita = findViewById(R.id.texto_tematica_punto_siguiente_contenido)
        textoContenidoTematicaVisita = findViewById(R.id.texto_principal_punto_siguiente_contenido)
        textoContador = findViewById(R.id.texto_contador_tiempo_visita_punto_siguiente_contenido)
        btnVolver = findViewById(R.id.boton_regresar_punto_siguiente_contenido)
        btnSiguiente = findViewById(R.id.boton_siguiente_pantalla_punto_siguiente_contenido)
        btnAudio = findViewById(R.id.boton_audio)
        btnSalir= findViewById(R.id.boton_salir)
    }

    /**
     * Este método inicializa los oyentes de los botones en la interfaz de usuario.
     *
     * - **btnVolver**: Cuando se hace clic, detiene ambos reproductores de audio, ajusta el índice de las pantallas,
     *   y navega a la pantalla anterior.
     *
     * - **btnSiguiente**: Al hacer clic, detiene ambos reproductores de audio, detiene el temporizador, ajusta el índice de las pantallas
     *   y navega a la siguiente pantalla en la colección.
     *
     * - **btnAudio**: Detiene el reproductor de audio de fondo y asegura que el reproductor principal comience a reproducir si no está activo.
     */
    private fun initListeners() {
        // Listener para el botón "Volver" que regresa a la pantalla anterior
        btnVolver.setOnClickListener {
            // Detener ambos audios
            mp.stop()
            mp1.stop()

            // Ajustar el índice para regresar a la pantalla anterior
            posicionArrayPantallas = posicionArrayPantallas?.minus(1) ?: 0
            numeroPantallaContenidoActual = numeroPantallaContenidoActual?.minus(1) ?: 0

            // Crear la siguiente pantalla (anterior) y navegar
            val siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla)
        }

        // Listener para el botón "Siguiente" que avanza a la siguiente pantalla
        btnSiguiente.setOnClickListener {
            // Detener ambos audios y el temporizador
            mp.stop()
            mp1.stop()
            stopTimer()

            // Ajustar el índice para avanzar a la siguiente pantalla
            posicionArrayPantallas = posicionArrayPantallas?.plus(1) ?: 0
            numeroPantallaContenidoActual = numeroPantallaContenidoActual?.plus(1) ?: 0

            // Crear la siguiente pantalla y navegar
            val siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla)
        }

        // Listener para el botón de audio
        btnAudio.setOnClickListener {

            if (!::mp.isInitialized || !mp.isPlaying) {
                mp1.setVolume(0.4F,0.4F) // Detener el segundo reproductor de audio
                mp.start() // Iniciar el primer reproductor de audio si no está ya reproduciendo
            }
        }

        btnSalir.setOnClickListener{
            mp1.stop()
            mp.stop() // Detiene la reproducción de audio
            var siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla) // Finaliza l
        }
    }

    /**
     * Actualiza el temporizador que cuenta el tiempo restante de la visita.
     *
     * Esta función utiliza coroutines para actualizar el temporizador cada segundo.
     * El temporizador decrementa el tiempo restante (`tiempoVisita`) y actualiza la interfaz
     * con el formato de tiempo en horas, minutos y segundos. La actualización se realiza
     * en el hilo principal mediante el uso de `Dispatchers.Main` para asegurar que los
     * cambios en la UI se realicen de manera correcta.
     *
     * El formato del tiempo se presenta como "hh:mm:ss", y se actualiza cada segundo.
     * Cuando el tiempo llega a cero, el ciclo se detiene.
     */
    private fun updateTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (tiempoVisita as Long > 0) {
                // Formatea la duración del tiempo en horas, minutos y segundos
                val hours = (tiempoVisita as Long / 3600).toString().padStart(2, '0')
                val minutes = ((tiempoVisita as Long % 3600) / 60).toString().padStart(2, '0')
                val seconds = (tiempoVisita as Long % 60).toString().padStart(2, '0')
                val timeFormatted = "$hours:$minutes:$seconds"

                // Actualiza el contador de tiempo en la interfaz
                textoContador.text = timeFormatted

                // Espera un segundo antes de continuar
                delay(1000L)
                var tiempo = tiempoVisita as Long
                tiempo-- // Decrementa el tiempo restante
                tiempoVisita = tiempo
            }
        }
    }

    /**
     * Detiene el temporizador y devuelve el tiempo restante.
     *
     * Este método cancela la tarea asociada al temporizador (si está activa), utilizando la referencia
     * `timerJob`, que es la tarea en ejecución dentro de un `CoroutineScope`. Después de cancelar
     * el temporizador, devuelve el valor actual de `tiempoVisita`, que representa el tiempo restante
     * de la visita.
     *
     * @return El tiempo restante de la visita en formato `Long`. Si el temporizador fue detenido,
     *         se devuelve el valor de `tiempoVisita` al momento de la cancelación.
     */
    private fun stopTimer(): Long? {
        timerJob?.cancel()
        return tiempoVisita
    }

    /**
     * Navega a la siguiente pantalla, pasando los datos relevantes a través de un Intent.
     *
     * Este método prepara el Intent que se utilizará para iniciar la siguiente actividad. El Intent contiene
     * información crucial que se pasa entre las pantallas, como el idioma seleccionado, el tiempo restante de la visita,
     * el número de la pantalla actual y el tema actual. Dependiendo del tipo de visita (express o personalizada),
     * también se pasan datos específicos relacionados con la visita.
     *
     * Parámetros:
     * @param siguientePantalla El Intent que se utilizará para iniciar la siguiente actividad.
     * @param goBack Booleano que indica si se está retrocediendo a la pantalla anterior (goBack = true)
     *               o avanzando a la siguiente (goBack = false).
     *
     * La función maneja la lógica para:
     * - Si la visita es una visita express, agrega la variable `visitaExpress` al Intent.
     * - Si la visita es personalizada, agrega la variable `visitaPersonalizada` al Intent.
     * - Dependiendo del valor de `goBack`, se agregan los datos de los temas seleccionados:
     *     - Si `goBack` es falso, se agregan los temas seleccionados directamente.
     *     - Si `goBack` es verdadero, se restaura el estado de los temas seleccionados antes de agregarlos.
     *
     * Al finalizar, inicia la actividad correspondiente usando el Intent configurado.
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        // Se agregan los datos comunes al Intent, como el idioma seleccionado, el tiempo restante de la visita,
        // el número de la pantalla actual y el tema actual.
        siguientePantalla.putExtra("idiomaSeleccionado", idiomaSeleccionado)
        siguientePantalla.putExtra("tiempoVisita", tiempoVisita)
        siguientePantalla.putExtra("coleccionPantallas", coleccionPantallas as Serializable)
        siguientePantalla.putExtra("posicionArrayPantallas", posicionArrayPantallas)
        siguientePantalla.putExtra("numeroPantallaContenido", numeroPantallaContenidoActual)
        siguientePantalla.putExtra("numPantallasContenidoTematica", numPantallasContenidoTematica)
        // Si la visita es una visita express (no personalizada), se agrega la variable 'visitaExpress' al Intent.
        if (!visitaExpress.isNullOrEmpty()) {
            siguientePantalla.putExtra("visitaExpress", visitaExpress)
        } else {
            // Si la visita es personalizada, se agrega la variable 'visitaPersonalizada' al Intent.
            siguientePantalla.putExtra("visitaPersonalizada", visitaPersonalizada)
        }
        siguientePantalla.putExtra("temasSeleccionados", temasSeleccionados)
        // Inicia la actividad correspondiente con el Intent configurado.
        startActivity(siguientePantalla)
    }

    /**
     * Modifica los componentes de la interfaz de usuario según el tema actual y el idioma seleccionado.
     *
     * Este método realiza los siguientes ajustes:
     * 1. Modifica los textos e imágenes en la interfaz según el tema seleccionado y el idioma actual.
     * 2. Usa la función `getIdentifier` para obtener los identificadores dinámicamente basados en el tema y el idioma, lo que permite manejar múltiples temas y traducir textos de manera eficiente.
     * 3. Establece la imagen del contenido y los textos relevantes para el tema actual, considerando el número de pantalla de contenido.
     * 4. Si el idioma seleccionado es uno de los soportados (inglés, alemán o francés), se ajustan los identificadores de los recursos para reflejar el idioma correcto.
     *
     * La función también actualiza la interfaz de acuerdo con el idioma elegido llamando a `changeLanguage` para cambiar los textos de la interfaz de usuario en función del idioma.
     */
    private fun modifyComponents() {
        // Definir el sufijo según el idioma seleccionado
        val sufijoIdioma = if (idiomaSeleccionado == "esp") "" else "_$idiomaSeleccionado"

        // Obtener los identificadores dinámicamente usando el sufijo
        val temaLowerCase = temaActual?.lowercase() ?: ""

        // Asignar los identificadores de recursos de texto e imagen
        val textoBotonTematicaVisita = resources.getIdentifier(
            "texto_boton_${temaLowerCase}_visita_personalizada$sufijoIdioma", "string", packageName
        )
        val textoContenidoTematica = resources.getIdentifier(
            "texto_principal_${temaLowerCase}_punto_siguiente_contenido_${numeroPantallaContenidoActual}$sufijoIdioma",
            "string", packageName
        )
        val imgContenidoTematica = resources.getIdentifier(
            "img_contenido_${temaLowerCase}_$numeroPantallaContenidoActual", "drawable", packageName
        )

        val sufijoIdiomaAudio = if (idiomaSeleccionado == "esp") "es" else idiomaSeleccionado
        val audio = resources.getIdentifier(
            "${temaLowerCase}${numeroPantallaContenidoActual}$sufijoIdiomaAudio", "raw", packageName
        )

        // Establecer los recursos en la interfaz de usuario
        this.imgPuntoSiguienteContenido.setImageResource(imgContenidoTematica)
        this.textoTematicaVisita.text = getString(textoBotonTematicaVisita)
        this.textoContenidoTematicaVisita.text = getString(textoContenidoTematica)
        mp = MediaPlayer.create(this, audio)

        // Cambiar el idioma de la interfaz de usuario
        changeLanguage()
    }

    /**
     * Cambia el idioma de los textos de la interfaz según el idioma seleccionado.
     *
     * Este método actualiza los textos de los elementos visuales de la interfaz de usuario en función
     * del idioma seleccionado por el usuario. Los textos de los botones y otros elementos (como `textoTipoVisita`)
     * se actualizan automáticamente con las traducciones correspondientes al idioma elegido.
     *
     * El método utiliza un mapa para gestionar las traducciones por idioma. A partir del idioma seleccionado,
     * se asignan los textos correspondientes a cada elemento de la interfaz.
     *
     * Además, se realiza una verificación para determinar si la visita es personalizada o express:
     * - Si la visita es personalizada, se muestra el texto adecuado para ese tipo de visita.
     * - Si la visita es express, se muestra el texto para la visita express.
     *
     * Los idiomas soportados actualmente son:
     * - Español ("esp")
     * - Inglés ("eng")
     * - Alemán ("deu")
     * - Francés ("fra")
     *
     * Este enfoque permite un fácil mantenimiento y expansión de idiomas, ya que basta con agregar nuevas
     * traducciones al mapa para soportar más idiomas.
     */
    private fun changeLanguage() {
        // Mapa de traducciones por idioma
        val textosPorIdioma = mapOf(
            "esp" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada),
                "btnVolver" to getString(R.string.texto_boton_regresar),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente)
            ),
            "eng" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express_eng),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada_eng),
                "btnVolver" to getString(R.string.texto_boton_regresar_eng),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente_eng)
            ),
            "deu" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express_deu),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada_deu),
                "btnVolver" to getString(R.string.texto_boton_regresar_deu),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente_deu)
            ),
            "fra" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express_fra),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada_fra),
                "btnVolver" to getString(R.string.texto_boton_regresar_fra),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente_fra)
            )
        )
        // Obtener el mapa de textos correspondientes al idioma seleccionado
        val textos = textosPorIdioma[idiomaSeleccionado] ?: return

        // Comprobar si es una visita personalizada o express
        if (!intent.extras?.getString("visitaPersonalizada").isNullOrEmpty()) {
            // Si es visita personalizada, asigna el texto adecuado
            textoTipoVisita.text = textos["textoTipoVisitaPersonalizada"]
        } else {
            // Si es visita express, asigna el texto adecuado
            textoTipoVisita.text = textos["textoTipoVisitaExpress"]
        }
            btnVolver.text = textos["btnVolver"]
            btnSiguiente.text = textos["btnSiguiente"]
    }

}