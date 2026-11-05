package com.example.expenso

import androidx.compose.ui.graphics.Color
import com.example.expenso.ui.common.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun testFormatAmount() {
        assertEquals("$150.00", Formatters.formatAmount(150.0, "$"))
        assertEquals("₹2,450.50", Formatters.formatAmount(2450.50, "₹"))
        assertEquals("€0.00", Formatters.formatAmount(0.0, "€"))
    }

    @Test
    fun testParseColor() {
        val color = Formatters.parseColor("#4CAF50")
        assertEquals(Color(0xFF4CAF50), color)
    }

    @Test
    fun testCategoryIconOptionsNotEmpty() {
        assert(Formatters.AVAILABLE_CATEGORY_ICONS.isNotEmpty())
        assert(Formatters.AVAILABLE_CATEGORY_COLORS.isNotEmpty())
    }

    @Test
    fun testGetCategoryIconFallback() {
        val icon = Formatters.getCategoryIcon("NonExistentIcon")
        org.junit.Assert.assertNotNull(icon)
        assertEquals("Filled.MoreHoriz", icon.name)
    }
}
