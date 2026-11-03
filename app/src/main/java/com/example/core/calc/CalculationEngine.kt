package com.example.core.calc

import com.example.core.model.BazarRecord
import com.example.core.model.BillRecord
import com.example.core.model.DepositRecord
import com.example.core.model.ExtraCostRecord
import com.example.core.model.MealEntry
import com.example.core.model.MealSystem
import com.example.core.model.Member
import com.example.core.model.MessConfig
import com.example.core.model.SplitMode
import java.util.Locale
import kotlin.math.abs

data class MemberCalculationSummary(
  val memberId: String,
  val memberName: String,
  val rawMeals: Double,
  val weightedMeals: Double,
  val mealCostPaisa: Long,
  val fixedBillsCostPaisa: Long,
  val billBreakdown: Map<String, Long>,
  val extraCostPaisa: Long,
  val totalExpensePaisa: Long,
  val totalDepositPaisa: Long,
  val openingBalancePaisa: Long,
  val finalBalancePaisa: Long // Positive = mess owes member (credit), negative = member owes mess (due)
) {
  fun isDue(): Boolean = finalBalancePaisa < 0
  fun isCredit(): Boolean = finalBalancePaisa > 0
}

data class MessCalculationSummary(
  val totalMeals: Double,
  val totalBazarPaisa: Long,
  val totalBillsPaisa: Long,
  val totalExtraPaisa: Long,
  val totalDepositPaisa: Long,
  val totalExpensesPaisa: Long,
  val mealRatePaisa: Double, // In paisa (e.g. 6245.5 paisa = 62.455 ৳)
  val messCashInHandPaisa: Long, // totalDeposit - totalBazar - totalBills
  val memberSummaries: Map<String, MemberCalculationSummary>
)

object CalculationEngine {

  /**
   * Distributes [totalPaisa] among [participantCount] ensuring the sum
   * of allocated shares equals [totalPaisa] exactly, avoiding fractional paisa loss.
   */
  fun splitPaisaEqually(totalPaisa: Long, participantCount: Int): List<Long> {
    if (participantCount <= 0) return emptyList()
    val baseShare = totalPaisa / participantCount
    val remainder = totalPaisa % participantCount
    val absRem = abs(remainder).toInt()
    val remSign = if (remainder >= 0) 1L else -1L

    return List(participantCount) { index ->
      baseShare + if (index < absRem) remSign else 0L
    }
  }

