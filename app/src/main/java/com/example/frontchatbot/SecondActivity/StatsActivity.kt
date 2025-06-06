package com.example.frontchatbot.SecondActivity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
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
import com.example.frontchatbot.ThirdActivity.ChangePromptActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException


class StatsActivity : AppCompatActivity() {

    private val categories = listOf(
        CategoryItem.Admission,
        CategoryItem.Academics,
        CategoryItem.Inscription,
        CategoryItem.Results,
        CategoryItem.Costs,
    )

    private val categories2 = listOf(
        CategoryItem.Contact,
        CategoryItem.StudentLife,
        CategoryItem.Dates,
        CategoryItem.Modalities,
        CategoryItem.Scholarships,
        CategoryItem.Other
    )

    private lateinit var adapter: CategoryAdapter
    private lateinit var rvRightCategory: RecyclerView
    private lateinit var adapter2: CategoryLeftAdapter
    private lateinit var rvLeftCategory: RecyclerView

    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerWeek: Spinner

    private lateinit var pieChart: PieChart

    private lateinit var mbLogout: MaterialButton
    private lateinit var FABPrompt: FloatingActionButton

    // Declarar el token como una propiedad de la clase
    private var authToken: String = ""

    // Constante para el nombre de Shared Preferences y la clave del token
    private val PREFS_NAME = "MyAuthPrefs"
    private val ACCESS_TOKEN_KEY = "access_token"

    private val categoryColorsMap = mapOf(
        "requisitos de admisión" to R.color.category_admission_color,
        "fechas importantes" to R.color.category_dates_color,
        "oferta académica" to R.color.category_academics_color,
        "proceso de inscripción" to R.color.category_inscription_color,
        "resultados y seguimiento" to R.color.category_results_color,
        "costos" to R.color.category_costs_color,
        "modalidades" to R.color.category_modalities_color,
        "información de contacto o ubicación" to R.color.category_contact_color,
        "vida estudiantil" to R.color.category_student_life_color,
        "becas y apoyos" to R.color.category_scholarships_color,
        "otros" to R.color.category_other_color
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // Obtener el token de SharedPreferences, NO del Intent aquí
        // El Intent se usó para la primera vez que se lanza StatsActivity
        // Después de eso, el token debe persistir en SharedPreferences
        authToken = getAccessTokenFromPrefs() ?: "" // Asignar el token persistido o vacío

        // Si el token no está presente al iniciar la actividad (ej. si se limpió la app),
        // o si es inválido, redirigir al login.
        if (authToken.isBlank() || !isTokenValid(authToken)) {
            Toast.makeText(this, "Sesión caducada o no iniciada. Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
            clearTokensFromPrefs()
            redirectToLogin()
            return // Salir de onCreate para evitar NullPointerException
        }

        initComponent()
        setListeners()
        initIU() // Esto incluye initSpinners
        setupSpinnerListeners() // Configura los listeners después de la inicialización de UI
    }

    override fun onResume() {
        super.onResume()
        // Verificar el token cada vez que la actividad se reanuda
        // Esto es crucial para detectar expiración mientras la app estuvo en segundo plano
        authToken = getAccessTokenFromPrefs() ?: "" // Recargar el token por si acaso se modificó en otro lado
        if (authToken.isBlank() || !isTokenValid(authToken)) {
            Toast.makeText(this, "Sesión expirada. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            clearTokensFromPrefs()
            redirectToLogin()
        } else {
            // Si el token es válido, forzar la recarga de datos para actualizar los gráficos
            val monthMap = getMonthNameToNumberMap()
            fetchAndDisplayStats(monthMap)
        }
    }

    private fun initComponent() {
        adapter = CategoryAdapter(categories)
        rvRightCategory = findViewById(R.id.rvRightCategory)

        adapter2 = CategoryLeftAdapter(categories2)
        rvLeftCategory = findViewById(R.id.rvLeftCategory)

        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerWeek = findViewById(R.id.spinnerWeek)

        pieChart = findViewById(R.id.pcData)

        mbLogout = findViewById(R.id.mbLogout)
        FABPrompt = findViewById(R.id.FABPrompt)
    }

    private fun setListeners() {
        mbLogout.setOnClickListener {
            logout()
        }
        FABPrompt.setOnClickListener {
            showChangePromptDialog()
        }
    }

    private fun initIU() {
        initRecyclerView()
        initLeftRecyclerView()
        initSpinners()
    }

    private fun initRecyclerView() {
        adapter = CategoryAdapter(categories)
        rvRightCategory.layoutManager = LinearLayoutManager(this)
        rvRightCategory.adapter = adapter
    }

    private fun initLeftRecyclerView() {
        adapter2 = CategoryLeftAdapter(categories2)
        rvLeftCategory.layoutManager = LinearLayoutManager(this)
        rvLeftCategory.adapter = adapter2
    }

    private fun initSpinners() {
        val years = resources.getStringArray(R.array.years)
        val months = resources.getStringArray(R.array.months)
        val weeks = resources.getStringArray(R.array.weeks)

        val yearAdapter = ArrayAdapter(
            this,
            R.layout.item_custom_spinner, // Layout para el ítem seleccionado
            years
        )
        yearAdapter.setDropDownViewResource(R.layout.item_custom_spinner_dropdown)
        spinnerYear.adapter = yearAdapter
        // Seleccionar "Año" por defecto (primera posición)
        spinnerYear.setSelection(0)

        val monthAdapter = ArrayAdapter(
            this,
            R.layout.item_custom_spinner, // Layout para el ítem seleccionado
            months
        )
        monthAdapter.setDropDownViewResource(R.layout.item_custom_spinner_dropdown)
        spinnerMonth.adapter = monthAdapter
        // Seleccionar "Mes" por defecto (primera posición)
        spinnerMonth.setSelection(0)

        val weekAdapter = ArrayAdapter(
            this,
            R.layout.item_custom_spinner, // Layout para el ítem seleccionado
            weeks
        )
        weekAdapter.setDropDownViewResource(R.layout.item_custom_spinner_dropdown)
        spinnerWeek.adapter = weekAdapter
        // Seleccionar "Semana" por defecto (primera posición)
        spinnerWeek.setSelection(0)
    }

    private fun setupSpinnerListeners() {
        val monthMap = getMonthNameToNumberMap()

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                spinnerWeek.setSelection(0)
                fetchAndDisplayStats(monthMap)
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
                fetchAndDisplayStats(monthMap)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* No-op */ }
        }

        // Ya se llama en onResume la primera vez que se inicia o reanuda
        // No necesitamos llamarla aquí de nuevo para la carga inicial si onResume lo hace
        // Pero si quieres asegurar que siempre se haga una vez después de los listeners, puedes descomentar.
        // fetchAndDisplayStats(monthMap)
    }

