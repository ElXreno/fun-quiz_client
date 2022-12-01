package com.github.elxreno.funquiz_client.ui.users

import com.github.elxreno.funquiz_client.R
import com.github.elxreno.funquiz_client.data.dto.UserInfoDto
import com.github.elxreno.funquiz_client.databinding.ItemUserBinding

class UserAdapter(
    private val list: List<UserInfoDto>,
    private val userListener: UserListener
) : BaseAdapter<ItemUserBinding, UserInfoDto>(list) {

    override val layoutId: Int
        get() = R.layout.item_user

    override fun bind(binding: ItemUserBinding, item: UserInfoDto) {
        binding.apply {
            user = item
            listener = userListener
            executePendingBindings()
        }
    }

}

interface UserListener {
    fun onUserClicked(user: UserInfoDto)
}