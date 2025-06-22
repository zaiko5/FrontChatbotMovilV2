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

        initComponents() //Se inicializan los componentes
        setListeners() //Se inicializan los listeners

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
                val request = LoginRequest(email, password) //Instanciamos el objeto request que es el que contiene las credenciales..
                val response = RetrofitClient.authService.login(request) //Con el objeto Retrofit,.

                withContext(Dispatchers.Main) { // Volver al hilo principal para actualizar la UI
                    if (response.isSuccessful) {
                        val tokenResponse: TokenResponse? = response.body() //Obtenemos el token de la respuesta.
                        tokenResponse?.let { receivedToken -> // Usamos 'receivedToken' para mayor claridad.
                            // Login exitoso, guardar tokens
                            saveTokens(receivedToken.accessToken, receivedToken.refreshToken)
                            Toast.makeText(this@LoginActivity, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                            // Redirigir a la siguiente actividad
                            val intent = Intent(this@LoginActivity, StatsActivity::class.java)
                            intent.putExtra("accessToken", receivedToken.accessToken) // Pasamos el token a la siguiente actividad
                            startActivity(intent)
                            finish() // Cierra LoginActivity para que el usuario no pueda volver con el botón "atrás"
                        } ?: run { //Si el body de la respuesta es null.
                            Toast.makeText(this@LoginActivity, "Error: Respuesta de token vacía.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Manejo de errores de la API (ej. 401 Unauthorized, 400 Bad Request)
                        val errorBody = response.errorBody()?.string()
                        Log.e("LoginActivity", "Error de login: ${response.code()} - $errorBody")

                        val errorMessage = when (response.code()) { //Mapeando los errores.
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

    private fun saveTokens(accessToken: String, refreshToken: String) { //Funcion que guarda los tokens.
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE) //Creando un archivo de preferencias con nombre y modo privado.
        with(sharedPrefs.edit()) { //Abriendo el archivo de preferencias para editarlo.
            putString(ACCESS_TOKEN_KEY, accessToken) //Guardando el token de acceso.
            putString(REFRESH_TOKEN_KEY, refreshToken) //Guardando el token de refresco.
            apply() // apply() es asíncrono, commit() es síncrono, guardamos los cambios al archivo de preferencias.
        }
    }

    fun getAccessToken(): String? { //Funcion que obtiene el token de acceso.
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPrefs.getString(ACCESS_TOKEN_KEY, null) //Obtenemos el token de acceso del archivo de preferencias.
    }

    fun isTokenValid(token: String): Boolean { //Funcion que verifica si un token es valido o no.
        try {
            // El token JWT tiene tres partes separadas por puntos: header.payload.signature
            val parts = token.split(".") //Separamos el token en cada que haya un "."
            if (parts.size != 3) return false //Si no hay 3 partes, no es valido.

            // El payload está en la segunda parte, que está base64 encoded
            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val payload = JSONObject(payloadJson)

            // Obtenemos la fecha de expiración en segundos desde epoch
            val exp = payload.optLong("exp", 0)
            if (exp == 0L) return false //Si no hay fecha de expiracion, no es valido.

            val currentTime = System.currentTimeMillis() / 1000  // en segundos

            // Retornamos true si el token NO está expirado
            return currentTime < exp
        } catch (e: Exception) { //Capturamos la excepcion.
            e.printStackTrace()
            return false
        }
    }

    private fun checkExistingToken() { //Checar si existe un token valido.
        val accessToken = getAccessToken() //Obtener el token de acceso desde otra funcion.

        if (accessToken != null && isTokenValid(accessToken)) { //Si hay un token y es valido.
            Toast.makeText(this, "Sesión activa, redirigiendo...", Toast.LENGTH_SHORT).show() //Ya hay un token, redirigir al usuario directamente
            val intent = Intent(this@LoginActivity, StatsActivity::class.java) //Hacemos un intent a la segunda página.
            startActivity(intent)
            finish()
        } else {// Token inválido, limpiar y quedarse en login
            clearTokens()
            Toast.makeText(this, "Token expirado o inválido, por favor inicia sesión.", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearTokens() { //Funcion que limpia los tokens.
        val sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE) //Instanciamos el archivo de preferencias.
        with(sharedPrefs.edit()) { //Abrimos el archivo de preferencias para editarlo.
            remove(ACCESS_TOKEN_KEY) //Eliminamos el token de acceso.
            remove(REFRESH_TOKEN_KEY) //Eliminamos el token de refresco.
            apply() // apply() es asíncrono, commit() es síncrono, guardamos los cambios al archivo de preferencias.
        }
    }
}