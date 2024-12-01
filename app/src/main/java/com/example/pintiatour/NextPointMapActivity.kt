package com.example.pintiatour

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import kotlin.random.Random

class NextPointMapActivity : AppCompatActivity() {

    private lateinit var textoTipoVisita: TextView
    private lateinit var textoDirijasePunto: TextView
    private lateinit var imgPuntoSiguienteMapa: ShapeableImageView
    private lateinit var textoContador: TextView
    private lateinit var btnVolver: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnAudio: FloatingActionButton
    private lateinit var btnSalir: FloatingActionButton
    private var idiomaSeleccionado: String? = ""
    private var visitaExpress: String? = ""
    private var visitaPersonalizada: String? = ""
    private var tiempoVisita: Long? = 0
    private var timerJob: Job? = null

    private var temasSeleccionados = BooleanArray(5) { false }
    private var temaActual: String? = ""
    private var coleccionPantallas = mutableListOf<Pantalla>()
    private var posicionArrayPantallas: Int? = 0
    private var numPantallasContenidoTematica: Int? = 0

    private lateinit var mp: MediaPlayer
    private lateinit var mp1: MediaPlayer


    /**
     * Método que se ejecuta cuando se crea la actividad.
     *
     * Este método es parte del ciclo de vida de la actividad y se invoca cuando la actividad es creada.
     * Se encarga de inicializar y configurar todos los componentes necesarios para la actividad, incluyendo:
     *
     * 1. **Habilitar el modo Edge-to-Edge**: Llama al método `enableEdgeToEdge()` para permitir que el contenido se muestre sin restricciones en los bordes de la pantalla.
     *
     * 2. **Establecer el contenido de la vista**: Utiliza `setContentView()` para cargar el layout correspondiente a la actividad (`activity_next_point_map`).
     *
     * 3. **Obtener los datos de la sesión**: Llama al método `getSessionData()` para recuperar la información necesaria (como el idioma, tiempo de visita y temas seleccionados).
     *
     * 4. **Inicializar los componentes**: Llama a `initComponents()` para configurar los elementos visuales de la interfaz de usuario, como los `TextViews`, `Buttons`, entre otros.
     *
     * 5. **Cambiar el idioma**: Llama a `changeLanguage()` para ajustar los textos de la interfaz según el idioma seleccionado por el usuario.
     *
     * 6. **Inicializar los listeners**: Llama a `initListeners()` para configurar los manejadores de eventos para los botones y otros elementos interactivos.
     *
     * 7. **Actualizar el temporizador**: Llama a `updateTimer()` para iniciar la cuenta atrás del temporizador.
     *
     * 8. **Ajuste de márgenes en pantallas con barra de sistema**: Usa `ViewCompat.setOnApplyWindowInsetsListener` para ajustar los márgenes de la vista según las barras del sistema (como la barra de estado y la barra de navegación) para asegurar una presentación adecuada en pantallas con bordes redondeados o con barras de sistema visibles.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crea y reproduce el MediaPlayer
        mp1 = MediaPlayer.create(this, R.raw.campana)
        mp1.start()
        // Habilitar el modo edge-to-edge
        enableEdgeToEdge()

        // Establecer el layout de la actividad
        setContentView(R.layout.activity_next_point_map)

        // Obtener los datos de la sesión (idioma, tiempo, etc.)
        getSessionData()

        // Inicializar los componentes visuales (TextViews, Buttons, etc.)
        initComponents()

        // Cambiar los textos de la interfaz según el idioma seleccionado
        changeLanguage()

        // Inicializar los listeners para los elementos interactivos
        initListeners()

        // Iniciar la actualización del temporizador
        updateTimer()

        // Ajustar los márgenes de la vista según las barras del sistema (para pantallas con bordes redondeados o barras de sistema visibles)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Se llama cuando la actividad está a punto de destruirse.
     *
     * Este método libera los recursos del `MediaPlayer` cuando la actividad se destruye, asegurándose
     * de que no haya recursos del `MediaPlayer` siendo utilizados una vez que la actividad haya terminado.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Libera los recursos del MediaPlayer al cerrar la actividad
        mp.release()
        mp1.release()
    }

    /**
     * Se llama cuando la actividad pasa a un estado de pausa.
     *
     * Este método se asegura de que, si el `MediaPlayer` está activo y reproduciendo, se detenga y libere
     * sus recursos antes de que la actividad sea puesta en pausa. Esto es útil para evitar que el
     * `MediaPlayer` siga reproduciendo sonido o consuma recursos cuando la actividad no está en primer plano.
     */
    override fun onPause() {
        super.onPause()
        if (::mp.isInitialized && mp.isPlaying) {
            mp.stop()       // Detiene la reproducción
            mp.release()
                            // Libera los recursos del MediaPlayer
        }
        if (::mp1.isInitialized && mp1.isPlaying) {
            mp1.stop()       // Detiene la reproducción
            mp1.release()
            // Libera los recursos del MediaPlayer
        }
    }

