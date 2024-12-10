package com.example.pintiatour
// Importación de las librerías necesarias
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Clase principal de la actividad para personalizar visitas
class CustomVisitActivity : AppCompatActivity() {

    // Declaración de variables para los componentes visuales y datos
    private lateinit var textoVisitaPersonalizada: TextView
    private lateinit var layoutContenidoPrincipal: LinearLayoutCompat
    private lateinit var textoFijoDuracion: TextView
    private lateinit var textoDuracion: TextView
    private var cuentaMinutosVisita: Int = 30
    private var cuentaHorasVisita: Int = 0
    private lateinit var btnFlechaArribaDuracion: FloatingActionButton
    private lateinit var btnFlechaAbajoDuracion: FloatingActionButton
    private lateinit var textoFijoTipoVisita: TextView
    private lateinit var textoTipoVisita: TextView
    private var opciones: String = ""
    private lateinit var textoFijoTemasVisita: TextView
    private lateinit var btnArquitectura: Button
    private var arquitecturaSeleccionado: Boolean = false
    private lateinit var btnFunerario: Button
    private var funerarioSeleccionado: Boolean = false
    private lateinit var btnMilitar: Button
    private var militarSeleccionado: Boolean = false
    private lateinit var btnCostumbres: Button
    private var costumbresSeleccionado: Boolean = false
    private lateinit var btnCuriosidades: Button
    private var curiosidadesSeleccionado: Boolean = false
    private lateinit var textoTemasReq: TextView
    // Arreglo para almacenar los temas seleccionados
    var temasSeleccionados = BooleanArray(5) { false }
    private lateinit var btnVolver: Button
    private lateinit var btnSiguiente: Button
    // Variables para almacenar el idioma seleccionado y el tipo de visita
    private var idiomaSeleccionado: String? = ""
    private var visitaPersonalizada: String? = ""
    private lateinit var audioIntent: Intent

    /**
     * Método principal que se ejecuta cuando se crea la actividad.
     * Aquí se configuran los componentes de la interfaz, se obtienen los datos de la sesión
     * y se configuran los eventos de los botones.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioIntent = Intent(this, AudioService::class.java)
        audioIntent.putExtra("AUDIO_RES_ID", R.raw.softpiano) // Recurso de audio
        audioIntent.putExtra("ACTION", "PLAY_BACKGROUND")
        audioIntent.putExtra("IS_LOOPING", true)
        startService(audioIntent)
        enableEdgeToEdge() // Configura la interfaz de usuario para pantalla completa
        setContentView(R.layout.activity_custom_visit) // Asigna el diseño de la actividad
        getSessionData() // Obtiene los datos de la sesión actual
        initComponents() // Inicializa los componentes visuales
        initListeners() // Configura los eventos de los botones
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // Ajusta el diseño para evitar superposiciones con las barras del sistema
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
        audioIntent.putExtra("ACTION", "STOP") // Libera los recursos del MediaPlayer
        startService(intent)
    }

    /**
     * Reanuda la reproducción del audio cuando la actividad vuelve a primer plano.
     * Verifica si el reproductor no está reproduciendo y lo inicia.
     */
    override fun onResume() {
        super.onResume()
        audioIntent.putExtra("ACTION", "RESUME")
        startService(audioIntent)
    }

    /**
     * Detiene la reproducción y libera los recursos del MediaPlayer cuando la actividad pasa a segundo plano.
     * Este método se llama cuando la actividad entra en pausa, asegurándose de que el MediaPlayer se detenga
     * y libere sus recursos si estaba en uso.
     */
    override fun onPause() {
        super.onPause()
        // Enviar la señal para pausar la música
        audioIntent.putExtra("ACTION", "PAUSE")
        startService(audioIntent)
    }

    /**
     * Obtiene los datos enviados desde la actividad previa.
     * Esta función extrae el idioma seleccionado y el tipo de visita personalizada
     * desde los extras del Intent que inicia esta actividad.
     */
    private fun getSessionData(){
        idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")
        visitaPersonalizada = intent.extras?.getString("visitaPersonalizada")
    }

