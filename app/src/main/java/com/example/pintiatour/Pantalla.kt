package com.example.pintiatour

import java.io.Serializable

/**
 * Representa una pantalla en la aplicación, que está asociada con una actividad y un tema.
 *
 * Esta clase se utiliza para almacenar información sobre una pantalla específica en la aplicación,
 * incluyendo la clase de la actividad que se debe mostrar y el tema actual relacionado con esa pantalla.
 * La clase implementa la interfaz `Serializable` para que las instancias puedan ser pasadas entre actividades
 * a través de `Intent` como parte del proceso de navegación.
 *
 * @property activityClass La clase de la actividad asociada a esta pantalla.
 * @property temaActual El tema actual que se muestra en la pantalla (opcional).
 */
class Pantalla(
    var activityClass: Class<*>,
    var temaActual: String? = null
) : Serializable