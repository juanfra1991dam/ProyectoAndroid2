package com.example.proyectoandroid2.fragments.scaffoldFragments

import ItemAdapter
import PilotosViewModel
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.databinding.FragmentListaBinding
import com.example.proyectoandroid2.items.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaFragment : Fragment() {

    private var _binding: FragmentListaBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

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

        // Crear el adaptador y pasarle la lista de items y la función para manejar el clic en favorito
        adapter = ItemAdapter(mutableListOf()) { item ->
            toggleFavorito(item)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Mostrar ProgressBar antes de cargar los pilotos
        showLoading()

        // Configuración del SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            getPilotosListFromFirestore()
        }

        // Simular un retraso de 1 segundos antes de cargar los pilotos
        Handler(Looper.getMainLooper()).postDelayed({
            getPilotosListFromFirestore()
        }, 1000)

        // Observa los cambios en la lista de pilotos
        viewModel.pilotosList.observe(viewLifecycleOwner) { pilotos ->
            adapter.updateList(pilotos)
            hideLoading()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        val recyclerView: RecyclerView? = view.findViewById(R.id.recyclerView)
        recyclerView?.scrollToPosition(0)
    }

    // Metodo para actualizar la lista cuando el fragmento vuelve al primer plano
    override fun onResume() {
        super.onResume()
        refreshPilotosList()
    }

    private fun getPilotosListFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            hideLoading()
            return
        }

        val uid = user.uid
        // Obtener los favoritos del usuario
        db.collection("usuarios").document(uid)
            .collection("favoritos")
            .get()
            .addOnSuccessListener { snapshot ->
                val favoritosSet = snapshot.documents.mapNotNull { document ->
                    // Obtener el número del piloto de la colección de favoritos
                    document.getLong("numero")?.toInt()
                }.toSet()

                // Ahora obtener todos los pilotos
                db.collection("pilotos")
                    .addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            hideLoading()
                            return@addSnapshotListener
                        }

                        val pilotosList = mutableListOf<Item>()
                        for (document in snapshot!!) {
                            val posicion = document.getLong("posicion")?.toInt() ?: 0
                            val numero = document.getLong("numero")?.toInt() ?: 0
                            val nombrePiloto = document.getString("nombrePiloto") ?: ""
                            val nombreEquipo = document.getString("nombreEquipo") ?: ""
                            val fabrica = document.getString("fabrica") ?: ""
                            val nacionalidad = document.getString("nacionalidad") ?: ""
                            val puntos = document.getLong("puntos")?.toInt() ?: 0
                            val isFavorito = favoritosSet.contains(numero)

                            val piloto = Item(
                                posicion,
                                numero,
                                nombrePiloto,
                                nombreEquipo,
                                fabrica,
                                nacionalidad,
                                puntos,
                                isFavorito
                            )
                            pilotosList.add(piloto)
                        }

                        val sortedList = pilotosList.sortedByDescending { it.puntos }
                        viewModel.setPilotos(sortedList)
                        adapter.updateList(sortedList)
                        hideLoading()
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
            }
    }

    // Función para refrescar la lista de pilotos
    private fun refreshPilotosList() {
        showLoading()
        Handler(Looper.getMainLooper()).postDelayed({
            getPilotosListFromFirestore()
        }, 1000)
    }

    private fun toggleFavorito(item: Item) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val userFavRef = db.collection("usuarios").document(uid)
            .collection("favoritos").document(item.numero.toString())

        val favoritoData = mapOf(
            "posicion" to item.posicion,
            "numero" to item.numero,
            "nombrePiloto" to item.nombrePiloto,
            "nombreEquipo" to item.nombreEquipo,
            "fabrica" to item.fabrica,
            "nacionalidad" to item.nacionalidad,
            "puntos" to item.puntos
        )

        // Si el piloto es favorito, eliminarlo de los favoritos
        if (item.favorito) {
            userFavRef.delete()
                .addOnSuccessListener {
                    getPilotosListFromFirestore()
                }
        } else {
            // Si el piloto no es favorito, agregarlo a los favoritos con todos los campos
            userFavRef.set(favoritoData)
                .addOnSuccessListener {
                    getPilotosListFromFirestore()
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val searchView = item.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
                val recyclerView: RecyclerView = requireView().findViewById(R.id.recyclerView)
                recyclerView.scrollToPosition(0)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
