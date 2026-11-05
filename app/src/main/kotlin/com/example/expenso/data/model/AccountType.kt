package com.example.expenso.data.model

enum class AccountType(val displayName: String, val defaultIcon: String) {
    BANK("Bank Account", "AccountBalance"),
    CASH("Cash in Hand", "Payments"),
    WALLET("Digital Wallet / UPI", "Wallet"),
    CREDIT_CARD("Credit Card", "CreditCard"),
    SAVINGS("Savings & Investment", "Savings"),
    OTHER("Other Account", "AccountBalanceWallet")
}
