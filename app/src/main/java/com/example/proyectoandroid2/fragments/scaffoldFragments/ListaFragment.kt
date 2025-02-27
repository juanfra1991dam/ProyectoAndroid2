package com.example.proyectoandroid2.fragments.scaffoldFragments

import ItemAdapter
import PilotosViewModel
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
import com.example.proyectoandroid2.databinding.FragmentListaBinding
import com.example.proyectoandroid2.items.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaFragment : Fragment() {

    private var _binding: FragmentListaBinding? = null
    private val binding get() = _binding

    private lateinit var adapter: ItemAdapter
    private lateinit var viewModel: PilotosViewModel
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListaBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PilotosViewModel::class.java)

        adapter = ItemAdapter(mutableListOf()) { item ->
            toggleFavorito(item)
        }
        binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.recyclerView?.adapter = adapter

        showLoading()
        Handler(Looper.getMainLooper()).postDelayed({
            getPilotosFromFirestore()
        }, 2000)

        binding?.swipeRefreshLayout?.setOnRefreshListener {
            getPilotosFromFirestore()
        }
    }

    override fun onResume() {
        super.onResume()
        getPilotosFromFirestore()
    }

    private fun getPilotosFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("Firestore", "Usuario no autenticado")
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

                            val piloto = Item(posicion, numero, nombrePiloto, nombreEquipo, fabrica, nacionalidad, puntos, isFavorito)
                            pilotosList.add(piloto)
                        }

                        val sortedList = pilotosList.sortedByDescending { it.puntos }
                        viewModel.setPilotos(sortedList)
                        adapter.updateList(sortedList)
                        hideLoading()
                        binding?.swipeRefreshLayout?.isRefreshing = false
                    }
            }
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
                    Log.d("Firestore", "Piloto eliminado de favoritos")
                    getPilotosFromFirestore()  // Recargar la lista
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error al eliminar favorito", exception)
                }
        } else {
            // Si el piloto no es favorito, agregarlo a los favoritos con todos los campos
            userFavRef.set(favoritoData)
                .addOnSuccessListener {
                    Log.d("Firestore", "Piloto agregado a favoritos")
                    getPilotosFromFirestore()  // Recargar la lista
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error al agregar favorito", exception)
                }
        }
    }

    private fun showLoading() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding?.progressBar?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

