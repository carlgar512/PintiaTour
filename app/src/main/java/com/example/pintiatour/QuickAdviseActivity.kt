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
import com.bumptech.glide.Glide
import java.io.Serializable

class QuickAdviseActivity : AppCompatActivity() {

    private lateinit var textoExpressPersonalizada: TextView
    private lateinit var textoConsejoVisita: TextView
    private lateinit var gifMovilRotate: ImageView
    private lateinit var btnVolver: Button
    private lateinit var btnSiguiente: Button
    private var idiomaSeleccionado: String? = ""
    private var visitaExpress: String? = ""
    private var visitaPersonalizada: String? = ""
    private var tiempoVisita: Long? = 3600 // 1 hora en seg
    private var temasSeleccionados = BooleanArray(5) { false }
    private var coleccionPantallas = mutableListOf<Pantalla>()
    private var numPantallasContenidoTematica: Int = 0
    private lateinit var mp: MediaPlayer


    /**
     * Método que se ejecuta al crear la actividad. Realiza las siguientes acciones:
     * - Habilita un diseño "edge-to-edge" para aprovechar toda la pantalla.
     * - Configura el diseño de la actividad mediante el archivo de layout.
     * - Recupera los datos de la sesión, como el idioma y la información de la visita.
     * - Inicializa los componentes de la interfaz de usuario (UI).
     * - Cambia los textos de la interfaz de acuerdo con el idioma seleccionado.
     * - Configura los listeners para los eventos de los elementos interactivos.
     * - Inicia las animaciones de los elementos de la interfaz (GIFs).
     * - Ajusta los márgenes de la vista principal para respetar las barras del sistema (como la barra de estado).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mp=MediaPlayer.create(this,R.raw.turnhorizontal)
        mp.start()
        enableEdgeToEdge()
        setContentView(R.layout.activity_quick_advise)
        getSessionData()
        initComponents()
        changeLanguage()
        initListeners()
        animateGif()
        createVisit()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Recupera los datos de la sesión desde el Intent que inició la actividad. Estos datos incluyen
     * el idioma seleccionado, la información sobre la visita (personalizada o express), los temas
     * seleccionados para la visita, y el tiempo disponible para la misma.
     * Si la visita es personalizada, se extraen los datos relacionados con la visita, de lo contrario,
     * se asignan valores predeterminados para los temas de la visita express.
     */
    private fun getSessionData() {
        // Recupera el idioma seleccionado desde el Intent
        idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")

        // Verifica si la visita es personalizada y recupera sus datos
        if (intent.extras?.getString("visitaPersonalizada") != null) {
            visitaPersonalizada = intent.extras?.getString("visitaPersonalizada")
            tiempoVisita = intent.extras?.getLong("tiempoVisita")
            temasSeleccionados = intent.extras?.getBooleanArray("temasSeleccionados") as BooleanArray
        } else {
            // Si la visita es express, establece valores predeterminados para los temas seleccionados
            temasSeleccionados.fill(true)
            visitaExpress = intent.extras?.getString("visitaExpress")
        }
    }

    /**
     * Inicializa los componentes de la interfaz gráfica y asigna los valores correspondientes
     * a los elementos de la pantalla. Este método se encarga de buscar las vistas en el layout
     * y asignarles los textos o comportamientos adecuados según si la visita es express o personalizada.
     * Además, se configuran los botones para la navegación.
     */
    private fun initComponents() {
        // Inicializa las referencias a los componentes de la interfaz
        textoExpressPersonalizada = findViewById(R.id.texto_visita_express_o_personalizada)
        textoConsejoVisita = findViewById(R.id.texto_consejo_visita)
        gifMovilRotate = findViewById(R.id.imagen_movil_volteado)

        // Establece el texto para el tipo de visita (express o personalizada)
        textoExpressPersonalizada.text = visitaExpress ?: visitaPersonalizada

        // Inicializa los botones de navegación
        btnVolver = findViewById(R.id.boton_regresar_visita_express)
        btnSiguiente = findViewById(R.id.boton_siguiente_pantalla_express)
    }

