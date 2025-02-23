import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoandroid2.items.Item

class PilotosViewModel : ViewModel() {

    private val _pilotosList = MutableLiveData<List<Item>>()
    val pilotosList: LiveData<List<Item>> get() = _pilotosList

    // Lista original de pilotos
    private val originalPilotosList: MutableList<Item> = mutableListOf()

    // Variable para llevar el control del estado del orden
    private var isAscending = true

    fun setPilotos(pilotos: List<Item>) {
        originalPilotosList.clear()
        originalPilotosList.addAll(pilotos)
        _pilotosList.value = originalPilotosList
    }

    fun sortPilotsByName() {
        // Alterna entre ascendente y descendente
        isAscending = !isAscending

        val sortedList = if (isAscending) {
            originalPilotosList.sortedBy { it.nombrePiloto }
        } else {
            originalPilotosList.sortedByDescending { it.nombrePiloto }
        }

        _pilotosList.value = sortedList
    }

    // Metodo de filtrado
    fun filterPilotos(query: String) {
        val filteredList = originalPilotosList.filter { item ->
            item.nombrePiloto.contains(query, ignoreCase = true) || item.numero.toString().contains(query)
        }
        _pilotosList.value = filteredList
    }
}

