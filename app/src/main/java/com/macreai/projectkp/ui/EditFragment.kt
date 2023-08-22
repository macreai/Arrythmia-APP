package com.macreai.projectkp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.macreai.projectkp.R
import com.macreai.projectkp.dataStore
import com.macreai.projectkp.databinding.FragmentEditBinding
import com.macreai.projectkp.viewmodel.AppViewModel
import com.macreai.projectkp.viewmodel.ViewModelFactory

class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get()= _binding!!

    private val viewModel: AppViewModel by activityViewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel.message.observe(this, Observer {  message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })

        binding.btnBackToHome.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnLogOut.setOnClickListener {
            viewModel.patientLogout()
            view.findNavController().navigate(R.id.action_editFragment_to_loginFragment)
        }

        binding.btnSubmitEdit.setOnClickListener {
            val password = binding.edPassword.text.toString()
            val address = binding.edAddress.text.toString()
            val phone = binding.edPhone.text.toString()
            val emergencyNumber = binding.edEmergencyPhone.text.toString()
            val age = binding.edAge.text.toString()
            var  gender = ""

            val selectedRadioButtonId = binding.radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                val selectedRadioButton = binding.root.findViewById<RadioButton>(selectedRadioButtonId)
                gender = selectedRadioButton.text.toString()
            }
            viewModel.patientEdit(address, phone, emergencyNumber, age, gender, password)
        }

        return view
    }
}