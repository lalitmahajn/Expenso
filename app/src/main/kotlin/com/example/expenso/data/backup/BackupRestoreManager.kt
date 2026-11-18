package com.example.expenso.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.expenso.data.local.AppDatabase
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.AccountType
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseEntity
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.data.model.TransactionType
import com.example.expenso.data.preferences.ThemeMode
import com.example.expenso.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRestoreManager(
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {

    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val accounts = database.accountDao().getAllAccountsSync()
        val categories = database.categoryDao().getAllCategoriesSync()
        val events = database.eventDao().getAllEventsSync()
        val expenses = database.expenseDao().getAllRawExpensesSync()

        val root = JSONObject()
        root.put("formatVersion", 1)
        root.put("app", "Expenso")
        root.put("databaseVersion", 4)
        val now = System.currentTimeMillis()
        root.put("exportedAt", now)
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now))
        root.put("exportedAtFormatted", formattedDate)

        // Preferences
        val prefsObj = JSONObject()
        prefsObj.put("currency", userPreferences.currency.value)
        prefsObj.put("themeMode", userPreferences.themeMode.value.name)
        root.put("preferences", prefsObj)

        // Accounts
        val accountsArray = JSONArray()
        for (acc in accounts) {
            val a = JSONObject()
            a.put("id", acc.id)
            a.put("name", acc.name)
            a.put("type", acc.type.name)
            a.put("balance", acc.balance)
            a.put("colorHex", acc.colorHex)
            a.put("iconName", acc.iconName)
            a.put("accountNumberLast4", acc.accountNumberLast4)
            a.put("updatedAt", acc.updatedAt)
            accountsArray.put(a)
        }
        root.put("accounts", accountsArray)

        // Categories
        val categoriesArray = JSONArray()
        for (cat in categories) {
            val c = JSONObject()
            c.put("id", cat.id)
            c.put("name", cat.name)
            c.put("iconName", cat.iconName)
            c.put("colorHex", cat.colorHex)
            categoriesArray.put(c)
        }
        root.put("categories", categoriesArray)

        // Events
        val eventsArray = JSONArray()
        for (ev in events) {
            val e = JSONObject()
            e.put("id", ev.id)
            e.put("name", ev.name)
            e.put("description", ev.description)
            if (ev.targetBudget != null) e.put("targetBudget", ev.targetBudget) else e.put("targetBudget", JSONObject.NULL)
            e.put("startDate", ev.startDate)
            if (ev.endDate != null) e.put("endDate", ev.endDate) else e.put("endDate", JSONObject.NULL)
            e.put("isArchived", ev.isArchived)
            if (ev.parentEventId != null) e.put("parentEventId", ev.parentEventId) else e.put("parentEventId", JSONObject.NULL)
            if (ev.colorHex != null) e.put("colorHex", ev.colorHex) else e.put("colorHex", JSONObject.NULL)
            eventsArray.put(e)
        }
        root.put("events", eventsArray)

        // Expenses
        val expensesArray = JSONArray()
        for (exp in expenses) {
            val x = JSONObject()
            x.put("id", exp.id)
            x.put("title", exp.title)
            x.put("amount", exp.amount)
            x.put("categoryId", exp.categoryId)
            if (exp.eventId != null) x.put("eventId", exp.eventId) else x.put("eventId", JSONObject.NULL)
            if (exp.accountId != null) x.put("accountId", exp.accountId) else x.put("accountId", JSONObject.NULL)
            if (exp.toAccountId != null) x.put("toAccountId", exp.toAccountId) else x.put("toAccountId", JSONObject.NULL)
            x.put("transactionType", exp.transactionType.name)
            x.put("timestamp", exp.timestamp)
            x.put("paymentMethod", exp.paymentMethod.name)
            x.put("note", exp.note)
            expensesArray.put(x)
        }
        root.put("expenses", expensesArray)

        root.toString(2)
    }

    suspend fun exportBackupToUri(context: Context, uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = generateBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonString.toByteArray(Charsets.UTF_8))
                stream.flush()
            } ?: return@withContext BackupResult.Error("Could not open destination storage file")

            BackupResult.Success("Backup exported successfully to your selected file!")
        } catch (e: Exception) {
            BackupResult.Error("Export failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun createShareableBackupFile(context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val jsonString = generateBackupJson()
            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(backupDir, "Expenso_Backup_$timestamp.json")
            FileOutputStream(file).use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
                out.flush()
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    suspend fun inspectBackupUri(context: Context, uri: Uri): RestorePreviewResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext RestorePreviewResult.Error("Unable to open selected file")

            val jsonString = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
            val root = JSONObject(jsonString)

            val appName = root.optString("app", "")
            if (appName != "Expenso") {
                return@withContext RestorePreviewResult.Error("Invalid backup file: Not an Expenso backup document.")
            }

            val formatVersion = root.optInt("formatVersion", 1)
            val dbVersion = root.optInt("databaseVersion", 4)
            val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())
            val exportedAtFormatted = root.optString(
                "exportedAtFormatted",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(exportedAt))
            )

            val prefsObj = root.optJSONObject("preferences")
            val currency = prefsObj?.optString("currency", "$") ?: "$"
            val themeMode = prefsObj?.optString("themeMode", "SYSTEM") ?: "SYSTEM"

            val accountsCount = root.optJSONArray("accounts")?.length() ?: 0
            val categoriesCount = root.optJSONArray("categories")?.length() ?: 0
            val eventsCount = root.optJSONArray("events")?.length() ?: 0
            val expensesCount = root.optJSONArray("expenses")?.length() ?: 0

            val metadata = BackupMetadata(
                formatVersion = formatVersion,
                app = appName,
                databaseVersion = dbVersion,
                exportedAt = exportedAt,
                exportedAtFormatted = exportedAtFormatted,
                currency = currency,
                themeMode = themeMode,
                accountCount = accountsCount,
                categoryCount = categoriesCount,
                eventCount = eventsCount,
                expenseCount = expensesCount
            )

            RestorePreviewResult.Success(metadata, jsonString)
        } catch (e: Exception) {
            RestorePreviewResult.Error("Failed to parse backup file: ${e.localizedMessage ?: "Invalid JSON syntax"}")
        }
    }

    suspend fun restoreFromRawJson(rawJson: String): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(rawJson)

            // Parse Accounts
            val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
            val accountsList = mutableListOf<AccountEntity>()
            for (i in 0 until accountsArray.length()) {
                val a = accountsArray.getJSONObject(i)
                val typeName = a.optString("type", AccountType.BANK.name)
                val type = try { AccountType.valueOf(typeName) } catch (_: Exception) { AccountType.BANK }
                accountsList.add(
                    AccountEntity(
                        id = a.getLong("id"),
                        name = a.getString("name"),
                        type = type,
                        balance = a.getDouble("balance"),
                        colorHex = a.optString("colorHex", "#2196F3"),
                        iconName = a.optString("iconName", "AccountBalance"),
                        accountNumberLast4 = a.optString("accountNumberLast4", ""),
                        updatedAt = a.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Categories
            val categoriesArray = root.optJSONArray("categories") ?: JSONArray()
            val categoriesList = mutableListOf<CategoryEntity>()
            for (i in 0 until categoriesArray.length()) {
                val c = categoriesArray.getJSONObject(i)
                categoriesList.add(
                    CategoryEntity(
                        id = c.getLong("id"),
                        name = c.getString("name"),
                        iconName = c.optString("iconName", "MoreHoriz"),
                        colorHex = c.optString("colorHex", "#607D8B")
                    )
                )
            }

            // Parse Events
            val eventsArray = root.optJSONArray("events") ?: JSONArray()
            val eventsList = mutableListOf<EventEntity>()
            for (i in 0 until eventsArray.length()) {
                val e = eventsArray.getJSONObject(i)
                val targetBudget = if (e.isNull("targetBudget")) null else e.optDouble("targetBudget")
                val endDate = if (e.isNull("endDate")) null else e.optLong("endDate")
                val parentId = if (e.isNull("parentEventId")) null else e.optLong("parentEventId")
                val colorHex = if (e.isNull("colorHex")) null else e.optString("colorHex")
                eventsList.add(
                    EventEntity(
                        id = e.getLong("id"),
                        name = e.getString("name"),
                        description = e.optString("description", ""),
                        targetBudget = targetBudget,
                        startDate = e.optLong("startDate", System.currentTimeMillis()),
                        endDate = endDate,
                        isArchived = e.optBoolean("isArchived", false),
                        parentEventId = parentId,
                        colorHex = colorHex
                    )
                )
            }

            // Parse Expenses
            val expensesArray = root.optJSONArray("expenses") ?: JSONArray()
            val expensesList = mutableListOf<ExpenseEntity>()
            for (i in 0 until expensesArray.length()) {
                val x = expensesArray.getJSONObject(i)
                val eventId = if (x.isNull("eventId")) null else x.optLong("eventId")
                val accountId = if (x.isNull("accountId")) null else x.optLong("accountId")
                val toAccountId = if (x.isNull("toAccountId")) null else x.optLong("toAccountId")

                val txnTypeName = x.optString("transactionType", TransactionType.EXPENSE.name)
                val txnType = try { TransactionType.valueOf(txnTypeName) } catch (_: Exception) { TransactionType.EXPENSE }

                val pmName = x.optString("paymentMethod", PaymentMethod.CASH.name)
                val pm = try { PaymentMethod.valueOf(pmName) } catch (_: Exception) { PaymentMethod.CASH }

                expensesList.add(
                    ExpenseEntity(
                        id = x.getLong("id"),
                        title = x.getString("title"),
                        amount = x.getDouble("amount"),
                        categoryId = x.getLong("categoryId"),
                        eventId = eventId,
                        accountId = accountId,
                        toAccountId = toAccountId,
                        transactionType = txnType,
                        timestamp = x.getLong("timestamp"),
                        paymentMethod = pm,
                        note = x.optString("note", "")
                    )
                )
            }

            // Atomic database wipe and restore
            database.withTransaction {
                database.expenseDao().deleteAllExpenses()
                database.eventDao().deleteAllEvents()
                database.categoryDao().deleteAllCategories()
                database.accountDao().deleteAllAccounts()

                if (categoriesList.isNotEmpty()) {
                    database.categoryDao().insertAll(categoriesList)
                }
                if (accountsList.isNotEmpty()) {
                    database.accountDao().insertAll(accountsList)
                }

                // Insert parent events first, then child sub-events to respect foreign keys
                val parents = eventsList.filter { it.parentEventId == null }
                val children = eventsList.filter { it.parentEventId != null }
                if (parents.isNotEmpty()) {
                    database.eventDao().insertAll(parents)
                }
                if (children.isNotEmpty()) {
                    database.eventDao().insertAll(children)
                }

                if (expensesList.isNotEmpty()) {
                    database.expenseDao().insertAll(expensesList)
                }
            }

            // Restore preferences
            val prefsObj = root.optJSONObject("preferences")
            if (prefsObj != null) {
                val currency = prefsObj.optString("currency", "")
                if (currency.isNotBlank()) {
                    userPreferences.setCurrency(currency)
                }
                val themeName = prefsObj.optString("themeMode", "")
                if (themeName.isNotBlank()) {
                    try {
                        userPreferences.setThemeMode(ThemeMode.valueOf(themeName))
                    } catch (_: Exception) {}
                }
            }

            RestoreResult.Success(
                accountCount = accountsList.size,
                categoryCount = categoriesList.size,
                eventCount = eventsList.size,
                expenseCount = expensesList.size
            )
        } catch (e: Exception) {
            RestoreResult.Error("Database restoration failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
