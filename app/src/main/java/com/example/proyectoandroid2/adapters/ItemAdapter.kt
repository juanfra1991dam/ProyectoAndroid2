import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.databinding.LayoutItemBinding
import com.example.proyectoandroid2.items.Item

class ItemAdapter(
    private val itemList: List<Item>,
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val binding: LayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Item) {
            binding.posicion.text = data.posicion.toString()
            binding.numero.text = data.numero.toString()
            binding.nombrePiloto.text = data.nombrePiloto
            binding.nombreEquipo.text = data.nombreEquipo
            binding.puntos.text = data.puntos.toString()

            // Configuramos las imágenes
            when (data.nacionalidad) {
                "ESP" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_esp)
                "ITA" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_ita)
                "RSA" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_sudafrica)
                "FRA" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_fra)
                "AUS" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_aus)
                "PTR" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_ptr)
                "JPN" -> binding.imagenNacionalidad.setImageResource(R.drawable.ic_bandera_jpn)
            }

            when (data.fabrica) {
                "Ducati" -> binding.imagenFabrica.setImageResource(R.drawable.ic_ducati)
                "Ktm" -> binding.imagenFabrica.setImageResource(R.drawable.ic_ktm)
                "Aprilia" -> binding.imagenFabrica.setImageResource(R.drawable.ic_aprilia)
                "Yamaha" -> binding.imagenFabrica.setImageResource(R.drawable.ic_yamaha)
                "Honda" -> binding.imagenFabrica.setImageResource(R.drawable.ic_honda)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
