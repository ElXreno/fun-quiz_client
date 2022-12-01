package com.github.elxreno.funquiz_client.ui

interface ClickListener {
    fun onPositionClicked(position: Int)
    fun onSavedClicked(pair: Pair<String, Pair<List<String>, List<String>>>)
}