    /**
     * Inicializa los componentes visuales de la interfaz de usuario, asignándolos a sus respectivos IDs.
     * Esta función configura los elementos de la pantalla, como los botones, los textos y otros componentes
     * visuales, para que puedan ser manipulados y actualizados según la interacción del usuario.
     */
    private fun initComponents() {
        textoVisitaPersonalizada = findViewById(R.id.texto_visita_personalizada)
        layoutContenidoPrincipal = findViewById(R.id.linear_layout_contenido_principal_personalizada)
        textoFijoDuracion = findViewById(R.id.texto_duracion_fijo_visita_personalizada)
        textoDuracion = findViewById(R.id.texto_duracion_visita_personalizada)
        modifyDurationText()
        btnFlechaArribaDuracion = findViewById(R.id.boton_flecha_arriba_duracion)
        btnFlechaAbajoDuracion = findViewById(R.id.boton_flecha_abajo_duracion)

        textoFijoTipoVisita = findViewById(R.id.texto_tipo_fijo_visita_personalizada)
        textoTipoVisita = findViewById(R.id.texto_tipo_visita_personalizada)

        textoFijoTemasVisita = findViewById(R.id.texto_temas_fijo_visita_personalizada)
        btnArquitectura = findViewById(R.id.boton_arquitectura)
        btnFunerario = findViewById(R.id.boton_funerario)
        btnMilitar = findViewById(R.id.boton_militar)
        btnCostumbres = findViewById(R.id.boton_costumbres)
        btnCuriosidades = findViewById(R.id.boton_curiosidades)
        btnVolver = findViewById(R.id.boton_regresar_visita_personalizada)
        btnSiguiente = findViewById(R.id.boton_siguiente_pantalla_personalizada)
        textoTemasReq = findViewById(R.id.texto_temas_requeridos)

        textoTemasReq.visibility = View.GONE
        changeLanguage() // Configura los textos según el idioma seleccionado
        textoTipoVisita.text = opciones // Configura el texto inicial del tipo de visita
    }

    /**
     * Configura los eventos de los botones y otros componentes de la interfaz.
     *
     * Este método establece los manejadores de eventos para cada uno de los botones en la interfaz de usuario.
     * Los botones permiten al usuario modificar la duración de la visita, elegir temas de visita,
     * y navegar entre pantallas. También hay validaciones para mostrar mensajes y actualizaciones en la interfaz
     * dependiendo de las selecciones realizadas por el usuario.
     */
    private fun initListeners() {
        btnFlechaArribaDuracion.setOnClickListener {
            //Maximo de 5 h
            if (cuentaHorasVisita<=4){
                if (cuentaMinutosVisita == 30) {
                    modifyTimeVariables(false) // Incrementa las horas si es necesario
                } else {
                    cuentaMinutosVisita += 30 // Incrementa los minutos
                }
                modifyDurationText() // Actualiza el texto de duración
            }
        }

        btnFlechaAbajoDuracion.setOnClickListener {

            // Solo se permite reducir si la duración es mayor a 30 minutos
            if (cuentaHorasVisita > 0 || (cuentaHorasVisita == 0 && cuentaMinutosVisita > 30)) {
                if (cuentaMinutosVisita == 0) {
                    // Si los minutos están en 0, reducimos una hora y ajustamos minutos a 30
                    modifyTimeVariables(true)
                } else if (cuentaMinutosVisita == 30) {
                    // Si los minutos son 30, simplemente los reducimos a 0
                    cuentaMinutosVisita -= 30
                }

                // Actualizar el texto después de los cambios
                modifyDurationText()
            }
        }

        btnArquitectura.setOnClickListener {
            arquitecturaSeleccionado = !arquitecturaSeleccionado
            handleButtonClick(0, btnArquitectura, arquitecturaSeleccionado)
        }

        btnFunerario.setOnClickListener {
            funerarioSeleccionado = !funerarioSeleccionado
            handleButtonClick(1, btnFunerario, funerarioSeleccionado)
        }

        btnMilitar.setOnClickListener {
            militarSeleccionado = !militarSeleccionado
            handleButtonClick(2, btnMilitar, militarSeleccionado)
        }

        btnCostumbres.setOnClickListener {
            costumbresSeleccionado = !costumbresSeleccionado
            handleButtonClick(3, btnCostumbres, costumbresSeleccionado)
        }

        btnCuriosidades.setOnClickListener {
            curiosidadesSeleccionado = !curiosidadesSeleccionado
            handleButtonClick(4, btnCuriosidades, curiosidadesSeleccionado)
        }

        btnVolver.setOnClickListener {
            val siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla)
        }

