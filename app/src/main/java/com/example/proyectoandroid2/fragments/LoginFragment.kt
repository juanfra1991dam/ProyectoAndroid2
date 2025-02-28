package com.example.proyectoandroid2.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*

class LoginFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val loginViewModel: LoginViewModel by viewModels()

    private var selectedLanguage: String = "es"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar el idioma seleccionado de SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        selectedLanguage = sharedPreferences.getString("selectedLanguage", "es") ?: "es"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        val changeLanguageImageButtonEs = view.findViewById<ImageButton>(R.id.buttonChangeLanguageEs)
        val changeLanguageImageButtonEn = view.findViewById<ImageButton>(R.id.buttonChangeLanguageEn)
        val changeLanguageImageButtonFr = view.findViewById<ImageButton>(R.id.buttonChangeLanguageFr)
        val submitButton = view.findViewById<Button>(R.id.buttonSubmit)
        val forgotPasswordText = view.findViewById<TextView>(R.id.textForgotPassword)
        val googleButton = view.findViewById<Button>(R.id.buttonGoogle)
        val facebookButton = view.findViewById<Button>(R.id.buttonFacebook)
        val registerText = view.findViewById<TextView>(R.id.textRegister)

        val emailEditText = view.findViewById<TextInputEditText>(R.id.editTextEmail)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.editTextPassword)

        val emailInputLayout = view.findViewById<TextInputLayout>(R.id.inputLayoutEmail)
        val passwordInputLayout = view.findViewById<TextInputLayout>(R.id.inputLayoutPassword)

        changeLanguageImageButtonEs.setOnClickListener { changeLanguage("es") }
        changeLanguageImageButtonEn.setOnClickListener { changeLanguage("en") }
        changeLanguageImageButtonFr.setOnClickListener { changeLanguage("fr") }

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

            var isValid = true
            if (emailText.isEmpty()) {
                emailInputLayout.error = getString(R.string.usuario_obligatorio)
                isValid = false
            } else {
                emailInputLayout.error = null
            }

            if (passwordText.isEmpty()) {
                passwordInputLayout.error = getString(R.string.password_obligatorio)
                isValid = false
            } else {
                passwordInputLayout.error = null
            }

            if (isValid) {
                firebaseAuth = FirebaseAuth.getInstance()
                signIn(emailText, passwordText)
            }
        }

        forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_ForgotPasswordFragment)
        }

        googleButton.setOnClickListener {
            signInWithGoogle()
        }

        facebookButton.setOnClickListener {
            showSnackbar(view, getString(R.string.snackbar_boton_pulsado) + " " + facebookButton.text)
        }

        registerText.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegistroFragment)
        }
    }

    // Cambiar idioma y reiniciar la actividad
    private fun changeLanguage(language: String) {
        selectedLanguage = language // Guardar idioma seleccionado
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(requireContext().resources.configuration)
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        // Guardar idioma en SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selectedLanguage", language)
        editor.apply()

        // Reiniciar la actividad para aplicar el cambio de idioma
        requireActivity().recreate()
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        Toast.makeText(requireContext(), getString(R.string.bienvenida, user.email), Toast.LENGTH_SHORT).show()
                        val bundle = Bundle().apply {
                            putString("selectedLanguage", selectedLanguage)
                        }
                        findNavController().navigate(R.id.action_LoginFragment_to_ScaffoldFragment, bundle)
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.credenciales_invalidas), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            user?.let {
                                Toast.makeText(requireContext(), getString(R.string.bienvenida, user.email), Toast.LENGTH_SHORT).show()
                                val bundle = Bundle().apply {
                                    putString("selectedLanguage", selectedLanguage)
                                }
                                findNavController().navigate(R.id.action_LoginFragment_to_ScaffoldFragment, bundle)
                            }
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.inicio_sesion_google_error), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                googleSignInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }
}
