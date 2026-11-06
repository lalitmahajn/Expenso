# Expenso - Project Progress & Architecture Ledger

**Repository:** `https://github.com/lalitmahajn/Expenso.git`  
**Account:** `lalitmahajn`  
**Last Updated:** November 6, 2026  
**Status:** Alpha / Verified Feature-Complete & UI Polished (Tested & Validated on Physical Android Device)

---

## 1. Executive Summary & Stack Overview

**Expenso** is a modern, offline-first personal expense tracker built with native Android architecture. It emphasizes **dense, information-rich UI layouts**, flexible multi-account rough balance tracking, event/function-based budgeting, and data privacy.

### Core Technology Stack
- **Language:** Kotlin 2.0.21 (JVM 17 toolchain)
- **UI Framework:** Jetpack Compose with Material 3 design system (Edge-to-Edge)
- **Architecture:** Clean Architecture / MVVM with Kotlin Coroutines & StateFlow
- **Dependency Injection:** Lightweight Manual Container (`AppContainer`, `DefaultAppContainer`)
- **Persistence:** Android Room Database 2.8.5 (Local SQLite, multi-table schema with Foreign Keys and migration paths)
- **Storage & Preferences:** Offline-first, Zero third-party cloud dependency; SharedPreferences for user preferences (currency symbol, dark mode / theme)
- **Exporting:** Local CSV file generation via Android `FileProvider` / Storage Access Framework

---

## 2. Completed Features & Capabilities

### A. High-Density Expense Tracking (`ExpensesScreen.kt`, `AddExpenseBottomSheet.kt`)
- **Dense Transaction Rows:** 44dp compact item heights displaying category icon badge, expense title, payment method, date/time, and colored amount.
- **Dedicated Event Badges:** Tagged occasions and functions displayed cleanly on their own formatted row with calendar icons.
- **Swipe-to-Delete Gesture:** Intuitive left-swipe on transaction cards with a custom styled confirmation card dialog highlighting the exact deletion amount.
- **Sleek 42dp Search Pill:** Compact rounded search bar with clear button.
- **Redesigned Modern Fintech Bottom Sheet:**
  - Modern Apple Card / Revolut-style hero amount card (`$ 0.00`) with inline merchant/title field separated by a subtle divider.
  - Zero-scroll viewport layout (~450dp height) fitting all controls on screen at once.
  - Single horizontal `LazyRow` compact chips for categories, accounts (with live balances), payment methods, and events.
  - Dynamic button preview (e.g. `Record Expense • $45.50`).
- **Consistent Floating Action Buttons:** Unified 48dp circular FABs across Expenses, Events, and Accounts screens.

### B. Dedicated Function & Event Budgeting (`EventsScreen.kt`, `EventDetailScreen.kt`)
- **Full-Screen Event Detail:** Dedicated full-screen view for granular event analysis with back navigation.
- **Budget Metrics Strip:** Real-time metrics showing Total Spent, Target Budget, Remaining / Over-budget indicator, Avg/Item, and Highest Expense.
- **Interactive Breakdown Filters:**
  - One-tap category chips displaying spending percentage (e.g. `Shopping 100%`).
  - One-tap payment method breakdown pills (e.g. `Card 72%`, `UPI 27%`).
  - Direct list filtering upon clicking any breakdown pill.
- **Event Archival & Completion:**
  - Mark functions as "Completed / Archived" with one tap from the detail top bar.
  - Dedicated "Active (N)" and "Archived (M)" tabs on the main Functions screen.
  - Archived status banner on event details with one-click "Restore" action.
- **Sort Controls:** Fast sorting by Newest First, Oldest First, Highest Amount, or Lowest Amount.
- **Direct Event Expense Addition & Swipe-to-Delete:** Floating Action Button (+) directly tags newly logged transactions to the active event; left-swipe to delete with confirmation.

### C. Account Balances & Fund Transfers (`AccountsScreen.kt`, `AccountViewModel.kt`)
- **Design Philosophy:** *"Flexible rough balance tracker"*. Designed so users do not have to cross-check bank apps every day; balances can be adjusted anytime.
- **Net Balance Banner:** Top banner displaying:
  - **Total Net Balance** (Assets minus Credit Card dues)
  - **Total Assets** (Bank accounts + Cash + UPI Wallets)
  - **Credit Card Dues / Liabilities** (highlighted in red)
- **Responsive Header Layout:** Weighted columns ensuring account count badge never compresses or truncates text.
- **Account Fund Transfers (`TransferFundsDialog`):**
  - Transfer funds directly between accounts (e.g. Bank to Cash ATM withdrawal, or Bank to Credit Card payment) without polluting expenses.
  - Atomic balance updating keeping credit card liability accounting consistent.
- **Quick-Adjust Balance Dialog:**
  - Direct numeric editing of current balance.
  - One-tap increment/decrement chips (`+500`, `+1,000`, `+5,000`, `-500`, etc.) for rapid synchronization.
- **Automatic Expense Debiting & Refunds:**
  - When recording/editing an expense, designated account balances are automatically debited or refunded.