        btnSiguiente.setOnClickListener {
            val siguientePantalla = Intent(this, QuickAdviseActivity::class.java)
            if(!showErrorMsg()){
                navigateToNextScreen(siguientePantalla)
            }
        }
    }

    /**
     * Maneja el clic de un botón que representa un tema en el sistema. Actualiza el estado de selección
     * del tema correspondiente, muestra u oculta un mensaje de error y realiza la actualización visual
     * del botón (destacándolo o restableciéndolo) según el estado de selección.
     *
     * @param index El índice del tema en el arreglo `temasSeleccionados` que se está actualizando.
     * @param btn El botón que se debe resaltar o restablecer, según el estado de selección.
     * @param seleccion El nuevo estado de selección del tema (true si seleccionado, false si no seleccionado).
     */
    private fun handleButtonClick(index: Int, btn: Button, seleccion: Boolean) {
        temasSeleccionados[index] = seleccion
        if (seleccion) {
            highlightButton(btn)
            textoTemasReq.visibility = View.GONE
        } else {
            resetButton(btn)
        }
    }

    /**
     * Verifica si todos los elementos del arreglo `temasSeleccionados` son `false`.
     * Si es así, muestra un mensaje de error solicitando seleccionar al menos un tema
     * y retorna `true`. Si al menos un elemento está seleccionado, oculta el mensaje
     * de error y retorna `false`.
     *
     * @return `true` si todos los temas están desmarcados, `false` en caso contrario.
     */
    private fun showErrorMsg():Boolean{
        if (temasSeleccionados.all { !it }) {
            textoTemasReq.visibility = View.VISIBLE
            return true
        }
        else{
            textoTemasReq.visibility = View.GONE
            return false
        }
    }

    /**
     * Método para formatear y mostrar la duración de la visita en un texto legible.
     *
     * Este método construye una cadena de texto que representa la duración de la visita de acuerdo con las horas y los minutos.
     * Se muestran los valores en el siguiente formato:
     * - Si solo hay minutos (30 minutos), se muestra en minutos.
     * - Si hay horas y minutos, se muestra en formato "Xh Ymin".
     * - Si solo hay horas completas, se muestra en horas.
     *
     * El resultado se actualiza en el `TextView` asociado a `textoDuracion`.
     */
    private fun modifyDurationText() {
        textoDuracion.text = buildString {
            when {
                // Caso en que solo hay minutos
                cuentaHorasVisita == 0 && cuentaMinutosVisita in listOf(30) -> {
                    append(cuentaMinutosVisita.toString())
                    append(" " + getString(R.string.minutos))
                }
                // Caso en que hay horas y minutos
                cuentaHorasVisita > 0 && cuentaMinutosVisita > 0 -> {
                    append(cuentaHorasVisita.toString())
                    append("h ")
                    append(cuentaMinutosVisita.toString())
                    append("min")
                }
                // Caso en que solo hay horas completas
                cuentaHorasVisita > 0 && cuentaMinutosVisita == 0 -> {
                    append(cuentaHorasVisita.toString())
                    append("h")
                }
            }
        }
    }

    /**
     * Método para modificar la duración de la visita, incrementando o decrementando el tiempo de forma controlada.
     *
     * Si el parámetro `subtract` es `false`, el método incrementa la duración sumando una hora y reiniciando los minutos a 0.
     * Si el parámetro `subtract` es `true`, el método decrementa la duración, restando una hora (si es posible) y ajustando los minutos a 30.
     *
     * Este método se utiliza para gestionar los cambios en el tiempo de la visita, ya sea aumentando o reduciendo el tiempo disponible.
     *
     * @param subtract Booleano que indica si se debe incrementar o decrementar la duración:
     *                - `false` para incrementar una hora.
     *                - `true` para decrementar una hora (ajustando los minutos a 30).
     */
    private fun modifyTimeVariables(subtract: Boolean) {
        if (subtract) {
            if (cuentaHorasVisita > 0) {
                cuentaHorasVisita-- // Decrementar la hora
                cuentaMinutosVisita = 30 // Ajustar minutos a 30 cuando se resta 1 hora
            }
        } else {
            // Incrementar la hora
            cuentaHorasVisita++
            cuentaMinutosVisita = 0 // Reiniciar minutos al sumar 1 hora
        }
    }

    //Restablece colores a los colores iniciales.
    private fun resetButton(boton: Button): Unit{
        boton.setTextColor(ContextCompat.getColor(this, R.color.black)) // Restablece color de texto
        boton.setBackgroundColor(ContextCompat.getColor(this, R.color.brown_normal)) // Restablece color de fondo
    }

    //Cambia colores de botones
    private fun highlightButton(boton: Button): Unit{
        boton.setTextColor(ContextCompat.getColor(this, R.color.grayish)) // Cambia color de texto
        boton.setBackgroundColor(ContextCompat.getColor(this, R.color.green_darker)) // Cambia color de fondo
    }

    /**
     * Este método se encarga de enviar la información necesaria a la siguiente pantalla.
     *
     * Se crea un `Intent` que contiene los siguientes datos:
     * - `idiomaSeleccionado`: El idioma actualmente seleccionado por el usuario.
     * - `visitaPersonalizada`: La etiqueta que indica que la visita es personalizada.
     * - `tiempoVisita`: El tiempo total de la visita en segundos, calculado usando el método `convertTimeToSeconds()`.
     * - `temasSeleccionados`: Los temas que el usuario ha seleccionado para la visita.
     *
     * Todos estos datos son enviados mediante el método `putExtra()` al `Intent`, para que puedan ser recuperados
     * por la siguiente actividad.
     *
     * Finalmente, el método `startActivity()` se llama para iniciar la nueva actividad a la cual se enviarán los datos.
     *
     * @param siguientePantalla El `Intent` que representa la siguiente pantalla a la que se navega.
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        val visitaPersonalizada: String = "Visita Personalizada" // Etiqueta para la visita
        siguientePantalla.putExtra("idiomaSeleccionado", this.idiomaSeleccionado) // Idioma seleccionado
        siguientePantalla.putExtra("visitaPersonalizada", visitaPersonalizada) // Tipo de visita
        siguientePantalla.putExtra("tiempoVisita", convertTimeToSeconds()) // Tiempo en segundos
        siguientePantalla.putExtra("temasSeleccionados", temasSeleccionados) // Temas seleccionados
        startActivity(siguientePantalla) // Inicia la nueva actividad
    }

    /**
     * Este método convierte el tiempo de la visita, expresado en horas y minutos, a segundos.
     *
     * Utiliza las variables `cuentaHorasVisita` y `cuentaMinutosVisita` para calcular el tiempo total
     * en segundos. La fórmula utilizada es:
     * - 1 hora = 3600 segundos (cuentaHorasVisita * 60 * 60)
     * - 1 minuto = 60 segundos (cuentaMinutosVisita * 60)
     *
     * El resultado es la suma de ambos valores en segundos, que es devuelto como un valor de tipo `Long`.
     *
     * @return El tiempo total en segundos como un valor de tipo `Long`.
     */
    private fun convertTimeToSeconds(): Long{
        return ((cuentaHorasVisita * 60 * 60) + cuentaMinutosVisita * 60).toLong()
    }

    /**
     * Este método cambia los textos de la interfaz de usuario al idioma seleccionado por el usuario.
     * Según el valor de `idiomaSeleccionado`, se actualizan los textos de los elementos visuales de la pantalla,
     * como botones, etiquetas, y opciones, con las traducciones correspondientes.
     * Los idiomas soportados son Español (esp), Inglés (eng), Alemán (deu), y Francés (fra).
     *
     * - Cambia los textos estáticos de la interfaz como títulos, botones y etiquetas a los valores traducidos.
     * - Modifica las opciones de visita según el idioma seleccionado.
     *
     * Esto permite ofrecer una experiencia de usuario personalizada y accesible según el idioma preferido.
     */
    private fun changeLanguage() {
        when (this.idiomaSeleccionado) {
            "esp" -> {
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada)
                textoFijoDuracion.text = getString(R.string.texto_duracion_visita_personalizada)
                textoFijoTipoVisita.text = getString(R.string.texto_tipo_visita_personalizada)
                opciones = "Recorrido por género"
                textoFijoTemasVisita.text = getString(R.string.texto_temas_visita_personalizada)
                btnArquitectura.text = getString(R.string.texto_boton_arquitectura_visita_personalizada)
                btnFunerario.text = getString(R.string.texto_boton_funerario_visita_personalizada)
                btnMilitar.text = getString(R.string.texto_boton_militar_visita_personalizada)
                btnCostumbres.text = getString(R.string.texto_boton_costumbres_visita_personalizada)
                btnCuriosidades.text = getString(R.string.texto_boton_curiosidades_visita_personalizada)
                textoTemasReq.text = getString(R.string.texto_temas_requeridos)
                btnVolver.text = getString(R.string.texto_boton_regresar)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente)
            }
            "eng" -> {
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada_eng)
                textoFijoDuracion.text = getString(R.string.texto_duracion_visita_personalizada_eng)
                textoFijoTipoVisita.text = getString(R.string.texto_tipo_visita_personalizada_eng)
                opciones = "Gender based tour"
                textoFijoTemasVisita.text = getString(R.string.texto_temas_visita_personalizada_eng)
                btnArquitectura.text = getString(R.string.texto_boton_arquitectura_visita_personalizada_eng)
                btnFunerario.text = getString(R.string.texto_boton_funerario_visita_personalizada_eng)
                btnMilitar.text = getString(R.string.texto_boton_militar_visita_personalizada_eng)
                btnCostumbres.text = getString(R.string.texto_boton_costumbres_visita_personalizada_eng)
                btnCuriosidades.text = getString(R.string.texto_boton_curiosidades_visita_personalizada_eng)
                textoTemasReq.text = getString(R.string.texto_temas_requeridos_eng)
                btnVolver.text = getString(R.string.texto_boton_regresar_eng)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente_eng)
            }
            "deu" -> {
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada_deu)
                textoFijoDuracion.text = getString(R.string.texto_duracion_visita_personalizada_deu)
                textoFijoTipoVisita.text = getString(R.string.texto_tipo_visita_personalizada_deu)
                opciones = "Aufschlüsselung nach Geschlecht"
                textoFijoTemasVisita.text = getString(R.string.texto_temas_visita_personalizada_deu)
                btnArquitectura.text = getString(R.string.texto_boton_arquitectura_visita_personalizada_deu)
                btnFunerario.text = getString(R.string.texto_boton_funerario_visita_personalizada_deu)
                btnMilitar.text = getString(R.string.texto_boton_militar_visita_personalizada_deu)
                btnCostumbres.text = getString(R.string.texto_boton_costumbres_visita_personalizada_deu)
                btnCuriosidades.text = getString(R.string.texto_boton_curiosidades_visita_personalizada_deu)
                textoTemasReq.text = getString(R.string.texto_temas_requeridos_deu)
                btnVolver.text = getString(R.string.texto_boton_regresar_deu)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente_deu)
            }
            "fra" -> {
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada_fra)
                textoFijoDuracion.text = getString(R.string.texto_duracion_visita_personalizada_fra)
                textoFijoTipoVisita.text = getString(R.string.texto_tipo_visita_personalizada_fra)
                opciones = "Tournée basée sur le genre"
                textoFijoTemasVisita.text = getString(R.string.texto_temas_visita_personalizada_fra)
                btnArquitectura.text = getString(R.string.texto_boton_arquitectura_visita_personalizada_fra)
                btnFunerario.text = getString(R.string.texto_boton_funerario_visita_personalizada_fra)
                btnMilitar.text = getString(R.string.texto_boton_militar_visita_personalizada_fra)
                btnCostumbres.text = getString(R.string.texto_boton_costumbres_visita_personalizada_fra)
                btnCuriosidades.text = getString(R.string.texto_boton_curiosidades_visita_personalizada_fra)
                textoTemasReq.text = getString(R.string.texto_temas_requeridos_fra)
                btnVolver.text = getString(R.string.texto_boton_regresar_fra)
                btnSiguiente.text = getString(R.string.texto_boton_siguiente_fra)
            }
        }
    }


}