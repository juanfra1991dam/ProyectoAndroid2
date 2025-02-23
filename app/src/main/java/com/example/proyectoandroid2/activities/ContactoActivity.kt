package com.example.proyectoandroid2.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyectoandroid2.R
import java.util.Locale

class ContactoActivity : AppCompatActivity() {

    // Código de solicitud para el permiso de llamada
    private val REQUEST_PHONE_CALL = 1

    // Código de solicitud para el permiso de ubicación
    private val REQUEST_LOCATION_PERMISSION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = intent.getStringExtra("languageCode")
        setLocale(languageCode.toString())

        setContentView(R.layout.activity_contacto)

        // Inicializar los EditTexts y la imagen
        val telefonoEditText: EditText = findViewById(R.id.etTelefono)
        val correoEditText: EditText = findViewById(R.id.etCorreo)
        val ubicacionEditText: EditText = findViewById(R.id.etUbicacion)
        val whatsappEditText: EditText = findViewById(R.id.etWhatsapp)
        val phoneImageView: ImageView = findViewById(R.id.phoneImage)
        val emailImageView: ImageView = findViewById(R.id.emailImage)
        val locationImageView: ImageView = findViewById(R.id.locationImage)
        val whatsappImageView: ImageView = findViewById(R.id.whatsappImage)


        // Configuración de la acción de llamada al hacer clic en la imagen del teléfono
        phoneImageView.setOnClickListener {
            val telefono = telefonoEditText.text.toString()
            if (telefono.isNotEmpty()) {
                // Validamos que el número tenga al menos 9 dígitos
                if (telefono.length >= 9 && telefono.all { it.isDigit() }) {
                    // Verificamos si ya tenemos el permiso de llamada
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        realizarLlamada(telefono)
                    } else {
                        // Solicitamos el permiso si no se tiene
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            REQUEST_PHONE_CALL
                        )
                    }
                } else {
                    telefonoEditText.error = getString(R.string.telefono_valido)
                }
            } else {
                telefonoEditText.error = getString(R.string.telefono_obligatorio)
            }
        }

        // Configuración de la acción de enviar correo al hacer clic en el correo
        emailImageView.setOnClickListener {
            val correo = correoEditText.text.toString()
            if (correo.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correo"))
                startActivity(intent)
            } else {
                correoEditText.error = getString(R.string.correo_obligatorio)
            }
        }

        // Configuración de la acción de abrir mapa al hacer clic en la imagen del mapa
        locationImageView.setOnClickListener {
            val ubicacion = ubicacionEditText.text.toString()
            if (ubicacion.isNotEmpty()) {
                // Verificamos si al menos uno de los permisos está concedido
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    abrirMapaConSeleccion(ubicacion)
                } else {
                    // Solicitamos los permisos si ninguno está concedido
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        REQUEST_LOCATION_PERMISSION
                    )
                }
            } else {
                ubicacionEditText.error = getString(R.string.ubicacion_obligatorio)
            }
        }

        // Configuración de la acción para abrir WhatsApp al hacer clic en la imagen de WhatsApp
        whatsappImageView.setOnClickListener {
            val whatsapp = whatsappEditText.text.toString().trim()
            if (whatsapp.isNotEmpty()) {
                // Validamos que el número de WhatsApp comience con "+"
                if (whatsapp.startsWith("+")) {
                    val phoneNumber = whatsapp.substring(1)
                    // Validamos que el número tenga al menos 9 dígitos
                    if (phoneNumber.length >= 9 && phoneNumber.all { it.isDigit() }) {
                        abrirWhatsApp(whatsapp)
                    } else {
                        whatsappEditText.error = getString(R.string.telefono_valido)
                    }
                } else {
                    whatsappEditText.error = getString(R.string.prefijo_obligatorio)
                }
            } else {
                whatsappEditText.error = getString(R.string.telefono_obligatorio)
            }
        }
    }

    // Función para establecer el idioma
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Función para realizar la llamada
    private fun realizarLlamada(telefono: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$telefono"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al realizar la llamada: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para abrir el una aplicacion de mapas con la ubicación proporcionada
    private fun abrirMapaConSeleccion(ubicacion: String) {
        try {
            val encodedLocation = Uri.encode(ubicacion)
            val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            // Intent implícito que permite elegir la aplicación de mapas
            val chooserIntent = Intent.createChooser(mapIntent, getString(R.string.abrir_con))
            startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir el mapa: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para abrir WhatsApp
    private fun abrirWhatsApp(whatsapp: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsapp"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para gestionar la respuesta de los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PHONE_CALL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val telefono = findViewById<EditText>(R.id.etTelefono).text.toString()
                    realizarLlamada(telefono)
                } else {
                    Toast.makeText(this, getString(R.string.permiso_llamada_denegado), Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_LOCATION_PERMISSION -> {
                val permisosConcedidos = permissions.zip(grantResults.toList())
                    .filter { it.second == PackageManager.PERMISSION_GRANTED }
                    .map { it.first }

                if (permisosConcedidos.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permisosConcedidos.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) {
                    val ubicacion = findViewById<EditText>(R.id.etUbicacion).text.toString()
                    if (ubicacion.isNotEmpty()) {
                        abrirMapaConSeleccion(ubicacion)
                    } else {
                        Toast.makeText(this, getString(R.string.ubicacion_obligatorio), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.permiso_ubicacion_denegado), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
