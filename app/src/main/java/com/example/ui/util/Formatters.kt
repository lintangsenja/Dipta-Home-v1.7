package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    fun formatRupiah(amount: Number): String {
        return rupiahFormat.format(amount).replace("Rp", "Rp ")
    }

    fun formatNumber(number: Number): String {
        val nf = NumberFormat.getInstance(Locale("id", "ID"))
        return nf.format(number)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE, d MMM yyyy • HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }
}
