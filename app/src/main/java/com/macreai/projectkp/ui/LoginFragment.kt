package com.macreai.projectkp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.macreai.projectkp.R
import com.macreai.projectkp.dataStore
import com.macreai.projectkp.databinding.FragmentLoginBinding
import com.macreai.projectkp.viewmodel.AppViewModel
import com.macreai.projectkp.viewmodel.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var loginJob: Job = Job()

    private val viewModel: AppViewModel by activityViewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        checkLoginUser()

        binding.btnSignUp.setOnClickListener {
            // Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment)
            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.edUsername.text.toString()
            val password = binding.edPassword.text.toString()
            showLoading(true)

            Log.d("Login Fragment", "loginUser: $username, $password")
            lifecycleScope.launchWhenResumed {
                if (username.isEmpty() || password.isEmpty()){
                    showLoading(false)
                    Toast.makeText(requireContext(), "Please insert the username or password", Toast.LENGTH_SHORT).show()
                } else {
                    if (loginJob.isActive) loginJob.cancel()
                    loginJob = launch {
                        viewModel.patientLogin(username, password).collect{ result ->
                            result.onSuccess { loginResponse ->
                                showLoading(false)
                                if (loginResponse.status == "200"){
                                    viewModel.saveToken(loginResponse.token)
                                    viewModel.saveId(loginResponse.data.id)
                                    view.findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                    Toast.makeText(requireContext(), loginResponse.message, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), loginResponse.message, Toast.LENGTH_SHORT).show()
                                }

                            }

                            result.onFailure { throwable ->
                                showLoading(false)
                                Log.e(TAG, "onFailureLogin: $throwable", )
                                throwable.printStackTrace()
                                Toast.makeText(requireContext(), "Invalid to Connect", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    // view.findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    // view.findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    /*
                    viewModel.status.observe(viewLifecycleOwner, Observer {
                        Log.d("Login Fragment", "onCreateView: $it")
                        if (it == "200"){
                            view.findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            Toast.makeText(requireContext(), "Validation Failed", Toast.LENGTH_SHORT).show()
                        }
                    })*/

                }
            }
            // viewModel.patientLogIn(username, password)
            // Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment)
            // view?.findNavController()?.navigate(R.id.action_loginFragment_to_homeFragment)
            // onDetach()
        }

        return view
    }

    private fun checkLoginUser(){
        viewModel.getLogin().observe(viewLifecycleOwner, Observer {  token ->
            if (token.isNotEmpty()){
                Log.d(TAG, "checkLoginUser: $token")
                view?.findNavController()?.navigate(R.id.action_loginFragment_to_homeFragment)
            }
        })
    }

    private fun showLoading(isLoading: Boolean){
        if (isLoading){
            binding.loading.visibility = View.VISIBLE
        } else {
            binding.loading.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        private const val TAG = "LoginFragment"
    }
}