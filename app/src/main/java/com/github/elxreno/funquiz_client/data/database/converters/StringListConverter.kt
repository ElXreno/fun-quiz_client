package com.github.elxreno.funquiz_client.data.database.converters

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi

class StringListConverter(
    private val moshi: Moshi = Moshi.Builder().build()
) {

    @TypeConverter
    fun fromString(value: String): List<String> {
        val type = moshi.adapter<List<String>>(List::class.java)
        return type.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        val type = moshi.adapter<List<String>>(List::class.java)
        return type.toJson(list)
    }
}