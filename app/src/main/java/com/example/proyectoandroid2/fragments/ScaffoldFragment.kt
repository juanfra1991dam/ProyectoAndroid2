package com.example.proyectoandroid2.fragments

import PilotosViewModel
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.activities.MainActivity
import com.example.proyectoandroid2.databinding.FragmentScaffoldBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ScaffoldFragment : Fragment() {

    private lateinit var binding: FragmentScaffoldBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var viewModel: PilotosViewModel
    private val firestore = FirebaseFirestore.getInstance()

    private var selectedLanguage: String = "es"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentScaffoldBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar el idioma seleccionado de SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        selectedLanguage = sharedPreferences.getString("selectedLanguage", "es") ?: "es"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLocale(selectedLanguage)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragment_container_view_scaffold) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_lista, R.id.nav_favoritos, R.id.nav_contacto), binding.drawerLayout
        )

        setupActionBarWithNavController(requireActivity() as AppCompatActivity, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navigation, navController)

        toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.bottomNavigation.setupWithNavController(navController)

        navView = binding.navigation
        val headerView = navView.getHeaderView(0)
        val userEmailTextView: TextView = headerView.findViewById(R.id.textViewName)
        val userProfileImageView: ImageView = headerView.findViewById(R.id.imageViewProfile)

        val user = FirebaseAuth.getInstance().currentUser
        userEmailTextView.text = user?.email ?: "Usuario no autenticado"

        val userId = user?.uid
        userId?.let {
            firestore.collection("usuarios")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("nombre") ?: userEmailTextView.text.toString()
                    val imageBase64 = document.getString("imagenPerfil")

                    userEmailTextView.text = name
                    imageBase64?.let { base64 ->
                        val bitmap = convertBase64ToBitmap(base64)
                        Glide.with(requireContext())
                            .load(bitmap)
                            .into(userProfileImageView)
                    } ?: run {
                        Glide.with(requireContext())
                            .load(R.drawable.ic_user)
                            .into(userProfileImageView)
                    }
                }
        }

        navController.addOnDestinationChangedListener { _, _, _ ->
            activity?.invalidateOptionsMenu()
        }

        // Manejo del botón "Atrás"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                verificarYCerrarSesion()
            }
        })

        this.activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                val searchItem = menu.findItem(R.id.action_search)
                val sortItem = menu.findItem(R.id.action_sort)
                val logoutItem = menu.findItem(R.id.action_logout)

                when (navController.currentDestination?.id) {
                    R.id.nav_home, R.id.nav_contacto -> {
                        searchItem.isVisible = false
                        sortItem.isVisible = false
                        logoutItem.isVisible = true
                    }
                    R.id.nav_lista, R.id.nav_favoritos -> {
                        searchItem.isVisible = true
                        sortItem.isVisible = true
                        logoutItem.isVisible = true
                    }
                    else -> {
                        searchItem.isVisible = false
                        sortItem.isVisible = false
                        logoutItem.isVisible = true
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
                        searchView.setOnQueryTextListener(object :
                            androidx.appcompat.widget.SearchView.OnQueryTextListener {
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

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun verificarYCerrarSesion() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            mostrarDialogoConfirmacion(getString(R.string.alert_cerrar_sesion), true)
        } else {
            mostrarDialogoConfirmacion(getString(R.string.alert_cerrar_sesion_inactiva), false)
        }
    }

    private fun mostrarDialogoConfirmacion(mensaje: String, cerrarSesion: Boolean) {
        AlertDialog.Builder(requireContext())
            .setMessage(mensaje)
            .setPositiveButton(R.string.alert_si) { _, _ ->
                if (cerrarSesion) {
                    auth.signOut()
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
