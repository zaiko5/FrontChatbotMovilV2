package com.example.frontchatbot.FirstActivity

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.frontchatbot.FirstActivity.api.RetrofitClient
import com.example.frontchatbot.FirstActivity.model.LoginRequest
import com.example.frontchatbot.FirstActivity.model.TokenResponse
import com.example.frontchatbot.R
import com.example.frontchatbot.SecondActivity.StatsActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    //Inicializando las instancias de los componentes de la vista.
    private lateinit var tieUser: TextInputEditText //EditText para el usuario.
    private lateinit var tiePassword: TextInputEditText //EditText para el password.
    private lateinit var mbLogin: MaterialButton //Material Button para iniciar sesion.

    // Para la persistencia del token (Shared Preferences)
    private val PREFS_NAME = "MyAuthPrefs"
    private val ACCESS_TOKEN_KEY = "access_token"
    private val REFRESH_TOKEN_KEY = "refresh_token"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initComponents()
        setListeners()

        //Verificar si ya hay un token y redirigir
        checkExistingToken()
    }

    private fun initComponents() {//Iniciamos los componentes 
        tieUser = findViewById(R.id.tieUser)
        tiePassword = findViewById(R.id.tiePassword)
        mbLogin = findViewById(R.id.mbLogin)
    }

    private fun setListeners() { //Iniciamos los listeners (solo el del MB)
        mbLogin.setOnClickListener {
            login() //Funcion para loguearse.
        }
    }

    private fun login() {
        //Obtenemos las credenciales.
        val email = tieUser.text.toString()
        val password = tiePassword.text.toString()

        //Si alguno de los 2 campos es vacio.
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu correo y contraseña.", Toast.LENGTH_SHORT).show() //Mostrando un mensaje al tener los campos vacios.
            return //Retornamos para no seguir con la funcion.
        }

        // Ejecutar la llamada a la API en una coroutine
        lifecycleScope.launch(Dispatchers.IO) { // Ejecutar en el hilo de IO (Input/Output) para operaciones de red
            try {
                val request = LoginRequest(email, password) //Instanciamos el objeto request.
                val response = RetrofitClient.authService.login(request) //Con el objeto Retrofit, 

                withContext(Dispatchers.Main) { // Volver al hilo principal para actualizar la UI
                    if (response.isSuccessful) {
                        val tokenResponse: TokenResponse? = response.body()
                        tokenResponse?.let { receivedToken -> // Usamos 'receivedToken' para mayor claridad y para evitar el error anterior
                            // Login exitoso, guardar tokens
                            saveTokens(receivedToken.accessToken, receivedToken.refreshToken)
                            Toast.makeText(this@LoginActivity, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                            // Redirigir a la siguiente actividad (ej. MainActivity o DashboardActivity)
                            val intent = Intent(this@LoginActivity, StatsActivity::class.java)
                            intent.putExtra("accessToken", receivedToken.accessToken)
                            startActivity(intent)
                            finish() // Cierra LoginActivity para que el usuario no pueda volver con el botón "atrás"
                        } ?: run {
                            Toast.makeText(this@LoginActivity, "Error: Respuesta de token vacía.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Manejo de errores de la API (ej. 401 Unauthorized, 400 Bad Request)
                        val errorBody = response.errorBody()?.string()
                        Log.e("LoginActivity", "Error de login: ${response.code()} - $errorBody")

                        // Puedes mejorar este mensaje parseando el 'errorBody' si tu API devuelve un JSON estructurado de errores.
                        val errorMessage = when (response.code()) {
                            400 -> "Credenciales incorrectas. Verifica tu email y contraseña."
                            401 -> "Acceso no autorizado. Credenciales inválidas."
                            else -> "Error en el servidor: ${response.code()} - ${response.message()}"
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // Manejo de errores de red o excepciones generales (ej. no hay internet, URL incorrecta, JSON malformado)
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "Error de red o desconocido: ${e.localizedMessage}", e)
                    Toast.makeText(this@LoginActivity, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            apply() // apply() es asíncrono, commit() es síncrono
        }
    }

    private fun checkExistingToken() {
        val accessToken = getAccessToken()

        if (accessToken != null && isTokenValid(accessToken)) {
            // Ya hay un token, redirigir al usuario directamente
            Toast.makeText(this, "Sesión activa, redirigiendo...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@LoginActivity, StatsActivity::class.java)
            startActivity(intent)
            finish()
        } else {
        // Token inválido, limpiar y quedate en login
        clearTokens()
        Toast.makeText(this, "Token expirado o inválido, por favor inicia sesión.", Toast.LENGTH_SHORT).show()
    }
    }

    // Puedes agregar esta función si necesitas obtener los tokens en otro lugar
    fun getAccessToken(): String? {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPrefs.getString(ACCESS_TOKEN_KEY, null)
    }

    fun clearTokens() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            apply()
        }
    }

    fun isTokenValid(token: String): Boolean {
        try {
            // El token JWT tiene tres partes separadas por puntos: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) return false

            // El payload está en la segunda parte, que está base64 encoded
            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val payload = JSONObject(payloadJson)

            // Obtenemos la fecha de expiración en segundos desde epoch
            val exp = payload.optLong("exp", 0)
            if (exp == 0L) return false

            val currentTime = System.currentTimeMillis() / 1000  // en segundos

            // Retornamos true si el token NO está expirado
            return currentTime < exp
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}