### D. Analytics & Reporting (`AnalyticsScreen.kt`, `AnalyticsViewModel.kt`)
- **Visual Spending Composition:** Segmented proportional horizontal composition bar displaying top category allocations at a glance with colored legend badges.
- **Multi-Period Filtering:** Today, This Week, This Month, This Year, All Time.
- **Custom Date Range Filter (`CustomDateRangeDialog`):**
  - Quick presets: Last 7 Days, Last 14 Days, Last 30 Days, Last 60 Days, Last 90 Days, Last 6 Months, Last 1 Year.
  - Active custom range badge with one-tap "Change" action.
- **Detailed Aggregates:** Total spend, average per item, peak transaction, transaction count, category breakdown progress bars, event allocations, and payment method distribution.

### E. Dedicated Financial Statement Generator (`GenerateStatementScreen.kt`, `StatementPdfHelper.kt`, `StatementCsvHelper.kt`)
- **Dedicated Settings Hub:** Accessible directly from `Settings` ➔ `Generate Financial Statement`.
- **Flexible Scope & Filter Engine:**
  - **Time Periods:** `This Month`, `Last Month`, `Last 30 Days`, `Last 90 Days`, `This Year`, `All Time`, plus `Custom Date Range` with quick presets (7d, 14d, 60d, 180d, 1y).
  - **Account Filter:** Generate statements for all accounts or isolate single accounts (e.g. *Main Bank*, *Cash in Hand*, *Credit Card*).
  - **Occasion / Event Filter:** Filter exclusively for a trip or function (e.g. *Goa Road Trip*, *Wedding*).
  - **Category Filter:** Multi-select category dialog to generate statements for specific expenditure types (e.g. only *Business*, *Bills*, *Travel*).
- **Live Summary & Dense Preview:**
  - Real-time outflow sum, transaction count, and active scope badges.
  - Live 5-item preview list with color badges and count indicator.
- **Native Offline PDF Engine (`StatementPdfHelper.kt`):**
  - Uses native Android `PdfDocument` (zero third-party dependencies, 100% offline & private).
  - Multi-page pagination support with A4 dimensions.
  - Professional bank-grade executive summary card, metadata header, alternating zebra striped table with column dividers, and page footers.
- **Structured CSV Engine (`StatementCsvHelper.kt`):**
  - Formatted CSV spreadsheet with metadata header comments and escaped columns for Excel, Google Sheets, or CA accounting.
- **Action Options:**
  - "Preview" (opens system PDF/CSV viewer via `FileProvider`).
  - "Share Statement" (launches Android Sharesheet for WhatsApp, Email, Drive).

### F. Theme, System Navigation & App Customization (`Theme.kt`, `SettingsScreen.kt`)
- **Seamless Edge-to-Edge & Status Bar Theming:** Inset zeroing across inner scaffolds; headers extend behind the status bar with matching dynamic icons (`isAppearanceLightStatusBars`).
- **Theme Selection / Dark Mode:** Choose between "System Default", "Light Mode", and "Dark Mode" with persistent preference.
- **Currency Preferences & Formatting:** Switch between `₹` (INR), `$` (USD), `€` (EUR), `£` (GBP), `¥` (JPY), `₩`, `A$`, `C$`, and `Fr` with proper negative currency sign placement (`-$X.XX`).
- **Custom Category Manager:** Add custom categories with color picker; delete custom categories.
- **CSV Data Export:** Export full expense history to a shareable `.csv` file via Android Sharesheet.
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
   - `description: String`
   - `targetBudget: Double?`
   - `startDate: Long`
   - `endDate: Long?`
   - `isArchived: Boolean`

3. **`accounts`** (`AccountEntity`)
   - `id: Long` (Auto-generate PK)
   - `name: String`
   - `type: AccountType` (BANK, CASH, WALLET, CREDIT_CARD, OTHER)
   - `balance: Double`
   - `accountNumberLast4: String`
   - `colorHex: String`
   - `iconName: String`
   - `updatedAt: Long`

4. **`expenses`** (`ExpenseEntity`)
   - `id: Long` (Auto-generate PK)
   - `title: String`
   - `amount: Double`
   - `categoryId: Long` (FK -> `categories.id` ON DELETE RESTRICT)
   - `eventId: Long?` (FK -> `events.id` ON DELETE SET NULL)
   - `accountId: Long?` (FK -> `accounts.id` ON DELETE SET NULL)
   - `paymentMethod: PaymentMethod` (CASH, CARD, UPI, BANK_TRANSFER, OTHER)
   - `timestamp: Long`
   - `note: String`

---

## 4. How to Build, Test & Run

### Prerequisites
- **Android SDK:** Installed at `%LOCALAPPDATA%\Android\Sdk` (configured in `local.properties`)
- **JDK:** Version 17+ (Java 21 supported)

### Commands
```powershell
# Run Unit Tests
.\gradlew.bat testDebugUnitTest

# Build Debug APK
.\gradlew.bat assembleDebug

# Output APK Location
app\build\outputs\apk\debug\app-debug.apk
```

---

## 5. Potential Future Explorations

1. **SMS / Notification Parsing (Optional Opt-In):** Local regex parsing of UPI / Bank debit SMS alerts to auto-suggest expense creation.
2. **Encrypted JSON / SQLite Backup & Restore:** Offline file export and import for device migration beyond CSV.
