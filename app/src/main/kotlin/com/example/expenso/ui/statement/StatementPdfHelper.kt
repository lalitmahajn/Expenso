package com.example.expenso.ui.statement

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseWithDetails
import com.example.expenso.data.model.TransactionType
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

    // Column widths for Section 3 Flat Ledger
    private const val COL_DATE_W = 58f
    private const val COL_TITLE_W = 135f
    private const val COL_EVENT_W = 110f
    private const val COL_CAT_W = 75f
    private const val COL_METHOD_W = 65f
    private const val COL_AMOUNT_W = 80f

    private const val COL_DATE_X = MARGIN
    private const val COL_TITLE_X = COL_DATE_X + COL_DATE_W
    private const val COL_EVENT_X = COL_TITLE_X + COL_TITLE_W
    private const val COL_CAT_X = COL_EVENT_X + COL_EVENT_W
    private const val COL_METHOD_X = COL_CAT_X + COL_CAT_W
    private const val COL_AMOUNT_X = COL_METHOD_X + COL_METHOD_W

    fun generatePdf(
        context: Context,
        expenses: List<ExpenseWithDetails>,
        currency: String,
        periodLabel: String,
        scopeLabel: String,
        totalAmount: Double,
        allEvents: List<EventEntity> = emptyList(),
        selectedEvent: EventEntity? = null,
        subEvents: List<EventEntity> = emptyList()
    ): Uri? {
        val document = PdfDocument()

        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "Expenso_Statement_$timestamp.pdf")

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val genDateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            // Paints
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(30, 41, 59)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 8.5f
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

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            // Page 1 Header Banner
            bgPaint.color = Color.rgb(15, 23, 42) // Deep navy
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 72f, bgPaint)

            // Brand Title
            headerPaint.textSize = 19f
            headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("EXPENSO", MARGIN, 34f, headerPaint)

            headerPaint.textSize = 9f
            headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            headerPaint.color = Color.rgb(148, 163, 184)
            canvas.drawText("COMPREHENSIVE MULTI-SECTION FINANCIAL STATEMENT", MARGIN, 49f, headerPaint)

            // Generated on metadata
            headerPaint.color = Color.rgb(203, 213, 225)
            headerPaint.textSize = 8f
            val metaStr = "Generated: $genDateStr"
            val metaWidth = headerPaint.measureText(metaStr)
            canvas.drawText(metaStr, PAGE_WIDTH - MARGIN - metaWidth, 34f, headerPaint)

            val offlineStr = "100% Offline & Private"
            val offlineWidth = headerPaint.measureText(offlineStr)
            canvas.drawText(offlineStr, PAGE_WIDTH - MARGIN - offlineWidth, 49f, headerPaint)

            // Summary Strip Card
            val summaryTop = 82f
            val summaryHeight = 46f
            bgPaint.color = Color.rgb(241, 245, 249) // Light slate card
            canvas.drawRoundRect(RectF(MARGIN, summaryTop, PAGE_WIDTH - MARGIN, summaryTop + summaryHeight), 6f, 6f, bgPaint)

            // Metric 1: Total Spend
            textPaint.textSize = 7.5f
            textPaint.color = Color.rgb(100, 116, 139)
            canvas.drawText("TOTAL OUTFLOW", MARGIN + 10f, summaryTop + 16f, textPaint)
            boldPaint.textSize = 13f
            boldPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText(Formatters.formatAmount(totalAmount, currency), MARGIN + 10f, summaryTop + 34f, boldPaint)

            // Metric 2: Transaction Count
            canvas.drawText("TRANSACTIONS", MARGIN + 140f, summaryTop + 16f, textPaint)
            boldPaint.textSize = 13f
            canvas.drawText("${expenses.size} items", MARGIN + 140f, summaryTop + 34f, boldPaint)

            // Metric 3: Period Filter
            canvas.drawText("PERIOD", MARGIN + 260f, summaryTop + 16f, textPaint)
            boldPaint.textSize = 10.5f
            canvas.drawText(periodLabel, MARGIN + 260f, summaryTop + 34f, boldPaint)

            // Metric 4: Scope / Account
            canvas.drawText("SCOPE / FILTER", MARGIN + 380f, summaryTop + 16f, textPaint)
            boldPaint.textSize = 9.5f
            val scopeTrunc = truncate(scopeLabel, 20)
            canvas.drawText(scopeTrunc, MARGIN + 380f, summaryTop + 34f, boldPaint)

            var currentY = summaryTop + summaryHeight + 14f

            // -------------------------------------------------------------
            // SECTION 1: EXECUTIVE SUMMARY & CATEGORY BREAKDOWN
            // -------------------------------------------------------------
            drawSectionBar(canvas, currentY, "SECTION 1: EXECUTIVE SUMMARY & CATEGORY BREAKDOWN")
            currentY += 18f

            val categoryBreakdowns = expenses
                .filter { it.expense.transactionType == TransactionType.EXPENSE && it.category != null }
                .groupBy { it.category!! }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.expense.amount }
                    val count = list.size
                    val pct = if (totalAmount > 0) ((sum / totalAmount) * 100).toInt() else 0
                    Triple(cat.name, sum, "$pct% • $count txns")
                }
                .sortedByDescending { it.second }
                .take(6) // Top categories

            if (categoryBreakdowns.isNotEmpty()) {
                val halfW = CONTENT_WIDTH / 2f
                val rowH = 15f
                for (i in categoryBreakdowns.indices step 2) {
                    val item1 = categoryBreakdowns[i]
                    val cat1Str = truncate(item1.first, 18)
                    boldPaint.textSize = 8f
                    boldPaint.color = Color.rgb(30, 41, 59)
                    canvas.drawText("• $cat1Str: ${Formatters.formatAmount(item1.second, currency)} (${item1.third})", MARGIN + 8f, currentY + 11f, boldPaint)

                    if (i + 1 < categoryBreakdowns.size) {
                        val item2 = categoryBreakdowns[i + 1]
                        val cat2Str = truncate(item2.first, 18)
                        canvas.drawText("• $cat2Str: ${Formatters.formatAmount(item2.second, currency)} (${item2.third})", MARGIN + halfW + 8f, currentY + 11f, boldPaint)
                    }
                    currentY += rowH
                }
            } else {
                textPaint.textSize = 8f
                canvas.drawText("No categorized expenses recorded in this period.", MARGIN + 8f, currentY + 12f, textPaint)
                currentY += 16f
            }
            currentY += 8f

            // -------------------------------------------------------------
            // SECTION 2: SUB-EVENTS BREAKDOWN (when event is filtered)
            // -------------------------------------------------------------
            if (selectedEvent != null) {
                val secTitle = if (subEvents.isNotEmpty()) {
                    "SECTION 2: SUB-EVENTS BREAKDOWN (${selectedEvent.name})"
                } else {
                    "SECTION 2: EVENT BUDGET STATUS (${selectedEvent.name})"
                }
                drawSectionBar(canvas, currentY, secTitle)
                currentY += 18f

                // Table headers for sub-events
                val cHeaderH = 16f
                bgPaint.color = Color.rgb(241, 245, 249)
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + cHeaderH, bgPaint)
                boldPaint.textSize = 7.5f
                boldPaint.color = Color.rgb(71, 85, 105)
                canvas.drawText("SUB-EVENT", MARGIN + 8f, currentY + 11f, boldPaint)
                canvas.drawText("MINI-BUDGET", MARGIN + 180f, currentY + 11f, boldPaint)
                canvas.drawText("TOTAL SPENT", MARGIN + 290f, currentY + 11f, boldPaint)
                canvas.drawText("BUDGET STATUS", MARGIN + 400f, currentY + 11f, boldPaint)
                currentY += cHeaderH

                // Direct parent event spend
                val directExpenses = expenses.filter { it.expense.eventId == selectedEvent.id }
                val directTotal = directExpenses.sumOf { it.expense.amount }
                val cRowH = 16f

                // Draw General Parent Row
                bgPaint.color = Color.WHITE
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + cRowH, bgPaint)
                canvas.drawLine(MARGIN, currentY + cRowH, PAGE_WIDTH - MARGIN, currentY + cRowH, linePaint)
                boldPaint.textSize = 8f
                boldPaint.color = Color.rgb(15, 23, 42)
                canvas.drawText("General (${truncate(selectedEvent.name, 16)})", MARGIN + 8f, currentY + 11f, boldPaint)
                textPaint.textSize = 8f
                textPaint.color = Color.rgb(100, 116, 139)
                canvas.drawText("-", MARGIN + 180f, currentY + 11f, textPaint)
                canvas.drawText(Formatters.formatAmount(directTotal, currency), MARGIN + 290f, currentY + 11f, boldPaint)
                canvas.drawText("${directExpenses.size} direct items", MARGIN + 400f, currentY + 11f, textPaint)
                currentY += cRowH

                // Draw each Sub-Event
                subEvents.forEachIndexed { idx, subEvent ->
                    val cExpenses = expenses.filter { it.expense.eventId == subEvent.id }
                    val cSpent = cExpenses.sumOf { it.expense.amount }
                    val isAlt = idx % 2 == 1
                    bgPaint.color = if (isAlt) Color.rgb(248, 250, 252) else Color.WHITE
                    canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + cRowH, bgPaint)
                    canvas.drawLine(MARGIN, currentY + cRowH, PAGE_WIDTH - MARGIN, currentY + cRowH, linePaint)

                    boldPaint.textSize = 8f
                    boldPaint.color = Color.rgb(15, 23, 42)
                    canvas.drawText("• ${truncate(subEvent.name, 22)}", MARGIN + 8f, currentY + 11f, boldPaint)

                    val budgetStr = subEvent.targetBudget?.let { Formatters.formatAmount(it, currency) } ?: "No Limit"
                    textPaint.textSize = 8f
                    textPaint.color = Color.rgb(51, 65, 85)
                    canvas.drawText(budgetStr, MARGIN + 180f, currentY + 11f, textPaint)
                    canvas.drawText(Formatters.formatAmount(cSpent, currency), MARGIN + 290f, currentY + 11f, boldPaint)

                    if (subEvent.targetBudget != null && subEvent.targetBudget > 0) {
                        val isOver = cSpent > subEvent.targetBudget
                        val statusStr = if (isOver) {
                            "OVER by ${Formatters.formatAmount(cSpent - subEvent.targetBudget, currency)}"
                        } else {
                            "Left: ${Formatters.formatAmount(subEvent.targetBudget - cSpent, currency)}"
                        }
                        boldPaint.color = if (isOver) Color.rgb(220, 38, 38) else Color.rgb(13, 148, 136)
                        canvas.drawText(statusStr, MARGIN + 400f, currentY + 11f, boldPaint)
                    } else {
                        textPaint.color = Color.rgb(100, 116, 139)
                        canvas.drawText("${cExpenses.size} items", MARGIN + 400f, currentY + 11f, textPaint)
                    }

                    currentY += cRowH
                }
                currentY += 8f
            }

            // -------------------------------------------------------------
            // SECTION 3: CHRONOLOGICAL FLAT LEDGER WITH EVENT • SUB-EVENT
            // -------------------------------------------------------------
            drawSectionBar(canvas, currentY, "SECTION 3: CHRONOLOGICAL TRANSACTION LEDGER")
            currentY += 18f

            currentY = drawTableHeader(canvas, currentY, currency, boldPaint, bgPaint)

            val rowHeight = 20f
            var rowIndex = 0
            val totalRows = expenses.size

            while (rowIndex < totalRows) {
                // If row overflows current page, break to new page
                if (currentY + rowHeight > PAGE_HEIGHT - 35f) {
                    drawFooter(canvas, pageNumber, linePaint, textPaint)
                    document.finishPage(page)
                    pageNumber++

                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas

                    currentY = drawSubsequentPageHeader(canvas, pageNumber, periodLabel, headerPaint, bgPaint)
                    currentY = drawTableHeader(canvas, currentY, currency, boldPaint, bgPaint)
                }

                val item = expenses[rowIndex]
                val isAlt = rowIndex % 2 == 1
                if (isAlt) {
                    bgPaint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, bgPaint)
                }

                // Divider line
                canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, linePaint)

                // Date
                textPaint.textSize = 7.5f
                textPaint.color = Color.rgb(71, 85, 105)
                val dateStr = dateFormat.format(Date(item.expense.timestamp))
                canvas.drawText(dateStr, COL_DATE_X + 4f, currentY + 13f, textPaint)

                // Title / Merchant
                boldPaint.textSize = 8f
                boldPaint.color = Color.rgb(15, 23, 42)
                val safeTitle = truncate(item.expense.title, 20)
                canvas.drawText(safeTitle, COL_TITLE_X + 4f, currentY + 13f, boldPaint)

                // Event • Sub-Event Labeling
                textPaint.textSize = 7.5f
                val eventLabel = if (item.event != null) {
                    if (item.event.parentEventId != null) {
                        val parent = allEvents.find { it.id == item.event.parentEventId }?.name ?: "Event"
                        "$parent • ${item.event.name}"
                    } else {
                        item.event.name
                    }
                } else {
                    "-"
                }
                textPaint.color = if (eventLabel != "-") Color.rgb(37, 99, 235) else Color.rgb(148, 163, 184)
                canvas.drawText(truncate(eventLabel, 18), COL_EVENT_X + 4f, currentY + 13f, textPaint)

                // Category
                textPaint.textSize = 7.5f
                textPaint.color = Color.rgb(51, 65, 85)
                val catName = truncate(item.category?.name ?: "Other", 12)
                canvas.drawText(catName, COL_CAT_X + 4f, currentY + 13f, textPaint)

                // Account / Method
                val accName = item.account?.name ?: item.expense.paymentMethod.name
                canvas.drawText(truncate(accName, 10), COL_METHOD_X + 4f, currentY + 13f, textPaint)

                // Amount
                boldPaint.textSize = 8f
                boldPaint.color = Color.rgb(15, 23, 42)
                val amtStr = Formatters.formatAmount(item.expense.amount, currency)
                val amtW = boldPaint.measureText(amtStr)
                canvas.drawText(amtStr, PAGE_WIDTH - MARGIN - amtW - 4f, currentY + 13f, boldPaint)

                currentY += rowHeight
                rowIndex++
            }

            // Finish the last page
            drawFooter(canvas, pageNumber, linePaint, textPaint)
            document.finishPage(page)

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

    private fun drawSectionBar(canvas: Canvas, y: Float, title: String) {
        val barH = 14f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59) // Slate-800
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + barH), 3f, 3f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title, MARGIN + 8f, y + 10f, textPaint)
    }

    private fun drawTableHeader(
        canvas: Canvas,
        currentY: Float,
        currency: String,
        boldPaint: Paint,
        bgPaint: Paint
    ): Float {
        val thHeight = 18f
        bgPaint.color = Color.rgb(226, 232, 240) // Slate-200
        canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + thHeight, bgPaint)

        boldPaint.textSize = 7.5f
        boldPaint.color = Color.rgb(51, 65, 85)
        canvas.drawText("DATE", COL_DATE_X + 4f, currentY + 12f, boldPaint)
        canvas.drawText("TITLE / MERCHANT", COL_TITLE_X + 4f, currentY + 12f, boldPaint)
        canvas.drawText("EVENT • SUB-EVENT", COL_EVENT_X + 4f, currentY + 12f, boldPaint)
        canvas.drawText("CATEGORY", COL_CAT_X + 4f, currentY + 12f, boldPaint)
        canvas.drawText("METHOD", COL_METHOD_X + 4f, currentY + 12f, boldPaint)

        val amtHeader = "AMOUNT ($currency)"
        val amtHeaderW = boldPaint.measureText(amtHeader)
        canvas.drawText(amtHeader, PAGE_WIDTH - MARGIN - amtHeaderW - 4f, currentY + 12f, boldPaint)

        return currentY + thHeight
    }

    private fun drawSubsequentPageHeader(
        canvas: Canvas,
        pageNumber: Int,
        periodLabel: String,
        headerPaint: Paint,
        bgPaint: Paint
    ): Float {
        bgPaint.color = Color.rgb(15, 23, 42)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 30f, bgPaint)

        headerPaint.textSize = 10f
        headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        headerPaint.color = Color.WHITE
        canvas.drawText("EXPENSO FINANCIAL STATEMENT", MARGIN, 19f, headerPaint)

        headerPaint.textSize = 8f
        headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        headerPaint.color = Color.rgb(148, 163, 184)
        val contStr = "$periodLabel • Page $pageNumber"
        val contW = headerPaint.measureText(contStr)
        canvas.drawText(contStr, PAGE_WIDTH - MARGIN - contW, 19f, headerPaint)

        return 42f
    }

    private fun drawFooter(
        canvas: Canvas,
        pageNumber: Int,
        linePaint: Paint,
        textPaint: Paint
    ) {
        val footerY = PAGE_HEIGHT - 24f
        canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, linePaint)

        textPaint.textSize = 7f
        textPaint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Expenso • 100% Offline & Private Financial Record", MARGIN, footerY + 12f, textPaint)

        val pageStr = "Page $pageNumber"
        val pageW = textPaint.measureText(pageStr)
        canvas.drawText(pageStr, PAGE_WIDTH - MARGIN - pageW, footerY + 12f, textPaint)
    }

    private fun truncate(str: String, maxChars: Int): String {
        return if (str.length <= maxChars) str else str.take(maxChars - 2) + ".."
    }
}

typealias PdfStatementGenerator = StatementPdfHelper
