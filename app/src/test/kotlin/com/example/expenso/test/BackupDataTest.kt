package com.example.expenso.test

import com.example.expenso.data.model.AccountType
import com.example.expenso.data.model.PaymentMethod
import com.example.expenso.data.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BackupDataTest {

    @Test
    fun testBackupJsonStructureAndParsing() {
        val root = JSONObject().apply {
            put("formatVersion", 1)
            put("app", "Expenso")
            put("databaseVersion", 4)
            put("exportedAt", 1731750000000L)
            put("exportedAtFormatted", "2026-11-16 16:00:00")

            val prefs = JSONObject().apply {
                put("currency", "₹")
                put("themeMode", "DARK")
            }
            put("preferences", prefs)

            val accounts = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 1L)
                    put("name", "HDFC Bank")
                    put("type", AccountType.BANK.name)
                    put("balance", 25000.0)
                    put("colorHex", "#2196F3")
                    put("iconName", "AccountBalance")
                    put("accountNumberLast4", "1234")
                    put("updatedAt", 1731750000000L)
                })
            }
            put("accounts", accounts)

            val categories = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 1L)
                    put("name", "Groceries")
                    put("iconName", "ShoppingCart")
                    put("colorHex", "#4CAF50")
                })
            }
            put("categories", categories)

            val events = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 10L)
                    put("name", "Trip")
                    put("description", "Friends Trip")
                    put("targetBudget", 15000.0)
                    put("startDate", 1731750000000L)
                    put("endDate", JSONObject.NULL)
                    put("isArchived", false)
                    put("parentEventId", JSONObject.NULL)
                    put("colorHex", JSONObject.NULL)
                })
            }
            put("events", events)

            val expenses = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 100L)
                    put("title", "Supermarket")
                    put("amount", 1250.50)
                    put("categoryId", 1L)
                    put("eventId", 10L)
                    put("accountId", 1L)
                    put("toAccountId", JSONObject.NULL)
                    put("transactionType", TransactionType.EXPENSE.name)
                    put("timestamp", 1731751000000L)
                    put("paymentMethod", PaymentMethod.CARD.name)
                    put("note", "Weekly shopping")
                })
            }
            put("expenses", expenses)
        }

        val jsonString = root.toString(2)
        assertNotNull(jsonString)

        // Parse back
        val parsed = JSONObject(jsonString)
        assertEquals("Expenso", parsed.getString("app"))
        assertEquals(1, parsed.getInt("formatVersion"))
        assertEquals(4, parsed.getInt("databaseVersion"))

        val parsedPrefs = parsed.getJSONObject("preferences")
        assertEquals("₹", parsedPrefs.getString("currency"))
        assertEquals("DARK", parsedPrefs.getString("themeMode"))

        val parsedAccounts = parsed.getJSONArray("accounts")
        assertEquals(1, parsedAccounts.length())
        val acc0 = parsedAccounts.getJSONObject(0)
        assertEquals("HDFC Bank", acc0.getString("name"))
        assertEquals(25000.0, acc0.getDouble("balance"), 0.001)

        val parsedExpenses = parsed.getJSONArray("expenses")
        assertEquals(1, parsedExpenses.length())
        val exp0 = parsedExpenses.getJSONObject(0)
        assertEquals(1250.50, exp0.getDouble("amount"), 0.001)
        assertEquals("Weekly shopping", exp0.getString("note"))
    }
}