    /**
     * Método que obtiene los datos de la sesión actuales desde los extras del `Intent`.
     *
     * Este método se encarga de recuperar los datos necesarios para la sesión de la visita, tales como:
     * - El idioma seleccionado (`idiomaSeleccionado`).
     * - El tiempo restante para la visita (`tiempoVisita`).
     * - El tipo de visita, ya sea personalizada o express.
     * - Los temas seleccionados para la visita.
     *
     * A continuación, realiza las siguientes acciones dependiendo de si la visita es personalizada o express:
     *
     * 1. **Visita Personalizada**:
     *    - Recupera los datos de la visita personalizada desde los `Intent` extras.
     *    - Recupera los temas seleccionados como un arreglo de booleanos y verifica cuántos están desactivados.
     *    - Si todos los temas están desactivados, se detiene el temporizador y se navega a la pantalla de finalización de la visita.
     *    - Si no todos los temas están desactivados, se selecciona el tema actual.
     *
     * 2. **Visita Express**:
     *    - Recupera los datos de la visita express desde los `Intent` extras.
     *    - Realiza los mismos pasos de verificación y navegación, pero en el contexto de una visita express.
     *
     * El método también verifica si todos los temas están desactivados y, si es así, navega a la actividad `EndOfVisitActivity` para finalizar la visita.
     */
    private fun getSessionData() {
        // Se obtiene el idioma seleccionado de los extras
        this.idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")

        // Se obtiene el tiempo de la visita desde los extras
        this.tiempoVisita = intent.extras?.getLong("tiempoVisita")

        // Verifica si la visita es personalizada
        if (!intent.extras?.getString("visitaPersonalizada").isNullOrEmpty()) {
            // Si es una visita personalizada, se recuperan los datos correspondientes
            this.visitaPersonalizada = intent.extras?.getString("visitaPersonalizada")
            } else {
            // Si es una visita express, se recuperan los datos correspondientes
            this.visitaExpress = intent.extras?.getString("visitaExpress")
            }
        this.temasSeleccionados = intent.extras?.getBooleanArray("temasSeleccionados") as BooleanArray
        coleccionPantallas = (intent.getSerializableExtra("coleccionPantallas") as? MutableList<Pantalla>)!!
        posicionArrayPantallas = intent.extras?.getInt("posicionArrayPantallas")
        this.temaActual = coleccionPantallas[posicionArrayPantallas as Int].temaActual
        numPantallasContenidoTematica = intent.extras?.getInt("numPantallasContenidoTematica")
    }

    /**
     * Método que inicializa los componentes de la interfaz de usuario de la pantalla actual.
     *
     * Este método se encarga de vincular los elementos de la interfaz (como `TextView`, `Button`, `ImageView`, etc.)
     * con sus respectivos identificadores en el layout XML.
     *
     * Además, se configura el texto de los elementos dependiendo de si la visita es personalizada o express.
     * - Si `visitaPersonalizada` no es nula ni vacía, se muestra como texto en el `textoTipoVisita`.
     * - Si no, se establece `visitaExpress` como el texto.
     *
     * También se vinculan los otros componentes de la interfaz para poder ser utilizados posteriormente en el código,
     * como los botones `btnVolver` y `btnSiguiente`, el `TextView` para mostrar el contador de tiempo (`textoContador`),
     * y la imagen `imgPuntoSiguienteMapa` para mostrar el mapa del siguiente punto.
     */
    private fun initComponents() {
        // Vinculamos el componente de texto para el tipo de visita
        textoTipoVisita = findViewById(R.id.texto_visita_express_o_personalizada_punto_siguiente_mapa)

        // Se verifica si la visita es personalizada, en cuyo caso se muestra el texto correspondiente
        if (!visitaPersonalizada.isNullOrEmpty()) {
            textoTipoVisita.text = visitaPersonalizada
        } else {
            // Si no es personalizada, se muestra el texto de la visita express
            textoTipoVisita.text = visitaExpress
        }

        // Vinculamos el componente de texto que indica el siguiente punto a visitar
        textoDirijasePunto = findViewById(R.id.texto_dirijase_siguiente_punto)

        // Vinculamos la imagen del mapa del siguiente punto
        imgPuntoSiguienteMapa = findViewById(R.id.imagen_mapa_punto_siguiente_mapa)
        changeMapImage()

        // Vinculamos el contador de tiempo
        textoContador = findViewById(R.id.texto_contador_tiempo_visita_punto_siguiente_mapa)

        // Vinculamos el botón de volver
        btnVolver = findViewById(R.id.boton_regresar_punto_siguiente_mapa)

        // Vinculamos el botón de siguiente
        btnSiguiente = findViewById(R.id.boton_siguiente_pantalla_punto_siguiente_mapa)
        btnAudio = findViewById(R.id.boton_audio)
        btnSalir= findViewById(R.id.boton_salir)

    }

