import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.databinding.LayoutItemBinding
import com.example.proyectoandroid2.items.Item

class ItemAdapter(
    var itemList: MutableList<Item>,
    private val onFavoritoClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // Actualización visual del estado del favorito dentro del adaptador
    class ItemViewHolder(private val binding: LayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Item, onFavoritoClick: (Item) -> Unit) {
            binding.posicion.text = data.posicion.toString()
            binding.numero.text = data.numero.toString()
            binding.nombrePiloto.text = data.nombrePiloto
            binding.nombreEquipo.text = data.nombreEquipo
            binding.puntos.text = data.puntos.toString()

            // Configuración de la bandera según la nacionalidad
            when (data.nacionalidad) {
                "ESP" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_esp)
                "ITA" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_ita)
                "RSA" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_sudafrica)
                "FRA" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_fra)
                "AUS" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_aus)
                "PTR" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_ptr)
                "JPN" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_jpn)
            }

            // Configuración de la fábrica según el equipo
            when (data.fabrica) {
                "Ducati" -> binding.imagenFabrica.setImageResource(R.drawable.ic_ducati)
                "Ktm" -> binding.imagenFabrica.setImageResource(R.drawable.ic_ktm)
                "Aprilia" -> binding.imagenFabrica.setImageResource(R.drawable.ic_aprilia)
                "Yamaha" -> binding.imagenFabrica.setImageResource(R.drawable.ic_yamaha)
                "Honda" -> binding.imagenFabrica.setImageResource(R.drawable.ic_honda)
            }

            // Configuración de la imagen de favorito según el estado del objeto 'Item'
            if (data.favorito) {
                binding.imagenFavorito.setImageResource(R.drawable.ic_favorito_selected)
            } else {
                binding.imagenFavorito.setImageResource(R.drawable.ic_favorito_unselected)
            }

            // Cambiar el estado del favorito cuando se haga clic
            binding.imagenFavorito.setOnClickListener {
                onFavoritoClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position], onFavoritoClick)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    // Metodo para actualizar la lista general (todos los elementos)
    fun updateList(newList: List<Item>) {
        val diffResult = DiffUtil.calculateDiff(ItemDiffCallback(itemList, newList))
        itemList.clear()
        itemList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ItemDiffCallback(private val oldList: List<Item>, private val newList: List<Item>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].numero == newList[newItemPosition].numero
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
