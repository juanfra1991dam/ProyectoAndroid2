package com.example.proyectoandroid2.fragments

import PilotosViewModel
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.*
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.activities.MainActivity
import com.example.proyectoandroid2.databinding.FragmentScaffoldBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ScaffoldFragment : Fragment() {

    private lateinit var binding: FragmentScaffoldBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var viewModel: PilotosViewModel
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScaffoldBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el callback para el botón de retroceso
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                verificarYCerrarSesion()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        /* TOOLBAR */
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(PilotosViewModel::class.java)

        // Unir Navigation con DrawerMenu (NavigationView)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragment_container_view_scaffold) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_contacto, R.id.nav_lista, R.id.nav_favoritos), binding.drawerLayout
        )

        setupActionBarWithNavController(requireActivity() as AppCompatActivity, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navigation, navController)

        /* DRAWERLAYOUT */
        toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /* BOTTOM NAVIGATION MENU */
        binding.bottomNavigation.setupWithNavController(navController)

        /* ACTUALIZAR EMAIL Y FOTO DEL USUARIO EN EL HEADER DEL DRAWER */
        navView = binding.navigation
        val headerView = navView.getHeaderView(0)
        val userEmailTextView: TextView = headerView.findViewById(R.id.textViewName)
        val userProfileImageView: ImageView = headerView.findViewById(R.id.imageViewProfile)

        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email ?: "Usuario no autenticado"
        userEmailTextView.text = userEmail

        // Obtener la imagen y nombre del usuario desde Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            firestore.collection("usuarios")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("nombre") ?: userEmail
                    val imageBase64 = document.getString("imagenPerfil")

                    // Actualizar el nombre
                    userEmailTextView.text = name

                    // Si hay una imagen en Base64, convertirla y cargarla
                    imageBase64?.let { base64 ->
                        val bitmap = convertBase64ToBitmap(base64)
                        Glide.with(requireContext())
                            .load(bitmap)
                            .into(userProfileImageView)
                    } ?: run {
                        // Si no hay imagen, cargar la imagen por defecto
                        Glide.with(requireContext())
                            .load(R.drawable.ic_user)
                            .into(userProfileImageView)
                    }
                }
                .addOnFailureListener { e ->
                    // Manejar error en la carga de datos
                }
        }

        // Agregar proveedor de menú
        this.activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
                        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                query?.let { viewModel.filterPilotos(it) }
                                return true
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                newText?.let { viewModel.filterPilotos(it) }
                                return true
                            }
                        })
                        true
                    }

                    R.id.action_sort -> {
                        viewModel.sortPilotsByPoints()
                        true
                    }

                    R.id.action_logout -> {
                        verificarYCerrarSesion()
                        true
                    }

                    else -> false
                }
            }
        }, activity as AppCompatActivity, Lifecycle.State.RESUMED)
    }

    private fun verificarYCerrarSesion() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si hay un usuario autenticado, mostrar diálogo de confirmación para cerrar sesión
            mostrarDialogoConfirmacion(getString(R.string.alert_cerrar_sesion), true)
        } else {
            // Si no hay sesión activa, continuar con la navegación
            mostrarDialogoConfirmacion(getString(R.string.alert_cerrar_sesion_inactiva), false)
        }
    }

    private fun mostrarDialogoConfirmacion(mensaje: String, cerrarSesion: Boolean) {
        AlertDialog.Builder(requireContext())
            .setMessage(mensaje)
            .setPositiveButton(R.string.alert_si) { _, _ ->
                if (cerrarSesion) {
                    // Cerrar sesión de Firebase
                    auth.signOut()

                    // Navegar a MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
            .setNegativeButton(R.string.alert_no, null)
            .show()
    }

    private fun convertBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
