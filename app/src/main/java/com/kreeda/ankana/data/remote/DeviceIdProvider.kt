package com.kreeda.ankana.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anonymous per-install identifier. Generated once and persisted; lets the
 * Supabase rows record an owner without needing a real auth flow.
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("kreeda_device", Context.MODE_PRIVATE)

    val id: String by lazy {
        prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY, it).apply()
        }
    }

    companion object {
        private const val KEY = "device_id"
    }
}
