package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.entity.ChildExpenseLog
import com.example.data.entity.DailyGroceryLog
import com.example.data.entity.ElectricityLog
import com.example.data.entity.FuelLog
import com.example.data.entity.OilLog
import com.example.data.entity.RandomExpense
import com.example.data.entity.ServiceLog
import com.example.data.entity.SocialLog
import com.example.data.entity.WarungDebt
import com.example.ui.util.Formatters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class ExportPeriodType {
    MONTHLY,
    CUSTOM_RANGE
}

data class ReportExportOptions(
    val periodType: ExportPeriodType = ExportPeriodType.MONTHLY,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-based index
    val startDate: String = "", // YYYY-MM-DD
    val endDate: String = "", // YYYY-MM-DD
    val includeBelanja: Boolean = true,
    val includeRandom: Boolean = true,
    val includeAnak: Boolean = true,
    val includeBensin: Boolean = true,
    val includeOli: Boolean = true,
    val includeServis: Boolean = true,
    val includeListrik: Boolean = true,
    val includeJimpitan: Boolean = true,
    val includeHutang: Boolean = true
)

data class FormattedReportData(
    val periodTitle: String,
    val totalModal: Double,
    val totalPengeluaran: Double,
    val sisaSaldo: Double,
    val belanjaLogs: List<DailyGroceryLog>,
    val randomExpenses: List<RandomExpense>,
    val childExpenses: List<ChildExpenseLog>,
    val fuelLogs: List<FuelLog>,
    val oilLogs: List<OilLog>,
    val serviceLogs: List<ServiceLog>,
    val electricityLogs: List<ElectricityLog>,
    val socialLogs: List<SocialLog>,
    val warungDebts: List<WarungDebt>
)

object ExportReportManager {

    private const val TAG_EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    private const val TAG_PDF_MIME = "application/pdf"

    fun prepareData(
        options: ReportExportOptions,
        dailyGroceryLogs: List<DailyGroceryLog>,
        randomExpenses: List<RandomExpense>,
        childExpenses: List<ChildExpenseLog> = emptyList(),
        fuelLogs: List<FuelLog>,
        oilLogs: List<OilLog>,
        serviceLogs: List<ServiceLog>,
        electricityLogs: List<ElectricityLog>,
        socialLogs: List<SocialLog>,
        warungDebts: List<WarungDebt>
    ): FormattedReportData {
        val sdfDateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dateFilterCombined: (String, Long) -> Boolean = { dateStr, timestamp ->
            if (options.periodType == ExportPeriodType.MONTHLY) {
                var matched = false
                if (timestamp > 0L) {
                    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH)
                    if (year == options.selectedYear && month == options.selectedMonth) {
                        matched = true
                    }
                }
                if (!matched && dateStr.isNotBlank()) {
                    try {
                        val parts = dateStr.take(10).split("-")
                        if (parts.size >= 2) {
                            val year = parts[0].toInt()
                            val month = parts[1].toInt() - 1
                            if (year == options.selectedYear && month == options.selectedMonth) {
                                matched = true
                            }
                        } else matched = true
                    } catch (_: Exception) { matched = true }
                }
                matched
            } else {
                val cleanDate = if (timestamp > 0L) {
                    sdfDateOnly.format(Date(timestamp))
                } else {
                    dateStr.take(10)
                }
                val startOk = if (options.startDate.isNotBlank()) cleanDate >= options.startDate else true
                val endOk = if (options.endDate.isNotBlank()) cleanDate <= options.endDate else true
                startOk && endOk
            }
        }

        val monthNames = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val periodTitle = if (options.periodType == ExportPeriodType.MONTHLY) {
            "${monthNames.getOrElse(options.selectedMonth) { "" }} ${options.selectedYear}"
        } else {
            "${options.startDate.ifBlank { "Awal" }} s.d. ${options.endDate.ifBlank { "Akhir" }}"
        }

