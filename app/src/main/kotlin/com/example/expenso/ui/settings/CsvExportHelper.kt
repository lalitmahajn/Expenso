package com.example.expenso.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.expenso.data.model.ExpenseWithDetails
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportHelper {

    fun exportExpensesToCsv(context: Context, expenses: List<ExpenseWithDetails>): Boolean {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "Expenso_Export_$timestamp.csv")

            val writer = FileWriter(file)
            writer.append("ID,Date,Time,Title,Amount,Category,Function/Event,PaymentMethod,Note\n")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            expenses.forEach { item ->
                val date = Date(item.expense.timestamp)
                val safeTitle = escapeCsv(item.expense.title)
                val safeCategory = escapeCsv(item.category?.name ?: "Unknown")
                val safeEvent = escapeCsv(item.event?.name ?: "")
                val safeNote = escapeCsv(item.expense.note)

                writer.append("${item.expense.id},")
                    .append("${dateFormat.format(date)},")
                    .append("${timeFormat.format(date)},")
                    .append("$safeTitle,")
                    .append("${item.expense.amount},")
                    .append("$safeCategory,")
                    .append("$safeEvent,")
                    .append("${item.expense.paymentMethod.name},")
                    .append("$safeNote\n")
            }

            writer.flush()
            writer.close()

            // Launch system sharesheet
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Expenso Expenses Export")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Expenses CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun escapeCsv(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }
}