    /**
     * Configura los listeners para los botones de la interfaz de usuario.
     *
     * Este método se encarga de establecer los eventos (listeners) para los botones de navegación
     * en la actividad. Cuando el usuario hace clic en "Volver", se navega a la actividad correspondiente
     * dependiendo de si la visita es "express" o "personalizada". Al hacer clic en "Siguiente",
     * se navega a la actividad que muestra el primer punto de la visita.
     */
    private fun initListeners() {
        // Configura el listener para el botón "Volver"
        btnVolver.setOnClickListener {
            mp.release()
            val siguientePantalla: Intent
            // Si la visita es express, navega a la actividad de selección de visita
            if(visitaExpress != "" && visitaExpress != null){
                siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            }
            // Si la visita no es express, navega a la actividad de visita personalizada
            else{
                siguientePantalla = Intent(this, CustomVisitActivity::class.java)
            }
            navigateToNextScreen(siguientePantalla)
        }

        // Configura el listener para el botón "Siguiente"
        btnSiguiente.setOnClickListener {
            mp.release()
            val siguientePantalla = Intent(this, coleccionPantallas[0].activityClass)
            navigateToNextScreen(siguientePantalla)
        }
    }

    /**
     * Navega a la siguiente pantalla pasando los datos de la sesión a través del Intent.
     *
     * Este método prepara los datos necesarios para la navegación hacia la siguiente pantalla.
     * Dependiendo de si la visita es de tipo "express" o "personalizada", se incluyen los datos relevantes
     * en el Intent que se envía a la siguiente actividad. También se asegura de que se pasen los datos del
     * idioma seleccionado, el tiempo restante de la visita y los temas seleccionados.
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        siguientePantalla.putExtra("idiomaSeleccionado", idiomaSeleccionado)
        siguientePantalla.putExtra("tiempoVisita", tiempoVisita)
        siguientePantalla.putExtra("coleccionPantallas", coleccionPantallas as Serializable)
        siguientePantalla.putExtra("posicionArrayPantallas", 0)
        siguientePantalla.putExtra("numPantallasContenidoTematica", numPantallasContenidoTematica)

        if(visitaExpress != "" && visitaExpress != null){
            siguientePantalla.putExtra("visitaExpress", visitaExpress)
            siguientePantalla.putExtra("tiempoVisita", tiempoVisita)
            siguientePantalla.putExtra("temasSeleccionados", temasSeleccionados)
        }
        else{
            siguientePantalla.putExtra("visitaPersonalizada", visitaPersonalizada)
            siguientePantalla.putExtra("temasSeleccionados", temasSeleccionados)
        }
        startActivity(siguientePantalla)
    }

    /**
     * Cambia los textos de la interfaz según el idioma seleccionado.
     *
     * Este método actualiza dinámicamente los textos de la interfaz de usuario en función del idioma
     * seleccionado por el usuario. Utiliza un mapa (`languageMapping`) para asociar cada idioma
     * con sus recursos de texto correspondientes. Los idiomas soportados incluyen Español, Inglés,
     * Alemán y Francés.
     *
     * La lógica incluye:
     * - Verificar si un texto personalizado debe mostrarse basándose en los extras del `Intent`.
     * - Asignar los valores de los recursos de texto para elementos como `TextView` y `Button`.
     * - Proveer valores predeterminados en caso de que alguna clave o recurso esté ausente.
     *
     * Esto asegura que la interfaz de usuario sea consistente y esté localizada adecuadamente para
     * cada idioma.
     */
    private fun changeLanguage() {
        // Mapeo de las claves de idioma a su correspondiente valor de string
        val languageMapping = mapOf(
            "esp" to mapOf(
                "textoPersonalizada" to R.string.texto_select_visit_visita_personalizada,
                "textoExpress" to R.string.texto_visita_express,
                "textoConsejoVisita" to R.string.texto_consejo_girar_movil,
                "btnVolver" to R.string.texto_boton_regresar,
                "btnSiguiente" to R.string.texto_boton_siguiente
            ),
            "eng" to mapOf(
                "textoPersonalizada" to R.string.texto_select_visit_visita_personalizada_eng,
                "textoExpress" to R.string.texto_visita_express_eng,
                "textoConsejoVisita" to R.string.texto_consejo_girar_movil_eng,
                "btnVolver" to R.string.texto_boton_regresar_eng,
                "btnSiguiente" to R.string.texto_boton_siguiente_eng
            ),
            "deu" to mapOf(
                "textoPersonalizada" to R.string.texto_select_visit_visita_personalizada_deu,
                "textoExpress" to R.string.texto_visita_express_deu,
                "textoConsejoVisita" to R.string.texto_consejo_girar_movil_deu,
                "btnVolver" to R.string.texto_boton_regresar_deu,
                "btnSiguiente" to R.string.texto_boton_siguiente_deu
            ),
            "fra" to mapOf(
                "textoPersonalizada" to R.string.texto_select_visit_visita_personalizada_fra,
                "textoExpress" to R.string.texto_visita_express_fra,
                "textoConsejoVisita" to R.string.texto_consejo_girar_movil_fra,
                "btnVolver" to R.string.texto_boton_regresar_fra,
                "btnSiguiente" to R.string.texto_boton_siguiente_fra
            )
        )

        // Obtiene los recursos de idioma correspondientes
        val languageResources = languageMapping[idiomaSeleccionado] ?: return

        // Configura los textos según el idioma seleccionado
        textoExpressPersonalizada.text = if (intent.extras?.getString("visitaPersonalizada") != null)
            getString(languageResources["textoPersonalizada"] ?: R.string.texto_select_visit_visita_personalizada)
        else
            getString(languageResources["textoExpress"] ?: R.string.texto_visita_express)

        textoConsejoVisita.text = getString(languageResources["textoConsejoVisita"] ?: R.string.texto_consejo_girar_movil)
        btnVolver.text = getString(languageResources["btnVolver"] ?: R.string.texto_boton_regresar)
        btnSiguiente.text = getString(languageResources["btnSiguiente"] ?: R.string.texto_boton_siguiente)
    }

