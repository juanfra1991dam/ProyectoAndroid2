package com.example.proyectoandroid2.fragments.scaffoldFragments

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.viewmodels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class LoginFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val submitButton = view.findViewById<Button>(R.id.buttonSubmit)
        val registerButton = view.findViewById<Button>(R.id.buttonRegister)
        val forgotPasswordButton = view.findViewById<Button>(R.id.buttonForgotPassword)
        val facebookButton = view.findViewById<Button>(R.id.buttonFacebook)
        val googleButton = view.findViewById<Button>(R.id.buttonGoogle)
        val changeLanguageImageButtonEs = view.findViewById<ImageButton>(R.id.buttonChangeLanguageEs)
        val changeLanguageImageButtonEn = view.findViewById<ImageButton>(R.id.buttonChangeLanguageEn)
        val changeLanguageImageButtonFr = view.findViewById<ImageButton>(R.id.buttonChangeLanguageFr)

        val emailEditText = view.findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.editTextPassword)

        loginViewModel.isButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            submitButton.isEnabled = isEnabled
        }

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                loginViewModel.onEmailChanged(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                loginViewModel.onPasswordChanged(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        submitButton.setOnClickListener {
            val emailText = emailEditText.text.toString().trim()
            val passwordText = passwordEditText.text.toString().trim()
            firebaseAuth = FirebaseAuth.getInstance()
            signIn(emailText, passwordText)
        }

        registerButton.setOnClickListener {
            showSnackbar(view, getString(R.string.snackbar_boton_pulsado) + " " + registerButton.text)
        }

        forgotPasswordButton.setOnClickListener {
            showSnackbar(view, getString(R.string.snackbar_boton_pulsado) + " " + forgotPasswordButton.text)
        }

        handleButtonClick(view, facebookButton)
        handleButtonClick(view, googleButton)

        changeLanguageImageButtonEs.setOnClickListener { changeLanguage("es") }
        changeLanguageImageButtonEn.setOnClickListener { changeLanguage("en") }
        changeLanguageImageButtonFr.setOnClickListener { changeLanguage("fr") }
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        Toast.makeText(requireContext(), "Bienvenido, ${user.email}", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_LoginFragment_to_ScaffoldFragment)
                    }
                } else {
                    view?.let { showSnackbar(it, getString(R.string.credenciales_invalidas)) }
                }
            }
    }

    private fun handleButtonClick(view: View, button: Button) {
        button.setOnClickListener {
            showSnackbar(view, getString(R.string.snackbar_boton_pulsado) + " " + button.text)
        }
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun changeLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(requireContext().resources.configuration)
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        requireActivity().recreate()
    }
}
