package com.example.pintiatour

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView


class ContactInfoActivity : AppCompatActivity() {

    // Declaración de las variables para los elementos de la interfaz de usuario
    private lateinit var textoUbiPintia: TextView
    private lateinit var imgGoogleMaps: ShapeableImageView
    private lateinit var textoContactoInfo: TextView
    private lateinit var gifTelefonoContacto: ImageView
    private lateinit var textoTelefonoContacto: TextView
    private lateinit var gifGoogleChrome: ImageView
    private lateinit var textoWebContacto: TextView
    private lateinit var btnVolver: Button
    private var idiomaSeleccionado: String? = ""
    private lateinit var mp:MediaPlayer

    /**
     * Método principal que se ejecuta al crear la actividad.
     *
     * Este método es llamado cuando la actividad es creada. Se encarga de configurar la interfaz de usuario,
     * recuperar los datos de sesión, inicializar los componentes gráficos, configurar los eventos (listeners),
     * y ejecutar animaciones. Además, ajusta los márgenes de la vista principal para tener en cuenta las
     * barras del sistema, asegurando que el contenido no quede oculto bajo ellas.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        mp = MediaPlayer.create(this,R.raw.softpiano)
        mp.isLooping = true
        super.onCreate(savedInstanceState)
        // Habilita compatibilidad con bordes y márgenes de pantalla
        enableEdgeToEdge()
        // Configura el diseño de la actividad
        setContentView(R.layout.activity_contact_info)
        // Recupera los datos de sesión, inicializa los componentes y listeners
        getSessionData()
        initComponents()
        initListeners()
        animateGif()
        // Ajusta los márgenes de la vista principal para los bordes del sistema
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
     * Recupera los datos enviados a través del Intent, como el idioma seleccionado.
     *
     * Este método accede a los datos que fueron enviados a través del Intent, específicamente al
     * valor del idioma seleccionado por el usuario. El dato se recupera utilizando la clave
     * "idiomaSeleccionado" y se asigna a la variable `idiomaSeleccionado` de la clase.
     */
    private fun getSessionData() {
        idiomaSeleccionado = intent.extras?.getString("idiomaSeleccionado")
    }

    /**
     * Inicializa las referencias a los elementos de la interfaz y configura el idioma según los datos recuperados.
     *
     * Este método se encarga de encontrar las vistas de la interfaz de usuario por su ID, y luego
     * asignarlas a las variables correspondientes. Además, se invoca el método `changeLanguage()`
     * para ajustar los textos de la interfaz al idioma seleccionado por el usuario, basándose
     * en los datos que se hayan recuperado de la sesión.
     */
    private fun initComponents() {
        textoUbiPintia = findViewById(R.id.texto_ubicacion_pintia)
        imgGoogleMaps = findViewById(R.id.imagen_gps_google_maps_pintia)
        textoContactoInfo = findViewById(R.id.texto_contacto_mas_info)
        gifTelefonoContacto = findViewById(R.id.imagen_telefono_contacto)
        textoTelefonoContacto = findViewById(R.id.texto_contacto_telefono)
        textoWebContacto = findViewById(R.id.texto_contacto_web_pintia)
        gifGoogleChrome = findViewById(R.id.imagen_google_chrome)
        btnVolver = findViewById(R.id.boton_regresar_contacto)
        changeLanguage() // Cambia el idioma de los textos en función del idioma seleccionado
    }

    /**
     * Configura los eventos (listeners) para los elementos interactivos de la interfaz.
     *
     * Este método se encarga de asignar las acciones correspondientes a los elementos de la interfaz
     * de usuario como botones, imágenes, y textos. Cada evento es configurado para realizar una
     * acción específica como abrir Google Maps, realizar una llamada telefónica, abrir una página web
     * o navegar a la actividad anterior.
     */
    private fun initListeners() {
        // Al hacer clic en el ícono de Google Maps, abre la ubicación en la app de Maps
        imgGoogleMaps.setOnClickListener{
            openGoogleMaps()
        }
        // Al hacer clic en el número de teléfono, inicia una llamada telefónica
        textoTelefonoContacto.setOnClickListener {
            val telefono = Uri.parse("tel:${textoTelefonoContacto.text}")
            val siguientePantalla = Intent(Intent.ACTION_DIAL, telefono)
            navigateToNextScreen(siguientePantalla)
        }
        // Al hacer clic en el texto del sitio web, abre la página web en un navegador
        textoWebContacto.setOnClickListener {
            val urlWebContacto = Uri.parse(getString(R.string.web_pintia)) // Cambia esta URL por la real
            val siguientePantalla = Intent(Intent.ACTION_VIEW, urlWebContacto)
            navigateToNextScreen(siguientePantalla)
        }
        // Al hacer clic en el botón "Volver", navega a la actividad anterior
        btnVolver.setOnClickListener {
            val siguientePantalla = Intent(this, SelectVisitActivity::class.java)
            navigateToNextScreen(siguientePantalla)
        }

    }

