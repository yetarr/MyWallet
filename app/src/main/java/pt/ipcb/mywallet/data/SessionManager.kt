package pt.ipcb.mywallet.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("mywallet_prefs", Context.MODE_PRIVATE)

    var userId: Int
        get() = prefs.getInt("user_id", -1)
        set(value) = prefs.edit().putInt("user_id", value).apply()

    fun isLoggedIn(): Boolean = userId != -1

    fun logout() = prefs.edit().remove("user_id").apply()
}
