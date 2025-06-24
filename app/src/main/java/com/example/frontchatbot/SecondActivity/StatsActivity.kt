package com.example.frontchatbot.SecondActivity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.frontchatbot.FirstActivity.LoginActivity
import com.example.frontchatbot.R
import com.example.frontchatbot.SecondActivity.RecyclerView.CategoryAdapter
import com.example.frontchatbot.SecondActivity.RecyclerView.CategoryItem
import com.example.frontchatbot.SecondActivity.RecyclerView.CategoryLeftAdapter
import com.example.frontchatbot.SecondActivity.api.RetrofitClient
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class StatsActivity : AppCompatActivity() {
    //Creando listas de categorias y subcategorias para los RV.
    private val categories = listOf(
        CategoryItem.Campus,
        CategoryItem.Admission,
        CategoryItem.Other
    )
    private val categories2 = listOf(
        CategoryItem.Inscription,
        CategoryItem.Context,
        CategoryItem.WOAnswer
    )
    private val subCategories1 = listOf(
        CategoryItem.Buildings,
        CategoryItem.Services,
        CategoryItem.Careers,
        CategoryItem.GeneralInfo,
        CategoryItem.Costs,
        CategoryItem.ContactFollowup,
        CategoryItem.DocumentsDelivery,
        CategoryItem.WOAnswer
    )
    private val subCategories2 = listOf(
        CategoryItem.Reenrollment,
        CategoryItem.StudyGuide,
        CategoryItem.Graduation,
        CategoryItem.Scholarships,
        CategoryItem.SchoolControl,
        CategoryItem.AcademicLoad,
        CategoryItem.English,
        CategoryItem.ImportantDates,
        CategoryItem.Context
    )

    //Creando las instancias para los objetos de la vista.
    private lateinit var adapter: CategoryAdapter
    private lateinit var rvRightCategory: RecyclerView
    private lateinit var adapter2: CategoryLeftAdapter
    private lateinit var rvLeftCategory: RecyclerView

    private lateinit var adapterSub: CategoryAdapter
    private lateinit var rvSubthemeR: RecyclerView
    private lateinit var adapterSub2: CategoryLeftAdapter
    private lateinit var rvSubthemeL: RecyclerView

    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerWeek: Spinner

    private lateinit var pieChart: PieChart
    private lateinit var pieSubthemes: PieChart

    private lateinit var mbLogout: MaterialButton

    private var authToken: String = ""// Declarar el token como una propiedad de la clase

    // Constante para el nombre de Shared Preferences y la clave del token
    private val PREFS_NAME = "MyAuthPrefs"
    private val ACCESS_TOKEN_KEY = "access_token"

    private val categoryColorsMap = mapOf( //Mapeando cada una de las categorias de la respuesta de la API con su respectivo color para el pie chart.
        "Fuera de contexto" to R.color.category_context_color,
        "Información del campus" to R.color.category_campus_color,
        "Proceso de admisión" to R.color.category_admission_color,
        "Proceso de inscripción" to R.color.category_inscription_color,
        "Otros procesos" to R.color.category_other_color,
        "Sin respuesta" to R.color.category_woanswer_color,
        "Campus" to R.color.subcategory_buildings_color,
        "Servicios (Servicios escolares, cafeterias, TOEFL, Asesorias)" to R.color.subcategory_services_color,
        "Carreras" to R.color.subcategory_careers_color,
        "Informacion general (Ficha de registro, pre-registro, configuracion de correo electronico, ejercicio practico, atencion personalizada, documentos a cargar, consulta de resultados, guia de estudios)" to R.color.subcategory_general_info_color,
        "Costos (Ficha de admision, inscripcion, curso de nivelacion)" to R.color.subcategory_costs_color,
        "Fechas importantes (Inicio de clases)" to R.color.subcategory_important_dates_color,
        "Seguimiento y contacto (Correos y contactos por área)" to R.color.subcategory_contact_followup_color,
        "Entrega de documentos" to R.color.subcategory_documents_delivery_color,
        "Reinscripción en línea" to R.color.subcategory_reenrollment_color,
        "Guia de estudio" to R.color.subcategory_study_guide_color,
        "Titulacion (Contacto)" to R.color.subcategory_graduation_color,
        "Becas (Contacto)" to R.color.subcategory_scholarships_color,
        "Control escolar (Contacto)" to R.color.subcategory_school_control_color,
        "Carga academica (Contacto)" to R.color.subcategory_academic_load_color,
        "Ingles (Contacto)" to R.color.subcategory_english_color
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        authToken = getAccessTokenFromPrefs() ?: "" // Obtener el token de SharedPreferences, si no existe, se crea una cadena vacía.

        if (authToken.isBlank() || !isTokenValid(authToken)) { //Si el token no existe o no es valido.
            Toast.makeText(this, "Sesión caducada o no iniciada. Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
            clearTokensFromPrefs()
            redirectToLogin()
            return // Salir de onCreate para evitar NullPointerException
        }

        initComponent() //Iniciamos los componentes de la vista.
        setListeners() //Iniciamos los listeners
        initIU() // Esto incluye initSpinners
        setupSpinnerListeners() // Configura los listeners después de la inicialización de UI
    }

    override fun onResume() { //Funcion para cuando se recargue la actividad.
        super.onResume()
        authToken = getAccessTokenFromPrefs() ?: "" // Recargar el token por si acaso se modificó en otro lado
        if (authToken.isBlank() || !isTokenValid(authToken)) { //Si el token no existe o no es valido.
            Toast.makeText(this, "Sesión expirada. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            clearTokensFromPrefs() //Limpiamos los tokens
            redirectToLogin() //Redirigimos al login
        } else { // Si el token es válido, forzar la recarga de datos para actualizar los gráficos
            val monthMap = getMonthNameToNumberMap()
            fetchAndDisplayStats(monthMap)
        }
    }

    private fun initComponent() { //Inicializacion de componentes de la vista
        adapter = CategoryAdapter(categories)
        rvRightCategory = findViewById(R.id.rvRightCategory)

        adapter2 = CategoryLeftAdapter(categories2)
        rvLeftCategory = findViewById(R.id.rvLeftCategory)

        adapterSub = CategoryAdapter(subCategories1)
        rvSubthemeR = findViewById(R.id.rvSubthemeRight)

        adapterSub2 = CategoryLeftAdapter(subCategories2)
        rvSubthemeL = findViewById(R.id.rvSubthemeLeft)

        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerWeek = findViewById(R.id.spinnerWeek)

        pieChart = findViewById(R.id.pcData)
        pieSubthemes = findViewById(R.id.pcSubTheme)

        mbLogout = findViewById(R.id.mbLogout)
    }

    private fun setListeners() {
        mbLogout.setOnClickListener { //Listener para el boton de logout.
            logout() //Cierra la sesion
        }
    }

    private fun initIU() {
        initRecyclerViews() //Iniciamos los RV
        initSpinners() //Iniciamos los spinners.
    }

    private fun initRecyclerViews() { //Para iniciar los RV.
        adapter = CategoryAdapter(categories) //Instanciamos el adapter con las categorias que varian de RV a RV.
        rvRightCategory.layoutManager = LinearLayoutManager(this) //Definimos el layout manager.
        rvRightCategory.adapter = adapter //Definimos el adapter con las categorias que varian de RV a RV.

        adapter2 = CategoryLeftAdapter(categories2) //Para el segundo RV de las categorias.
        rvLeftCategory.layoutManager = LinearLayoutManager(this)
        rvLeftCategory.adapter = adapter2

        adapterSub = CategoryAdapter(subCategories1) //Para el RV de las subcategorias.
        rvSubthemeR.layoutManager = LinearLayoutManager(this)
        rvSubthemeR.adapter = adapterSub

        adapterSub2 = CategoryLeftAdapter(subCategories2) //Para el segundo RV de las subcategorias.
        rvSubthemeL.layoutManager = LinearLayoutManager(this)
        rvSubthemeL.adapter = adapterSub2
    }

    private fun initSpinners() { //Inicializamos los spinners
        val years = resources.getStringArray(R.array.years) //Obtenemos los arrays para cada uno de los spinners, definidos desde strings.xml.
        val months = resources.getStringArray(R.array.months)
        val weeks = resources.getStringArray(R.array.weeks)

        val yearAdapter = ArrayAdapter( //Creando un ArrayAdapter para definir el como se veran los spinners (layout creado por mi).
            this,
            R.layout.item_custom_spinner, // Layout para el ítem seleccionado
            years) //Mando la lista de años
        yearAdapter.setDropDownViewResource(R.layout.item_custom_spinner_dropdown) //Definiendo el como se verá el dropdown del spinner.
        spinnerYear.adapter = yearAdapter //Asignando el adapter al spinner.
        spinnerYear.setSelection(0) // Seleccionar "Año" por defecto (primera posición)

        val monthAdapter = ArrayAdapter(
            this,
            R.layout.item_custom_spinner,
            months)
        monthAdapter.setDropDownViewResource(R.layout.item_custom_spinner_dropdown)
        spinnerMonth.adapter = monthAdapter
        spinnerMonth.setSelection(0)

        val weekAdapter = ArrayAdapter(
            this,
            R.layout.item_custom_spinner,
            weeks)
        weekAdapter.setDropDownViewResource(R.layout.item_custom_spinner_dropdown)
        spinnerWeek.adapter = weekAdapter
        spinnerWeek.setSelection(0)
    }

    private fun getMonthNameToNumberMap(): Map<String, Int> { //Funcion que mapea los numeros del mes con el nombre del mes.
        val months = resources.getStringArray(R.array.months) //Obtenemos el array de meses.
        val map = mutableMapOf<String, Int>() //Creamos un mapa mutable.
        for (i in 1 until months.size) { //Recorremos el array de meses, comenzando desde el segundo elemento.
            map[months[i]] = i
        }
        return map
    }

    private fun fetchAndDisplayStats(monthMap: Map<String, Int>) {
        if (authToken.isBlank() || !isTokenValid(authToken)) { // Asegurarse de que el token esté presente y sea válido antes de hacer la llamada a la API
            Toast.makeText(this@StatsActivity, "Sesión inválida, redirigiendo al login.", Toast.LENGTH_LONG).show()
            redirectToLogin() // Redirigir al login
            return
        }

        lifecycleScope.launch {
            try {
                val selectedYearString = spinnerYear.selectedItem?.toString()
                val year: Int? = if (selectedYearString == resources.getStringArray(R.array.years)[0]) {
                    null
                } else {
                    selectedYearString?.toIntOrNull()
                }

                val selectedMonthName = spinnerMonth.selectedItem?.toString()
                val month: Int? = if (selectedMonthName == resources.getStringArray(R.array.months)[0]) {
                    null
                } else {
                    selectedMonthName?.let { monthMap[it] }
                }

                val selecedWeekString = spinnerWeek.selectedItem?.toString()
                val week: Int? = if (selecedWeekString == resources.getStringArray(R.array.weeks)[0]) {
                    null
                } else {
                    selecedWeekString?.toIntOrNull()
                }

                //Habilitamos y deshabilitamos el spinnerWeek según los valores de month y year
                if(month == null || year == null){
                    spinnerWeek.isEnabled = false
                    spinnerWeek.isClickable = false
                    spinnerWeek.alpha = 0.5f
                    spinnerWeek.setSelection(0) // Restaura la selección a "Semana"

                }else {
                    spinnerWeek.isEnabled = true
                    spinnerWeek.isClickable = true
                    spinnerWeek.alpha = 1.0f // Restaura la opacidad normal
                }

                val api = RetrofitClient.create(authToken) //Creamos la instancia de la API con el token.
                val statsTheme = api.getEstadisticasPorTema(year, month, week) //Obtenemos las estadisticas de tema.
                val statsSubTheme = api.getEstadisticasPorSubTema(year, month, week) //Obtenemos las estadisticas de subtema.
                val total = api.getTotalConsultas(year, month, week) //Obtenemos el total de consultas.
                val cantidadUsuarios = api.getCantidadUsuarios(year, month, week) //Obtenemos la cantidad de usuarios.

                actualizarPieChart(statsTheme, total, cantidadUsuarios) //Actualizamos el pie chart con las estadisticas de tema.
                actualizarPieChartSubtemas(statsSubTheme, total, cantidadUsuarios) //Actualizamos el pie chart con las estadisticas de subtema.

            } catch (e: HttpException) {
                // Captura errores HTTP (por ejemplo, 401 Unauthorized)
                if (e.code() == 401) {
                    Toast.makeText(this@StatsActivity, "Sesión expirada. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
                    logout() // Redirigir al login y limpiar token
                } else {
                    Toast.makeText(this@StatsActivity, "Error de servidor: ${e.message()}", Toast.LENGTH_LONG).show()
                }
                pieChart.clear() //Limpiamos el pie chart.
                pieChart.setNoDataText("Error al cargar datos. Intenta de nuevo.") //Definimos el texto del pie chart.
                pieChart.invalidate() //Actualizamos el pie chart.
                pieSubthemes.clear()
                pieSubthemes.setNoDataText("Error al cargar datos. Intenta de nuevo.")
                pieSubthemes.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StatsActivity, "Error al obtener estadísticas: ${e.message}", Toast.LENGTH_LONG).show()
                pieChart.clear()
                pieChart.setNoDataText("Error al cargar datos. Intenta de nuevo.")
                pieChart.invalidate()
                pieSubthemes.clear()
                pieSubthemes.setNoDataText("Error al cargar datos. Intenta de nuevo.")
                pieSubthemes.invalidate()
            }
        }
    }

    private fun setupSpinnerListeners() { //Funcion para configurar los listeners de los spinners.
        val monthMap = getMonthNameToNumberMap() //Obtenemos el mapeo de meses

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { //Listener para el spinner de años.
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { //Funcion que se ejecuta cuando se selecciona un año.
                spinnerWeek.setSelection(0) //Reseteamos el spinner de semanas
                fetchAndDisplayStats(monthMap) //Lanzamos las estadisticas del años eleccionado.
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* No-op */ }
        }

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                spinnerWeek.setSelection(0)
                fetchAndDisplayStats(monthMap)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* No-op */ }
        }

        spinnerWeek.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                fetchAndDisplayStats(monthMap) //Lanzamos las estadisticas del año seleccionado, al haber cambiado la semana no se resetea la misma.
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* No-op */ }
        }
    }

    private fun actualizarPieChart(datos: Map<String, Double>, total: Long, cantidadUsuarios: Long) { //Funcion para actualizar el pie chart.
        val entries = mutableListOf<PieEntry>() //Instanciamos una lista para los entries
        val colors = mutableListOf<Int>() //Instanciamos una lista para los colores

        datos.forEach { (categoryName, value) -> //Recorremos el mapa de datos.
            entries.add(PieEntry(value.toFloat(), categoryName.trim())) //Agregamos el entry al mapa (que viene a ser el nombre de la categoria).

            val colorResId = categoryColorsMap[categoryName.trim()] ?: R.color.category_other_color //Obtenemos el color de la categoria, si no se mapea se pone el color gris.
            colors.add(ContextCompat.getColor(this, colorResId)) //Agregamos el color al mapa.
        }

        val dataSet = PieDataSet(entries, "Categorías") //Agregamos el dataSet con el titulo de categorias.
        dataSet.colors = colors //Agregamos los colores al dataSet.
        dataSet.valueTextSize = 14f //Tamaño del texto de los valores.
        dataSet.valueTextColor = Color.BLACK //Color del texto de los valores.

        val data = PieData(dataSet) //Agregamos el dataSet al pie chart.
        pieChart.data = data //Agregamos los datos al pie chart.
        pieChart.centerText = "Consultas: $total\nUsuarios: $cantidadUsuarios" //Agregamos el texto del centro.
        pieChart.setEntryLabelColor(Color.BLACK) //Color de las etiquetas.
        pieChart.setDrawEntryLabels(false) //Deshabilitamos las etiquetas.
        pieChart.description.isEnabled = false //Deshabilitamos la descripcion.
        pieChart.legend.isEnabled = false //Deshabilitamos la leyenda.
        pieChart.legend.textColor = Color.BLACK //Color de la leyenda.
        pieChart.animateY(1000) //Animacion del pie chart.
        pieChart.invalidate() //Actualizamos el pie chart.

        if (entries.isEmpty()) { //Si no hay datos, mostramos un mensaje.
            pieChart.setNoDataText("No hay datos disponibles para la selección actual.") //Definimos el texto del pie chart.
            pieChart.setNoDataTextColor(Color.GRAY) //Definimos el color del texto del pie chart.
            pieChart.invalidate() //Actualizamos el pie chart.
        }
    }

    private fun actualizarPieChartSubtemas( //Funcion para actualizar el pie chart de subtemas.
        datosSubtema: Map<String, Double>,
        total: Long,
        cantidadUsuarios: Long
    ) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        datosSubtema.forEach { (subthemeName, value) ->
            entries.add(PieEntry(value.toFloat(), subthemeName.trim()))

            val colorResId = categoryColorsMap[subthemeName.trim()] ?: R.color.category_other_color
            colors.add(ContextCompat.getColor(this, colorResId))
        }

        val dataSet = PieDataSet(entries, "Subtemas")
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)
        pieSubthemes.data = data
        pieSubthemes.centerText = "Consultas: $total\nUsuarios: $cantidadUsuarios"
        pieSubthemes.setEntryLabelColor(Color.BLACK)
        pieSubthemes.setDrawEntryLabels(false)
        pieSubthemes.description.isEnabled = false
        pieSubthemes.legend.isEnabled = false
        pieSubthemes.legend.textColor = Color.BLACK
        pieSubthemes.animateY(1000)
        pieSubthemes.invalidate()

        if (entries.isEmpty()) {
            pieSubthemes.setNoDataText("No hay datos disponibles para la selección actual.")
            pieSubthemes.setNoDataTextColor(Color.GRAY)
            pieSubthemes.invalidate()
        }
    }

    private fun isTokenValid(token: String): Boolean { //Funcion para validar el token.
        try {
            val parts = token.split(".") // Dividir el token en partes
            if (parts.size != 3) {
                Log.w("StatsActivity", "JWT no tiene 3 partes: $token") //Si no tiene 3 partes, no es valido
                return false
            }

            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE)) // Obtener el payload en formato JSON
            val payload = JSONObject(payloadJson) // Convertir el payload a un objeto JSON

            val exp = payload.optLong("exp", 0) // Obtener el valor de 'exp' del payload
            if (exp == 0L) { //Si no existe o es 0, no es valido
                Log.w("StatsActivity", "JWT no tiene campo 'exp' o es 0")
                return false
            }

            val currentTime = System.currentTimeMillis() / 1000  // en segundos

            Log.d("StatsActivity", "Current time: $currentTime, Token exp: $exp")
            return currentTime < exp
        } catch (e: Exception) {
            Log.e("StatsActivity", "Error al validar token JWT", e)
            return false
        }
    }

    private fun getAccessTokenFromPrefs(): String? { //Funcion para obtener el token de Shared Preferences.
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) //Obtenemos las preferencias de Shared Preferences.
        return sharedPrefs.getString(ACCESS_TOKEN_KEY, null) //Obtenemos el token.
    }

    private fun clearTokensFromPrefs() { //Funcion para limpiar el token de Shared Preferences.
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) //Obtenemos las preferencias de Shared Preferences.
        with(sharedPrefs.edit()) { //Editamos las preferencias.
            remove(ACCESS_TOKEN_KEY)
            remove("refresh_token")
            apply()
        }
    }

    private fun logout() { //Funcion para cerrar la sesion.
        clearTokensFromPrefs() //Limpiamos el token.
        redirectToLogin() //Redirigimos al login.
    }

    private fun redirectToLogin() { //Funcion que te redirige al login.
        val intent = Intent(this, LoginActivity::class.java) //Instanciamos el intent.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Hacemos que no se pueda volver atrás, limpiamos las actividades anteriores y creamos una nueva.
        startActivity(intent)
        finish() // Finaliza la actividad actual para que no se pueda volver atrás
    }
}