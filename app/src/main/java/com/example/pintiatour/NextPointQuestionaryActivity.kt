package com.example.pintiatour

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.Pair
import java.io.Serializable

class NextPointQuestionaryActivity : AppCompatActivity() {

    private lateinit var textoTipoVisita: TextView

    private lateinit var imgTematica: ShapeableImageView
    private lateinit var textoImgTematica: TextView
    private lateinit var btnAnteriorImg: FloatingActionButton
    private lateinit var btnSiguienteImg: FloatingActionButton
    private lateinit var textoPreguntaTematica: TextView
    private lateinit var cardViewRespuestaUno: CardView
    private lateinit var textoRespuestaUno: TextView
    private lateinit var cardViewRespuestaDos: CardView
    private lateinit var textoRespuestaDos: TextView
    private lateinit var cardViewRespuestaTres: CardView
    private lateinit var textoRespuestaTres: TextView
    private lateinit var cardViewRespuestaCuatro: CardView
    private lateinit var textoRespuestaCuatro: TextView
    private lateinit var textoContador: TextView
    private lateinit var btnVolver: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnSalir: FloatingActionButton
    private var idiomaSeleccionado: String? = ""
    private var visitaExpress: String? = ""
    private var visitaPersonalizada: String? = ""
    private var tiempoVisita: Long? = 0
    private var timerJob: Job? = null

    private var temasSeleccionados = BooleanArray(5) { false }
    private var temaActual: String? = ""
    private var cuestionarioElegido: Int? = 0
    private var respuestaCorrecta: Int = 0

    private var imagenes = mutableListOf<Pair<Int, String>>()
    private var imgActual: Int = 0
    private var coleccionPantallas = mutableListOf<Pantalla>()
    private var posicionArrayPantallas: Int? = 0
    private var numeroPantallaContenidoActual: Int? = 0
    private var numPantallasContenidoTematica: Int? = 0

    private lateinit var mp: MediaPlayer
    private lateinit var mp1: MediaPlayer


    /**
     * Método llamado cuando la actividad es creada. Inicializa los componentes de la actividad
     * y establece las configuraciones necesarias para la funcionalidad de la pantalla.
     *
     * Este método es responsable de:
     * 1. Configurar la interfaz de usuario para que ocupe toda la pantalla disponible (modo edge-to-edge).
     * 2. Establecer el diseño de la actividad con el layout correspondiente.
     * 3. Recuperar los datos de sesión mediante el método `getSessionData()` (como idioma, tema y tipo de visita).
     * 4. Seleccionar la fuente del cuestionario a través del método `selectSourceOfQuestionary()`.
     * 5. Inicializar los componentes de la interfaz mediante `initComponents()`, que establece las vistas necesarias.
     * 6. Asignar los listeners a los componentes interactivos con `initListeners()`.
     * 7. Modificar la interfaz y las respuestas según el cuestionario mediante `modifyComponents()`.
     * 8. Configurar el idioma de la interfaz con `changeLanguage()`.
     * 9. Iniciar el temporizador y actualizarlo constantemente con `updateTimer()`.
     * 10. Ajustar el padding de la vista principal para manejar correctamente los márgenes de las barras de sistema
     *     (como la barra de estado y la barra de navegación) mediante `ViewCompat.setOnApplyWindowInsetsListener`.
     *
     * Este método asegura que todos los elementos necesarios para la actividad estén configurados y listos
     * antes de que el usuario interactúe con la pantalla.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mp = MediaPlayer.create(this,R.raw.clock)
        mp.start()
        enableEdgeToEdge()
        setContentView(R.layout.activity_next_point_questionary)
        getSessionData()
        selectSourceOfQuestionary()
        initComponents()
        initListeners()
        modifyComponents()
        changeLanguage()
        updateTimer()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Reanuda la reproducción del audio cuando la actividad vuelve a estar visible.
     *
     * Este método se ejecuta automáticamente cuando la actividad pasa al estado `Resumed`.
     * Si el audio no está reproduciéndose, se inicia nuevamente. Esto asegura que el usuario
     * pueda continuar escuchando el audio desde donde lo dejó al salir brevemente de la actividad.
     */
    override fun onResume() {
        super.onResume()
        // Reanuda la reproducción del audio al volver a la pantalla
        if (!mp.isPlaying) {
            mp.start()
        }
    }