    /**
     * Cambia la imagen del mapa según el tema actual.
     * Este método actualiza el recurso de imagen de un `ImageView` para reflejar
     * un mapa asociado al tema seleccionado.
     */
    private fun changeMapImage(){
        var imgMapa = resources.getIdentifier("mapa_pintia_punto_${temaActual?.lowercase()}_recortada", "drawable", packageName)
        imgPuntoSiguienteMapa.setImageResource(imgMapa)
    }

    /**
     * Método que inicializa los listeners de los botones de la interfaz de usuario y otros elementos interactivos.
     *
     * Este método configura los eventos `onClickListener` para los botones de la interfaz de usuario (`btnVolver`, `btnSiguiente`, `imgPuntoSiguienteMapa`, `btnAudio`).
     *
     * - Para el botón `btnVolver`, al hacer clic, se detiene la reproducción de audio, se detiene el temporizador, se ajusta la posición del array de pantallas y se navega a la actividad anterior en la lista de pantallas (`coleccionPantallas`), pasando el parámetro `goBack = true` a la función `navigateToNextScreen` para restaurar los temas seleccionados.
     * - Para el botón `btnSiguiente`, al hacer clic, se detiene la reproducción de audio, se detiene el temporizador, se ajusta la posición del array de pantallas y se navega a la siguiente actividad en la lista de pantallas, pasando el parámetro `goBack = false` a la función `navigateToNextScreen` para continuar con la visita sin restaurar los temas seleccionados.
     * - Para el botón `imgPuntoSiguienteMapa`, al hacer clic, se detiene el audio y se abre Google Maps.
     * - Para el botón `btnAudio`, al hacer clic, se inicia la reproducción de audio.
     *
     * El método asegura que las acciones correctas se tomen según el botón presionado, proporcionando una navegación fluida entre pantallas y gestionando la reproducción de audio y temporizador de manera adecuada.
     */
    private fun initListeners() {
        btnVolver.setOnClickListener {
            mp.stop()
            mp1.stop()
            stopTimer()
            posicionArrayPantallas = posicionArrayPantallas!! - 1
            Log.i("posicionArr", posicionArrayPantallas.toString())
            val siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla)

        }

