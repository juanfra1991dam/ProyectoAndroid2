package com.example.proyectoandroid2.fragments

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.databinding.FragmentRegistroBinding
import com.example.proyectoandroid2.viewmodels.RegistroViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.util.*

class RegistroFragment : Fragment() {

    private lateinit var binding: FragmentRegistroBinding
    private val registroViewModel: RegistroViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

        // Habilitar la detección del botón de retroceso en el fragmento
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Glide.with(requireContext())
                .load(it)
                .into(binding.imgLogo)
            // Convertir la imagen seleccionada a Bitmap
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            // Convertir el Bitmap a Base64
            val imageBase64 = convertBitmapToBase64(bitmap)
            registroViewModel.imageBase64 = imageBase64
        }
    }

    private fun setupObservers() {
        registroViewModel.nombreError.observe(viewLifecycleOwner) { error ->
            binding.inputLayoutNombre.error = error
        }

        registroViewModel.emailError.observe(viewLifecycleOwner) { error ->
            binding.inputLayoutEmail.error = error
        }

        registroViewModel.passwordError.observe(viewLifecycleOwner) { error ->
            binding.inputLayoutPassword.error = error
        }

        registroViewModel.fechaNacimientoError.observe(viewLifecycleOwner) { error ->
            binding.inputLayoutFechaNacimiento.error = error
        }

        registroViewModel.registroExitoso.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), getString(R.string.registro_correcto), Toast.LENGTH_SHORT).show()
            }
        }

        registroViewModel.loginExitoso.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                findNavController().navigate(R.id.action_RegistroFragment_to_ScaffoldFragment)
            }
        }

        registroViewModel.mensajeError.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnRegistro.setOnClickListener {
            val nombre = binding.edtNombre.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            val fechaNacimiento = binding.edtFechaNacimiento.text.toString()

            // Obtener la imagen en Base64
            val imageBase64 = registroViewModel.imageBase64

            // Validar formulario y registrar usuario
            registroViewModel.validarFormulario(nombre, email, password, fechaNacimiento, imageBase64)
        }

        binding.edtFechaNacimiento.setOnClickListener {
            showDatePickerDialog()
        }

        binding.imgLogo.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                binding.edtFechaNacimiento.setText("$dayOfMonth/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Metodo para convertir el Bitmap a Base64
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
