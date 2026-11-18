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

### A. Unified Multi-Tab Transaction Logging (`ExpensesScreen.kt`, `AddExpenseBottomSheet.kt`)
- **3-in-1 FAB Action:** The `+` button opens a unified transaction modal with 3 top segmented tabs:
  - **Expense Tab:** Hero amount card with merchant/title, category chips, optional account debiting, payment method, and event linking.
  - **Income Tab:** Emerald green styled hero amount card (`+ $0.00`), deposit-into-account selector with live balances, income source category chips, received-via method chips, and automatic account balance crediting.
  - **Transfer Tab:** Blue styled hero amount card (`➔ $0.00`), intuitive From-Account (debit) and To-Account (credit) selectors with validation, transfer reference/notes, and atomic fund movement.
- **Dense Transaction Rows:** 44dp compact item heights displaying:
  - **Expenses:** Red/primary amount, category icon badge.
  - **Income:** Emerald green amount (`+$X.XX`), trending-up badge, deposited account subtitle.
  - **Transfers:** Blue amount, transfer icon badge, `From ➔ To` account path subtitle.
- **Dedicated Event Badges:** Tagged occasions and functions displayed cleanly on their own formatted row with calendar icons.
- **Swipe-to-Delete Gesture:** Intuitive left-swipe on transaction cards with confirmation dialog and automatic balance reversal (refunding debited accounts, reverting income credits, or reversing transfers).
- **Single Source of Truth Transaction Card (`ExpenseCard`):** Standardized across both ExpensesScreen and EventDetailScreen with soft-tinted pastel circle icon badges, dual-column amount/timestamp alignment, sub-event pill badges with accent colors, and identical 10dp card and swipe containers.
- **Collapsible Sub-Events Section:** Animated accordion toggle on EventDetailScreen to expand/collapse sub-events and free up ~280dp of vertical space for transaction scanning.
- **Reworked Analytics ("Unified Financial Cockpit"):** Replaced disjointed multi-card layout with a 4-in-1 consolidated hero card (Total Spend, Txns, Avg/Item, Peak Spend, Active Categories), a 3-way segmented breakdown selector (`Categories | Functions | Payments`), an integrated composition progress bar, and dense single-container cards (~60% vertical space saved).
- **Hierarchical & Expandable Functions in Analytics:** Analytics > Functions rolls child sub-event expenses up into their parent events, displays the true total configured sub-event count (e.g. `4 sub-events`), and allows expanding to inspect each sub-event and general event allocations with individual progress bars.
- **Universal Header Standardization:** Unified top banner layout, typography (`titleLarge`, `FontWeight.Bold`), subtitle typography (`labelSmall`), and padding (`horizontal = 16.dp, vertical = 10.dp`) across all 5 primary tabs (Expenses, Events, Accounts, Analytics, Settings).
- **Sleek 42dp Search Pill & Filter Chips:** Compact rounded search bar with clear button and category chips.
- **Consistent Floating Action Buttons:** Unified 48dp circular FABs across Expenses, Events, and Accounts screens.

### B. Dedicated Function & Event Budgeting (`EventsScreen.kt`, `EventDetailScreen.kt`)
- **Hierarchical Multi-Phase Events (Parent Events & Sub-Events):**
  - **Database & Room Migration 3->4 (`MIGRATION_3_4`):** Self-referencing foreign key on `events(id)` (`parentEventId: Long? = null`) with cascade deletion and index on `parentEventId`.
  - **Rolled-Up Spend Calculations:** Parent events aggregate direct expenses + all child sub-event expenses in real-time.
  - **Inline Mini-Accordion on Main Screen:** Parent cards show sub-events badge (e.g. `4 sub-events • ₹1,45,000 / ₹5,00,000`) and chevron toggle to unfold sub-event spends and mini-budget progress bars.
  - **Dedicated Sub-Events Section in Detail Screen:** Add sub-events with Name, optional mini-budget target, date, and color accents.
  - **Sub-Event Breakdown & Filtering:** Filter event transactions by specific sub-event or view all aggregated.
  - **Cascade Deletion Lifecycle (Option B):** Deleting an event or sub-event deletes all linked child sub-events and expenses, reversing account debits/credits automatically with explicit safety warnings.
- **Expanding Sub-Chips in Transaction Logger (`AddExpenseBottomSheet.kt`):**
  - Selecting an event with sub-events unfolds a secondary chip row: `[General <Event>]`, `[Day 1]`, `[North Leg]`, `[Reception]`. Default remains parent level.
- **Multi-Section Statements (`StatementPdfHelper.kt`, `StatementCsvHelper.kt`, `StatementViewModel.kt`):**
  - **PDF Section 1:** Executive Summary & Category Breakdown.
  - **PDF Section 2:** Sub-Events Breakdown (when filtered by event, comparing spends vs mini-budgets).
  - **PDF Section 3:** Chronological Flat Ledger with `Event • Sub-Event` labeling.
  - **CSV Export:** Dedicated `Event` and `Sub-Event` columns.
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
- **Data Portability:** Consolidated under "Generate Financial Statement" (supports comprehensive and filtered PDF and CSV reports).
- **Sample Data Seeding & Clearing:**
  - One-tap demo dataset for instant evaluation.
  - **Clear Demo / All Data Option:** Dedicated button with a safety confirmation dialog to permanently wipe all test transactions and custom events, with an option to reset account balances to defaults, leaving categories intact for an instant clean slate.

---

## 3. Database Schema & Architecture

