package com.macreai.projectkp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.macreai.projectkp.R
import com.macreai.projectkp.dataStore
import com.macreai.projectkp.databinding.FragmentLoginBinding
import com.macreai.projectkp.viewmodel.AppViewModel
import com.macreai.projectkp.viewmodel.ViewModelFactory

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppViewModel by activityViewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel.message.observe(this, Observer {  message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })

        binding.btnSignUp.setOnClickListener {
            // Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment)
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.edUsername.text.toString()
            val password = binding.edPassword.text.toString()

            Log.d("Login Fragment", "loginUser: $username, $password")
            viewModel.patientLogIn(username, password)
            // Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment)
            view?.findNavController()?.navigate(R.id.action_loginFragment_to_homeFragment)
            onDetach()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}