        val filteredBelanja = if (options.includeBelanja) dailyGroceryLogs.filter { dateFilterCombined(it.tanggal, it.timestamp) } else emptyList()
        val filteredRandom = if (options.includeRandom) randomExpenses.filter { dateFilterCombined(it.tanggal, it.timestamp) } else emptyList()
        val filteredChild = if (options.includeAnak) childExpenses.filter { dateFilterCombined(it.tanggal, it.timestamp) } else emptyList()
        val filteredFuel = if (options.includeBensin) fuelLogs.filter { dateFilterCombined("", it.tanggal) } else emptyList()
        val filteredOil = if (options.includeOli) oilLogs.filter { dateFilterCombined("", it.tanggal) } else emptyList()
        val filteredService = if (options.includeServis) serviceLogs.filter { dateFilterCombined("", it.tanggal) } else emptyList()
        val filteredElectricity = if (options.includeListrik) electricityLogs.filter { dateFilterCombined("", it.tanggal) } else emptyList()
        val filteredSocial = if (options.includeJimpitan) socialLogs.filter { dateFilterCombined("", it.tanggal) } else emptyList()
        val filteredDebt = if (options.includeHutang) warungDebts.filter { dateFilterCombined(it.tanggal, it.timestamp) } else emptyList()

        val totalModal = filteredBelanja.sumOf { it.modalAwal } + filteredRandom.sumOf { it.modalAwal } + filteredChild.sumOf { it.modalAwal }
        val totalExpBelanja = filteredBelanja.sumOf { it.totalPengeluaran }
        val totalExpRandom = filteredRandom.sumOf { it.totalPengeluaran }
        val totalExpChild = filteredChild.sumOf { it.totalPengeluaran }
        val totalExpFuel = filteredFuel.sumOf { it.nominal.toDouble() }
        val totalExpOil = filteredOil.sumOf { it.harga.toDouble() }
        val totalExpService = filteredService.sumOf { it.total_biaya.toDouble() }
        val totalExpElectricity = filteredElectricity.sumOf { it.harga.toDouble() }
        val totalExpSocial = filteredSocial.sumOf { it.nominal.toDouble() }
        val totalExpDebt = filteredDebt.sumOf { it.nominal }

        val grandPengeluaran = totalExpBelanja + totalExpRandom + totalExpChild + totalExpFuel + totalExpOil + totalExpService + totalExpElectricity + totalExpSocial + totalExpDebt
        val sisaSaldo = totalModal - (totalExpBelanja + totalExpRandom + totalExpChild)