    /**
     * Pausa la reproducción del audio y libera recursos temporalmente cuando la actividad deja de ser visible.
     *
     * Este método se ejecuta automáticamente cuando la actividad pasa al estado `Paused`.
     * Si el `MediaPlayer` está inicializado y reproduciendo audio, se detiene la reproducción
     * y se liberan los recursos asociados para optimizar el uso de memoria.
     */
    override fun onPause() {
        super.onPause()
        if (::mp.isInitialized && mp.isPlaying) {
            mp.stop()       // Detiene la reproducción
            mp.release()    // Libera los recursos del MediaPlayer
        }
    }

    /**
     * Libera definitivamente los recursos asociados al `MediaPlayer` al destruir la actividad.
     *
     * Este método se ejecuta automáticamente cuando la actividad pasa al estado `Destroyed`.
     * Libera todos los recursos utilizados por el `MediaPlayer`, asegurando que no haya fugas de memoria
     * ni uso innecesario de recursos del sistema después de que la actividad se cierre.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Libera los recursos del MediaPlayer al cerrar la actividad
        mp.release()
    }

    /**
     * Obtiene y asigna los datos de sesión desde el `Intent` de la actividad anterior.
     *
     * Este método extrae la información almacenada en el `Intent` que inicia la actividad actual. Los datos recuperados
     * son esenciales para determinar el idioma seleccionado, el tema actual, el tipo de visita, los temas seleccionados
     * y el tiempo restante de la visita. Dependiendo de la presencia de ciertos parámetros, se ajustan las variables
     * internas correspondientes.
     *
     * - **Idioma seleccionado**: Se obtiene el idioma que el usuario ha elegido para la visita.
     * - **Tema actual**: El tema que se está tratando en la visita se recupera desde el Intent.
     * - **Visita personalizada o express**: Se verifica si el tipo de visita es personalizada o express. Si es personalizada,
     *   se asigna el valor correspondiente y se obtiene el array de temas seleccionados. Si es una visita express, se asigna
     *   el valor de la visita express.
     * - **Temas seleccionados**: Se recupera el array de booleanos que indica los temas que han sido seleccionados.
     * - **Tiempo de visita**: Se extrae el tiempo restante de la visita desde el Intent.
     *
     * Este método asegura que los datos necesarios para la lógica de la actividad estén correctamente cargados antes
     * de que el usuario interactúe con la interfaz.
     */
    private fun getSessionData(){
        this.idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")
        this.temaActual = intent.extras?.getString("temaActual")
        if(!intent.extras?.getString("visitaPersonalizada").isNullOrEmpty()){
            this.visitaPersonalizada = intent.extras?.getString("visitaPersonalizada")
        }
        else{
            this.visitaExpress = intent.extras?.getString("visitaExpress")
        }
        this.temasSeleccionados = intent.extras?.getBooleanArray("temasSeleccionados") as BooleanArray
        this.tiempoVisita = intent.extras?.getLong("tiempoVisita")
        numPantallasContenidoTematica = intent.extras?.getInt("numPantallasContenidoTematica")
        this.numeroPantallaContenidoActual = numPantallasContenidoTematica
        coleccionPantallas = (intent.getSerializableExtra("coleccionPantallas") as? MutableList<Pantalla>)!!
        posicionArrayPantallas = intent.extras?.getInt("posicionArrayPantallas")
        this.temaActual = coleccionPantallas[posicionArrayPantallas as Int].temaActual
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     *
     * Este método busca y asigna los valores a las vistas de la actividad, configurando los textos y las imágenes
     * según el tipo de visita y otros datos asociados. Se asegura de que los componentes gráficos estén correctamente
     * vinculados con sus identificadores y establece los valores iniciales para ciertos elementos de la UI.
     *
     * - **Texto de tipo de visita**: Muestra el tipo de visita (personalizada o express) dependiendo de los datos
     *   disponibles (`visitaPersonalizada` o `visitaExpress`).
     *
     * - **Imagen temática y texto de referencia**: Se asigna la imagen temática y su texto de referencia a los
     *   componentes correspondientes.
     *
     * - **Respuestas y temporizador**: Asocia las vistas para las respuestas (tarjetas de opción) y el temporizador
     *   que se mostrará durante la actividad.
     *
     * - **Botones de navegación**: Configura los botones de navegación ("Volver", "Siguiente") y las flechas para
     *   navegar entre imágenes.
     *
     * Este método se asegura de que todos los elementos visuales de la actividad estén correctamente inicializados
     * antes de que el usuario interactúe con ellos.
     */
    private fun initComponents(){
        textoTipoVisita = findViewById(R.id.texto_visita_express_o_personalizada_punto_siguiente_cuestionario)
        if(!visitaPersonalizada.isNullOrEmpty()){
            textoTipoVisita.text = visitaPersonalizada
        }
        else{
            textoTipoVisita.text = visitaExpress
        }

        imgTematica = findViewById(R.id.imagen_contenido_punto_siguiente_cuestionario)
        textoImgTematica = findViewById(R.id.texto_referencia_imagen_tematica_punto_siguiente_cuestionario)
        btnAnteriorImg = findViewById(R.id.boton_flecha_anterior_imagen)
        btnSiguienteImg = findViewById(R.id.boton_flecha_siguiente_imagen)

        textoPreguntaTematica = findViewById(R.id.texto_pregunta_tematica_punto_siguiente_cuestionario)
        cardViewRespuestaUno = findViewById(R.id.CardViewPrimeraRespuesta)
        textoRespuestaUno = findViewById(R.id.texto_respuesta_uno_punto_siguiente_cuestionario)
        cardViewRespuestaDos = findViewById(R.id.CardViewSegundaRespuesta)
        textoRespuestaDos = findViewById(R.id.texto_respuesta_dos_punto_siguiente_cuestionario)
        cardViewRespuestaTres = findViewById(R.id.CardViewTerceraRespuesta)
        textoRespuestaTres = findViewById(R.id.texto_respuesta_tres_punto_siguiente_cuestionario)
        cardViewRespuestaCuatro = findViewById(R.id.CardViewCuartaRespuesta)
        textoRespuestaCuatro = findViewById(R.id.texto_respuesta_cuatro_punto_siguiente_cuestionario)

        textoContador = findViewById(R.id.texto_contador_tiempo_visita_punto_siguiente_cuestionario)
        btnVolver = findViewById(R.id.boton_regresar_punto_siguiente_cuestionario)
        btnSiguiente = findViewById(R.id.boton_siguiente_pantalla_punto_siguiente_cuestionario)
        btnSalir= findViewById(R.id.boton_salir)
        collectImgsAndTxt()
    }

    /**
     * Inicializa los listeners para los botones y las tarjetas de respuesta.
     *
     * Este método configura los `OnClickListener` para las opciones de respuesta, los botones de navegación
     * (Volver, Siguiente, Anterior Imagen y Siguiente Imagen), así como la lógica para manejar las interacciones
     * con cada uno de estos componentes.
     *
     * - **Opciones de respuesta**: Al hacer clic en una de las opciones de respuesta, se verifica si la respuesta seleccionada
     *   es correcta.
     *
     * - **Botón Volver**: Al hacer clic en el botón "Volver", se detiene el temporizador y se navega a la siguiente pantalla
     *   (NextPointContentActivity).
     *
     * - **Botón Siguiente**: Al hacer clic en el botón "Siguiente", se detiene el temporizador y se navega a la siguiente pantalla
     *   (NextPointMapActivity).
     *
     * - **Botones de imágenes**: Los botones de "Anterior Imagen" y "Siguiente Imagen" permiten navegar entre las imágenes
     *   disponibles, actualizando la vista según corresponda.
     */
    private fun initListeners(){
        val opciones = listOf(
            Pair(cardViewRespuestaUno, textoRespuestaUno),
            Pair(cardViewRespuestaDos, textoRespuestaDos),
            Pair(cardViewRespuestaTres, textoRespuestaTres),
            Pair(cardViewRespuestaCuatro, textoRespuestaCuatro)
        )

        opciones.forEachIndexed { index, (cardView, textoView) ->
            cardView.setOnClickListener {
                val opcionElegida = index + 1
                if (opcionElegida == respuestaCorrecta) {
                    mp1 = MediaPlayer.create(this,R.raw.correct)
                    mp1.start()
                } else {
                    mp1 = MediaPlayer.create(this,R.raw.error)
                    mp1.start()
                }
                highlightGreenCardView(opciones[respuestaCorrecta - 1].first, opciones[respuestaCorrecta - 1].second)
                val incorrectas = opciones.filterIndexed { i, _ -> i != respuestaCorrecta - 1 }
                if (incorrectas.isNotEmpty()) {
                    incorrectas.take(3).forEach { (cardView, textView) ->
                        highlightRedCardView(cardView, textView)
                    }
                }
            }
        }

        btnVolver.setOnClickListener {
            stopTimer()
            mp.stop()
            posicionArrayPantallas = posicionArrayPantallas!! - 1
            var siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla)
        }

        btnSiguiente.setOnClickListener {
            stopTimer()
            mp.stop()
            posicionArrayPantallas = posicionArrayPantallas!! + 1
            var siguientePantalla = Intent(this, coleccionPantallas[posicionArrayPantallas as Int].activityClass)
            navigateToNextScreen(siguientePantalla)
        }

        btnAnteriorImg.setOnClickListener {
            changeImg(imagenes, false)
        }

        btnSiguienteImg.setOnClickListener {
            changeImg(imagenes, true)
        }

        btnSalir.setOnClickListener{
            mp.stop() // Detiene la reproducción de audio
            var siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla) // Finaliza l
        }

    }

    /**
     * Inicia o actualiza el temporizador de la visita, mostrando el tiempo restante en formato `hh:mm:ss`.
     *
     * Este método ejecuta una corutina en el hilo principal que se encarga de actualizar el contador
     * de tiempo en un `TextView` cada segundo. El tiempo restante se muestra en formato de horas, minutos
     * y segundos, y decrece cada segundo hasta que el tiempo de visita llegue a cero.
     *
     * La duración del temporizador se almacena en la variable `tiempoVisita`, la cual es modificada
     * dentro del bucle de la corutina. Cada vez que se actualiza el tiempo, la vista en pantalla se
     * actualiza también para reflejar el tiempo restante.
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
     * Detiene el temporizador y devuelve el tiempo de visita actual.
     *
     * Este método cancela la ejecución del temporizador (si está activo) utilizando el `Job` de la
     * corutina asociado al temporizador. Después de cancelar el temporizador, el método retorna
     * el valor actual de `tiempoVisita`, que representa el tiempo acumulado de la visita hasta el momento.
     *
     * @return El tiempo de visita actual (`tiempoVisita`) en milisegundos, o null si no está disponible.
     */
    private fun stopTimer(): Long? {
        timerJob?.cancel()
        return tiempoVisita
    }

    /**
     * Navega a la siguiente pantalla pasando datos relevantes a través de un Intent.
     *
     * Este método configura el Intent `siguientePantalla` con los valores actuales de los parámetros
     * como el idioma seleccionado, tiempo de visita, cuestionario actual, tema y selección de visitas.
     * Además, dependiendo de si se trata de una visita express o personalizada, se incluyen diferentes
     * extras en el Intent, y se restauran o destruyen los temas seleccionados dependiendo del parámetro
     * `goBack`.
     *
     * @param siguientePantalla Intent al cual se le agregan los extras antes de iniciar la siguiente pantalla.
     * @param goBack Booleano que indica si la navegación es hacia atrás (true) o hacia adelante (false).
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        siguientePantalla.putExtra("idiomaSeleccionado", idiomaSeleccionado)
        siguientePantalla.putExtra("tiempoVisita", tiempoVisita)
        siguientePantalla.putExtra("cuestionarioActual", cuestionarioElegido)
        siguientePantalla.putExtra("coleccionPantallas", coleccionPantallas as Serializable)
        siguientePantalla.putExtra("posicionArrayPantallas", posicionArrayPantallas)
        siguientePantalla.putExtra("numeroPantallaContenido", numeroPantallaContenidoActual)
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
     * Configura y asigna el texto, las imágenes y las respuestas para un cuestionario específico basado en el tema,
     * el idioma seleccionado y el cuestionario elegido. Baraja las respuestas y establece la respuesta correcta.
     *
     * Este método utiliza `resources.getIdentifier` para recuperar los identificadores de recursos dinámicamente
     * según el tema y el cuestionario seleccionados. Además, actualiza los componentes visuales como `TextView` y
     * asigna márgenes ajustados para las vistas de las respuestas.
     */
    private fun modifyComponents() {
        var respuestas: MutableList<Pair<String, Boolean>>
        val sufijoIdioma = if (idiomaSeleccionado == "esp") "" else "_$idiomaSeleccionado"
        var textoRespuestaUno = resources.getIdentifier("texto_respuesta_uno_${temaActual?.lowercase()}_$cuestionarioElegido$sufijoIdioma", "string", packageName)
        var textoRespuestaDos = resources.getIdentifier("texto_respuesta_dos_${temaActual?.lowercase()}_$cuestionarioElegido$sufijoIdioma", "string", packageName)
        var textoRespuestaTres = resources.getIdentifier("texto_respuesta_tres_${temaActual?.lowercase()}_$cuestionarioElegido$sufijoIdioma", "string", packageName)
        var textoRespuestaCuatro = resources.getIdentifier("texto_respuesta_cuatro_${temaActual?.lowercase()}_$cuestionarioElegido$sufijoIdioma", "string", packageName)
        var textoPreguntaTematica = resources.getIdentifier("texto_pregunta_tematica_punto_siguiente_cuestionario_${temaActual?.lowercase()}_$cuestionarioElegido$sufijoIdioma", "string", packageName)
        var textoReferenciaTematica = resources.getIdentifier("texto_referencia_respuesta_uno_${temaActual?.lowercase()}_$cuestionarioElegido$sufijoIdioma", "string", packageName)

        // Configurar respuestas basadas en el tema y el cuestionario elegido
        respuestas = when (this.temaActual) {
            "Arquitectura" -> when (cuestionarioElegido) {
                0,2 -> mutableListOf(
                    Pair(getString(textoRespuestaUno), true),
                    Pair(getString(textoRespuestaDos), false),
                    Pair(getString(textoRespuestaTres), false),
                    Pair(getString(textoRespuestaCuatro), false)
                )
                1 -> mutableListOf(
                    Pair(getString(textoRespuestaUno), false),
                    Pair(getString(textoRespuestaDos), true),
                    Pair(getString(textoRespuestaTres), false),
                    Pair(getString(textoRespuestaCuatro), false)
                )
                else -> mutableListOf()
            }
            "Funerario" -> when (cuestionarioElegido) {
                0, 1 -> mutableListOf(
                    Pair(getString(textoRespuestaUno), false),
                    Pair(getString(textoRespuestaDos), false),
                    Pair(getString(textoRespuestaTres), true),
                    Pair(getString(textoRespuestaCuatro), false)
                )
                2 -> mutableListOf(
                    Pair(getString(textoRespuestaUno), true),
                    Pair(getString(textoRespuestaDos), false),
                    Pair(getString(textoRespuestaTres), false),
                    Pair(getString(textoRespuestaCuatro), false)
                )
                else -> mutableListOf()
            }
            "Militar" -> when  (cuestionarioElegido) {
                0 -> mutableListOf(
                Pair(getString(textoRespuestaUno), false),
                Pair(getString(textoRespuestaDos), false),
                Pair(getString(textoRespuestaTres), true),
                Pair(getString(textoRespuestaCuatro), false)
                )
                1,2 -> mutableListOf(
                Pair(getString(textoRespuestaUno), true),
                Pair(getString(textoRespuestaDos), false),
                Pair(getString(textoRespuestaTres), false),
                Pair(getString(textoRespuestaCuatro), false)
                )
                else -> mutableListOf()
            }
            "Costumbres", "Curiosidades" -> mutableListOf(
                Pair(getString(textoRespuestaUno), true),
                Pair(getString(textoRespuestaDos), false),
                Pair(getString(textoRespuestaTres), false),
                Pair(getString(textoRespuestaCuatro), false)
            )
            else -> mutableListOf()
        }

        // Asignar textos y referencias
        this.textoPreguntaTematica.text = getString(textoPreguntaTematica)
        this.textoImgTematica.text = getString(textoReferenciaTematica)

        // Mostrar las respuestas barajadas en los componentes
        this.textoRespuestaUno.text = respuestas[0].first
        this.textoRespuestaDos.text = respuestas[1].first
        this.textoRespuestaTres.text = respuestas[2].first
        this.textoRespuestaCuatro.text = respuestas[3].first

        // El índice de la respuesta correcta ahora está en la posición 'true'
        val correctAnswerIndex = respuestas.indexOfFirst { it.second }
        this.respuestaCorrecta = correctAnswerIndex + 1

        // Ajustar márgenes de las vistas
        giveMarginToCardViews()
    }

    /**
     * Resalta en rojo un conjunto de `CardView` y cambia el color del texto asociado a un color grisáceo.
     *
     * Este método aplica un color de fondo rojo oscuro a los `CardView` proporcionados y ajusta el color del texto
     * relacionado a un tono gris. Se utiliza para indicar visualmente una selección o estado incorrecto.
     *
     * @param card1 Primer `CardView` a resaltar en rojo.
     * @param texto1 Primer `TextView` cuyo color se ajustará.
     */
    private fun highlightRedCardView(card1: CardView, texto1: TextView): Unit{
        texto1.setTextColor(ContextCompat.getColor(this, R.color.grayish))
        card1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red_darker))
    }

    /**
     * Resalta en verde un `CardView` y cambia el color del texto asociado a un color grisáceo.
     *
     * Este método aplica un color de fondo verde oscuro al `CardView` proporcionado y ajusta el color del texto
     * relacionado a un tono gris. Se utiliza para indicar visualmente una selección o estado correcto.
     *
     * @param card `CardView` a resaltar en verde.
     * @param texto `TextView` cuyo color se ajustará.
     */
    private fun highlightGreenCardView(card: CardView, texto: TextView): Unit{
        texto.setTextColor(ContextCompat.getColor(this, R.color.grayish))
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green_darker))
    }

    /**
     * Selecciona la fuente del cuestionario.
     *
     * Este método determina qué cuestionario se debe utilizar en base a la información proporcionada en el Intent:
     * - Si el extra `recuperarCuestionario` es `true`, se recupera el cuestionario actual desde los extras del Intent (`cuestionarioActual`).
     * - Si el extra `recuperarCuestionario` es `false` o no está presente, se selecciona un cuestionario aleatorio llamando a `obtainRandomQuestionary()`.
     */
    private fun selectSourceOfQuestionary(){
        this.cuestionarioElegido = if (intent.extras?.getBoolean("recuperarCuestionario") != false) {
            intent.extras?.getInt("cuestionarioActual")
        } else {
            obtainRandomQuestionary()
        }
    }

    /**
     * Obtiene un cuestionario aleatorio.
     *
     * Este método genera y devuelve un número entero aleatorio dentro del rango [0, 2],
     * que representa la selección de un cuestionario aleatorio entre tres opciones posibles.
     *
     * @return Un entero aleatorio entre 0 y 2.
     */
    private fun obtainRandomQuestionary(): Int{
        return (0..2).random()
    }

    /**
     * Recolecta los recursos de imágenes y textos para las respuestas.
     *
     * Este método obtiene los identificadores de recursos (imágenes y textos) basados en el tema actual (`temaActual`),
     * el cuestionario seleccionado (`cuestionarioElegido`), y el idioma seleccionado (`idiomaSeleccionado`).
     *
     * Si el idioma es diferente del predeterminado ("esp"), ajusta las claves de los textos
     * para incluir el código del idioma (por ejemplo, "eng", "deu", "fra").
     *
     * Los recursos obtenidos se agregan a la lista `imagenes` como pares (imagen, texto).
     */
    private fun collectImgsAndTxt(){

        val sufijoIdioma = if (idiomaSeleccionado == "esp") "" else "_$idiomaSeleccionado"
        var textoRespuestaUno = resources.getIdentifier("texto_referencia_respuesta_uno_${temaActual?.lowercase()}_${cuestionarioElegido}$sufijoIdioma", "string", packageName)
        var textoRespuestaDos = resources.getIdentifier("texto_referencia_respuesta_dos_${temaActual?.lowercase()}_${cuestionarioElegido}$sufijoIdioma", "string", packageName)
        var textoRespuestaTres = resources.getIdentifier("texto_referencia_respuesta_tres_${temaActual?.lowercase()}_${cuestionarioElegido}$sufijoIdioma", "string", packageName)
        var textoRespuestaCuatro = resources.getIdentifier("texto_referencia_respuesta_cuatro_${temaActual?.lowercase()}_${cuestionarioElegido}$sufijoIdioma", "string", packageName)
        var imgRespuestaUno = resources.getIdentifier("img_respuesta_uno_${temaActual?.lowercase()}_${cuestionarioElegido}", "drawable", packageName)
        var imgRespuestaDos = resources.getIdentifier("img_respuesta_dos_${temaActual?.lowercase()}_${cuestionarioElegido}", "drawable", packageName)
        var imgRespuestaTres = resources.getIdentifier("img_respuesta_tres_${temaActual?.lowercase()}_${cuestionarioElegido}", "drawable", packageName)
        var imgRespuestaCuatro = resources.getIdentifier("img_respuesta_cuatro_${temaActual?.lowercase()}_${cuestionarioElegido}", "drawable", packageName)

        imagenes.addAll(
            listOf(
                Pair(imgRespuestaUno, getString(textoRespuestaUno)),
                Pair(imgRespuestaDos, getString(textoRespuestaDos)),
                Pair(imgRespuestaTres, getString(textoRespuestaTres)),
                Pair(imgRespuestaCuatro, getString(textoRespuestaCuatro))
            )
        )
        this.imgTematica.setImageResource(imagenes[0].first)
    }

    /**
     * Cambia la imagen y el texto asociados en función de la dirección seleccionada.
     *
     * Este método actualiza la imagen y el texto del tema actual de acuerdo con un conjunto de imágenes
     * (`imgColection`). Se puede avanzar o retroceder entre los elementos de la lista, con un comportamiento
     * cíclico que garantiza que al avanzar más allá del último elemento se vuelve al primero, y al retroceder
     * antes del primero se vuelve al último.
     *
     * @param imgColection Una lista mutable de pares que contienen el ID del recurso de la imagen y el texto asociado.
     * @param goForward `true` para avanzar hacia adelante; `false` para retroceder.
     */
    private fun changeImg(imgColection: MutableList<Pair<Int, String>>, goForward: Boolean){
        // lista ciclica
        imgActual = (imgActual + if (goForward) 1 else -1 + imgColection.size) % imgColection.size
        this.imgTematica.setImageResource(imgColection[imgActual].first)
        this.textoImgTematica.text = (imgColection[imgActual].second)
    }

    /**
     * Cambia el idioma de los textos en la interfaz según el idioma seleccionado.
     *
     * Este método ajusta los textos de los componentes visuales (`TextView` y `Button`) para reflejar
     * el idioma elegido por el usuario. Utiliza un mapa (`textosPorIdioma`) para asociar cada idioma con
     * los textos correspondientes a las vistas, como el tipo de visita (personalizada o express) y los botones
     * "Volver" y "Siguiente".
     *
     * - Traduce el texto de la visita personalizada o express en `textoTipoVisita`, dependiendo de si
     *   el extra `visitaPersonalizada` está presente en el `Intent`.
     * - Actualiza los textos de los botones "Volver" y "Siguiente" según el idioma seleccionado.
     *
     * Los idiomas soportados actualmente son:
     * - Español (`esp`)
     * - Inglés (`eng`)
     * - Alemán (`deu`)
     * - Francés (`fra`)
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

    /**
     * Ajusta dinámicamente los márgenes inferiores de los CardViews en la pantalla.
     *
     * Este método se ejecuta después de que las vistas han sido renderizadas, utilizando el método `post`
     * para garantizar que las alturas de los elementos ya están calculadas.
     * Se comparan las alturas de cada par consecutivo de `CardView` y, si existe una diferencia,
     * se establece un margen inferior para compensar visualmente la disparidad.
     *
     * - Si las alturas de dos `CardView` consecutivos son diferentes, se aplica un margen inferior al primero.
     * - Si las alturas son iguales, no se establece margen.
     *
     * Este ajuste se realiza dinámicamente para mantener una disposición uniforme,
     * evitando inconsistencias visuales en la presentación de los elementos.
     */
    private fun giveMarginToCardViews() {
            // Ejecutar la lógica después de que las vistas hayan sido renderizadas
            cardViewRespuestaUno.post {
                // Array de los CardViews
                val cardViews = listOf(cardViewRespuestaUno, cardViewRespuestaDos, cardViewRespuestaTres, cardViewRespuestaCuatro)

                // Iterar sobre los CardViews hasta el penúltimo
                for (i in 0 until cardViews.size - 1) {
                    val currentCardView = cardViews[i]
                    val nextCardView = cardViews[i + 1]

                    // Comparar las alturas de los CardViews
                    val hasHeightDifference = currentCardView.height != nextCardView.height

                    // Ajustar el margen inferior del CardView actual
                    val layoutParams = currentCardView.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.bottomMargin = if (hasHeightDifference) {
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            3f, // Margen en dp
                            resources.displayMetrics
                        ).toInt()
                    } else {
                        0 // Sin margen si las alturas son iguales
                    }
                    currentCardView.layoutParams = layoutParams
                }
            }

    }

}