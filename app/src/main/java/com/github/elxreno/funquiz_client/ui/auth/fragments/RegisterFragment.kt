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
import com.github.elxreno.funquiz_client.databinding.FragmentRegisterBinding
import com.github.elxreno.funquiz_client.ui.auth.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = RegisterFragment()
    }

    private val viewModel by activityViewModels<AuthViewModel>()

    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)

        viewModel.authUiState.observe(viewLifecycleOwner) {
            if (it.isFetchingPasswordRequirements) {
                snackbar.setText("Fetching password requirements...")
                snackbar.show()
            } else {
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.setText("Password requirements fetched")
                snackbar.show()
            }
            if (it.isServerError) {
                snackbar.setText("Server error: ${it.serverErrorResponse?.status ?: "Unknown code"}, ${it.serverErrorResponse?.title ?: "Unknown error"}")
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.show()
            }
        }

        viewModel.registerUiState.observe(viewLifecycleOwner) {
            val username = it.usernameError
            val emailError = it.emailError
            val passwordError = it.passwordError
            val passwordConfirmError = it.passwordConfirmError
            val passwordRequirements = viewModel.passwordRequirements.value
            binding.editTextUsername.error = if (username == null) null else getString(username)
            binding.editTextEmail.error = if (emailError == null) null else getString(emailError)
            binding.editTextPassword.error =
                if (passwordError == null) null else getString(passwordError).format(
                    passwordRequirements?.requiredLength
                )
            binding.editTextPasswordConfirm.error =
                if (passwordConfirmError == null) null else getString(passwordConfirmError)

            if (it.isRegistering) {
                snackbar.setText("Registering...")
                snackbar.show()
            }
            if (it.isSuccess) {
                binding.root.findNavController()
                    .navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
    }
}