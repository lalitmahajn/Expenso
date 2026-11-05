package com.example.expenso.data.repository

import com.example.expenso.data.local.AppDatabase
import com.example.expenso.data.local.CategoryDao
import com.example.expenso.data.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun ensureDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertAll(AppDatabase.DEFAULT_CATEGORIES)
        }
    }
}
