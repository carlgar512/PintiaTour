package com.example.pintiatour

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class SelectVisitActivity : AppCompatActivity() {

    private lateinit var textoIdioma: TextView
    private lateinit var btnIdiomaEsp: Button
    private lateinit var btnIdiomaEng: Button
    private lateinit var btnIdiomaDeu: Button
    private lateinit var btnIdiomaFra: Button
    private var idiomaSeleccionado: String? = "esp"
    private lateinit var cardViewVisitaExpress: CardView
    private lateinit var textoVisitaExpress: TextView
    private lateinit var cardViewVisitaPersonalizada: CardView
    private lateinit var textoVisitaPersonalizada: TextView
    private lateinit var cardViewContacto: CardView
    private lateinit var textoContacto: TextView
    private lateinit var mp: MediaPlayer

    /**
     * Inicializa la actividad configurando la interfaz, datos de sesión, y ajustes visuales.
     *
     * - Configura el reproductor de audio (`MediaPlayer`) con reproducción en bucle.
     * - Habilita el modo "Edge-to-Edge" para maximizar el uso de pantalla.
     * - Establece el diseño principal (`R.layout.activity_select_visit`).
     * - Recupera datos de sesión (como el idioma) y configura componentes y listeners.
     * - Ajusta los márgenes para evitar superposición con las barras del sistema.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mp = MediaPlayer.create(this,R.raw.softpiano)
        mp.isLooping = true
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_visit)
        getSessionData()
        initComponents()
        initListeners()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Reanuda la reproducción del audio cuando la actividad vuelve a primer plano.
     * Verifica si el reproductor no está reproduciendo y lo inicia.
     */
    override fun onResume() {
        super.onResume()
        if (!mp.isPlaying) {
            mp.start()
        }
    }

    /**
     * Pausa la reproducción del audio cuando la actividad pasa a segundo plano.
     * Verifica si el reproductor está reproduciendo y lo detiene temporalmente.
     */
    override fun onPause() {
        super.onPause()
        if (mp.isPlaying) {
            mp.pause()
        }
    }

    /**
     * Libera los recursos del MediaPlayer al destruir la actividad.
     * Esto asegura que no haya fugas de memoria relacionadas con el reproductor.
     */
    override fun onDestroy() {
        super.onDestroy()
        mp.release()
    }

    /**
     * Recupera los datos de sesión, como el idioma seleccionado, desde el Intent.
     */
    private fun getSessionData() {
        // Asigna el idioma seleccionado desde los extras del Intent si está disponible
        idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado") ?: idiomaSeleccionado
    }

    /**
     * Inicializa las referencias a los elementos de la interfaz y configura el idioma de la interfaz según el idioma seleccionado.
     * Se asignan los componentes de la vista (como botones, textos, y cardViews) a sus respectivas variables.
     * También se ajusta el idioma de la interfaz utilizando el método `changeLanguage` y se resalta el botón correspondiente al idioma actual.
     */
    private fun initComponents() {

        textoIdioma = findViewById(R.id.texto_select_visit_idioma)
        btnIdiomaEsp = findViewById(R.id.boton_idioma_espanol)
        btnIdiomaEng = findViewById(R.id.boton_idioma_ingles)
        btnIdiomaDeu = findViewById(R.id.boton_idioma_aleman)
        btnIdiomaFra = findViewById(R.id.boton_idioma_frances)
        cardViewVisitaExpress = findViewById(R.id.CardViewVisitaExpress)
        textoVisitaExpress = findViewById(R.id.texto_select_visita_express)
        cardViewVisitaPersonalizada = findViewById(R.id.CardViewVisitaPersonalizada)
        textoVisitaPersonalizada = findViewById(R.id.texto_select_visita_personalizada)
        cardViewContacto = findViewById(R.id.CardViewContacto)
        textoContacto = findViewById(R.id.texto_select_visit_contacto)

        // Cambia el idioma de la interfaz
        changeLanguage()

        // Resalta el botón correspondiente al idioma actual
        when (idiomaSeleccionado) {
            "esp" -> highlightButton(btnIdiomaEsp, btnIdiomaEng, btnIdiomaDeu, btnIdiomaFra)
            "eng" -> highlightButton(btnIdiomaEng, btnIdiomaEsp, btnIdiomaDeu, btnIdiomaFra)
            "deu" -> highlightButton(btnIdiomaDeu, btnIdiomaEsp, btnIdiomaEng, btnIdiomaFra)
            "fra" -> highlightButton(btnIdiomaFra, btnIdiomaEsp, btnIdiomaEng, btnIdiomaDeu)
        }
    }

    /**
     * Configura los listeners para los elementos interactivos de la interfaz.
     * Cada botón de idioma establece el idioma seleccionado y cambia el idioma de la interfaz.
     * Los listeners de los "cardViews" de visita expres y personalizada navegan a las actividades correspondientes
     * pasándoles los datos necesarios, como el tipo de visita. Además, se configura la navegación a la pantalla de contacto.
     */
    private fun initListeners() {
        btnIdiomaEsp.setOnClickListener {
            idiomaSeleccionado = "esp"
            highlightButton(btnIdiomaEsp, btnIdiomaEng, btnIdiomaDeu, btnIdiomaFra)
            changeLanguage()
        }

        btnIdiomaEng.setOnClickListener {
            idiomaSeleccionado = "eng"
            highlightButton(btnIdiomaEng, btnIdiomaEsp, btnIdiomaDeu, btnIdiomaFra)
            changeLanguage()
        }

        btnIdiomaDeu.setOnClickListener {
            idiomaSeleccionado = "deu"
            highlightButton(btnIdiomaDeu, btnIdiomaEng, btnIdiomaEsp, btnIdiomaFra)
            changeLanguage()
        }

        btnIdiomaFra.setOnClickListener {
            idiomaSeleccionado = "fra"
            highlightButton(btnIdiomaFra, btnIdiomaEng, btnIdiomaEsp, btnIdiomaDeu)
            changeLanguage()
        }

        cardViewVisitaExpress.setOnClickListener {
            val siguientePantalla = Intent(this, QuickAdviseActivity::class.java)
            navigateToNextScreen(siguientePantalla, "express")
        }

        cardViewVisitaPersonalizada.setOnClickListener {
            val siguientePantalla = Intent(this, CustomVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla, "personalizada")
        }

        cardViewContacto.setOnClickListener {
            val siguientePantalla = Intent(this, ContactInfoActivity::class.java)
            navigateToNextScreen(siguientePantalla, "contacto")
        }
    }

    /**
     * Método que navega a la siguiente pantalla y pasa los datos relevantes del idioma seleccionado
     * y el tipo de visita (express o personalizada) a la actividad siguiente a través de un Intent.
     * Dependiendo del tipo de visita, se agrega información específica (como el tiempo de visita para
     * la visita express) a los extras del Intent.
     *
     * @param siguientePantalla El Intent que define la siguiente pantalla a la que se navegará.
     * @param tipoVisita El tipo de visita seleccionado ("express" o "personalizada"), que determina
     *                   qué información adicional se incluye en el Intent.
     */
    private fun navigateToNextScreen(siguientePantalla: Intent, tipoVisita: String) {
        // Asigna el idioma seleccionado a la siguiente pantalla
        siguientePantalla.putExtra("idiomaSeleccionado", this.idiomaSeleccionado)

        // Agrega los extras correspondientes según el tipo de visita
        when (tipoVisita) {
            "express" -> {
                siguientePantalla.apply {
                    putExtra("visitaExpress", "Visita Express")
                    putExtra("tiempoVisita", 30)
                }
            }
            "personalizada" -> siguientePantalla.putExtra("visitaPersonalizada", "Visita Personalizada")
        }

        // Inicia la siguiente pantalla
        startActivity(siguientePantalla)
    }

    /**
     * Método que resalta el botón correspondiente al idioma seleccionado cambiando su color de fondo
     * a un color más oscuro. Los otros tres botones se configuran con un color más claro.
     * Esto proporciona una indicación visual clara del idioma actualmente seleccionado por el usuario.
     *
     * @param btn1 El botón que representa el idioma seleccionado, que será resaltado en color verde oscuro.
     * @param btn2 El segundo botón que será coloreado con un tono marrón.
     * @param btn3 El tercer botón que será coloreado con un tono marrón.
     * @param btn4 El cuarto botón que será coloreado con un tono marrón.
     */
    private fun highlightButton(btn1: Button, btn2: Button, btn3: Button, btn4: Button) {
        btn1.setBackgroundColor(ContextCompat.getColor(this, R.color.green_darker))
        btn2.setBackgroundColor(ContextCompat.getColor(this, R.color.brown_normal))
        btn3.setBackgroundColor(ContextCompat.getColor(this, R.color.brown_normal))
        btn4.setBackgroundColor(ContextCompat.getColor(this, R.color.brown_normal))
    }

    /**
     * Método que cambia los textos de la interfaz según el idioma seleccionado por el usuario.
     * Además, resalta el botón del idioma actual y desactiva los demás.
     * Los textos de la interfaz para las visitas express y personalizadas, así como el contacto,
     * se actualizan dinámicamente según el idioma seleccionado.
     */
    private fun changeLanguage() {
        when (this.idiomaSeleccionado) {
            "esp" -> {
                textoIdioma.text = getString(R.string.texto_select_visit_idioma)
                textoVisitaExpress.text = getString(R.string.texto_select_visit_visita_express)
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada)
                textoContacto.text = getString(R.string.texto_select_visit_contacto)
                highlightButton(btnIdiomaEsp, btnIdiomaEng, btnIdiomaDeu, btnIdiomaFra)
            }
            "eng" -> {
                textoIdioma.text = getString(R.string.texto_select_visit_idioma_eng)
                textoVisitaExpress.text = getString(R.string.texto_select_visit_visita_express_eng)
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada_eng)
                textoContacto.text = getString(R.string.texto_select_visit_contacto_eng)
                highlightButton(btnIdiomaEng, btnIdiomaEsp, btnIdiomaDeu, btnIdiomaFra)
            }
            "deu" -> {
                textoIdioma.text = getString(R.string.texto_select_visit_idioma_deu)
                textoVisitaExpress.text = getString(R.string.texto_select_visit_visita_express_deu)
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada_deu)
                textoContacto.text = getString(R.string.texto_select_visit_contacto_deu)
                highlightButton(btnIdiomaDeu, btnIdiomaEng, btnIdiomaEsp, btnIdiomaFra)
            }
            "fra" -> {
                textoIdioma.text = getString(R.string.texto_select_visit_idioma_fra)
                textoVisitaExpress.text = getString(R.string.texto_select_visit_visita_express_fra)
                textoVisitaPersonalizada.text = getString(R.string.texto_select_visit_visita_personalizada_fra)
                textoContacto.text = getString(R.string.texto_select_visit_contacto_fra)
                highlightButton(btnIdiomaFra, btnIdiomaEsp, btnIdiomaDeu, btnIdiomaEng)
            }
        }
    }

}