package com.example.proyectoandroid2.fragments.scaffoldFragments

import ItemAdapter
import PilotosViewModel
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.databinding.FragmentListaBinding
import com.example.proyectoandroid2.items.Item
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class FavoritosFragment : Fragment() {

    private var _binding: FragmentListaBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ItemAdapter
    private lateinit var viewModel: PilotosViewModel
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(PilotosViewModel::class.java)

        val languageCode = arguments?.getString("languageCode") ?: "es"
        setLocale(languageCode)

        // Crear el adaptador y pasarle la lista de items
        adapter = ItemAdapter(mutableListOf()) { item ->
            toggleFavorito(item)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Mostrar ProgressBar antes de cargar los pilotos
        showLoading()

        // Configuración del SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Cuando el usuario hace swipe para refrescar, cargamos los pilotos
            refreshPilotosList()
        }

        // Simular un retraso de 2 segundos antes de cargar los pilotos
        Handler(Looper.getMainLooper()).postDelayed({
            // Después de 2 segundos, obtenemos los pilotos desde Firestore
            getFavoritosFromFirestore()
        }, 2000)

        // Observa los cambios en la lista de pilotos
        viewModel.pilotosList.observe(viewLifecycleOwner) { pilotos ->
            adapter.updateList(pilotos)
            // Ocultar ProgressBar una vez que los datos estén listos
            hideLoading()
            // Detener la animación de refresco
            binding.swipeRefreshLayout.isRefreshing = false
        }

        val recyclerView: RecyclerView? = view.findViewById(R.id.recyclerView)
        recyclerView?.scrollToPosition(0)
    }

    // Metodo para actualizar la lista cuando el fragmento vuelve al primer plano
    override fun onResume() {
        super.onResume()
        // Volver a cargar la lista de pilotos cuando el fragmento vuelve al primer plano
        refreshPilotosList()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Función para obtener solo los pilotos favoritos desde Firestore
    private fun getFavoritosFromFirestore() {
        db.collection("pilotos")
            .whereEqualTo("favorito", true) // Filtra solo los pilotos favoritos
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Ocultar ProgressBar si ocurre un error
                    hideLoading()
                    return@addSnapshotListener
                }

                val favoritosList = mutableListOf<Item>()
                for (document in snapshot!!) {
                    val posicion = document.getLong("posicion")?.toInt() ?: 0
                    val numero = document.getLong("numero")?.toInt() ?: 0
                    val nombrePiloto = document.getString("nombrePiloto") ?: ""
                    val nombreEquipo = document.getString("nombreEquipo") ?: ""
                    val fabrica = document.getString("fabrica") ?: ""
                    val nacionalidad = document.getString("nacionalidad") ?: ""
                    val puntos = document.getLong("puntos")?.toInt() ?: 0
                    val isFavorito = document.getBoolean("favorito") ?: false

                    // Crear un objeto Item con los datos obtenidos de Firestore
                    val piloto = Item(posicion, numero, nombrePiloto, nombreEquipo, fabrica, nacionalidad, puntos, isFavorito)
                    favoritosList.add(piloto)
                }

                // Ordenar la lista por puntos en orden descendente (mayor puntaje primero)
                val sortedList = favoritosList.sortedByDescending { it.puntos }

                // Establecer los datos en el ViewModel
                viewModel.setPilotos(sortedList)
            }
    }

    private fun toggleFavorito(item: Item) {
        viewModel.toggleFavorito(item,
            onSuccess = {
                // Actualizar el item en el adaptador después de que el favorito se haya actualizado correctamente
                updateItemInAdapter(item)
            },
            onFailure = { exception ->
                // Manejo de errores en caso de fallo
                Log.e("Firestore", "Error al actualizar el favorito", exception)
            }
        )
    }

    private fun updateItemInAdapter(item: Item) {
        // Buscar el índice del item en el adaptador
        val position = adapter.itemList.indexOf(item)

        if (position != -1) {
            // Actualizar el item en la lista del adaptador
            adapter.itemList[position] = item
            // Notificar al adaptador que el item en esa posición ha cambiado
            adapter.notifyItemChanged(position)
        }
    }


    // Función para refrescar la lista de pilotos
    private fun refreshPilotosList() {
        // Mostrar ProgressBar mientras refrescamos los datos
        showLoading()
        // Obtener los pilotos favoritos nuevamente desde Firestore
        getFavoritosFromFirestore()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
