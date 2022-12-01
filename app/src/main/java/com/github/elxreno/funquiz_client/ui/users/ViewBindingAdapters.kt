package com.github.elxreno.funquiz_client.ui.users

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.github.elxreno.funquiz_client.data.dto.UserInfoDto

@BindingAdapter("setAdapter")
fun setAdapter(
    recyclerView: RecyclerView,
    adapter: BaseAdapter<ViewDataBinding, UserInfoDto>?
) {
    adapter?.let {
        recyclerView.adapter = it
    }
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter("submitList")
fun submitList(recyclerView: RecyclerView, list: List<UserInfoDto>?) {
    val adapter = recyclerView.adapter as BaseAdapter<ViewDataBinding, UserInfoDto>?
    adapter?.updateData(list ?: listOf())
}

@BindingAdapter("manageState")
fun manageState(progressBar: ProgressBar, state: Boolean) {
    progressBar.visibility = if (state) View.VISIBLE else View.GONE
}