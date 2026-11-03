package com.example

import com.example.core.calc.CalculationEngine
import com.example.core.model.BazarItem
import com.example.core.model.BazarRecord
import com.example.core.model.BillRecord
import com.example.core.model.BillType
import com.example.core.model.DepositRecord
import com.example.core.model.ExtraCostRecord
import com.example.core.model.MealEntry
import com.example.core.model.MealSystem
import com.example.core.model.Member
import com.example.core.model.MessConfig
import com.example.core.model.PaymentMethod
import com.example.core.model.Role
import com.example.core.model.SplitMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationEngineTest {

  @Test
  fun testSplitPaisaEqually_distributesRemainderWithoutLoss() {
    // 1000 paisa (10.00 ৳) divided among 3 members: 334, 333, 333 -> sum = 1000
    val shares = CalculationEngine.splitPaisaEqually(1000L, 3)
    assertEquals(3, shares.size)
    assertEquals(334L, shares[0])
    assertEquals(333L, shares[1])
    assertEquals(333L, shares[2])
    assertEquals(1000L, shares.sum())
  }

  @Test
  fun testSplitPaisaEqually_negativeCorrectionWithoutLoss() {
    // -1000 paisa divided among 3 members: -334, -333, -333 -> sum = -1000
    val shares = CalculationEngine.splitPaisaEqually(-1000L, 3)
    assertEquals(3, shares.size)
    assertEquals(-334L, shares[0])
    assertEquals(-333L, shares[1])
    assertEquals(-333L, shares[2])
    assertEquals(-1000L, shares.sum())
  }

  @Test
  fun testZeroMeals_doesNotCrashOrNaN() {
    val config = MessConfig(mealSystem = MealSystem.SHARED_COST)
    val member = Member(id = "m1", name = "Sakib", phone = "01700000001", openingBalancePaisa = 0L)
    val bazar = BazarRecord(
      date = "2026-11-01",
      shopperId = "m1",
      shopperName = "Sakib",
      items = listOf(BazarItem(name = "Fish", costPaisa = 50000L)),
      totalPaisa = 50000L
    )

    val summary = CalculationEngine.calculatePeriodSummary(
      config = config,
      members = listOf(member),
      meals = emptyList(), // 0 meals
      bazarRecords = listOf(bazar),
      deposits = emptyList(),
      bills = emptyList(),
      extraCosts = emptyList()
    )

    assertEquals(0.0, summary.totalMeals, 0.0001)
    assertEquals(0.0, summary.mealRatePaisa, 0.0001)
    assertEquals(0L, summary.memberSummaries["m1"]!!.mealCostPaisa)
  }

  @Test
  fun testHalfMealsAndWeights() {
    val config = MessConfig(
      mealSystem = MealSystem.SHARED_COST,
      breakfastWeight = 0.5,
      lunchWeight = 1.0,
      dinnerWeight = 1.0
    )
    val member1 = Member(id = "m1", name = "Rahim", phone = "01711111111")
    val member2 = Member(id = "m2", name = "Karim", phone = "01722222222")

    // Member 1: 1 B (0.5), 0.5 L (0.5), 1 D (1.0) = 2.0 weighted
    val entry1 = MealEntry(
      memberId = "m1",
      date = "2026-11-01",
      breakfast = 1.0,
      lunch = 0.5,
      dinner = 1.0
    )
    // Member 2: 0 B, 1 L (1.0), 1 D (1.0) = 2.0 weighted
    val entry2 = MealEntry(
      memberId = "m2",
      date = "2026-11-01",
      breakfast = 0.0,
      lunch = 1.0,
      dinner = 1.0
    )

    // Total meals = 4.0. Total bazar = 240.00 ৳ (24000 paisa)
    val bazar = BazarRecord(
      date = "2026-11-01",
      shopperId = "m1",
      shopperName = "Rahim",
      items = listOf(BazarItem("Chicken", costPaisa = 24000L)),
      totalPaisa = 24000L
    )

    val summary = CalculationEngine.calculatePeriodSummary(
      config = config,
      members = listOf(member1, member2),
      meals = listOf(entry1, entry2),
      bazarRecords = listOf(bazar),
      deposits = emptyList(),
      bills = emptyList(),
      extraCosts = emptyList()
    )

    assertEquals(4.0, summary.totalMeals, 0.0001)
    // Rate = 24000 / 4.0 = 6000 paisa (60.00 ৳)
    assertEquals(6000.0, summary.mealRatePaisa, 0.0001)
    assertEquals(12000L, summary.memberSummaries["m1"]!!.mealCostPaisa)
    assertEquals(12000L, summary.memberSummaries["m2"]!!.mealCostPaisa)
  }

  @Test
  fun testDepositsCorrectionsAndNetBalance() {
    val config = MessConfig()
    val member = Member(
      id = "m1",
      name = "Anik",
      phone = "01733333333",
      openingBalancePaisa = 50000L // 500.00 ৳ previous carry-over
    )

    // Deposit 1000.00 ৳ (100000 paisa)
    val dep1 = DepositRecord(
      memberId = "m1",
      date = "2026-11-01",
      amountPaisa = 100000L,
      method = PaymentMethod.BKASH
    )
    // Mistaken entry correction: -200.00 ৳ (-20000 paisa)
    val dep2 = DepositRecord(
      memberId = "m1",
      date = "2026-11-02",
      amountPaisa = -20000L,
      method = PaymentMethod.BKASH,
      note = "Correction of overcharge"
    )

    // Bill: 300.00 ৳
    val bill = BillRecord(
      type = BillType.ELECTRICITY,
      name = "Current Bill",
      amountPaisa = 30000L,
      splitMode = SplitMode.SHARED_EQUALLY
    )

    val summary = CalculationEngine.calculatePeriodSummary(
      config = config,
      members = listOf(member),
      meals = emptyList(),
      bazarRecords = emptyList(),
      deposits = listOf(dep1, dep2),
      bills = listOf(bill),
      extraCosts = emptyList()
    )

    val memSum = summary.memberSummaries["m1"]!!
    // Total deposits = 100000 - 20000 = 80000 paisa (800.00 ৳)
    assertEquals(80000L, memSum.totalDepositPaisa)
    // Total expenses = 30000 paisa
    assertEquals(30000L, memSum.totalExpensePaisa)
    // Opening = 50000
    // Final balance = 50000 + 80000 - 30000 = 100000 paisa (+1000.00 ৳ mess owes member)
    assertEquals(100000L, memSum.finalBalancePaisa)
    assertTrue(memSum.isCredit())
  }

  @Test
  fun testMessCashInHandFormula() {
    val config = MessConfig()
    val member = Member(id = "m1", name = "Test", phone = "01700000000")

    val deposit = DepositRecord(memberId = "m1", date = "2026-11-01", amountPaisa = 500000L) // 5000 ৳
    val bazar = BazarRecord(date = "2026-11-01", shopperId = "m1", shopperName = "Test", items = emptyList(), totalPaisa = 300000L) // 3000 ৳
    val bill = BillRecord(type = BillType.GAS, name = "Gas", amountPaisa = 100000L) // 1000 ৳

    val summary = CalculationEngine.calculatePeriodSummary(
      config = config,
      members = listOf(member),
      meals = emptyList(),
      bazarRecords = listOf(bazar),
      deposits = listOf(deposit),
      bills = listOf(bill),
      extraCosts = emptyList()
    )

    // Cash in hand = 5000 - 3000 - 1000 = 1000 ৳ (100000 paisa)
    assertEquals(100000L, summary.messCashInHandPaisa)
  }
}
