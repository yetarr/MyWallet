package pt.ipcb.mywallet.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    fun formatCurrency(amount: Double, currency: String = "EUR"): String {
        val symbol = when (currency) { "USD" -> "$"; "GBP" -> "£"; else -> "€" }
        val abs = "%.2f".format(kotlin.math.abs(amount))
        return if (amount < 0) "-$abs $symbol" else "$abs $symbol"
    }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

    fun formatRelativeDate(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 86_400_000L -> "Hoje"
            diff < 172_800_000L -> "Ontem"
            else -> formatDate(timestamp)
        }
    }

    fun monthLabel(timestamp: Long): String =
        SimpleDateFormat("MMM", Locale("pt")).format(Date(timestamp))
}