        return FormattedReportData(
            periodTitle = periodTitle,
            totalModal = totalModal,
            totalPengeluaran = grandPengeluaran,
            sisaSaldo = sisaSaldo,
            belanjaLogs = filteredBelanja,
            randomExpenses = filteredRandom,
            childExpenses = filteredChild,
            fuelLogs = filteredFuel,
            oilLogs = filteredOil,
            serviceLogs = filteredService,
            electricityLogs = filteredElectricity,
            socialLogs = filteredSocial,
            warungDebts = filteredDebt
        )
    }

    fun exportToExcel(context: Context, data: FormattedReportData) {
        try {
            val fileName = "Laporan_Keuangan_DiptaHome_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xlsx"
            val file = File(context.cacheDir, fileName)

            ZipOutputStream(FileOutputStream(file)).use { zos ->
                // 1. [Content_Types].xml
                zos.putNextEntry(ZipEntry("[Content_Types].xml"))
                zos.write(getContentTypesXml().toByteArray())
                zos.closeEntry()

                // 2. _rels/.rels
                zos.putNextEntry(ZipEntry("_rels/.rels"))
                zos.write(getRelsXml().toByteArray())
                zos.closeEntry()

                // 3. xl/_rels/workbook.xml.rels
                zos.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
                zos.write(getWorkbookRelsXml().toByteArray())
                zos.closeEntry()

                // 4. xl/workbook.xml
                zos.putNextEntry(ZipEntry("xl/workbook.xml"))
                zos.write(getWorkbookXml().toByteArray())
                zos.closeEntry()

                // 5. xl/worksheets/sheet1.xml
                zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
                zos.write(buildSheetXml(data).toByteArray())
                zos.closeEntry()
            }

            shareFile(context, file, TAG_EXCEL_MIME, "Laporan Keuangan Excel")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal export ke Excel: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportToPdf(context: Context, data: FormattedReportData) {
        try {
            val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }
            var currentY = 40f

            fun checkNewPage(neededSpace: Float) {
                if (currentY + neededSpace > pageHeight - 50) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                    // Draw mini header on new page
                    paint.color = Color.DKGRAY
                    paint.textSize = 10f
                    canvas.drawText("DIPTA HOME - Laporan Keuangan (${data.periodTitle}) - Halaman $pageNumber", 40f, currentY, paint)
                    currentY += 20f
                    paint.color = Color.LTGRAY
                    canvas.drawLine(40f, currentY, pageWidth - 40f, currentY, paint)
                    currentY += 15f
                }
            }

            // Header Banner
            paint.color = 0xFF2E7D32.toInt() // Forest Green
            canvas.drawRect(40f, currentY, pageWidth - 40f, currentY + 50f, paint)

            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("DIPTA HOME - LAPORAN KEUANGAN", 55f, currentY + 32f, paint)

            currentY += 65f

            // Period & Generated Info
            paint.color = Color.BLACK
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("Periode: ${data.periodTitle}", 40f, currentY, paint)

            paint.isFakeBoldText = false
            paint.textSize = 10f
            paint.color = Color.GRAY
            val todayStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())
            canvas.drawText("Dicetak pada: $todayStr", pageWidth - 200f, currentY, paint)

            currentY += 25f

            // Executive Summary Card
            paint.color = 0xFFF1F8E9.toInt()
            val summaryRect = RectF(40f, currentY, pageWidth - 40f, currentY + 70f)
            canvas.drawRoundRect(summaryRect, 10f, 10f, paint)

            paint.color = 0xFF333333.toInt()
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("RINGKASAN EKSEKUTIF", 55f, currentY + 22f, paint)

            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Total Modal / Anggaran: ${Formatters.formatRupiah(data.totalModal)}", 55f, currentY + 42f, paint)
            canvas.drawText("Total Pengeluaran: ${Formatters.formatRupiah(data.totalPengeluaran)}", 240f, currentY + 42f, paint)
            canvas.drawText("Sisa Saldo: ${Formatters.formatRupiah(data.sisaSaldo)}", 430f, currentY + 42f, paint)

            currentY += 85f

            // Section Renderer Helper
            fun drawSectionHeader(title: String, colorHex: Int) {
                checkNewPage(40f)
                paint.color = colorHex
                canvas.drawRect(40f, currentY, pageWidth - 40f, currentY + 22f, paint)
                paint.color = Color.WHITE
                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText(title, 48f, currentY + 15f, paint)
                currentY += 28f
            }

            fun drawTableHeader(col1: String, col2: String, col3: String) {
                checkNewPage(25f)
                paint.color = 0xFFEEEEEE.toInt()
                canvas.drawRect(40f, currentY, pageWidth - 40f, currentY + 18f, paint)
                paint.color = Color.BLACK
                paint.textSize = 9f
                paint.isFakeBoldText = true
                canvas.drawText(col1, 48f, currentY + 13f, paint)
                canvas.drawText(col2, 130f, currentY + 13f, paint)
                canvas.drawText(col3, pageWidth - 140f, currentY + 13f, paint)
                currentY += 22f
            }

            fun drawTableRow(col1: String, col2: String, col3: String, isAlt: Boolean) {
                checkNewPage(20f)
                if (isAlt) {
                    paint.color = 0xFFFAFAFA.toInt()
                    canvas.drawRect(40f, currentY - 2f, pageWidth - 40f, currentY + 16f, paint)
                }
                paint.color = 0xFF333333.toInt()
                paint.textSize = 9f
                paint.isFakeBoldText = false
                val safeCol2 = if (col2.length > 50) col2.take(47) + "..." else col2
                canvas.drawText(col1.take(10), 48f, currentY + 11f, paint)
                canvas.drawText(safeCol2, 130f, currentY + 11f, paint)
                canvas.drawText(col3, pageWidth - 140f, currentY + 11f, paint)

                paint.color = 0xFFE0E0E0.toInt()
                canvas.drawLine(40f, currentY + 17f, pageWidth - 40f, currentY + 17f, paint)
                currentY += 20f
            }

            var sectionIdx = 1

            // 1. Belanja Harian
            if (data.belanjaLogs.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. CATATAN BELANJA HARIAN", 0xFF388E3C.toInt())
                drawTableHeader("Tanggal", "Rincian & Catatan", "Pengeluaran / Modal")
                data.belanjaLogs.forEachIndexed { idx, log ->
                    val desc = if (log.rincian.isNotBlank()) log.rincian else log.catatan.ifBlank { "Belanja harian" }
                    val amountStr = "${Formatters.formatRupiah(log.totalPengeluaran)} (Modal: ${Formatters.formatRupiah(log.modalAwal)})"
                    drawTableRow(log.tanggal, desc, amountStr, idx % 2 == 1)
                }
                currentY += 10f
            }

            // 2. Random / Tersier
            if (data.randomExpenses.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. CATATAN PENGELUARAN RANDOM (TERSIER)", 0xFF8E24AA.toInt())
                drawTableHeader("Tanggal", "Rincian & Catatan", "Pengeluaran / Modal")
                data.randomExpenses.forEachIndexed { idx, log ->
                    val desc = if (log.rincian.isNotBlank()) log.rincian else log.catatan.ifBlank { "Pengeluaran random" }
                    val amountStr = "${Formatters.formatRupiah(log.totalPengeluaran)} (Modal: ${Formatters.formatRupiah(log.modalAwal)})"
                    drawTableRow(log.tanggal, desc, amountStr, idx % 2 == 1)
                }
                currentY += 10f
            }

            // 3. Kebutuhan Anak
            if (data.childExpenses.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. CATATAN BELANJA ANAK", 0xFFE91E63.toInt())
                drawTableHeader("Tanggal", "Rincian & Catatan", "Pengeluaran / Modal")
                data.childExpenses.forEachIndexed { idx, log ->
                    val desc = if (log.rincian.isNotBlank()) log.rincian else log.catatan.ifBlank { "Belanja anak" }
                    val amountStr = "${Formatters.formatRupiah(log.totalPengeluaran)} (Modal: ${Formatters.formatRupiah(log.modalAwal)})"
                    drawTableRow(log.tanggal, desc, amountStr, idx % 2 == 1)
                }
                currentY += 10f
            }

            // 4. Bensin
            if (data.fuelLogs.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. CATATAN ISI BENSIN", 0xFF1976D2.toInt())
                drawTableHeader("Tanggal", "BBM, KM & Liter", "Total Biaya")
                data.fuelLogs.forEachIndexed { idx, log ->
                    val hargaStr = if (log.harga_per_liter > 0) " @ ${Formatters.formatRupiah(log.harga_per_liter.toDouble())}/L" else ""
                    val desc = "${log.jenis_bbm} • KM ${Formatters.formatNumber(log.km_motor)} • ${log.liter}L$hargaStr"
                    val tglStr = sdfDate.format(Date(log.tanggal))
                    drawTableRow(tglStr, desc, Formatters.formatRupiah(log.nominal.toDouble()), idx % 2 == 1)
                }
                currentY += 10f
            }

            // 5. Oli Motor
            if (data.oilLogs.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. CATATAN OLI MOTOR", 0xFFF57C00.toInt())
                drawTableHeader("Tanggal", "Jenis Oli & KM", "Harga")
                data.oilLogs.forEachIndexed { idx, log ->
                    val desc = "${log.jenis_oli} (${log.kapasitas_ml} ml) • KM ${Formatters.formatNumber(log.km_motor)}"
                    val tglStr = sdfDate.format(Date(log.tanggal))
                    drawTableRow(tglStr, desc, Formatters.formatRupiah(log.harga.toDouble()), idx % 2 == 1)
                }
                currentY += 10f
            }

            // 6. Servis Kendaraan
            if (data.serviceLogs.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. CATATAN SERVIS & PERAWATAN", 0xFF7B1FA2.toInt())
                drawTableHeader("Tanggal", "Kategori & Item", "Total Biaya")
                data.serviceLogs.forEachIndexed { idx, log ->
                    val desc = "${log.kategori} - ${log.deskripsi_item}"
                    val tglStr = sdfDate.format(Date(log.tanggal))
                    drawTableRow(tglStr, desc, Formatters.formatRupiah(log.total_biaya.toDouble()), idx % 2 == 1)
                }
                currentY += 10f
            }

            // 7. Listrik kWh
            if (data.electricityLogs.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. CATATAN KWH LISTRIK", 0xFFE65100.toInt())
                drawTableHeader("Tanggal", "Jumlah kWh", "Total Bayar")
                data.electricityLogs.forEachIndexed { idx, log ->
                    val desc = "${Formatters.formatNumber(log.jumlah_kwh)} kWh"
                    val tglStr = sdfDate.format(Date(log.tanggal))
                    drawTableRow(tglStr, desc, Formatters.formatRupiah(log.harga.toDouble()), idx % 2 == 1)
                }
                currentY += 10f
            }

            // 8. Jimpitan & Sosial
            if (data.socialLogs.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. JIMPITAN & SOSIAL", 0xFF00796B.toInt())
                drawTableHeader("Tanggal", "Kategori & Keterangan", "Nominal")
                data.socialLogs.forEachIndexed { idx, log ->
                    val desc = "${log.kategori} ${if (log.keterangan.isNotBlank()) "• ${log.keterangan}" else ""}"
                    val tglStr = sdfDate.format(Date(log.tanggal))
                    drawTableRow(tglStr, desc, Formatters.formatRupiah(log.nominal.toDouble()), idx % 2 == 1)
                }
                currentY += 10f
            }

            // 9. Hutang Warung
            if (data.warungDebts.isNotEmpty()) {
                drawSectionHeader("${sectionIdx++}. HUTANG WARUNG", 0xFFC62828.toInt())
                drawTableHeader("Tanggal", "Nama Warung & Alasan", "Nominal / Status")
                data.warungDebts.forEachIndexed { idx, log ->
                    val desc = "${log.namaWarung} - ${log.alasan}"
                    val statusStr = "${Formatters.formatRupiah(log.nominal)} (${if (log.isLunas) "LUNAS" else "BELUM LUNAS"})"
                    drawTableRow(log.tanggal, desc, statusStr, idx % 2 == 1)
                }
                currentY += 10f
            }

            if (sectionIdx == 1) {
                drawSectionHeader("CATATAN LAPORAN", 0xFF757575.toInt())
                drawTableHeader("Tanggal", "Keterangan", "Status")
                drawTableRow("-", "Tidak ada transaksi tercatat pada periode ini.", "-", false)
            }

            pdfDocument.finishPage(page)

            val fileName = "Laporan_Keuangan_DiptaHome_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            shareFile(context, file, TAG_PDF_MIME, "Laporan Keuangan PDF")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal cetak PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Buka / Bagikan $title")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    // ===============================================
    // OPENXML EXCEL (.XLSX) STRING BUILDERS
    // ===============================================
    private fun getContentTypesXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>""".trimIndent()
    }

    private fun getRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".trimIndent()
    }

    private fun getWorkbookRelsXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>""".trimIndent()
    }

    private fun getWorkbookXml(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Laporan Keuangan" sheetId="1" r:id="rId1"/></sheets>
</workbook>""".trimIndent()
    }

    private fun buildSheetXml(data: FormattedReportData): String {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("<sheetData>")

        var rowIdx = 1

        fun esc(text: String): String {
            return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
        }

        fun addRow(cellA: String, cellB: String = "", cellC: String = "", cellD: String = "", cellE: String = "", cellF: String = "") {
            sb.append("""<row r="$rowIdx">""")
            if (cellA.isNotBlank()) sb.append("""<c r="A$rowIdx" t="inlineStr"><is><t>${esc(cellA)}</t></is></c>""")
            if (cellB.isNotBlank()) sb.append("""<c r="B$rowIdx" t="inlineStr"><is><t>${esc(cellB)}</t></is></c>""")
            if (cellC.isNotBlank()) sb.append("""<c r="C$rowIdx" t="inlineStr"><is><t>${esc(cellC)}</t></is></c>""")
            if (cellD.isNotBlank()) sb.append("""<c r="D$rowIdx" t="inlineStr"><is><t>${esc(cellD)}</t></is></c>""")
            if (cellE.isNotBlank()) sb.append("""<c r="E$rowIdx" t="inlineStr"><is><t>${esc(cellE)}</t></is></c>""")
            if (cellF.isNotBlank()) sb.append("""<c r="F$rowIdx" t="inlineStr"><is><t>${esc(cellF)}</t></is></c>""")
            sb.append("</row>")
            rowIdx++
        }

        // Header
        addRow("DIPTA HOME - LAPORAN KEUANGAN & PENGELUARAN")
        addRow("Periode:", data.periodTitle)
        addRow("Dicetak pada:", SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date()))
        addRow("")

        // Executive Summary
        addRow("--- RINGKASAN EKSEKUTIF ---")
        addRow("Total Modal / Anggaran", Formatters.formatRupiah(data.totalModal))
        addRow("Total Pengeluaran", Formatters.formatRupiah(data.totalPengeluaran))
        addRow("Sisa Saldo Net", Formatters.formatRupiah(data.sisaSaldo))
        addRow("")

        // Belanja Harian
        if (data.belanjaLogs.isNotEmpty()) {
            addRow("=== CATATAN BELANJA HARIAN ===")
            addRow("Tanggal", "Modal Awal", "Sisa Uang", "Total Pengeluaran", "Rincian / Catatan")
            data.belanjaLogs.forEach { log ->
                val rincianFull = "${log.rincian} ${if (log.catatan.isNotBlank()) "[Catatan: ${log.catatan}]" else ""}".trim()
                addRow(log.tanggal, Formatters.formatRupiah(log.modalAwal), Formatters.formatRupiah(log.sisaUang), Formatters.formatRupiah(log.totalPengeluaran), rincianFull)
            }
            addRow("")
        }

        // Random / Tersier
        if (data.randomExpenses.isNotEmpty()) {
            addRow("=== CATATAN PENGELUARAN RANDOM (TERSIER) ===")
            addRow("Tanggal", "Modal Awal", "Sisa Uang", "Total Pengeluaran", "Rincian / Catatan")
            data.randomExpenses.forEach { log ->
                val rincianFull = "${log.rincian} ${if (log.catatan.isNotBlank()) "[Catatan: ${log.catatan}]" else ""}".trim()
                addRow(log.tanggal, Formatters.formatRupiah(log.modalAwal), Formatters.formatRupiah(log.sisaUang), Formatters.formatRupiah(log.totalPengeluaran), rincianFull)
            }
            addRow("")
        }

        // Kebutuhan Anak
        if (data.childExpenses.isNotEmpty()) {
            addRow("=== CATATAN BELANJA ANAK ===")
            addRow("Tanggal", "Modal Awal", "Sisa Uang", "Total Pengeluaran", "Rincian / Catatan")
            data.childExpenses.forEach { log ->
                val rincianFull = "${log.rincian} ${if (log.catatan.isNotBlank()) "[Catatan: ${log.catatan}]" else ""}".trim()
                addRow(log.tanggal, Formatters.formatRupiah(log.modalAwal), Formatters.formatRupiah(log.sisaUang), Formatters.formatRupiah(log.totalPengeluaran), rincianFull)
            }
            addRow("")
        }

        // Bensin
        if (data.fuelLogs.isNotEmpty()) {
            addRow("=== CATATAN ISI BENSIN ===")
            addRow("Tanggal", "KM Motor", "Jenis BBM", "Liter", "Harga/Liter", "Total Biaya")
            data.fuelLogs.forEach { log ->
                val tglStr = sdfDate.format(Date(log.tanggal))
                val hargaStr = if (log.harga_per_liter > 0) Formatters.formatRupiah(log.harga_per_liter.toDouble()) else "-"
                addRow(tglStr, "${Formatters.formatNumber(log.km_motor)} km", log.jenis_bbm, "${log.liter} L", hargaStr, Formatters.formatRupiah(log.nominal.toDouble()))
            }
            addRow("")
        }

        // Oli
        if (data.oilLogs.isNotEmpty()) {
            addRow("=== CATATAN OLI MOTOR ===")
            addRow("Tanggal", "Jenis Oli", "Kapasitas (ml)", "Harga")
            data.oilLogs.forEach { log ->
                val tglStr = sdfDate.format(Date(log.tanggal))
                addRow(tglStr, log.jenis_oli, "${log.kapasitas_ml} ml", Formatters.formatRupiah(log.harga.toDouble()))
            }
            addRow("")
        }

        // Servis
        if (data.serviceLogs.isNotEmpty()) {
            addRow("=== CATATAN SERVIS & PERAWATAN ===")
            addRow("Tanggal", "Kategori", "Deskripsi", "Total Biaya")
            data.serviceLogs.forEach { log ->
                val tglStr = sdfDate.format(Date(log.tanggal))
                addRow(tglStr, log.kategori, log.deskripsi_item, Formatters.formatRupiah(log.total_biaya.toDouble()))
            }
            addRow("")
        }

        // Listrik
        if (data.electricityLogs.isNotEmpty()) {
            addRow("=== CATATAN KWH LISTRIK ===")
            addRow("Tanggal", "Jumlah kWh", "Keterangan", "Total Bayar")
            data.electricityLogs.forEach { log ->
                val tglStr = sdfDate.format(Date(log.tanggal))
                val desc = if (log.is_initial) "Pengisian Awal" else "Token Listrik"
                addRow(tglStr, "${Formatters.formatNumber(log.jumlah_kwh)} kWh", desc, Formatters.formatRupiah(log.harga.toDouble()))
            }
            addRow("")
        }

        // Jimpitan & Sosial
        if (data.socialLogs.isNotEmpty()) {
            addRow("=== JIMPITAN & SOSIAL ===")
            addRow("Tanggal", "Kategori", "Catatan", "Nominal")
            data.socialLogs.forEach { log ->
                val tglStr = sdfDate.format(Date(log.tanggal))
                addRow(tglStr, log.kategori, log.keterangan, Formatters.formatRupiah(log.nominal.toDouble()))
            }
            addRow("")
        }

        // Hutang Warung
        if (data.warungDebts.isNotEmpty()) {
            addRow("=== HUTANG WARUNG ===")
            addRow("Tanggal", "Nama Warung", "Alasan", "Nominal / Status")
            data.warungDebts.forEach { log ->
                val statusStr = "${Formatters.formatRupiah(log.nominal)} (${if (log.isLunas) "LUNAS" else "BELUM LUNAS"})"
                addRow(log.tanggal, log.namaWarung, log.alasan, statusStr)
            }
            addRow("")
        }

        sb.append("</sheetData>")
        sb.append("</worksheet>")
        return sb.toString()
    }
}
