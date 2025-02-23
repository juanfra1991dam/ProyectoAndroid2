package com.example.proyectoandroid2.fragments.scaffoldFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.proyectoandroid2.R
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val welcomeTextView = view.findViewById<TextView>(R.id.textBienvenida)

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "Invitado"

        welcomeTextView.text = getString(R.string.bienvenida, email)

        return view
    }
}
