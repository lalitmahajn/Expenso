package com.example.expenso.ui.statement

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.expenso.data.model.ExpenseWithDetails
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StatementCsvHelper {

    fun generateCsv(
        context: Context,
        expenses: List<ExpenseWithDetails>,
        currency: String,
        periodLabel: String,
        scopeLabel: String,
        totalAmount: Double
    ): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "Expenso_Statement_$timestamp.csv")

            val writer = FileWriter(file)
            val genDateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Metadata Header Rows
            writer.append("# EXPENSO FINANCIAL STATEMENT\n")
            writer.append("# Generated: $genDateStr\n")
            writer.append("# Period: $periodLabel\n")
            writer.append("# Filter/Scope: $scopeLabel\n")
            writer.append("# Total Outflow: $currency $totalAmount\n")
            writer.append("# Transactions Count: ${expenses.size}\n\n")

            // Column Headers
            writer.append("ID,Date,Time,Merchant/Title,Amount,Currency,Category,Account,Function/Event,PaymentMethod,Notes\n")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            expenses.forEach { item ->
                val date = Date(item.expense.timestamp)
                val safeTitle = escapeCsv(item.expense.title)
                val safeCat = escapeCsv(item.category?.name ?: "Other")
                val safeAccount = escapeCsv(item.account?.name ?: "")
                val safeEvent = escapeCsv(item.event?.name ?: "")
                val safeNotes = escapeCsv(item.expense.note)

                writer.append("${item.expense.id},")
                    .append("${dateFormat.format(date)},")
                    .append("${timeFormat.format(date)},")
                    .append("$safeTitle,")
                    .append("${item.expense.amount},")
                    .append("$currency,")
                    .append("$safeCat,")
                    .append("$safeAccount,")
                    .append("$safeEvent,")
                    .append("${item.expense.paymentMethod.name},")
                    .append("$safeNotes\n")
            }

            writer.flush()
            writer.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
