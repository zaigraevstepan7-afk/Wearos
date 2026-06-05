package com.oneplus.watchsearch.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.wear.remote.interactions.RemoteActivityHelper

/**
 * Opens a Google search for a query as robustly as possible:
 *  1. the device's own browser (works on Wear OS with a browser and on phones);
 *  2. failing that, the browser on the paired phone (Wear OS without a browser);
 *  3. failing that, a short message — never a crash.
 */
class QueryOpener(private val context: Context) {

    fun open(query: String) {
        val uri = Uri.parse(GoogleSearch.url(query))

        if (openLocally(uri)) return
        if (openOnPhone(uri)) return

        Toast.makeText(
            context,
            "Не удалось открыть результаты поиска",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun openLocally(uri: Uri): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: Throwable) {
            // No browser, or the launch was rejected for any reason.
            false
        }
    }

    private fun openOnPhone(uri: Uri): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri)
                .addCategory(Intent.CATEGORY_BROWSABLE)
            RemoteActivityHelper(context).startRemoteActivity(intent)
            Toast.makeText(context, "Открываю на телефоне", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            false
        }
    }
}
