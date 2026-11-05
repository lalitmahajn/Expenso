package com.example.expenso.data.local

import androidx.room.TypeConverter
import com.example.expenso.data.model.AccountType
import com.example.expenso.data.model.PaymentMethod

class Converters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod {
        return try {
            PaymentMethod.valueOf(value)
        } catch (e: Exception) {
            PaymentMethod.CASH
        }
    }

    @TypeConverter
    fun fromAccountType(value: AccountType): String {
        return value.name
    }

    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return try {
            AccountType.valueOf(value)
        } catch (e: Exception) {
            AccountType.BANK
        }
    }
}
