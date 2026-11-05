package com.example.expenso.data.model

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CARD("Card"),
    UPI("UPI / Online"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other")
}