  /**
   * Calculates entire mess summary and per-member breakdown.
   * Single source of truth across all screens.
   */
  fun calculatePeriodSummary(
    config: MessConfig,
    members: List<Member>,
    meals: List<MealEntry>,
    bazarRecords: List<BazarRecord>,
    deposits: List<DepositRecord>,
    bills: List<BillRecord>,
    extraCosts: List<ExtraCostRecord>
  ): MessCalculationSummary {
    val activeMembers = members.filter { it.active }
    val allMemberMap = members.associateBy { it.id }

    // 1. Calculate weighted meals per member
    val memberMealMap = mutableMapOf<String, Double>()
    val memberRawMealMap = mutableMapOf<String, Double>()
    members.forEach { m ->
      memberMealMap[m.id] = 0.0
      memberRawMealMap[m.id] = 0.0
    }

    meals.forEach { entry ->
      val currentWeighted = memberMealMap[entry.memberId] ?: 0.0
      val currentRaw = memberRawMealMap[entry.memberId] ?: 0.0
      val weighted = entry.weightedMealCount(
        config.breakfastWeight,
        config.lunchWeight,
        config.dinnerWeight
      )
      memberMealMap[entry.memberId] = currentWeighted + weighted
      memberRawMealMap[entry.memberId] = currentRaw + entry.rawMealCount()
    }

    val totalMeals = memberMealMap.values.sum()

    // 2. Total Bazar Cost
    val totalBazarPaisa = bazarRecords.sumOf { it.totalPaisa }

    // 3. Meal Rate
    val mealRatePaisa: Double = when (config.mealSystem) {
      MealSystem.SHARED_COST -> {
        if (totalMeals > 0.0) {
          totalBazarPaisa.toDouble() / totalMeals
        } else {
          0.0
        }
      }
      MealSystem.FIXED_RATE -> config.fixedRatePaisa.toDouble()
    }

    // 4. Per Member Meal Cost
    val memberMealCostMap = mutableMapOf<String, Long>()
    members.forEach { m ->
      val mealsCount = memberMealMap[m.id] ?: 0.0
      // Round to nearest integer paisa
      val cost = Math.round(mealsCount * mealRatePaisa)
      memberMealCostMap[m.id] = cost
    }

    // 5. Calculate Fixed / Utility Bills distribution
    val memberFixedCostMap = mutableMapOf<String, Long>()
    val memberBillBreakdownMap = mutableMapOf<String, MutableMap<String, Long>>()
    members.forEach { m ->
      memberFixedCostMap[m.id] = 0L
      memberBillBreakdownMap[m.id] = mutableMapOf()
    }

    var totalBillsPaisa = 0L
    bills.forEach { bill ->
      totalBillsPaisa += bill.amountPaisa

      val participants: List<Member> = when (bill.splitMode) {
        SplitMode.SHARED_EQUALLY -> activeMembers
        SplitMode.PER_PERSON -> activeMembers
        SplitMode.SELECTED_MEMBERS -> {
          members.filter { it.id in bill.participantIds }
        }
      }

      if (participants.isNotEmpty()) {
        val shares = splitPaisaEqually(bill.amountPaisa, participants.size)
        participants.forEachIndexed { index, member ->
          val share = shares[index]
          memberFixedCostMap[member.id] = (memberFixedCostMap[member.id] ?: 0L) + share
          val breakdown = memberBillBreakdownMap[member.id] ?: mutableMapOf()
          val currentForBill = breakdown[bill.name] ?: 0L
          breakdown[bill.name] = currentForBill + share
        }
      }
    }

    // 6. Calculate Extra Costs (shared among selected participants)
    val memberExtraCostMap = mutableMapOf<String, Long>()
    members.forEach { m -> memberExtraCostMap[m.id] = 0L }

    var totalExtraPaisa = 0L
    extraCosts.forEach { extra ->
      totalExtraPaisa += extra.amountPaisa
      val participants = if (extra.participantIds.isEmpty()) {
        activeMembers
      } else {
        members.filter { it.id in extra.participantIds }
      }

      if (participants.isNotEmpty()) {
        val shares = splitPaisaEqually(extra.amountPaisa, participants.size)
        participants.forEachIndexed { index, member ->
          val share = shares[index]
          memberExtraCostMap[member.id] = (memberExtraCostMap[member.id] ?: 0L) + share
        }
      }
    }

    // 7. Calculate Deposits per member
    val memberDepositMap = mutableMapOf<String, Long>()
    members.forEach { m -> memberDepositMap[m.id] = 0L }
    var totalDepositPaisa = 0L

    deposits.forEach { deposit ->
      totalDepositPaisa += deposit.amountPaisa
      memberDepositMap[deposit.memberId] = (memberDepositMap[deposit.memberId] ?: 0L) + deposit.amountPaisa
    }

    // 8. Compile per-member summaries
    val summaries = members.associate { member ->
      val mealsCount = memberMealMap[member.id] ?: 0.0
      val rawCount = memberRawMealMap[member.id] ?: 0.0
      val mealCost = memberMealCostMap[member.id] ?: 0L
      val fixedCost = memberFixedCostMap[member.id] ?: 0L
      val extraCost = memberExtraCostMap[member.id] ?: 0L
      val totalExpense = mealCost + fixedCost + extraCost
      val deposit = memberDepositMap[member.id] ?: 0L
      val opening = member.openingBalancePaisa
      val finalBalance = opening + deposit - totalExpense

      member.id to MemberCalculationSummary(
        memberId = member.id,
        memberName = member.name,
        rawMeals = rawCount,
        weightedMeals = mealsCount,
        mealCostPaisa = mealCost,
        fixedBillsCostPaisa = fixedCost,
        billBreakdown = memberBillBreakdownMap[member.id] ?: emptyMap(),
        extraCostPaisa = extraCost,
        totalExpensePaisa = totalExpense,
        totalDepositPaisa = deposit,
        openingBalancePaisa = opening,
        finalBalancePaisa = finalBalance
      )
    }

    val totalExpensesPaisa = summaries.values.sumOf { it.totalExpensePaisa }
    val messCashInHandPaisa = totalDepositPaisa - totalBazarPaisa - totalBillsPaisa

    return MessCalculationSummary(
      totalMeals = totalMeals,
      totalBazarPaisa = totalBazarPaisa,
      totalBillsPaisa = totalBillsPaisa,
      totalExtraPaisa = totalExtraPaisa,
      totalDepositPaisa = totalDepositPaisa,
      totalExpensesPaisa = totalExpensesPaisa,
      mealRatePaisa = mealRatePaisa,
      messCashInHandPaisa = messCashInHandPaisa,
      memberSummaries = summaries
    )
  }

  /**
   * Helper to format paisa to standard currency display "65.50 ৳"
   */
  fun formatPaisa(paisa: Long, currencySymbol: String = "৳", showSign: Boolean = false): String {
    val takaValue = paisa / 100.0
    val signStr = if (showSign && paisa > 0) "+" else ""
    return "$signStr${String.format(Locale.US, "%.2f", takaValue)} $currencySymbol"
  }

  /**
   * Helper to format meal rate with 2 decimals
   */
  fun formatMealRate(ratePaisa: Double, currencySymbol: String = "৳"): String {
    val rateTaka = ratePaisa / 100.0
    return "${String.format(Locale.US, "%.2f", rateTaka)} $currencySymbol"
  }
}
