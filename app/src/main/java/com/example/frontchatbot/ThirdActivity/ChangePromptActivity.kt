package com.example.frontchatbot.ThirdActivity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.frontchatbot.FirstActivity.LoginActivity
import com.example.frontchatbot.R
import com.example.frontchatbot.ThirdActivity.model.PromptResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChangePromptActivity : AppCompatActivity() {

    // Declarar el token como una propiedad de la clase
    private var authToken: String = ""

    // Constante para el nombre de Shared Preferences y la clave del token
    private val PREFS_NAME = "MyAuthPrefs"
    private val ACCESS_TOKEN_KEY = "access_token"

    private lateinit var FABReturn: FloatingActionButton
    private lateinit var etPrompt: EditText
    private lateinit var tvDate: TextView
    private lateinit var mbConfirm: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_prompt)

        // Obtener el token de SharedPreferences, NO del Intent aquí
        // El Intent se usó para la primera vez que se lanza StatsActivity
        // Después de eso, el token debe persistir en SharedPreferences
        authToken = getAccessTokenFromPrefs() ?: "" // Asignar el token persistido o vacío

        if (authToken.isBlank() || !isTokenValid(authToken)) {
            Toast.makeText(
                this,
                "Sesión caducada o no iniciada. Por favor, inicia sesión.",
                Toast.LENGTH_LONG
            ).show()
            clearTokensFromPrefs()
            redirectToLogin()
            return // Salir de onCreate para evitar NullPointerException
        }

        initComponents()
        setListeners()
        initIU()
    }

    override fun onResume() {
        super.onResume()
        // Verificar el token cada vez que la actividad se reanuda
        // Esto es crucial para detectar expiración mientras la app estuvo en segundo plano
        authToken = getAccessTokenFromPrefs()
            ?: "" // Recargar el token por si acaso se modificó en otro lado
        if (authToken.isBlank() || !isTokenValid(authToken)) {
            Toast.makeText(
                this,
                "Sesión expirada. Por favor, inicia sesión de nuevo.",
                Toast.LENGTH_LONG
            ).show()
            clearTokensFromPrefs()
            redirectToLogin()
        } else {
            // Si el token es válido, forzar la recarga de datos para actualizar los gráficos
            getPromptAPI() // Ensure UI is updated if app was in background
        }
    }

    private fun initComponents() {
        FABReturn = findViewById(R.id.FABReturn)
        etPrompt = findViewById(R.id.etPrompt)
        tvDate = findViewById(R.id.tvDate)
        mbConfirm = findViewById(R.id.mbConfirm)
    }

    private fun setListeners() {
        FABReturn.setOnClickListener {
            // This is for cancelling, no prompt validation needed here
            showChangePromptDialog(getString(R.string.cancel_prompt))
        }

        mbConfirm.setOnClickListener {
            val newPrompt = etPrompt.text.toString()

            if (newPrompt.isEmpty()) {
                Toast.makeText(this, "El campo de mensaje no puede estar vacío", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener // Stop execution if empty
            }

            // Now, fetch the current prompt to compare
            lifecycleScope.launch {
                try {
                    // Ensure token is valid before making the API call for comparison
                    if (authToken.isBlank() || !isTokenValid(authToken)) {
                        Toast.makeText(
                            this@ChangePromptActivity,
                            "Sesión inválida, redirigiendo al login.",
                            Toast.LENGTH_LONG
                        ).show()
                        redirectToLogin()
                        return@launch
                    }

                    val api =
                        com.example.frontchatbot.ThirdActivity.api.RetrofitClient.create(authToken)
                    val currentPromptResponse: PromptResponse =
                        api.getPrompt() // Fetch current prompt

                    if (currentPromptResponse.mensaje == newPrompt) {
                        Toast.makeText(
                            this@ChangePromptActivity,
                            "El prompt nuevo debe ser diferente que el actual",
                            Toast.LENGTH_LONG
                        ).show()
                        // Do NOT show the dialog if the prompt is the same
                    } else {
                        // If the new prompt is different, show the confirmation dialog
                        showChangePromptDialog(getString(R.string.confirm_prompt))
                    }
                } catch (e: HttpException) {
                    if (e.code() == 401) {
                        Toast.makeText(
                            this@ChangePromptActivity,
                            "Sesión expirada. Por favor, inicia sesión de nuevo.",
                            Toast.LENGTH_LONG
                        ).show()
                        logout()
                    } else {
                        Toast.makeText(
                            this@ChangePromptActivity,
                            "Error de servidor al obtener prompt para validación: ${e.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@ChangePromptActivity,
                        "Error inesperado al validar prompt: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun initIU() {
        getPromptAPI()
    }

    // Removed validatenewPrompt as its logic is now embedded in mbConfirm.setOnClickListener
    // private fun validatenewPrompt(newPrompt: String){ ... }

    private fun showChangePromptDialog(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_prompt)

        val mbNo: MaterialButton = dialog.findViewById(R.id.mbNo)
        val mbYes: MaterialButton = dialog.findViewById(R.id.mbYes)
        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        tvMessage.text = message // Set the dialog message

        mbNo.setOnClickListener {
            dialog.dismiss()
        }

        mbYes.setOnClickListener {
            if (message == getString(R.string.confirm_prompt)) {
                val newPrompt = etPrompt.text.toString() // Get the latest text from the EditText
                // Call postPromptAPI directly, as validation was done in mbConfirm's listener

                postPromptAPI(newPrompt, dialog)
                // postPromptAPI will handle dialog dismissal on success/error
            } else {
                // For "cancel_prompt" message, just finish the activity
                finish()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun postPromptAPI(newPrompt: String, dialog: Dialog) {
        // Keep this token validation as a final safeguard
        if (authToken.isBlank() || !isTokenValid(authToken)) {
            Toast.makeText(
                this@ChangePromptActivity,
                "Sesión inválida, redirigiendo al login.",
                Toast.LENGTH_LONG
            ).show()
            redirectToLogin()
            dialog.dismiss() // Dismiss dialog if redirecting
            return
        }

        lifecycleScope.launch {
            try {
                val api =
                    com.example.frontchatbot.ThirdActivity.api.RetrofitClient.create(authToken)
                // Call updatePrompt directly, no need to getPrompt() here
                val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val fechaActual = formatter.format(Date())
                val prompt = PromptResponse(
                    mensaje = newPrompt,
                    fecha = fechaActual
                )
                api.updatePrompt(prompt)
                Toast.makeText(
                    this@ChangePromptActivity,
                    "Prompt actualizado con éxito",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss() // Dismiss the dialog after successful update
                finish()
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    Toast.makeText(
                        this@ChangePromptActivity,
                        "Sesión expirada. Por favor, inicia sesión de nuevo.",
                        Toast.LENGTH_LONG
                    ).show()
                    logout()
                } else {
                    Toast.makeText(
                        this@ChangePromptActivity,
                        "Error de servidor al actualizar prompt: ${e.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                dialog.dismiss() // Dismiss the dialog on error
            } catch (e: Exception) {
                Toast.makeText(this@ChangePromptActivity, "${e.message}", Toast.LENGTH_LONG).show()
                dialog.dismiss() // Dismiss dialog on unexpected error
            }
        }
    }

    private fun getPromptAPI() {
        // Asegurarse de que el token esté presente y sea válido ANTES de hacer la llamada a la API
        if (authToken.isBlank() || !isTokenValid(authToken)) {
            Toast.makeText(
                this@ChangePromptActivity,
                "Sesión inválida, redirigiendo al login.",
                Toast.LENGTH_LONG
            ).show()
            redirectToLogin()
            return
        }

        lifecycleScope.launch {
            try {
                val api =
                    com.example.frontchatbot.ThirdActivity.api.RetrofitClient.create(authToken)
                val prompt: PromptResponse = api.getPrompt()
                etPrompt.setText(prompt.mensaje)
                if (prompt.fecha == null) {
                    tvDate.setText("")
                } else {
                    tvDate.setText(prompt.fecha)
                }
            } catch (e: HttpException) {
                // Captura errores HTTP (por ejemplo, 401 Unauthorized)
                if (e.code() == 401) {
                    Toast.makeText(
                        this@ChangePromptActivity,
                        "Sesión expirada. Por favor, inicia sesión de nuevo.",
                        Toast.LENGTH_LONG
                    ).show()
                    logout() // Redirigir al login y limpiar token
                } else {
                    Toast.makeText(
                        this@ChangePromptActivity,
                        "Error de servidor: ${e.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun isTokenValid(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.w("ChangePromptActivity", "JWT no tiene 3 partes: $token")
                return false
            }

            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val payload = JSONObject(payloadJson)

            val exp = payload.optLong("exp", 0)
            if (exp == 0L) {
                Log.w("ChangePromptActivity", "JWT no tiene campo 'exp' o es 0")
                return false
            }

            val currentTime = System.currentTimeMillis() / 1000  // en segundos

            Log.d("ChangePromptActivity", "Current time: $currentTime, Token exp: $exp")
            return currentTime < exp
        } catch (e: Exception) {
            Log.e("ChangePromptActivity", "Error al validar token JWT", e)
            return false
        }
    }

    private fun getAccessTokenFromPrefs(): String? {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(ACCESS_TOKEN_KEY, null)
    }

    private fun clearTokensFromPrefs() {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            remove(ACCESS_TOKEN_KEY)
            remove("refresh_token")
            apply()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun logout() {
        clearTokensFromPrefs()
        redirectToLogin()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }
}