    /**
     * Navega a la siguiente pantalla enviando el idioma seleccionado.
     *
     * Este método recibe un Intent para una nueva actividad y agrega el idioma seleccionado
     * como un dato extra al Intent. Luego, inicia la nueva actividad con los datos correspondientes.
     *
     * @param siguientePantalla El Intent que representa la actividad a la que se desea navegar.
     */
    private fun navigateToNextScreen(siguientePantalla: Intent) {
        siguientePantalla.putExtra("idiomaSeleccionado", this.idiomaSeleccionado)
        startActivity(siguientePantalla)
    }

    /**
     * Cambia los textos de la interfaz según el idioma seleccionado.
     *
     * Este método ajusta los textos de varios elementos de la interfaz (como textos de ubicación,
     * información de contacto y el botón de regresar) en función del idioma seleccionado por el usuario.
     * Se utilizan cadenas de texto específicas para cada idioma disponible (Español, Inglés, Alemán, Francés).
     * Los textos se actualizan a partir de los recursos de cadenas correspondientes.
     */
    private fun changeLanguage() {
        when (this.idiomaSeleccionado) {
            "esp" -> {
                textoUbiPintia.text = getString(R.string.texto_ubicacion_pintia)
                textoContactoInfo.text = getString(R.string.texto_contacto_mas_info)
                textoWebContacto.text = getString(R.string.texto_contacto_web_pintia)
                btnVolver.text = getString(R.string.texto_boton_regresar)
            }
            "eng" -> {
                textoUbiPintia.text = getString(R.string.texto_ubicacion_pintia_eng)
                textoContactoInfo.text = getString(R.string.texto_contacto_mas_info_eng)
                textoWebContacto.text = getString(R.string.texto_contacto_web_pintia_eng)
                btnVolver.text = getString(R.string.texto_boton_regresar_eng)
            }
            "deu" -> {
                textoUbiPintia.text = getString(R.string.texto_ubicacion_pintia_deu)
                textoContactoInfo.text = getString(R.string.texto_contacto_mas_info_deu)
                textoWebContacto.text = getString(R.string.texto_contacto_web_pintia_deu)
                btnVolver.text = getString(R.string.texto_boton_regresar_deu)
            }
            "fra" -> {
                textoUbiPintia.text = getString(R.string.texto_ubicacion_pintia_fra)
                textoContactoInfo.text = getString(R.string.texto_contacto_mas_info_fra)
                textoWebContacto.text = getString(R.string.texto_contacto_web_pintia_fra)
                btnVolver.text = getString(R.string.texto_boton_regresar_fra)
            }
        }
    }

    /**
     * Muestra animaciones GIF en los elementos correspondientes de la interfaz.
     *
     * Este método carga y reproduce dos animaciones GIF en los elementos de la interfaz correspondientes:
     * - Una animación de una llamada telefónica en `gifTelefonoContacto`.
     * - Una animación de Google Chrome en `gifGoogleChrome`.
     * Utiliza la biblioteca Glide para gestionar la carga y visualización de los GIFs.
     */
    private fun animateGif(){
        Glide.with(this)
            .asGif() // Asegúrate de especificar que es un GIF
            .load(R.drawable.phone_call_animation) // Tu GIF en res/drawable // Carga el GIF de la llamada telefónica
            .into(gifTelefonoContacto)

        Glide.with(this)
            .asGif() // Asegúrate de especificar que es un GIF
            .load(R.drawable.google_chrome_animation) // Tu GIF en res/drawable // Carga el GIF de Google Chrome
            .into(gifGoogleChrome)
    }

    /**
     * Abre la ubicación del Centro de Estudios Vacceos en Google Maps o un navegador web.
     *
     * Este método intenta abrir la ubicación del "Centro de Estudios Vacceos Federico Wattenberg"
     * en Google Maps utilizando un `Intent`. Si Google Maps no está disponible, intenta abrir
     * la ubicación en Google Chrome. Si ninguno de los dos está instalado, el método abrirá la
     * ubicación en cualquier navegador web disponible.
     *
     */
    private fun openGoogleMaps() {
        var geoUri = "https://www.google.com/maps/place/Centro+de+Estudios+Vacceos+Federico+Wattenberg/@41.6130664,-4.165301,18z/data=!3m1!4b1!4m6!3m5!1s0xd46eeb1b22583b5:0x2fe2bab3869175b7!8m2!3d41.6130664!4d-4.1640135!16s%2Fg%2F1ptypsjh5?entry=ttu&g_ep=EgoyMDI0MTExOC4wIKXMDSoASAFQAw%3D%3D"

        // Intent para abrir Google Maps
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        mapsIntent.setPackage("com.google.android.apps.maps")

        if (mapsIntent.resolveActivity(packageManager) != null) {
            // Si Google Maps está disponible, lo abre
            startActivity(mapsIntent)
        } else {
            // Si no encuentra Google Maps, intenta abrir con Google Chrome
            val chromeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
            chromeIntent.setPackage("com.android.chrome")

            if (chromeIntent.resolveActivity(packageManager) != null) {
                // Si Google Chrome está disponible, lo abre
                startActivity(chromeIntent)
            } else {
                // Si no hay Chrome, abre con cualquier navegador disponible
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                startActivity(fallbackIntent)
            }
        }
    }

}