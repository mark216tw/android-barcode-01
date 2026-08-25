package com.simplebarcode.app.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BarcodeRepository(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val _items = MutableStateFlow(loadItems())

    val items: StateFlow<List<BarcodeItem>> = _items.asStateFlow()

    fun getById(id: Long): BarcodeItem? = _items.value.firstOrNull { it.id == id }

    fun save(item: BarcodeItem) {
        val current = _items.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) current[index] = item else current.add(0, item)
        persist(current)
    }

    fun delete(id: Long) {
        persist(_items.value.filterNot { it.id == id })
    }

    fun toggleFavorite(id: Long) {
        persist(_items.value.map { item ->
            if (item.id == id) item.copy(isFavorite = !item.isFavorite) else item
        })
    }

    private fun persist(items: List<BarcodeItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("content", item.content)
                put("type", item.type.name)
                put("showText", item.showText)
                put("favorite", item.isFavorite)
                put("createdAt", item.createdAt)
            })
        }
        preferences.edit { putString(KEY_ITEMS, array.toString()) }
        _items.value = items
    }

    private fun loadItems(): List<BarcodeItem> {
        val raw = preferences.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val value = array.getJSONObject(index)
                    add(
                        BarcodeItem(
                            id = value.getLong("id"),
                            name = value.getString("name"),
                            content = value.getString("content"),
                            type = BarcodeType.valueOf(value.getString("type")),
                            showText = value.optBoolean("showText", true),
                            isFavorite = value.optBoolean("favorite", false),
                            createdAt = value.optLong("createdAt", value.getLong("id")),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val FILE_NAME = "barcodes"
        private const val KEY_ITEMS = "barcode_items"
    }
}
