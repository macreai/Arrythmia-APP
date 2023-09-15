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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            showLoading(it)
        })

        binding.btnBackToHome.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnLogOut.setOnClickListener {
            showLogoutDialog()
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

            val allFieldIsEmpty = address.isEmpty() || phone.isEmpty() || emergencyNumber.isEmpty()
                    || age.isEmpty() || gender.isEmpty() || password.isEmpty()
            if (allFieldIsEmpty){
                Toast.makeText(requireContext(), "Field(s) is Empty", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.patientEdit(address, phone, emergencyNumber, age, gender, password)
                Toast.makeText(requireContext(), "Edit Successful", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
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

    private fun showLogoutDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure want to logout?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes"){ _, _ ->
                viewModel.saveToken("")
                viewModel.saveId(0)
                viewModel.patientLogout()
                activity?.finish()
            }.show()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}