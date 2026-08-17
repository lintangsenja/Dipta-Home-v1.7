package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Model representing a monthly paycheck cycle (e.g., from 25th of last month to 24th of this month).
 */
data class PaycheckPeriod(
    val startDay: Int,
    val offset: Int,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val startDateStr: String,
    val endDateStr: String,
    val displayPeriod: String,
    val shortLabel: String,
    val monthTitle: String,
    val isCurrentCycle: Boolean
) {
    val label: String get() = shortLabel

    /**
     * Checks if a given timestamp (in milliseconds) or a date string ("yyyy-MM-dd")
     * falls within this paycheck period.
     */
    fun contains(timestamp: Long = 0L, dateStr: String = ""): Boolean {
        if (dateStr.isNotBlank()) {
            val dateClean = dateStr.trim()
            if (dateClean >= startDateStr && dateClean <= endDateStr) {
                return true
            }
        }
        if (timestamp > 0L) {
            return timestamp in startTimestamp..endTimestamp
        }
        return false
    }
}

object PaycheckCycleHelper {

    private val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfDisplay = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
    private val sdfShort = SimpleDateFormat("d MMM", Locale("id", "ID"))
    private val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

    /**
     * Calculates the PaycheckPeriod given a startDay (1..31) and an offset (0 = current, -1 = prev, +1 = next).
     */
    fun calculatePeriod(startDay: Int = 25, offset: Int = 0, referenceCalendar: Calendar = Calendar.getInstance()): PaycheckPeriod {
        val safeStartDay = startDay.coerceIn(1, 31)

        val calStart = (referenceCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayDay = calStart.get(Calendar.DAY_OF_MONTH)

        if (safeStartDay == 1) {
            // Standard calendar month
            calStart.add(Calendar.MONTH, offset)
            calStart.set(Calendar.DAY_OF_MONTH, 1)

            val calEnd = (calStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val startTs = calStart.timeInMillis
            val endTs = calEnd.timeInMillis
            val startStr = sdfDate.format(calStart.time)
            val endStr = sdfDate.format(calEnd.time)
            val display = "${sdfDisplay.format(calStart.time)} - ${sdfDisplay.format(calEnd.time)}"
            val short = "${sdfShort.format(calStart.time)} - ${sdfShort.format(calEnd.time)}"
            val monthTitle = sdfMonthYear.format(calStart.time)

            return PaycheckPeriod(
                startDay = safeStartDay,
                offset = offset,
                startTimestamp = startTs,
                endTimestamp = endTs,
                startDateStr = startStr,
                endDateStr = endStr,
                displayPeriod = display,
                shortLabel = short,
                monthTitle = monthTitle,
                isCurrentCycle = offset == 0
            )
        } else {
            // Custom day (e.g. 25)
            // If today < startDay, current cycle started in the previous month
            if (todayDay < safeStartDay) {
                calStart.add(Calendar.MONTH, -1)
            }
            // Apply cycle offset
            calStart.add(Calendar.MONTH, offset)

            val maxDayStart = calStart.getActualMaximum(Calendar.DAY_OF_MONTH)
            calStart.set(Calendar.DAY_OF_MONTH, safeStartDay.coerceAtMost(maxDayStart))

            // End date is 1 month later, day = startDay - 1
            val calEnd = (calStart.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                val targetEndDay = safeStartDay - 1
                val maxDayEnd = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, targetEndDay.coerceIn(1, maxDayEnd))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val startTs = calStart.timeInMillis
            val endTs = calEnd.timeInMillis
            val startStr = sdfDate.format(calStart.time)
            val endStr = sdfDate.format(calEnd.time)
            val display = "${sdfDisplay.format(calStart.time)} - ${sdfDisplay.format(calEnd.time)}"
            val short = "${sdfShort.format(calStart.time)} - ${sdfShort.format(calEnd.time)}"
            val monthTitle = "Gajian ${sdfMonthYear.format(calEnd.time)}"

            return PaycheckPeriod(
                startDay = safeStartDay,
                offset = offset,
                startTimestamp = startTs,
                endTimestamp = endTs,
                startDateStr = startStr,
                endDateStr = endStr,
                displayPeriod = display,
                shortLabel = short,
                monthTitle = monthTitle,
                isCurrentCycle = offset == 0
            )
        }
    }

    /**
     * Generates a list of recent paycheck cycles (e.g. last 6 cycles) for chart comparisons.
     */
    fun getRecentCycles(startDay: Int = 25, count: Int = 6): List<PaycheckPeriod> {
        val list = mutableListOf<PaycheckPeriod>()
        for (i in (count - 1) downTo 0) {
            list.add(calculatePeriod(startDay = startDay, offset = -i))
        }
        return list
    }
}
