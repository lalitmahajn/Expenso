# Expenso - Project Progress & Architecture Ledger

**Repository:** `https://github.com/lalitmahajn/Expenso.git`  
**Account:** `lalitmahajn`  
**Last Updated:** November 5, 2026  
**Status:** Alpha / Functional MVP deployed & verified on Android Emulator (`medium_phone:5554`)

---

## 1. Executive Summary & Stack Overview

**Expenso** is a modern, offline-first personal expense tracker built with native Android architecture. It emphasizes **dense, information-rich UI layouts**, flexible multi-account rough balance tracking, and event/function-based budgeting.

### Core Technology Stack
- **Language:** Kotlin 2.0.21 (JVM 17)
- **UI Framework:** Jetpack Compose with Material 3 design system
- **Architecture:** Clean Architecture / MVVM (Model-View-ViewModel) with Kotlin Coroutines & StateFlow
- **Persistence:** Android Room Database 2.6.1 (Local SQLite, multi-table schema with Foreign Keys and migration paths)
- **Storage:** Offline-first, Zero third-party cloud dependency; Local DataStore for user preferences (currency symbol, dark mode)
- **Exporting:** Local CSV file generation via Android `FileProvider` / Storage Access Framework

---

## 2. Completed Features & Capabilities

### A. High-Density Expense Tracking (`ExpensesScreen.kt`, `AddExpenseBottomSheet.kt`)
- **Dense Transaction Rows:** 44dp compact item heights displaying category icon badge, expense title, function/event pill tag, payment method, date/time, and colored amount.
- **Dynamic Category & Search Filters:** Filter by category chips, text search (merchant/note), or link to an event.
- **Custom Categories:** Built-in standard categories (Food, Bills, Shopping, Travel, etc.) plus user-created custom categories with customizable color chips.
- **Expense Logging Modal:** Input amount, merchant/title, category, optional event/occasion link, payment method (Cash, Card, UPI, Bank Transfer), and **optional account debiting**.

### B. Dedicated Function & Event Budgeting (`EventsScreen.kt`, `EventDetailScreen.kt`)
- **Full-Screen Event Detail:** Replaced bottom sheets with a dedicated full-screen view for granular event analysis.
- **Budget Metrics Strip:** Real-time metrics showing Total Spent, Target Budget, Remaining / Over-budget indicator, Avg/Item, and Highest Expense.
- **Interactive Breakdown Filters:**
  - One-tap category chips displaying spending percentage (e.g. `Shopping 100%`).
  - One-tap payment method breakdown pills (e.g. `Card 72%`, `UPI 27%`).
  - Direct list filtering upon clicking any breakdown pill.
- **Sort Controls:** Fast sorting by Newest First, Oldest First, Highest Amount, or Lowest Amount.
- **Direct Event Expense Addition:** Floating Action Button (+) directly tags newly logged transactions to the active event.

### C. Account Balances & Rough Tracking (`AccountsScreen.kt`, `AccountViewModel.kt`)
- **Design Philosophy:** *"Flexible rough balance tracker"*. Designed so users do not have to cross-check bank apps every day; balances can be adjusted anytime.
- **Net Balance Banner:** Top banner displaying:
  - **Total Net Balance** (Assets minus Credit Card dues)
  - **Total Assets** (Bank accounts + Cash + UPI Wallets)
  - **Credit Card Dues / Liabilities** (highlighted in red)
- **Account Types Supported:**
  - `BANK`: Checking / Savings accounts
  - `CASH`: Cash in hand
  - `WALLET`: UPI / Digital Wallets (Paytm, PhonePe, Google Pay, Amazon Pay)
  - `CREDIT_CARD`: Credit card with negative liability accounting
  - `OTHER`: Custom assets or funds
- **Quick-Adjust Balance Dialog:**
  - Direct numeric editing of current balance.
  - One-tap increment/decrement chips (`+500`, `+1,000`, `+5,000`, `-500`, etc.) for rapid synchronization.
