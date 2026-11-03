package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bazar_expenses")
data class BazarExpenseEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val date: String, // YYYY-MM-DD
  val itemDescription: String,
  val category: String, // Fish, Meat, Vegetables, Groceries, Spices, Dairy, Fruits, Other
  val totalAmountPaisa: Long, // Integer paisa (1 ৳ = 100 paisa)
  val shopperId: String = "",
  val shopperName: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
