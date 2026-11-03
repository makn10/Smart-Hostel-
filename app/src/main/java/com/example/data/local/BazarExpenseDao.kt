package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BazarExpenseDao {

  @Query("SELECT * FROM bazar_expenses ORDER BY date DESC, timestamp DESC")
  fun getAllExpenses(): Flow<List<BazarExpenseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: BazarExpenseEntity)

  @Query("DELETE FROM bazar_expenses WHERE id = :id")
  suspend fun deleteExpenseById(id: String)

  @Query("SELECT SUM(totalAmountPaisa) FROM bazar_expenses")
  fun getTotalExpensePaisa(): Flow<Long?>

  @Query("DELETE FROM bazar_expenses")
  suspend fun clearAll()
}
