package com.example.expenso.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expenso.data.model.AccountEntity
import com.example.expenso.data.model.AccountType
import com.example.expenso.data.model.CategoryEntity
import com.example.expenso.data.model.EventEntity
import com.example.expenso.data.model.ExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        EventEntity::class,
        CategoryEntity::class,
        AccountEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun eventDao(): EventDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `balance` REAL NOT NULL, `colorHex` TEXT NOT NULL, `iconName` TEXT NOT NULL, `accountNumberLast4` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL)")

                val now = System.currentTimeMillis()
                db.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `balance`, `colorHex`, `iconName`, `accountNumberLast4`, `updatedAt`) VALUES (1, 'Main Bank Account', 'BANK', 35000.0, '#2196F3', 'AccountBalance', '4821', $now)")
                db.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `balance`, `colorHex`, `iconName`, `accountNumberLast4`, `updatedAt`) VALUES (2, 'Cash in Hand', 'CASH', 4500.0, '#4CAF50', 'Payments', '', $now)")
                db.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `balance`, `colorHex`, `iconName`, `accountNumberLast4`, `updatedAt`) VALUES (3, 'UPI Wallet', 'WALLET', 2850.0, '#9C27B0', 'Wallet', '', $now)")
                db.execSQL("INSERT INTO `accounts` (`id`, `name`, `type`, `balance`, `colorHex`, `iconName`, `accountNumberLast4`, `updatedAt`) VALUES (4, 'Credit Card', 'CREDIT_CARD', -8200.0, '#FF5722', 'CreditCard', '7712', $now)")

                // Recreate expenses with matching foreign keys
                db.execSQL("CREATE TABLE IF NOT EXISTS `expenses_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `categoryId` INTEGER NOT NULL, `eventId` INTEGER, `accountId` INTEGER, `timestamp` INTEGER NOT NULL, `paymentMethod` TEXT NOT NULL, `note` TEXT NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`eventId`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
                db.execSQL("INSERT INTO `expenses_new` (`id`, `title`, `amount`, `categoryId`, `eventId`, `accountId`, `timestamp`, `paymentMethod`, `note`) SELECT `id`, `title`, `amount`, `categoryId`, `eventId`, NULL, `timestamp`, `paymentMethod`, `note` FROM `expenses`")
                db.execSQL("DROP TABLE `expenses`")
                db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_eventId` ON `expenses` (`eventId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_accountId` ON `expenses` (`accountId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_timestamp` ON `expenses` (`timestamp`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `expenses_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`amount` REAL NOT NULL, " +
                        "`categoryId` INTEGER NOT NULL, " +
                        "`eventId` INTEGER, " +
                        "`accountId` INTEGER, " +
                        "`toAccountId` INTEGER, " +
                        "`transactionType` TEXT NOT NULL DEFAULT 'EXPENSE', " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`paymentMethod` TEXT NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , " +
                        "FOREIGN KEY(`eventId`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , " +
                        "FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , " +
                        "FOREIGN KEY(`toAccountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )"
                )
                db.execSQL(
                    "INSERT INTO `expenses_new` (`id`, `title`, `amount`, `categoryId`, `eventId`, `accountId`, `toAccountId`, `transactionType`, `timestamp`, `paymentMethod`, `note`) " +
                        "SELECT `id`, `title`, `amount`, `categoryId`, `eventId`, `accountId`, NULL, 'EXPENSE', `timestamp`, `paymentMethod`, `note` FROM `expenses`"
                )
                db.execSQL("DROP TABLE `expenses`")
                db.execSQL("ALTER TABLE `expenses_new` RENAME TO `expenses`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_eventId` ON `expenses` (`eventId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_accountId` ON `expenses` (`accountId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_toAccountId` ON `expenses` (`toAccountId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_timestamp` ON `expenses` (`timestamp`)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expenso_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(1, "Food & Dining", "Restaurant", "#FF5722"),
            CategoryEntity(2, "Groceries", "ShoppingCart", "#4CAF50"),
            CategoryEntity(3, "Transport", "DirectionsCar", "#2196F3"),
            CategoryEntity(4, "Bills & Utilities", "Receipt", "#FF9800"),
            CategoryEntity(5, "Shopping", "ShoppingBag", "#E91E63"),
            CategoryEntity(6, "Entertainment", "Movie", "#9C27B0"),
            CategoryEntity(7, "Health & Medical", "LocalHospital", "#F44336"),
            CategoryEntity(8, "Travel", "Flight", "#00BCD4"),
            CategoryEntity(9, "Education", "School", "#3F51B5"),
            CategoryEntity(10, "Other", "MoreHoriz", "#607D8B")
        )

        val DEFAULT_ACCOUNTS = listOf(
            AccountEntity(1, "Main Bank Account", AccountType.BANK, 35000.0, "#2196F3", "AccountBalance", "4821"),
            AccountEntity(2, "Cash in Hand", AccountType.CASH, 4500.0, "#4CAF50", "Payments", ""),
            AccountEntity(3, "UPI Wallet", AccountType.WALLET, 2850.0, "#9C27B0", "Wallet", ""),
            AccountEntity(4, "Credit Card", AccountType.CREDIT_CARD, -8200.0, "#FF5722", "CreditCard", "7712")
        )
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.categoryDao().insertAll(DEFAULT_CATEGORIES)
                    database.accountDao().insertAll(DEFAULT_ACCOUNTS)
                }
            }
        }
    }
}
