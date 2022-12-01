package com.github.elxreno.funquiz_client.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.databinding.FragmentUsersBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding

    private val viewModel by viewModels<UsersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_users, container, false)
        binding.apply {
            viewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.adapter = UserAdapter(listOf(), viewModel)

        val snackbar = Snackbar.make(binding.root, "Hello", Snackbar.LENGTH_LONG)

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
            binding.recyclerViewUsers.visibility = if (uiState.isLoaded) View.VISIBLE else View.GONE
            if (uiState.error.isNotEmpty()) {
                snackbar.setText(uiState.error)
                snackbar.duration = Snackbar.LENGTH_INDEFINITE
                snackbar.show()
            } else {
                snackbar.setText("Data loaded")
                snackbar.duration = Snackbar.LENGTH_SHORT
                snackbar.show()
            }
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            binding.adapter?.updateData(users)
        }
    }
}