    private fun fetchAndDisplayStats(monthMap: Map<String, Int>) {
        // Asegurarse de que el token esté presente y sea válido ANTES de hacer la llamada a la API
        if (authToken.isBlank() || !isTokenValid(authToken)) {
            Toast.makeText(this@StatsActivity, "Sesión inválida, redirigiendo al login.", Toast.LENGTH_LONG).show()
            redirectToLogin()
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

                val api = RetrofitClient.create(authToken)
                val stats = api.getEstadisticasPorCategoria(year, month, week)
                val total = api.getTotalConsultas(year, month, week)
                val cantidadUsuarios = api.getCantidadUsuarios(year, month, week)

                actualizarPieChart(stats, total, cantidadUsuarios)

            } catch (e: HttpException) {
                // Captura errores HTTP (por ejemplo, 401 Unauthorized)
                if (e.code() == 401) {
                    Toast.makeText(this@StatsActivity, "Sesión expirada. Por favor, inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
                    logout() // Redirigir al login y limpiar token
                } else {
                    Toast.makeText(this@StatsActivity, "Error de servidor: ${e.message()}", Toast.LENGTH_LONG).show()
                }
                pieChart.clear()
                pieChart.setNoDataText("Error al cargar datos. Intenta de nuevo.")
                pieChart.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@StatsActivity, "Error al obtener estadísticas: ${e.message}", Toast.LENGTH_LONG).show()
                pieChart.clear()
                pieChart.setNoDataText("Error al cargar datos. Intenta de nuevo.")
                pieChart.invalidate()
            }
        }
    }

    private fun getMonthNameToNumberMap(): Map<String, Int> {
        val months = resources.getStringArray(R.array.months)
        val map = mutableMapOf<String, Int>()
        for (i in 1 until months.size) {
            map[months[i]] = i
        }
        return map
    }

    private fun actualizarPieChart(datos: Map<String, Double>, total: Long, cantidadUsuarios: Long) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        datos.forEach { (categoryName, value) ->
            entries.add(PieEntry(value.toFloat(), categoryName.trim()))

            val colorResId = categoryColorsMap[categoryName.trim()] ?: R.color.category_other_color
            colors.add(ContextCompat.getColor(this, colorResId))
        }

        val dataSet = PieDataSet(entries, "Categorías")
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.centerText = "Consultas: $total\nUsuarios: $cantidadUsuarios"
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setDrawEntryLabels(false)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.legend.textColor = Color.BLACK
        pieChart.animateY(1000)
        pieChart.invalidate()

        if (entries.isEmpty()) {
            pieChart.setNoDataText("No hay datos disponibles para la selección actual.")
            pieChart.setNoDataTextColor(Color.GRAY)
            pieChart.invalidate()
        }
    }

    // Mueve la lógica de validación del token aquí, pero hazla privada si solo se usa en esta clase.
    // O mejor aún, hazla una función de utilidad global o en un objeto si se usa en varias actividades.
    private fun isTokenValid(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.w("StatsActivity", "JWT no tiene 3 partes: $token")
                return false
            }

            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val payload = JSONObject(payloadJson)

            val exp = payload.optLong("exp", 0)
            if (exp == 0L) {
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

    // Modifica para obtener el token del SharedPreferences que se usa consistentemente
    private fun getAccessTokenFromPrefs(): String? {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(ACCESS_TOKEN_KEY, null)
    }

    private fun clearTokensFromPrefs() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove(ACCESS_TOKEN_KEY)
            // Si guardas refresh_token, también deberías removerlo aquí
            remove("refresh_token") // Asumiendo que esta es la clave en LoginActivity
            apply()
        }
    }

    private fun logout() {
        clearTokensFromPrefs() // Asegúrate de limpiar los tokens
        redirectToLogin()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza la actividad actual para que no se pueda volver atrás
    }

    private fun showChangePromptDialog() {
        val dialog = Dialog(this) //Instanciamos el dialog
        dialog.setContentView(R.layout.dialog_add_prompt) //Le pasamos el layout del dialog y lo setea como vista del dialog.

        val mbNo: MaterialButton = dialog.findViewById(R.id.mbNo) //Instanciamos el boton del dialog para agregar tareas.
        val mbYes: MaterialButton = dialog.findViewById(R.id.mbYes) //Instanciamos el boton del dialog para eliminar tareas.
        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        tvMessage.text = getString(R.string.change_prompt)

        mbNo.setOnClickListener() {
            dialog.dismiss()
        }
        mbYes.setOnClickListener() {
            val intent = Intent(this, ChangePromptActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
        dialog.show()
    }
}