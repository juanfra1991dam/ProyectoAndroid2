package com.example.proyectoandroid2.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RegistroViewModel : ViewModel() {

    val nombreError = MutableLiveData<String?>()
    val emailError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val fechaNacimientoError = MutableLiveData<String?>()
    val registroCorrecto = MutableLiveData<Boolean>()
    val loginCorrecto = MutableLiveData<Boolean>()
    val mensajeError = MutableLiveData<String?>()

    var imageBase64: String? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun validarFormulario(nombre: String, email: String, password: String, fechaNacimiento: String, imageBase64: String?) {
        var isValid = true

        if (nombre.isEmpty()) {
            nombreError.value = "El nombre es obligatorio"
            isValid = false
        } else {
            nombreError.value = null
        }

        if (!email.matches(Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))) {
            emailError.value = "Correo electrónico inválido"
            isValid = false
        } else {
            emailError.value = null
        }

        if (password.length < 6) {
            passwordError.value = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            passwordError.value = null
        }

        if (fechaNacimiento.isEmpty()) {
            fechaNacimientoError.value = "La fecha de nacimiento es obligatoria"
            isValid = false
        } else {
            fechaNacimientoError.value = null
        }

        if (isValid) {
            verificarCorreoYRegistrar(email, password, nombre, fechaNacimiento, imageBase64)
        }
    }

    private fun verificarCorreoYRegistrar(email: String, password: String, nombre: String, fechaNacimiento: String, imageBase64: String?) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods ?: emptyList()
                if (signInMethods.isNotEmpty()) {
                    emailError.value = "El correo ya está en uso"
                } else {
                    registrarUsuario(email, password, nombre, fechaNacimiento, imageBase64)
                }
            } else {
                mensajeError.value = "Error al verificar el correo: ${task.exception?.message}"
            }
        }
    }

    private fun registrarUsuario(email: String, password: String, nombre: String, fechaNacimiento: String, imageBase64: String?) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val usuario = auth.currentUser
                    usuario?.let {
                        val usuarioData = hashMapOf(
                            "imagenPerfil" to imageBase64,
                            "nombre" to nombre,
                            "email" to email,
                            "fechaNacimiento" to fechaNacimiento,
                            "uid" to it.uid
                        )

                        firestore.collection("usuarios")
                            .document(it.uid)
                            .set(usuarioData, SetOptions.merge())
                            .addOnSuccessListener {
                                registroCorrecto.value = true
                                signIn(email, password)
                            }
                            .addOnFailureListener { e ->
                                mensajeError.value = "Error al guardar usuario en Firestore"
                            }
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        emailError.value = "El correo ya está en uso"
                    } else {
                        mensajeError.value = "Error al registrar usuario: ${task.exception?.message}"
                    }
                }
            }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loginCorrecto.value = true
                } else {
                    mensajeError.value = "Error al iniciar sesión: ${task.exception?.message}"
                }
            }
    }
}