### Database Name: `expenso_database`
**Current Version:** `3`

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
   - `toAccountId: Long?` (FK -> `accounts.id` ON DELETE SET NULL)
   - `transactionType: TransactionType` (EXPENSE, INCOME, TRANSFER)
   - `paymentMethod: PaymentMethod` (CASH, CARD, UPI, BANK_TRANSFER, OTHER)
   - `timestamp: Long`
   - `note: String`

#### Migrations
- **`MIGRATION_1_2`**: Added `accounts` table and linked foreign key to expenses.
- **`MIGRATION_2_3`**: Added `toAccountId` and `transactionType` with full foreign keys and indices.

---

## 4. Production Hardening & Skill-Driven Optimizations (November 2026)

Applied targeted optimizations across the codebase derived from official Android CLI skills:

1. **Back Navigation Resilience (`navigation-event`):**
   - Integrated `BackHandler` in [`ExpensoMainScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/main/ExpensoMainScreen.kt) for event detail, statement generator, and multi-tab return to home tab (`Expenses`), preventing accidental app kills on system back swipe.
   - Connected localized `BackHandler` within [`EventDetailScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/events/EventDetailScreen.kt) and [`GenerateStatementScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/statement/GenerateStatementScreen.kt).

2. **Compose Performance & Lifecycle Collection (`jetpack-compose-performance`):**
   - Converted 100% of state observation across all screens from `collectAsState()` to `collectAsStateWithLifecycle()` to pause flow collection when the app is backgrounded.
   - Added explicit primitive `key` and `contentType` definitions to all `LazyColumn` and `LazyRow` containers across [`ExpensesScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/expenses/ExpensesScreen.kt), [`EventsScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/events/EventsScreen.kt), [`EventDetailScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/events/EventDetailScreen.kt), [`AccountsScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/accounts/AccountsScreen.kt), [`AnalyticsScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/analytics/AnalyticsScreen.kt), [`SettingsScreen.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/settings/SettingsScreen.kt), and [`AddExpenseBottomSheet.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/expenses/AddExpenseBottomSheet.kt).
   - Precomputed and cached date-total sums inside `remember(uiState.expenses)` rather than executing `sumOf` inside the item layout composition pass.

3. **Edge-to-Edge & IME Smoothing (`edge-to-edge`):**
   - Corrected [`AndroidManifest.xml`](file:///d:/Learning/Project%20x/app/src/main/AndroidManifest.xml) by placing `android:windowSoftInputMode="adjustResize"` onto `<activity>` rather than `<application>`, ensuring the keyboard properly resizes windows.
   - Applied `Modifier.imePadding()` and `Modifier.navigationBarsPadding()` to [`AddExpenseBottomSheet.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/ui/expenses/AddExpenseBottomSheet.kt) to keep save buttons and inputs above the soft keyboard.

4. **Testing Infrastructure (`testing-setup`):**
   - Created test double [`FakeAccountDao.kt`](file:///d:/Learning/Project%20x/app/src/test/kotlin/com/example/expenso/test/FakeAccountDao.kt) implementing `AccountDao`.
   - Added unit test suite [`AccountRepositoryTest.kt`](file:///d:/Learning/Project%20x/app/src/test/kotlin/com/example/expenso/test/AccountRepositoryTest.kt) asserting account insertion, debits, credits, atomic fund transfers, direct balance updates, and sorted Flow emissions.
   - Verified that `testDebugUnitTest` passes 100%.

5. **Offline Database Backup & Restore (`BackupRestoreManager.kt`):**
   - Created [`BackupRestoreManager.kt`](file:///d:/Learning/Project%20x/app/src/main/kotlin/com/example/expenso/data/backup/BackupRestoreManager.kt) enabling complete, portable, offline JSON backups of the Room database and user preferences.
   - **Export Capabilities:**
     - Save to Device / Google Drive via Android Storage Access Framework (`ActivityResultContracts.CreateDocument`).
     - Share Backup File directly to WhatsApp, Gmail, Telegram, or Cloud via Android Share Sheet (`FileProvider`).
   - **Restore Capabilities:**
     - Open and inspect any Expenso backup document via Storage Access Framework (`ActivityResultContracts.OpenDocument`).
     - Pre-restoration verification dialog showing timestamp, transaction count, accounts, events, and currency.
     - Atomic restoration inside `database.withTransaction`: resets tables, restores categories, accounts, hierarchical parent/child events, and all transaction entries (expenses, incomes, transfers), updating reactive UI state in real-time.
   - Added unit test suite [`BackupDataTest.kt`](file:///d:/Learning/Project%20x/app/src/test/kotlin/com/example/expenso/test/BackupDataTest.kt) asserting backup schema consistency and JSON serialization/deserialization.

6. **Production Adaptive App Icon (Material Design & Android 13+ Themed Icons):**
   - Replaced generic template with high-contrast, modern fintech identity:
     - **Background (`ic_launcher_background.xml`):** Luminous Emerald & Jade diagonal gradient (`#00D26A` -> `#059669` -> `#047857` -> `#064E3B`) with specular radial glow.
     - **Foreground (`ic_launcher_foreground.xml`):** Crisp, bold white geometric "E" lettermark with 5dp corner rounding and subtle drop shadow, precisely centered within the 66dp safe zone.
     - **Themed Icons (`ic_launcher_monochrome.xml`):** Full vector layer support for Android 13+ Material You dynamic color tinting.

---

## 5. How to Build, Test & Run

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
