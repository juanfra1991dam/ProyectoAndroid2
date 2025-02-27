import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoandroid2.items.Item
import com.google.firebase.firestore.FirebaseFirestore

class PilotosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _pilotosList = MutableLiveData<List<Item>>()
    val pilotosList: LiveData<List<Item>> get() = _pilotosList

    // Lista original de pilotos
    private val originalPilotosList: MutableList<Item> = mutableListOf()
    private var isAscending = true

    fun setPilotos(pilotos: List<Item>) {
        originalPilotosList.clear()
        originalPilotosList.addAll(pilotos)
        _pilotosList.value = originalPilotosList
    }

    fun sortPilotsByPoints() {
        isAscending = !isAscending
        val sortedList = if (isAscending) {
            originalPilotosList.sortedByDescending { it.puntos }
        } else {
            originalPilotosList.sortedBy { it.puntos }
        }
        _pilotosList.value = sortedList
    }

    fun filterPilotos(query: String) {
        val filteredList = originalPilotosList.filter { item ->
            item.nombrePiloto.contains(query, ignoreCase = true) || item.numero.toString().contains(query)
        }
        _pilotosList.value = filteredList
    }

    // Función para alternar el estado de 'favorito' de un piloto
    fun toggleFavorito(item: Item, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Cambiar el estado de 'favorito' del item
        item.favorito = !item.favorito

        // Actualizar el estado en Firestore
        db.collection("pilotos")
            .document(item.posicion.toString())
            .update("favorito", item.favorito)
            .addOnSuccessListener {
                // Llamar al callback de éxito
                onSuccess()
            }
            .addOnFailureListener { exception ->
                // Llamar al callback de error
                onFailure(exception)
            }
    }
}