        btnSiguiente.setOnClickListener {
            mp1.stop()
            mp.stop()
            stopTimer()
            posicionArrayPantallas = posicionArrayPantallas!! + 1
            val siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla)
        }

        imgPuntoSiguienteMapa.setOnClickListener{
            mp.stop()
            mp1.stop()
            openGoogleMaps()
        }

        btnAudio.setOnClickListener{
            mp1.stop()
            mp.start()
        }

        btnSalir.setOnClickListener{
            mp1.stop()
            mp.stop() // Detiene la reproducción de audio
            var siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla) // Finaliza l
        }
    }

    /**
     * Método que actualiza un temporizador y muestra el tiempo restante en formato hh:mm:ss.
     *
     * Este método inicia un trabajo en segundo plano utilizando corrutinas (`CoroutineScope` y `launch`),
     * que actualiza el temporizador cada segundo. El temporizador se muestra en formato horas, minutos y
     * segundos, y se actualiza en un `TextView` en la interfaz de usuario. El tiempo restante se va decreciendo
     * cada segundo hasta llegar a cero.
     *
     * Se utiliza la variable `tiempoVisita`, que es de tipo `Long`, para gestionar el tiempo restante en segundos.
     * Cada vez que se actualiza el temporizador, el valor de `tiempoVisita` se decrementa y el tiempo formateado
     * se muestra en el `textoContador`.
     *
     * @param tiempoVisita Tiempo restante de la visita en segundos, representado por una variable `Long`.
     * @return No devuelve nada, actualiza el valor de `tiempoVisita` y el `TextView` con el tiempo formateado.
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
                tiempo--  // Decremetar el tiempo restante
                tiempoVisita = tiempo
            }
        }
    }

    /**
     * Este método detiene el temporizador en ejecución cancelando la tarea asociada a `timerJob`.
     *
     * Además, devuelve el valor actual del tiempo de visita (`tiempoVisita`), el cual puede ser utilizado
     * para registrar o mostrar la duración transcurrida hasta el momento en que se detiene el temporizador
     *
     * @return El tiempo de visita actual (en milisegundos o la unidad que represente `tiempoVisita`).
     */
    private fun stopTimer(): Long? {
        timerJob?.cancel()
        return tiempoVisita
    }

    /**
     * Navega a la siguiente pantalla, pasando los datos necesarios a través de un Intent.
     *
     * Este método maneja tanto las visitas express como las personalizadas. Dependiendo de si la visita
     * es express o personalizada, se agregan los datos correspondientes al Intent. Además, se incluyen
     * los datos del idioma seleccionado, el tiempo de la visita, el número de la pantalla de contenido
     * y los temas seleccionados.
     *
     * Si se indica que se va hacia atrás (`goBack == true`), se restaura el tema actual antes de
     * pasar los datos.
     *
     * @param siguientePantalla El Intent que representa la pantalla a la que se navega.
     * @param goBack Indica si la navegación es hacia atrás o no. Si es `true`, se restaurará el tema.
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        siguientePantalla.putExtra("idiomaSeleccionado", idiomaSeleccionado)
        siguientePantalla.putExtra("tiempoVisita", tiempoVisita)
        siguientePantalla.putExtra("coleccionPantallas", coleccionPantallas as Serializable)
        siguientePantalla.putExtra("posicionArrayPantallas", posicionArrayPantallas)
        siguientePantalla.putExtra("numPantallasContenidoTematica", numPantallasContenidoTematica)

        siguientePantalla.putExtra("numeroPantallaContenido", 0)
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
     * Método que cambia los textos y el audio de la interfaz según el idioma seleccionado.
     *
     * Este método verifica el valor de la variable `idiomaSeleccionado` y, dependiendo del idioma,
     * actualiza los textos de los elementos de la interfaz de usuario (como botones y textos) con las
     * cadenas correspondientes en el idioma seleccionado. Además, asigna el archivo de audio correspondiente
     * al idioma seleccionado para ser reproducido con un `MediaPlayer`.
     *
     * El método utiliza un `mapa` que asocia cada idioma (español, inglés, alemán, francés) con las traducciones
     * de los textos necesarios y el archivo de audio correspondiente para cada idioma. El contenido de la interfaz
     * se actualiza dependiendo de si se trata de una visita personalizada o una visita express.
     *
     * El cambio de texto afecta los siguientes elementos:
     * - El texto de la visita (personalizada o express).
     * - Las instrucciones para dirigirse al siguiente punto en el mapa.
     * - Los botones "Volver" y "Siguiente".
     * Además, se asigna el archivo de audio adecuado para cada idioma, el cual se puede reproducir a través de `MediaPlayer`.
     *
     * El idioma que el usuario ha seleccionado. Puede ser uno de los siguientes:
     *                            "esp", "eng", "deu", "fra".
     */
    private fun changeLanguage() {
        // Mapa de traducciones por idioma
        val textAndAudio = mapOf(
            "esp" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada),
                "textoDirijasePunto" to getString(R.string.texto_dirijase_punto_punto_siguiente_mapa),
                "btnVolver" to getString(R.string.texto_boton_regresar),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente),
                "mp" to MediaPlayer.create(this,R.raw.mapaes)
            ),
            "eng" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express_eng),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada_eng),
                "textoDirijasePunto" to getString(R.string.texto_dirijase_punto_punto_siguiente_mapa_eng),
                "btnVolver" to getString(R.string.texto_boton_regresar_eng),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente_eng),
                "mp" to MediaPlayer.create(this,R.raw.mapaen)
            ),
            "deu" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express_deu),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada_deu),
                "textoDirijasePunto" to getString(R.string.texto_dirijase_punto_punto_siguiente_mapa_deu),
                "btnVolver" to getString(R.string.texto_boton_regresar_deu),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente_deu),
                "mp" to MediaPlayer.create(this,R.raw.mapaal)
            ),
            "fra" to mapOf(
                "textoTipoVisitaExpress" to getString(R.string.texto_visita_express_fra),
                "textoTipoVisitaPersonalizada" to getString(R.string.texto_select_visit_visita_personalizada_fra),
                "textoDirijasePunto" to getString(R.string.texto_dirijase_punto_punto_siguiente_mapa_fra),
                "btnVolver" to getString(R.string.texto_boton_regresar_fra),
                "btnSiguiente" to getString(R.string.texto_boton_siguiente_fra),
                "mp" to MediaPlayer.create(this,R.raw.mapafr)
            )
        )
        // Obtener el mapa de textos correspondientes al idioma seleccionado
        val audioText = textAndAudio[idiomaSeleccionado] ?: return

        // Comprobar si es una visita personalizada o express
        if (!intent.extras?.getString("visitaPersonalizada").isNullOrEmpty()) {
            // Si es visita personalizada, asigna el texto adecuado
            textoTipoVisita.text = audioText["textoTipoVisitaPersonalizada"].toString()
        } else {
            // Si es visita express, asigna el texto adecuado
            textoTipoVisita.text = audioText["textoTipoVisitaExpress"].toString()
        }
        textoDirijasePunto.text = audioText["textoDirijasePunto"].toString()
        btnVolver.text = audioText["btnVolver"].toString()
        btnSiguiente.text = audioText["btnSiguiente"].toString()
        mp= audioText["mp"] as MediaPlayer

    }

    /**
     * Abre la ubicación delos puntos dados en Google Maps o un navegador web.
     *
     * Este método intenta abrir la ubicación dada
     * en Google Maps utilizando un `Intent`. Si Google Maps no está disponible, intenta abrir
     * la ubicación en Google Chrome. Si ninguno de los dos está instalado, el método abrirá la
     * ubicación en cualquier navegador web disponible.
     *
     */
    private fun openGoogleMaps() {
        // Mapa de URI de localización por tema
        val geoUris = mapOf(
            "arquitectura" to "https://www.google.es/maps/place/Ciudad+de+Las+Quintanas/@41.6238091,-4.1763549,620m/data=!3m2!1e3!4b1!4m6!3m5!1s0xd46ef354677ed63:0x1e787942dd10a059!8m2!3d41.6238051!4d-4.17378!16s%2Fg%2F11fsmd52st?hl=es&entry=ttu&g_ep=EgoyMDI0MTExOS4yIKXMDSoASAFQAw%3D%3D",
            "funerario" to "https://www.google.es/maps/dir//41.6159235,-4.1693616/@41.6162738,-4.1695437,219m/data=!3m1!1e3?hl=es&entry=ttu&g_ep=EgoyMDI0MTExOS4yIKXMDSoASAFQAw%3D%3D",
            "militar" to "https://www.google.es/maps/place/Ruinas+Del+Oppidum+Vacceo+De+Pintia/@41.6232211,-4.1716696,737m/data=!3m1!1e3!4m6!3m5!1s0xd46eebfaa7be96b:0x93b3ffeda4c6f2f3!8m2!3d41.6230278!4d-4.1694782!16s%2Fg%2F11hbvbkw1c?hl=es&entry=ttu&g_ep=EgoyMDI0MTExOS4yIKXMDSoASAFQAw%3D%3D",
            "costumbres" to "https://www.google.es/maps/place/Centro+de+Estudios+Vacceos+Federico+Wattenberg/@41.6130263,-4.1642471,219m/data=!3m1!1e3!4m6!3m5!1s0xd46eeb1b22583b5:0x2fe2bab3869175b7!8m2!3d41.6130664!4d-4.1640135!16s%2Fg%2F1ptypsjh5?hl=es&entry=ttu&g_ep=EgoyMDI0MTExOS4yIKXMDSoASAFQAw%3D%3D",
            "curiosidades" to "https://www.google.es/maps/dir//41.6174302,-4.1712477/@41.6165789,-4.1706353,369m/data=!3m1!1e3?hl=es&entry=ttu&g_ep=EgoyMDI0MTExOS4yIKXMDSoASAFQAw%3D%3D"
        )
        // Obtener el URI del tema actual, si existe
        val geoUri = geoUris[temaActual?.lowercase()] ?: return

        // Crear un Intent para abrir Google Maps con el URI correspondiente
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)).apply {
            setPackage("com.google.android.apps.maps")
        }

        // Intent para intentar abrir con Google Maps, si no está disponible, intenta con Chrome o cualquier navegador
        val intentToOpen = when {
            mapsIntent.resolveActivity(packageManager) != null -> mapsIntent
            else -> Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)).apply {
                setPackage("com.android.chrome")
            }
        }

        // Si no se puede abrir con Chrome, intenta con cualquier navegador disponible
        if (intentToOpen.resolveActivity(packageManager) != null) {
            startActivity(intentToOpen)
        } else {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
            startActivity(fallbackIntent)
        }
    }
}