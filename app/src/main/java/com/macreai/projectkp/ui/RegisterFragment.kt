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
import androidx.navigation.fragment.findNavController
import com.macreai.projectkp.dataStore
import com.macreai.projectkp.databinding.FragmentRegisterBinding
import com.macreai.projectkp.viewmodel.AppViewModel
import com.macreai.projectkp.viewmodel.ViewModelFactory

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppViewModel by activityViewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            showLoading(it)
        })

        viewModel.message.observe(viewLifecycleOwner, Observer {  message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })

        binding.btnSubmitRegister.setOnClickListener {
            val username = binding.edUsername.text.toString()
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
            } else {
                // Jika tidak ada radio button yang terpilih, Anda bisa memberikan nilai default atau menampilkan pesan kesalahan
                // gender = "Default Gender"
                // Atau tampilkan pesan kesalahan kepada pengguna
            }
            Log.d("Register Fragment", "onCreateView: $gender")
            if (username.isEmpty() || password.isEmpty() || address.isEmpty() || phone.isEmpty() || emergencyNumber.isEmpty() || age.isEmpty() || gender.isEmpty()){
                Toast.makeText(requireContext(), "Field(s) is Empty", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8){
                Toast.makeText(requireContext(), "Password must be at least 8 character", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.patientRegister(username, password, address, phone, emergencyNumber, age, gender)
                findNavController().popBackStack()
                Toast.makeText(requireContext(), "Register Success", Toast.LENGTH_SHORT).show()
            }

        }

        return view
    }

    private fun showLoading(isLoading: Boolean){
        if (isLoading){
            binding.loading.visibility = View.VISIBLE
        } else {
            binding.loading.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}