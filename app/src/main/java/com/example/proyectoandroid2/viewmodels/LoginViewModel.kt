package com.example.proyectoandroid2.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    // Campos de texto (Email y Password)
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    // Estado del botón (habilitado o deshabilitado)
    private val _isButtonEnabled = MutableLiveData<Boolean>().apply { value = false }
    val isButtonEnabled: LiveData<Boolean> = _isButtonEnabled

    // Función para actualizar el campo de email
    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
        checkButtonState()
    }

    // Función para actualizar el campo de password
    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
        checkButtonState()
    }

    // Comprobar si ambos campos están llenos para habilitar el botón
    private fun checkButtonState() {
        _isButtonEnabled.value = !(_email.value.isNullOrEmpty() || _password.value.isNullOrEmpty())
    }
}
