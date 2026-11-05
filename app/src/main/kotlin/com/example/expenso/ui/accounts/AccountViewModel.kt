package com.example.expenso.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.AccountType
import com.example.expenso.data.preferences.UserPreferences
import com.example.expenso.data.repository.AccountRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountsUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val currency: String = "$",
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val netBalance: Double = 0.0,
    val isLoading: Boolean = false
)

class AccountViewModel(
    private val accountRepository: AccountRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = combine(
        accountRepository.allAccounts,
        userPreferences.currency
    ) { accounts, currency ->
        var assets = 0.0
        var liabilities = 0.0

        accounts.forEach { account ->
            if (account.type == AccountType.CREDIT_CARD) {
                // For credit cards, negative balance is debt, positive is credit
                if (account.balance < 0) {
                    liabilities += kotlin.math.abs(account.balance)
                } else {
                    assets += account.balance
                }
            } else {
                if (account.balance >= 0) {
                    assets += account.balance
                } else {
                    liabilities += kotlin.math.abs(account.balance)
                }
            }
        }

        AccountsUiState(
            accounts = accounts,
            currency = currency,
            totalAssets = assets,
            totalLiabilities = liabilities,
            netBalance = assets - liabilities,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountsUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            accountRepository.ensureDefaultAccounts()
        }
    }

    fun updateBalance(accountId: Long, newBalance: Double) {
        viewModelScope.launch {
            accountRepository.setAccountBalance(accountId, newBalance)
        }
    }

    fun adjustBalanceBy(accountId: Long, amount: Double) {
        viewModelScope.launch {
            accountRepository.creditAccount(accountId, amount)
        }
    }

    fun createAccount(
        name: String,
        type: AccountType,
        initialBalance: Double,
        colorHex: String,
        accountNumberLast4: String = ""
    ) {
        viewModelScope.launch {
            val account = AccountEntity(
                name = name.trim(),
                type = type,
                balance = initialBalance,
                colorHex = colorHex,
                iconName = type.defaultIcon,
                accountNumberLast4 = accountNumberLast4.trim()
            )
            accountRepository.insertAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
        }
    }

    companion object {
        fun provideFactory(
            accountRepository: AccountRepository,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AccountViewModel(accountRepository, userPreferences) as T
            }
        }
    }
}