- **Automatic Expense Debiting:**
  - When recording an expense, users can optionally select an account to debit.
  - Normal accounts (Bank, Cash, Wallet) are reduced by the expense amount.
  - Credit Card balances reflect increased dues.
  - Deleting an expense automatically refunds the debited account.

### D. Settings & Data Portability (`SettingsScreen.kt`)
- **Currency Preferences:** Switch between `₹` (INR), `$` (USD), `€` (EUR), `£` (GBP), and `¥` (JPY).
- **Custom Category Manager:** Add custom categories with color picker; delete custom categories.
- **CSV Data Export:** Export full expense history to a shareable `.csv` file.
- **Sample Data Seeding:** One-tap demo dataset for quick evaluation.

---

## 3. Database Schema & Architecture

### Database Name: `expenso_database`
**Current Version:** `2`

#### Entities
1. **`categories`** (`CategoryEntity`)
   - `id: Long` (Auto-generate PK)
   - `name: String`
   - `colorHex: String`
   - `iconName: String`
   - `isCustom: Boolean`

2. **`events`** (`EventEntity`)
   - `id: Long` (Auto-generate PK)
   - `name: String`
   - `description: String?`
   - `targetBudget: Double?`
   - `isArchived: Boolean`
   - `createdAt: Long`

3. **`accounts`** (`AccountEntity`)
   - `id: Long` (Auto-generate PK)
   - `name: String`
   - `type: AccountType` (BANK, CASH, WALLET, CREDIT_CARD, OTHER)
   - `balance: Double`
   - `accountNumberLast4: String?`
   - `colorHex: String`
   - `isArchived: Boolean`

4. **`expenses`** (`ExpenseEntity`)
   - `id: Long` (Auto-generate PK)
   - `title: String`
   - `amount: Double`
   - `categoryId: Long` (FK -> `categories.id` ON DELETE RESTRICT)
   - `eventId: Long?` (FK -> `events.id` ON DELETE SET NULL)
   - `accountId: Long?` (FK -> `accounts.id` ON DELETE SET NULL)
   - `paymentMethod: PaymentMethod` (CASH, CARD, UPI, BANK_TRANSFER, OTHER)
   - `timestamp: Long`
   - `notes: String?`

#### Migrations
- **`MIGRATION_1_2`**:
  - Creates the `accounts` table.
  - Recreates `expenses` as `expenses_new` with foreign keys to `accounts(id)`, indices on `categoryId`, `eventId`, and `accountId`.
  - Migrates existing data and replaces `expenses`.
  - Seeds default accounts: `Main Bank Account` (₹35,000), `Cash in Hand` (₹4,500), `UPI Wallet` (₹2,850), and `Credit Card` (-₹8,200).

---

## 4. Project Directory Map

