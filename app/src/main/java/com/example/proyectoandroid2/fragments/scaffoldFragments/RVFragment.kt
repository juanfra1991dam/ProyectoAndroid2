package com.example.proyectoandroid2.fragments.scaffoldFragments

import ItemAdapter
import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid2.R
import com.example.proyectoandroid2.databinding.FragmentRvBinding
import com.example.proyectoandroid2.items.Item
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class RVFragment : Fragment() {

    private var _binding: FragmentRvBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languageCode = arguments?.getString("languageCode") ?: "es"
        setLocale(languageCode)

        val pilotosList = getPilotosList()

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = ItemAdapter(pilotosList)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun getPilotosList(): List<Item> {
        return listOf(
            Item(1, 89, "J. Martin", "Prima Pramac Racing", "Ducati", "ESP", 508),
            Item(2, 1, "P. Bagnaia", "Ducati Lenovo Team", "Ducati", "ITA", 498),
            Item(3, 93, "M. Marquez", "Gresini Racing MotoGP", "Ducati", "ESP", 392),
            Item(4, 23, "E. Bastianini", "Ducati Lenovo Team", "Ducati", "ITA", 386),
            Item(5, 33, "B. Binder", "Red Bull KTM Factory Racing", "Ktm", "RSA", 217),
            Item(6, 31, "P. Acosta", "Red Bull GASGAS Tech3", "Ktm", "ESP", 215),
            Item(7, 12, "M. Viñales", "Aprilia Racing", "Aprilia", "ESP", 190),
            Item(8, 73, "A. Marquez", "Gresini Racing MotoGP", "Ducati", "ESP", 173),
            Item(9, 21, "F. Morbidelli", "Prima Pramac Racing", "Ducati", "ITA", 173),
            Item(10, 49, "F. Di Giannantonio", "Pertamina Enduro VR46 Racing Team", "Ducati", "ITA", 165),
            Item(11, 41, "A. Espargaro", "Aprilia Racing", "Aprilia", "ESP", 163),
            Item(12, 72, "M. Bezzecchi", "Pertamina Enduro VR46 Racing Team", "Ducati", "ITA", 153),
            Item(13, 20, "F. Quartararo", "Monster Energy Yamaha MotoGP Team", "Yamaha", "FRA", 113),
            Item(14, 43, "J. Miller", "Red Bull KTM Factory Racing", "Ktm", "AUS", 87),
            Item(15, 88, "M. Oliveira", "Trackhouse Racing", "Aprilia", "PRT", 75),
            Item(16, 25, "R. Fernandez", "Trackhouse Racing", "Aprilia", "ESP", 66),
            Item(17, 5, "J. Zarco", "Castrol Honda LCR", "Honda", "FRA", 55),
            Item(18, 42, "A. Rins", "Monster Energy Yamaha MotoGP Team", "Yamaha", "ESP", 31),
            Item(19, 30, "T. Nakagami", "Idemitsu Honda LCR", "Honda", "JPN", 31),
            Item(20, 37, "A. Fernandez", "Red Bull GASGAS Tech3", "Ktm", "ESP", 27),
            Item(21, 36, "J. Mir", "Repsol Honda Team", "Honda", "ESP", 21),
            Item(22, 10, "L. Marini", "Repsol Honda Team", "Honda", "ITA", 14)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
