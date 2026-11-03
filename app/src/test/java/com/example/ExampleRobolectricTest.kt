package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.calc.CalculationEngine
import com.example.data.local.BazarExpenseDao
import com.example.data.local.BazarExpenseEntity
import com.example.data.local.MessRoomDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: MessRoomDatabase
  private lateinit var dao: BazarExpenseDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, MessRoomDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.bazarExpenseDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart Mess", appName)
  }

  @Test
  fun `insert and retrieve daily bazar expense in Room`() = runBlocking {
    val expense = BazarExpenseEntity(
      id = "test-exp-1",
      date = "2026-11-02",
      itemDescription = "Fresh Hilsha fish and potatoes",
      category = "Fish",
      totalAmountPaisa = 85000L, // 850.00 taka
      shopperId = "shopper-1",
      shopperName = "Abdul Kuddus"
    )

    dao.insertExpense(expense)

    val list = dao.getAllExpenses().first()
    assertEquals(1, list.size)
    assertEquals("Fresh Hilsha fish and potatoes", list[0].itemDescription)
    assertEquals("Fish", list[0].category)
    assertEquals(85000L, list[0].totalAmountPaisa)

    val sum = dao.getTotalExpensePaisa().first()
    assertEquals(85000L, sum)
  }

  @Test
  fun `calculation engine accurately evaluates expenses vs deposits solvency`() {
    val totalDepositPaisa = 1500000L // 15,000.00 taka
    val totalExpensePaisa = 1200000L // 12,000.00 taka
    val netDelta = totalDepositPaisa - totalExpensePaisa

    assertTrue("Deposits exceed expenses", netDelta > 0)
    assertEquals("15000.00 ৳", CalculationEngine.formatPaisa(totalDepositPaisa, "৳"))
    assertEquals("12000.00 ৳", CalculationEngine.formatPaisa(totalExpensePaisa, "৳"))
    assertEquals("+3000.00 ৳", CalculationEngine.formatPaisa(netDelta, "৳", showSign = true))
  }
}