```
d:\Learning\Project x
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── kotlin/com/example/expenso/
│   │   │       ├── ExpensoApp.kt                  # Application class & DI container init
│   │   │       ├── MainActivity.kt                # Single activity entry point
│   │   │       ├── data/
│   │   │       │   ├── local/
│   │   │       │   │   ├── AccountDao.kt          # Room DAO for accounts
│   │   │       │   │   ├── AppDatabase.kt         # Database instance & MIGRATION_1_2
│   │   │       │   │   ├── CategoryDao.kt         # Room DAO for categories
│   │   │       │   │   ├── Converters.kt          # Room TypeConverters
│   │   │       │   │   ├── EventDao.kt            # Room DAO for events
│   │   │       │   │   └── ExpenseDao.kt          # Room DAO for expenses
│   │   │       │   ├── model/
│   │   │       │   │   ├── AccountEntity.kt       # Account Room entity
│   │   │       │   │   ├── AccountType.kt         # Account type enum
│   │   │       │   │   ├── CategoryEntity.kt      # Category Room entity
│   │   │       │   │   ├── EventEntity.kt         # Event Room entity
│   │   │       │   │   ├── ExpenseEntity.kt       # Expense Room entity
│   │   │       │   │   ├── ExpenseWithDetails.kt  # Composite Room relation model
│   │   │       │   │   ├── PaymentMethod.kt       # Payment method enum
│   │   │       │   │   └── SummaryModels.kt       # Aggregation data classes
│   │   │       │   ├── preferences/
│   │   │       │   │   └── UserPreferences.kt     # Currency & settings DataStore
│   │   │       │   └── repository/
│   │   │       │       ├── AccountRepository.kt
│   │   │       │       ├── CategoryRepository.kt
│   │   │       │       ├── EventRepository.kt
│   │   │       │       └── ExpenseRepository.kt
│   │   │       ├── di/
│   │   │       │   └── AppContainer.kt            # Manual dependency injection container
│   │   │       ├── ui/
│   │   │       │   ├── accounts/
│   │   │       │   │   ├── AccountsScreen.kt      # High-density account balances & dialogs
│   │   │       │   │   └── AccountViewModel.kt    # Account UI state and balance updates
│   │   │       │   ├── analytics/                 # Monthly & category charts/analytics
│   │   │       │   ├── events/
│   │   │       │   │   ├── EventDetailScreen.kt   # Dedicated event screen with analytics
│   │   │       │   │   ├── EventsScreen.kt        # Events list
│   │   │       │   │   └── EventViewModel.kt
│   │   │       │   ├── expenses/
│   │   │       │   │   ├── AddExpenseBottomSheet.kt # Expense creation with account linking
│   │   │       │   │   ├── ExpensesScreen.kt      # High-density transaction list
│   │   │       │   │   └── ExpenseViewModel.kt
│   │   │       │   ├── main/
│   │   │       │   │   └── ExpensoMainScreen.kt   # Bottom navigation host
│   │   │       │   ├── navigation/
│   │   │       │   │   └── NavDestination.kt      # Bottom nav destinations
│   │   │       │   ├── settings/                  # Settings, custom categories & CSV
│   │   │       │   └── theme/                     # Material 3 theme, colors, typography
│   │   │       └── utils/
│   │   │           ├── CsvExportHelper.kt         # CSV export utility
│   │   │           └── Formatters.kt              # Currency & date formatters
│   │   └── test/                                  # Unit tests (FormattersTest, etc.)
│   └── build.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── progress.md                                    # This progress file
├── PLAN.md                                        # Original implementation plan
├── SPEC.md                                        # Technical specification
└── CAPABILITY_MAP.md                              # Capability breakdown
```

---

## 5. How to Build, Test & Run

### A. Prerequisites
- **Android SDK:** Installed at `C:\Users\Auto\AppData\Local\Android\Sdk`
- **JDK:** Version 17+ (Java 21 supported)
- **Android Emulator / Device:** Running API 26+ (Tested on API 34 `medium_phone:5554`)

### B. Common Commands
```powershell
# Run Unit Tests
./gradlew.bat testDebugUnitTest

# Build Debug APK
./gradlew.bat assembleDebug

# Install to running emulator or USB device
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk

# Launch App
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.expenso.app/com.example.expenso.MainActivity
```

---

## 6. Roadmap / Next Steps for Future Sessions

When resuming work, here are recommended enhancements:
1. **Account Transfer Feature:** Allow transferring funds between two accounts (e.g. Bank to Cash, Bank to UPI) without recording it as an external expense.
2. **Account Reconciliation History:** Track recent balance manual adjustments or history logs for each account.
3. **Event Archival / Completion:** Add an option to mark an event as "Completed / Archived" so past trips or weddings move to an archived section.
4. **SMS / Notification Parsing (Optional Opt-In):** Local parsing of UPI / Bank debit SMS alerts to auto-suggest expense creation.
5. **Interactive Charts:** Enhanced visual sparklines or bar graphs on the Analytics tab for monthly trend comparisons.
