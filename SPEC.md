# Spec: Expenso (Native Android Offline-First Expense Tracker)

## 1. Objective
Expenso is a 100% native Android mobile application built with Jetpack Compose (Material 3) and Room Database. It solves the friction of tracking everyday expenses alongside distinct "event/function-specific" financial obligations (e.g., weddings, road trips, home repairs, office parties) with rich analytics, multi-period summaries, and data export.

### Target User
Individuals looking for private, offline-first personal financial management with fast input, visual budget tracking for specific events, and deep summarization capabilities without needing cloud subscriptions or third-party tracking.

### Core Metrics of Success
- Zero latency expense recording (under 3 taps to log an item).
- Multi-dimensional summaries (Daily, Weekly, Monthly, Yearly, Custom ranges).
- Event-specific ledger tracking with optional budget progress bars.
- 100% offline functionality with reliable CSV data export.

---

## 2. Commands

- **Build Project**: `.\gradlew.bat assembleDebug`
- **Run Unit Tests**: `.\gradlew.bat testDebugUnitTest`
- **Run Android CLI Check**: `& "C:\Users\Auto\AppData\AndroidCLI\android.exe" describe`
- **Lint Check**: `.\gradlew.bat lintDebug`

---

## 3. Project Structure

```
app/src/main/kotlin/com/example/expenso/
├── ExpensoApp.kt                  → Application class / DB container
├── MainActivity.kt                → Main activity host
├── navigation/
│   ├── Screen.kt                  → Screen routes (Expenses, Events, Analytics, Settings)
│   └── ExpensoNavGraph.kt         → Bottom navigation + destination routing
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         → Room Database definition
│   │   ├── Converters.kt          → Date / Enum type converters
│   │   ├── dao/
│   │   │   ├── ExpenseDao.kt      → Expense SQL queries & aggregations
│   │   │   ├── EventDao.kt        → Event/Function ledger queries
│   │   │   └── CategoryDao.kt     → Category queries & seed data
│   │   └── entity/
│   │       ├── ExpenseEntity.kt   → Expense table
│   │       ├── EventEntity.kt     → Event / Function table (with optional budget)
│   │       └── CategoryEntity.kt  → Category table
│   └── repository/
│       ├── ExpenseRepository.kt
│       ├── EventRepository.kt
│       └── AnalyticsRepository.kt
├── ui/
│   ├── theme/                     → Material 3 colors, typography, shapes
│   ├── components/                → Common cards, chips, dialogs, charts
│   ├── expenses/                  → Daily expense list, Quick-Add bottom sheet, filters
│   ├── events/                    → Function/Event list, Event detail with budget bar
│   ├── analytics/                 → Summaries (Weekly/Monthly/Yearly/Custom), Category charts
│   └── settings/                  → CSV Export, currency settings, sample data seed
```

---

## 4. Data Flow & Interfaces

### Room Schema

#### `EventEntity`
- `id`: Long (PK auto-generate)
- `name`: String (e.g., "Goa Road Trip", "Sister's Wedding")
- `description`: String?
- `targetBudget`: Double? (null if purely tagging, or positive number if budget-tracked)
- `startDate`: Long
- `endDate`: Long?
- `isArchived`: Boolean

#### `ExpenseEntity`
- `id`: Long (PK auto-generate)
- `title`: String
- `amount`: Double
- `categoryId`: Long
- `eventId`: Long? (FK nullable -> `EventEntity.id`, ON DELETE SET NULL)
- `timestamp`: Long (epoch milliseconds)
- `paymentMethod`: String ("CASH", "CARD", "UPI", "OTHER")
- `note`: String?

#### `CategoryEntity`
- `id`: Long (PK auto-generate)
- `name`: String ("Food", "Transport", "Shopping", "Bills", "Health", "Entertainment", "Other")
- `iconName`: String
- `colorHex`: String

### Key Aggregation Queries
```sql
-- Monthly Category Breakdown
SELECT c.name as categoryName, c.colorHex, SUM(e.amount) as totalAmount, COUNT(e.id) as count
FROM expenses e
JOIN categories c ON e.categoryId = c.id
WHERE e.timestamp BETWEEN :startTime AND :endTime
GROUP BY c.id;

-- Event Summary (Total spent against event budget)
SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE eventId = :eventId;
```

---

## 5. Acceptance Criteria

1. **Daily Expense Logging**:
   - GIVEN the user is on the main screen,
   - WHEN they tap "+" and input Amount (e.g. 150), Category ("Food"), and optional Note,
   - THEN the expense is immediately persisted in Room and appears in the daily transactions list.

2. **Function-Specific Expense Tracking**:
   - GIVEN the user creates an event "Goa Trip" with a target budget of 20,000,
   - WHEN expenses tagged to "Goa Trip" are added totaling 12,000,
   - THEN the Event Detail screen shows total spent: 12,000, remaining: 8,000, and a 60% progress indicator.

3. **Multi-Option Summaries**:
   - GIVEN the user switches the Analytics tab,
   - WHEN they toggle between Daily, Weekly, Monthly, Yearly, and Custom Range,
   - THEN the total spend, transaction count, average spend, and category breakdown update reactively.

4. **Data Portability**:
   - GIVEN expenses exist in the database,
   - WHEN the user taps "Export CSV" in Settings,
   - THEN a well-formed CSV file is generated with all columns and exported to device storage/sharesheet.

---

## 6. Failure Modes & Edge Cases

- **Negative or Zero Amount**: Form validator rejects 0 or negative numbers with clear visual feedback.
- **Deleted Event with Linked Expenses**: If an event is deleted, linked expenses have `eventId` set to `null` (ON DELETE SET NULL), preventing data loss.
- **Empty State**: Friendly empty state illustrations and "Add your first expense" CTA on all tabs.
- **Configuration Change / Screen Rotation**: ViewModels preserve all search queries and active filter states across orientation changes.
