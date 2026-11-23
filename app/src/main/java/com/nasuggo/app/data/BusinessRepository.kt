package com.nasuggo.app.data

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

interface BusinessSource {
    fun getAll(): List<Business>
    fun findById(id: String): Business? = getAll().firstOrNull { it.id == id }
}

class JsonBusinessSource(context: Context) : BusinessSource {
    private val appContext = context.applicationContext
    private val businesses: List<Business> by lazy { load() }

    override fun getAll(): List<Business> = businesses

    private fun load(): List<Business> = try {
        appContext.assets.open(ASSET_NAME).use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                Gson().fromJson(reader, BusinessFile::class.java)
                    ?.businesses.orEmpty()
                    .map { BusinessFactory.create(it) }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("Nasuggo", "Failed to load businesses.json", e)
        emptyList()
    }

    private companion object {
        const val ASSET_NAME = "businesses.json"
    }
}

object BusinessRepository {
    @Volatile
    private var source: BusinessSource? = null

    fun source(context: Context): BusinessSource =
        source ?: synchronized(this) {
            source ?: JsonBusinessSource(context).also { source = it }
        }

    fun getAll(context: Context): List<Business> = source(context).getAll()
    fun findById(context: Context, id: String): Business? = source(context).findById(id)
}
