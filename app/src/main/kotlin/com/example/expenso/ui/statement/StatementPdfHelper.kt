package com.example.expenso.ui.statement

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.ui.common.Formatters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StatementPdfHelper {

    private const val PAGE_WIDTH = 595 // A4 width in pt (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in pt (72 dpi)
    private const val MARGIN = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2) // 523f

    // Column widths
    private const val COL_DATE_W = 65f
    private const val COL_TITLE_W = 168f
    private const val COL_CAT_W = 95f
    private const val COL_METHOD_W = 105f
    private const val COL_AMOUNT_W = 90f

    private val COL_DATE_X = MARGIN
    private val COL_TITLE_X = COL_DATE_X + COL_DATE_W
    private val COL_CAT_X = COL_TITLE_X + COL_TITLE_W
    private val COL_METHOD_X = COL_CAT_X + COL_CAT_W
    private val COL_AMOUNT_X = COL_METHOD_X + COL_METHOD_W

    fun generatePdf(
        context: Context,
        expenses: List<ExpenseWithDetails>,
        currency: String,
        periodLabel: String,
        scopeLabel: String,
        totalAmount: Double
    ): Uri? {
        val document = PdfDocument()

        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "Expenso_Statement_$timestamp.pdf")

            val rowsPerPageFirst = 20
            val rowsPerPageSubsequent = 27

            val totalRows = expenses.size
            val totalPages = if (totalRows <= rowsPerPageFirst) {
                1
            } else {
                1 + Math.ceil((totalRows - rowsPerPageFirst).toDouble() / rowsPerPageSubsequent).toInt()
            }

            var rowIndex = 0
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val genDateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            // Paints
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
            }

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 0.75f
            }

            for (pageNumber in 1..totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                var currentY = 0f

                if (pageNumber == 1) {
                    // Page 1 Full Header Banner
                    bgPaint.color = Color.rgb(15, 23, 42) // Deep navy
                    canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 75f, bgPaint)

                    // Brand Title
                    headerPaint.textSize = 20f
                    headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("EXPENSO", MARGIN, 36f, headerPaint)

                    headerPaint.textSize = 9.5f
                    headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    headerPaint.color = Color.rgb(148, 163, 184)
                    canvas.drawText("OFFICIAL PERSONAL FINANCIAL STATEMENT", MARGIN, 52f, headerPaint)

                    // Generated on metadata
                    headerPaint.color = Color.rgb(203, 213, 225)
                    headerPaint.textSize = 8.5f
                    val metaStr = "Generated: $genDateStr"
                    val metaWidth = headerPaint.measureText(metaStr)
                    canvas.drawText(metaStr, PAGE_WIDTH - MARGIN - metaWidth, 36f, headerPaint)

                    val offlineStr = "100% Offline • Private"
                    val offlineWidth = headerPaint.measureText(offlineStr)
                    canvas.drawText(offlineStr, PAGE_WIDTH - MARGIN - offlineWidth, 52f, headerPaint)

                    // Summary Strip Card
                    val summaryTop = 88f
                    val summaryHeight = 52f
                    bgPaint.color = Color.rgb(241, 245, 249) // Light slate card
                    canvas.drawRoundRect(RectF(MARGIN, summaryTop, PAGE_WIDTH - MARGIN, summaryTop + summaryHeight), 6f, 6f, bgPaint)

                    // Metric 1: Total Spend
                    textPaint.textSize = 8f
                    textPaint.color = Color.rgb(100, 116, 139)
                    canvas.drawText("TOTAL OUTFLOW", MARGIN + 12f, summaryTop + 18f, textPaint)
                    boldPaint.textSize = 14f
                    boldPaint.color = Color.rgb(15, 23, 42)
                    canvas.drawText(Formatters.formatAmount(totalAmount, currency), MARGIN + 12f, summaryTop + 38f, boldPaint)

                    // Metric 2: Transaction Count
                    canvas.drawText("TRANSACTIONS", MARGIN + 145f, summaryTop + 18f, textPaint)
                    boldPaint.textSize = 14f
                    canvas.drawText("$totalRows items", MARGIN + 145f, summaryTop + 38f, boldPaint)

                    // Metric 3: Period Filter
                    canvas.drawText("PERIOD", MARGIN + 265f, summaryTop + 18f, textPaint)
                    boldPaint.textSize = 11f
                    canvas.drawText(periodLabel, MARGIN + 265f, summaryTop + 37f, boldPaint)

                    // Metric 4: Scope / Account
                    canvas.drawText("SCOPE / FILTER", MARGIN + 385f, summaryTop + 18f, textPaint)
                    boldPaint.textSize = 10f
                    val scopeTrunc = truncate(scopeLabel, 18)
                    canvas.drawText(scopeTrunc, MARGIN + 385f, summaryTop + 37f, boldPaint)

                    currentY = summaryTop + summaryHeight + 16f
                } else {
                    // Subsequent page compact header
                    bgPaint.color = Color.rgb(15, 23, 42)
                    canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 34f, bgPaint)

                    headerPaint.textSize = 11f
                    headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    headerPaint.color = Color.WHITE
                    canvas.drawText("EXPENSO FINANCIAL STATEMENT", MARGIN, 22f, headerPaint)

                    headerPaint.textSize = 8.5f
                    headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    headerPaint.color = Color.rgb(148, 163, 184)
                    val contStr = "$periodLabel • Page $pageNumber of $totalPages"
                    val contW = headerPaint.measureText(contStr)
                    canvas.drawText(contStr, PAGE_WIDTH - MARGIN - contW, 22f, headerPaint)

                    currentY = 48f
                }

                // Table Header
                val thHeight = 22f
                bgPaint.color = Color.rgb(226, 232, 240) // Slate-200
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + thHeight, bgPaint)

                boldPaint.textSize = 8f
                boldPaint.color = Color.rgb(51, 65, 85)
                canvas.drawText("DATE", COL_DATE_X + 6f, currentY + 14f, boldPaint)
                canvas.drawText("TITLE / MERCHANT", COL_TITLE_X + 6f, currentY + 14f, boldPaint)
                canvas.drawText("CATEGORY", COL_CAT_X + 6f, currentY + 14f, boldPaint)
                canvas.drawText("ACCOUNT / METHOD", COL_METHOD_X + 6f, currentY + 14f, boldPaint)

                val amtHeader = "AMOUNT ($currency)"
                val amtHeaderW = boldPaint.measureText(amtHeader)
                canvas.drawText(amtHeader, PAGE_WIDTH - MARGIN - amtHeaderW - 6f, currentY + 14f, boldPaint)

                currentY += thHeight

                // Draw Rows for this page
                val rowsForThisPage = if (pageNumber == 1) rowsPerPageFirst else rowsPerPageSubsequent
                val rowHeight = 22f

                var rowsDrawn = 0
                while (rowIndex < totalRows && rowsDrawn < rowsForThisPage) {
                    val item = expenses[rowIndex]
                    val isAlt = (rowsDrawn % 2 == 1)

                    if (isAlt) {
                        bgPaint.color = Color.rgb(248, 250, 252)
                        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, bgPaint)
                    }

                    // Divider line
                    canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, linePaint)

                    // Date & Time
                    textPaint.textSize = 8f
                    textPaint.color = Color.rgb(71, 85, 105)
                    val dateStr = dateFormat.format(Date(item.expense.timestamp))
                    canvas.drawText(dateStr, COL_DATE_X + 6f, currentY + 14f, textPaint)

                    // Title
                    boldPaint.textSize = 8.5f
                    boldPaint.color = Color.rgb(15, 23, 42)
                    var title = item.expense.title
                    if (item.event != null) {
                        title += " [${item.event.name}]"
                    }
                    val safeTitle = truncate(title, 26)
                    canvas.drawText(safeTitle, COL_TITLE_X + 6f, currentY + 14f, boldPaint)

                    // Category
                    textPaint.textSize = 8f
                    textPaint.color = Color.rgb(51, 65, 85)
                    val catName = truncate(item.category?.name ?: "Other", 16)
                    canvas.drawText(catName, COL_CAT_X + 6f, currentY + 14f, textPaint)

                    // Account / Method
                    val accName = item.account?.name ?: item.expense.paymentMethod.name
                    val safeAcc = truncate(accName, 18)
                    canvas.drawText(safeAcc, COL_METHOD_X + 6f, currentY + 14f, textPaint)

                    // Amount
                    boldPaint.textSize = 8.5f
                    boldPaint.color = Color.rgb(15, 23, 42)
                    val amtStr = Formatters.formatAmount(item.expense.amount, currency)
                    val amtW = boldPaint.measureText(amtStr)
                    canvas.drawText(amtStr, PAGE_WIDTH - MARGIN - amtW - 6f, currentY + 14f, boldPaint)

                    currentY += rowHeight
                    rowIndex++
                    rowsDrawn++
                }

                // Page Footer
                val footerY = PAGE_HEIGHT - 28f
                canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, linePaint)

                textPaint.textSize = 7.5f
                textPaint.color = Color.rgb(148, 163, 184)
                canvas.drawText("Expenso • 100% Offline & Private On-Device Financial Record", MARGIN, footerY + 14f, textPaint)

                val pageStr = "Page $pageNumber of $totalPages"
                val pageW = textPaint.measureText(pageStr)
                canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pageW, footerY + 14f, textPaint)

                document.finishPage(page)
            }

            val fos = FileOutputStream(file)
            document.writeTo(fos)
            fos.flush()
            fos.close()
            document.close()

            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                document.close()
            } catch (_: Exception) {}
            return null
        }
    }

    private fun truncate(str: String, maxChars: Int): String {
        return if (str.length <= maxChars) str else str.take(maxChars - 2) + ".."
    }
}
