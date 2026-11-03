package com.example.core.model

import com.example.core.calc.MemberCalculationSummary
import java.util.UUID

data class MessConfig(
  val id: String = "mess_default",
  val name: String = "Green House Mess",
  val joinCode: String = "654321",
  val managerId: String = "mem_1",
  val mealSystem: MealSystem = MealSystem.SHARED_COST,
  val fixedRatePaisa: Long = 6500L, // 65.00 ৳ in fixed rate mode
  val breakfastWeight: Double = 0.5,
  val lunchWeight: Double = 1.0,
  val dinnerWeight: Double = 1.0,
  val currencySymbol: String = "৳",
  val cutoffTimeHour: Int = 22, // 10 PM cut-off
  val autoApproveJoin: Boolean = true
)

data class Period(
  val id: String = "period_2026_11",
  val messId: String = "mess_default",
  val name: String = "November 2026",
  val startDate: String = "2026-11-01",
  val endDate: String = "2026-11-30",
  val isOpen: Boolean = true,
  val carryOverBalance: Boolean = true
)

data class Member(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val name: String,
  val phone: String,
  val email: String = "",
  val role: Role = Role.MEMBER,
  val active: Boolean = true,
  val openingBalancePaisa: Long = 0L, // + = mess owes member, - = member owes mess
  val joinedDate: String = "2026-11-01",
  val avatarColorHex: Long = 0xFF009688
)

data class MealEntry(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val periodId: String = "period_2026_11",
  val memberId: String,
  val date: String, // YYYY-MM-DD
  val breakfast: Double = 1.0,
  val lunch: Double = 1.0,
  val dinner: Double = 1.0,
  val guests: Double = 0.0,
  val updatedBy: String = "System"
) {
  fun rawMealCount(): Double = breakfast + lunch + dinner + guests
  fun weightedMealCount(bWeight: Double, lWeight: Double, dWeight: Double): Double =
    (breakfast * bWeight) + (lunch * lWeight) + (dinner * dWeight) + guests
}

data class BazarItem(
  val name: String,
  val qty: String = "",
  val unit: String = "kg",
  val costPaisa: Long = 0L
)

data class BazarRecord(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val periodId: String = "period_2026_11",
  val date: String,
  val shopperId: String,
  val shopperName: String,
  val items: List<BazarItem>,
  val totalPaisa: Long,
  val addToDeposit: Boolean = false,
  val linkedDepositId: String? = null
)

data class DepositRecord(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val periodId: String = "period_2026_11",
  val memberId: String,
  val date: String,
  val amountPaisa: Long, // negative allowed for correction
  val method: PaymentMethod = PaymentMethod.CASH,
  val note: String = "",
  val linkedBazarId: String? = null,
  val addedBy: String = "Manager"
)

data class BillRecord(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val periodId: String = "period_2026_11",
  val type: BillType,
  val name: String,
  val amountPaisa: Long,
  val splitMode: SplitMode = SplitMode.SHARED_EQUALLY,
  val participantIds: List<String> = emptyList(),
  val isCorrection: Boolean = false
)

data class ExtraCostRecord(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val periodId: String = "period_2026_11",
  val reason: String,
  val amountPaisa: Long, // negative allowed for correction
  val participantIds: List<String> = emptyList(),
  val date: String,
  val createdBy: String = "Manager"
)

data class Notice(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val title: String,
  val description: String,
  val pinned: Boolean = false,
  val createdAt: String,
  val createdBy: String,
  val readCount: Int = 0
)

data class MenuDay(
  val dayIndex: Int, // 1 = Monday ... 7 = Sunday
  val dayName: String,
  val breakfast: String,
  val lunch: String,
  val dinner: String
)

data class BazarRoutine(
  val dayIndex: Int,
  val dayName: String,
  val memberId: String,
  val memberName: String
)

data class AuditLog(
  val id: String = UUID.randomUUID().toString(),
  val messId: String = "mess_default",
  val entity: String,
  val entityId: String,
  val action: AuditAction,
  val details: String,
  val byUser: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class MonthSnapshot(
  val periodId: String,
  val periodName: String,
  val closedAt: String,
  val totalMeals: Double,
  val totalBazarPaisa: Long,
  val totalBillsPaisa: Long,
  val mealRatePaisa: Long,
  val memberSummaries: List<MemberCalculationSummary>
)

data class MessNotification(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val message: String,
  val category: String = "Meal Updates", // "Meal Updates", "Deposit", "System"
  val timestamp: String,
  val isRead: Boolean = false
)
