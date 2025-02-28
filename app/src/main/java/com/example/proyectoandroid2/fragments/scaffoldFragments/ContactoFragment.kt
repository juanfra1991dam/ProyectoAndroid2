package com.example.proyectoandroid2.fragments.scaffoldFragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.proyectoandroid2.R

class ContactoFragment : Fragment() {

    private val REQUEST_PHONE_CALL = 1
    private val REQUEST_LOCATION_PERMISSION = 2

    private var selectedLanguage: String = "es"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val telefonoEditText: EditText = view.findViewById(R.id.etTelefono)
        val correoEditText: EditText = view.findViewById(R.id.etCorreo)
        val ubicacionEditText: EditText = view.findViewById(R.id.etUbicacion)
        val whatsappEditText: EditText = view.findViewById(R.id.etWhatsapp)
        val phoneImageView: ImageView = view.findViewById(R.id.phoneImage)
        val emailImageView: ImageView = view.findViewById(R.id.emailImage)
        val locationImageView: ImageView = view.findViewById(R.id.locationImage)
        val whatsappImageView: ImageView = view.findViewById(R.id.whatsappImage)

        phoneImageView.setOnClickListener {
            val telefono = telefonoEditText.text.toString()
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                realizarLlamada(telefono)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
            }
        }

        emailImageView.setOnClickListener {
            val correo = correoEditText.text.toString()
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correo"))
            startActivity(intent)
        }

        locationImageView.setOnClickListener {
            val ubicacion = ubicacionEditText.text.toString()
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                abrirMapaConSeleccion(ubicacion)
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }

        whatsappImageView.setOnClickListener {
            val whatsapp = whatsappEditText.text.toString().trim()
            abrirWhatsApp(whatsapp)
        }
    }

    private fun realizarLlamada(telefono: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$telefono"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al realizar la llamada: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirMapaConSeleccion(ubicacion: String) {
        try {
            val encodedLocation = Uri.encode(ubicacion)
            val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            val chooserIntent = Intent.createChooser(mapIntent, getString(R.string.abrir_con))
            startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al abrir el mapa: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirWhatsApp(whatsapp: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsapp"))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PHONE_CALL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val telefono = view?.findViewById<EditText>(R.id.etTelefono)?.text.toString()
                    realizarLlamada(telefono)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.permiso_llamada_denegado), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            REQUEST_LOCATION_PERMISSION -> {
                val permisosConcedidos = permissions.zip(grantResults.toList())
                    .filter { it.second == PackageManager.PERMISSION_GRANTED }
                    .map { it.first }

                if (permisosConcedidos.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permisosConcedidos.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                ) {
                    val ubicacion = view?.findViewById<EditText>(R.id.etUbicacion)?.text.toString()
                    if (ubicacion.isNotEmpty()) {
                        abrirMapaConSeleccion(ubicacion)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.ubicacion_obligatorio), Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.permiso_ubicacion_denegado), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
