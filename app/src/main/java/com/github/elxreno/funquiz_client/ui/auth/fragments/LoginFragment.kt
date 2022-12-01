package com.github.elxreno.funquiz_client.ui.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.databinding.FragmentLoginBinding
import com.github.elxreno.funquiz_client.ui.auth.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }

    private val viewModel by activityViewModels<AuthViewModel>()

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)

        viewModel.authUiState.observe(viewLifecycleOwner) {
            if (it.isNetworkError) {
                snackbar.setText("Network error")
                snackbar.show()
            } else if (it.isServerError) {
                snackbar.setText("Server error: ${it.serverErrorResponse?.status ?: "Unknown code"}, ${it.serverErrorResponse?.title ?: "Unknown error"}")
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.show()
            } else if (it.isInvalidCredentials) {
                snackbar.setText("Invalid credentials")
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.show()
            } else if (it.isRegistrationSuccess) {
                snackbar.setText("Registration success")
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.show()
            } else if (it.isLoggingIn) {
                snackbar.setText("Logging in...")
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.show()
            } else if (it.isLoginSuccess) {
                binding.root.findNavController()
                    .navigate(R.id.action_loginFragment_to_nav_dashboard_graph)
                viewModel.onLoginSuccess()
                requireActivity().finish()
            } else if (!it.isFieldsValid) {
                val emailError = it.emailError
                binding.editTextEmail.error =
                    if (emailError == null) null else getString(emailError)
            }
        }

        binding.registerButton.setOnClickListener {
            binding.root.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}