    /**
     * Método para cargar y mostrar una animación GIF en un ImageView.
     *
     * Utiliza la biblioteca Glide para cargar un archivo GIF (en este caso, una animación de rotación de móvil) desde
     * los recursos `res/drawable` y ajusta su tamaño. El GIF es cargado en un ImageView de la interfaz, con un tamaño
     * específico de 100x100 píxeles y centrado para asegurar una visualización adecuada.
     */
    private fun animateGif(){
        Glide.with(this)
            .asGif() // Asegúrate de especificar que es un GIF
            .load(R.drawable.mobile_rotate_animation) // Tu GIF en res/drawable
            .override(100,100)
            .fitCenter()
            .into(gifMovilRotate)
    }

    /**
     * Genera las pantallas que compondrán la visita personalizada.
     *
     * Este método construye la secuencia de pantallas que se presentarán al usuario durante la visita.
     * Además facilita el proceso de avanzar y retroceder.
     * El proceso sigue los siguientes pasos:
     *
     * 1. **Cálculo de la estructura básica**:
     *    - Se calcula el tamaño total de la visita (`tamVisita`) en función del tiempo total disponible (`tiempoVisita`),
     *      dividido entre la duración promedio de una pantalla (300 segundos).
     *    - Se determina cuántas pantallas se asignarán por tema (`tamPorGenero`) basado en la cantidad de temas seleccionados.
     *    - Se ajusta el tamaño total de la visita y los límites de pantallas por tema (entre 2 y 4) para garantizar
     *      una distribución consistente y manejable con al menos una de contenido y 1 test y el maximo de 3 de contenido que hay actualmente
     *      Si hubiera más solo habria que cambiar estos valores.
     *
     * 2. **Definición de los temas seleccionados**:
     *    - Los temas activados por el usuario se filtran de una lista de temas predefinidos.
     *    - Estos temas se utilizarán para asignar contenido a las pantallas de la visita.
     *
     * 3. **Generación de pantallas**:
     *    - Se genera una secuencia de pantallas según la posición:
     *      - La primera pantalla se asigna al "Punto inicial" y utiliza `ShowInitialVisitPointActivity`.
     *      - Las pantallas intermedias se asignan a temas seleccionados, alternando entre:
     *        - Pantallas de contenido (`NextPointContentActivity`).
     *        - Pantallas de mapas (`NextPointMapActivity`).
     *        - Pantallas de cuestionarios (`NextPointQuestionaryActivity`).
     *      - La última pantalla se asigna al "Punto final" y utiliza `EndOfVisitActivity`.
     *    - Se controla la cantidad de pantallas por tema con un contador (`tamPorGeneroCopia`),
     *      y se alternan los temas una vez que se alcanza la cantidad asignada.
     *
     * 4. **Ajuste del promedio de pantallas por tema**:
     *    - Calcula cuántas pantallas de contenido, en promedio, se mostraron por tema excluyendo las pantallas inicial y final.
     *
     * El resultado es una lista ordenada de pantallas (`coleccionPantallas`), que cubre tanto los puntos inicial y final
     * como el contenido de cada tema seleccionado.
     */
    private fun createVisit() {
        // Calcular el tamaño por género dividiendo el tiempo de la visita entre el número de temas
        val temasSeleccionadosCount = temasSeleccionados.count { it }
        var tamVisita = (tiempoVisita?.toInt() ?: 0) / 300
        var tamPorGenero = tamVisita / temasSeleccionadosCount

        // Ajustar tamVisita si la división no es exacta
        if (tamVisita % temasSeleccionadosCount != 0) {
            tamVisita = temasSeleccionadosCount * tamPorGenero
        }

        // Agregar pantallas adicionales fijas
        tamVisita += 2 + temasSeleccionadosCount

        // Ajustar tamPorGenero dentro de los límites
        tamPorGenero = tamPorGenero.coerceIn(2, 5)

        // Recalcular tamVisita según tamPorGenero
        tamVisita = (tamPorGenero * temasSeleccionadosCount) + 2 + temasSeleccionadosCount

        // Copia del tamaño por género para manejar iteraciones
        var tamPorGeneroCopia = tamPorGenero + 1

        // Crear lista de temas seleccionados
        val temas = listOf("Arquitectura", "Funerario", "Militar", "Costumbres", "Curiosidades")
            .filterIndexed { index, _ -> temasSeleccionados[index] }

        var temaIndex = 0 // Índice del tema actual
        var pantalla: Pantalla

        // Generar las pantallas de la visita
        for (i in 0 until tamVisita) {
            val temaActual = when (i) {
                0 -> "Punto inicial"
                tamVisita - 1 -> "Punto final"
                else -> temas[temaIndex]
            }

            pantalla = when {
                i == 0 -> Pantalla(ShowInitialVisitPointActivity::class.java, temaActual)
                i == tamVisita - 1 -> Pantalla(EndOfVisitActivity::class.java, temaActual)
                tamPorGeneroCopia == tamPorGenero + 1 -> {
                    tamPorGeneroCopia--
                    Pantalla(NextPointMapActivity::class.java, temaActual)
                }
                tamPorGeneroCopia == 1 -> {
                    tamPorGeneroCopia = tamPorGenero + 1
                    temaIndex = (temaIndex + 1) % temas.size
                    Pantalla(NextPointQuestionaryActivity::class.java, temaActual)
                }
                else -> {
                    tamPorGeneroCopia--
                    numPantallasContenidoTematica++
                    Pantalla(NextPointContentActivity::class.java, temaActual)
                }
            }

            coleccionPantallas.add(i, pantalla)
        }

        // Calcular el número promedio de pantallas de contenido por tema
        numPantallasContenidoTematica = (numPantallasContenidoTematica / temasSeleccionadosCount) - 1
    }

}