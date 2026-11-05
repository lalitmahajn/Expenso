# Capability Map: Expenso

| Module ID | Responsibility | Depends On |
|---|---|---|
| `core-data` | Room DB (`AppDatabase`), Entities (`Expense`, `Event`, `Category`), DAOs, Repositories, TypeConverters, Migration/Seed | — |
| `feature-events` | Event/Function ledger management (standalone tags or budget-capped goals, balance tracking) | `core-data` |
| `feature-expenses` | Daily expense CRUD, Quick Add sheet, Category/Event assignment, Payment method tags, list & search | `core-data`, `feature-events` |
| `feature-analytics` | Comprehensive summaries: Daily/Weekly/Monthly/Yearly/Custom ranges, Category breakdown, Event breakdown, Payment mode breakdown | `core-data`, `feature-expenses`, `feature-events` |
| `feature-settings-export` | CSV export, currency configuration, sample data seeding, theme preferences | `core-data` |

**Build Order:**
`core-data` → `feature-events` → `feature-expenses` → `feature-analytics` → `feature-settings-export`
