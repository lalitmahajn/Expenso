# Implementation Plan: Expenso

## Phase 1: Dependencies & Gradle Setup
- Add Room (`androidx.room:room-runtime`, `androidx.room:room-ktx`, `androidx.room:room-compiler` with KSP plugin or annotation processor).
- Add Material Icons Extended (`androidx.compose.material:material-icons-extended`) for category and event icons.
- Verify build configuration with `./gradlew.bat --daemon assembleDebug`.

## Phase 2: Core Data Layer (`core-data`)
- Room Entities: `ExpenseEntity`, `EventEntity`, `CategoryEntity`.
- Data classes: `CategorySummary`, `EventSummary`, `DailyExpenseGroup`.
- DAOs: `ExpenseDao`, `EventDao`, `CategoryDao`.
- `AppDatabase` with automatic category seeding on first run.
- Repositories: `ExpenseRepository`, `EventRepository`, `AnalyticsRepository`.
- `AppContainer` & `ExpensoApp` application class for clean DI.

## Phase 3: Feature - Events / Functions (`feature-events`)
- `EventViewModel` managing events list and event details.
- Event card with budget progress bar (spent vs limit) or plain tag badge.
- Create/Edit Event dialog with optional budget toggle.
- Event detail view listing all expenses tagged to this function.

## Phase 4: Feature - Daily Expenses (`feature-expenses`)
- `ExpenseViewModel` handling daily transaction lists, filters, and quick adds.
- `AddExpenseBottomSheet` (amount, title, category, event association, payment method, date).
- Expense list grouped by date with search bar and filter chips.

## Phase 5: Feature - Analytics & Summaries (`feature-analytics`)
- `AnalyticsViewModel` with reactive time range calculations.
- Filter chips: Today, This Week, This Month, This Year, All Time, Custom Range.
- Metric cards (Total, Average, Max, Count).
- Category distribution bars with percentages and color indicators.
- Function/Event spending breakdown.

## Phase 6: Feature - Settings & Export (`feature-settings-export`)
- Currency preference (₹, $, €, £, ¥, etc.) saved in preferences.
- CSV export generator with Android Sharesheet (`Intent.ACTION_SEND`).
- "Seed Sample Data" button to populate rich mock records for testing.

## Phase 7: Verification & Build
- Run `./gradlew.bat testDebugUnitTest` to verify repository and calculation logic.
- Run `./gradlew.bat assembleDebug` to verify end-to-end compilation and APK creation.
- Check with Android CLI (`android describe`).
