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

class FavoritosFragment : Fragment() {

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
            getFavoritosFromFirestore()
        }, 2000)

        binding?.swipeRefreshLayout?.setOnRefreshListener {
            getFavoritosFromFirestore()
        }
    }

    override fun onResume() {
        super.onResume()
        getFavoritosFromFirestore()
    }

    private fun getFavoritosFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.e("Firestore", "Usuario no autenticado")
            hideLoading()
            return
        }

        val uid = user.uid
        db.collection("usuarios").document(uid)
            .collection("favoritos")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
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

                    val piloto = Item(posicion, numero, nombrePiloto, nombreEquipo, fabrica, nacionalidad, puntos, true)
                    favoritosList.add(piloto)
                }

                val sortedList = favoritosList.sortedByDescending { it.puntos }
                viewModel.setPilotos(sortedList)
                adapter.updateList(sortedList)
                hideLoading()
                binding?.swipeRefreshLayout?.isRefreshing = false
            }
    }

    private fun toggleFavorito(item: Item) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val userFavRef = db.collection("usuarios").document(uid)
            .collection("favoritos").document(item.numero.toString())

        userFavRef.delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Piloto eliminado de favoritos")
                getFavoritosFromFirestore()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error al eliminar favorito", exception)
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
