package com.example.proyectoandroid2.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
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
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.activities.MainActivity
import com.example.proyectoandroid2.databinding.FragmentScaffoldBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class ScaffoldFragment : Fragment() {
    private lateinit var binding: FragmentScaffoldBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController
    private lateinit var onBackPressedCallback: OnBackPressedCallback // Asegúrate de declararlo aquí
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflar el layout para este fragmento
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

        // Se asocia el callback al dispatcher del activity
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        /* TOOLBAR */
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        this.activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        true
                    }

                    R.id.action_sort -> {
                        true
                    }

                    R.id.action_settings -> {
                        true
                    }

                    else -> false
                }
            }
        }, activity as AppCompatActivity, Lifecycle.State.RESUMED)

        // Unir Navigation con DrawerMenu (NavigationView)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragment_container_view_scaffold) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_rv, R.id.nav_slideshow
            ), binding.drawerLayout
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

        /* ACTUALIZAR EMAIL DEL USUARIO EN EL HEADER DEL DRAWER */
        navView = binding.navigation
        val headerView = navView.getHeaderView(0)
        val userEmailTextView: TextView = headerView.findViewById(R.id.textViewName)

        // Obtener el email del usuario autenticado de Firebase
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email ?: "Usuario no autenticado"

        // Actualizar el TextView con el email del usuario
        userEmailTextView.text = userEmail
    }

    private fun verificarYCerrarSesion() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si hay un usuario autenticado, mostrar diálogo de confirmación para cerrar sesión
            mostrarDialogoConfirmacion("¿Deseas cerrar sesión?", true)
        } else {
            // Si no hay sesión activa, continuar con la navegación
            mostrarDialogoConfirmacion("No hay sesión activa", false)
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
                    // Asegurarse de que la nueva actividad se inicie de manera que no se pueda volver a la actividad anterior
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()  // Finaliza la actividad actual para que no se pueda regresar
                }
            }
            .setNegativeButton(R.string.alert_no, null)
            .show()
    }
}