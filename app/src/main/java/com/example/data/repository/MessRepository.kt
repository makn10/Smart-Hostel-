package com.example.data.repository

import com.example.core.calc.CalculationEngine
import com.example.core.calc.MessCalculationSummary
import com.example.core.localization.AppLanguage
import com.example.core.model.AuditAction
import com.example.core.model.AuditLog
import com.example.core.model.BazarItem
import com.example.core.model.BazarRecord
import com.example.core.model.BazarRoutine
import com.example.core.model.BillRecord
import com.example.core.model.BillType
import com.example.core.model.DepositRecord
import com.example.core.model.ExtraCostRecord
import com.example.core.model.MealEntry
import com.example.core.model.MealSystem
import com.example.core.model.Member
import com.example.core.model.MenuDay
import com.example.core.model.MessConfig
import com.example.core.model.MessNotification
import com.example.core.model.MonthSnapshot
import com.example.core.model.Notice
import com.example.core.model.PaymentMethod
import com.example.core.model.Period
import com.example.core.model.Role
import com.example.core.model.SplitMode
import com.example.data.local.BazarExpenseDao
import com.example.data.local.BazarExpenseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MessRepository(
  val bazarExpenseDao: BazarExpenseDao? = null
) {

  private val _config = MutableStateFlow(MessConfig())
  val config: StateFlow<MessConfig> = _config.asStateFlow()

  private val _currentPeriod = MutableStateFlow(Period())
  val currentPeriod: StateFlow<Period> = _currentPeriod.asStateFlow()

  private val _allPeriods = MutableStateFlow<List<Period>>(listOf(_currentPeriod.value))
  val allPeriods: StateFlow<List<Period>> = _allPeriods.asStateFlow()

  private val _members = MutableStateFlow<List<Member>>(emptyList())
  val members: StateFlow<List<Member>> = _members.asStateFlow()

  private val _meals = MutableStateFlow<List<MealEntry>>(emptyList())
  val meals: StateFlow<List<MealEntry>> = _meals.asStateFlow()

  private val _bazarRecords = MutableStateFlow<List<BazarRecord>>(emptyList())
  val bazarRecords: StateFlow<List<BazarRecord>> = _bazarRecords.asStateFlow()

  private val _deposits = MutableStateFlow<List<DepositRecord>>(emptyList())
  val deposits: StateFlow<List<DepositRecord>> = _deposits.asStateFlow()

  private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
  val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

  private val _extraCosts = MutableStateFlow<List<ExtraCostRecord>>(emptyList())
  val extraCosts: StateFlow<List<ExtraCostRecord>> = _extraCosts.asStateFlow()

  private val _notices = MutableStateFlow<List<Notice>>(emptyList())
  val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

  private val _menu = MutableStateFlow<List<MenuDay>>(emptyList())
  val menu: StateFlow<List<MenuDay>> = _menu.asStateFlow()

  private val _routine = MutableStateFlow<List<BazarRoutine>>(emptyList())
  val routine: StateFlow<List<BazarRoutine>> = _routine.asStateFlow()

  private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
  val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

  private val _snapshots = MutableStateFlow<List<MonthSnapshot>>(emptyList())
  val snapshots: StateFlow<List<MonthSnapshot>> = _snapshots.asStateFlow()

  private val _notifications = MutableStateFlow<List<MessNotification>>(emptyList())
  val notifications: StateFlow<List<MessNotification>> = _notifications.asStateFlow()

  private val _roomBazarExpenses = MutableStateFlow<List<BazarExpenseEntity>>(emptyList())
  val roomBazarExpenses: StateFlow<List<BazarExpenseEntity>> = _roomBazarExpenses.asStateFlow()

  private val _currentUserId = MutableStateFlow<String>("")
  val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

  private val _language = MutableStateFlow(AppLanguage.ENGLISH)
  val language: StateFlow<AppLanguage> = _language.asStateFlow()

  suspend fun insertRoomBazarExpense(expense: BazarExpenseEntity, addToDeposit: Boolean = false) {
    bazarExpenseDao?.insertExpense(expense)
    val currentRoomList = _roomBazarExpenses.value.filter { it.id != expense.id }
    _roomBazarExpenses.value = listOf(expense) + currentRoomList

    // Also synchronize with calculation engine bazarRecords
    val bItem = BazarItem(name = "${expense.itemDescription} (${expense.category})", costPaisa = expense.totalAmountPaisa)
    addBazar(
      date = expense.date,
      shopperId = expense.shopperId.ifEmpty { _members.value.firstOrNull()?.id ?: "mem_1" },
      items = listOf(bItem),
      addToDeposit = addToDeposit
    )
    logAudit("Room Bazar", expense.itemDescription, AuditAction.CREATE, "Recorded ${CalculationEngine.formatPaisa(expense.totalAmountPaisa)} in ${expense.category}")
  }

  suspend fun deleteRoomBazarExpense(id: String) {
    bazarExpenseDao?.deleteExpenseById(id)
    _roomBazarExpenses.value = _roomBazarExpenses.value.filter { it.id != id }
    deleteBazar(id)
  }

  fun markAllNotificationsAsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
  }

  fun clearAllNotifications() {
    _notifications.value = emptyList()
  }

  fun addNotification(title: String, message: String, category: String = "Meal Updates") {
    val notif = MessNotification(
      title = title,
      message = message,
      category = category,
      timestamp = SimpleDateFormat("hh:mm a, dd MMM, yyyy", Locale.US).format(Date()),
      isRead = false
    )
    _notifications.value = listOf(notif) + _notifications.value
  }

  fun saveAllMemberMeals(
    date: String,
    memberMeals: Map<String, Triple<Double, Double, Double>>
  ) {
    val current = _meals.value.toMutableList()
    val byUser = getCurrentMember()?.name ?: "Manager"

    memberMeals.forEach { (memberId, triple) ->
      val (b, l, d) = triple
      val existingIndex = current.indexOfFirst { it.memberId == memberId && it.date == date }
      if (existingIndex >= 0) {
        current[existingIndex] = current[existingIndex].copy(
          breakfast = b,
          lunch = l,
          dinner = d,
          updatedBy = byUser
        )
      } else {
        current.add(
          MealEntry(
            periodId = _currentPeriod.value.id,
            memberId = memberId,
            date = date,
            breakfast = b,
            lunch = l,
            dinner = d,
            guests = 0.0,
            updatedBy = byUser
          )
        )
      }
    }
    _meals.value = current
    logAudit("Meal", "Batch update on $date", AuditAction.UPDATE, "Updated meals for ${memberMeals.size} members")
    addNotification(
      title = "Meal Update",
      message = "$date তারিখের জন্য সকল সদস্যের মিল আপডেট করা হয়েছে।",
      category = "Meal Updates"
    )
  }

  init {
    seedDefaultData()
  }

  fun setLanguage(lang: AppLanguage) {
    _language.value = lang
  }

  fun setCurrentUserId(id: String) {
    _currentUserId.value = id
  }

  fun getCurrentMember(): Member? {
    return _members.value.find { it.id == _currentUserId.value } ?: _members.value.firstOrNull()
  }

  fun getPeriodSummary(): MessCalculationSummary {
    return CalculationEngine.calculatePeriodSummary(
      config = _config.value,
      members = _members.value,
      meals = _meals.value.filter { it.periodId == _currentPeriod.value.id },
      bazarRecords = _bazarRecords.value.filter { it.periodId == _currentPeriod.value.id },
      deposits = _deposits.value.filter { it.periodId == _currentPeriod.value.id },
      bills = _bills.value.filter { it.periodId == _currentPeriod.value.id },
      extraCosts = _extraCosts.value.filter { it.periodId == _currentPeriod.value.id }
    )
  }

  // --- CRUD Operations ---

  fun addOrUpdateMember(member: Member) {
    val current = _members.value.toMutableList()
    val index = current.indexOfFirst { it.id == member.id }
    val action = if (index >= 0) {
      current[index] = member
      AuditAction.UPDATE
    } else {
      current.add(member)
      AuditAction.CREATE
    }
    _members.value = current
    logAudit("Member", member.name, action, "Updated member details")
  }

  fun toggleMemberActive(memberId: String) {
    _members.value = _members.value.map {
      if (it.id == memberId) it.copy(active = !it.active) else it
    }
    val mem = _members.value.find { it.id == memberId }
    logAudit("Member", mem?.name ?: memberId, AuditAction.UPDATE, "Toggled active state")
  }

  fun setMeal(
    memberId: String,
    date: String,
    breakfast: Double,
    lunch: Double,
    dinner: Double,
    guests: Double = 0.0
  ) {
    val current = _meals.value.toMutableList()
    val existingIndex = current.indexOfFirst { it.memberId == memberId && it.date == date }
    val byUser = getCurrentMember()?.name ?: "Manager"

    if (existingIndex >= 0) {
      current[existingIndex] = current[existingIndex].copy(
        breakfast = breakfast,
        lunch = lunch,
        dinner = dinner,
        guests = guests,
        updatedBy = byUser
      )
    } else {
      current.add(
        MealEntry(
          periodId = _currentPeriod.value.id,
          memberId = memberId,
          date = date,
          breakfast = breakfast,
          lunch = lunch,
          dinner = dinner,
          guests = guests,
          updatedBy = byUser
        )
      )
    }
    _meals.value = current
    logAudit("Meal", "$date for $memberId", AuditAction.UPDATE, "B:$breakfast L:$lunch D:$dinner G:$guests")
  }

  fun setMealsForAll(
    date: String,
    breakfast: Double,
    lunch: Double,
    dinner: Double
  ) {
    _members.value.filter { it.active }.forEach { member ->
      setMeal(member.id, date, breakfast, lunch, dinner)
    }
  }

  fun addBazar(
    date: String,
    shopperId: String,
    items: List<BazarItem>,
    addToDeposit: Boolean
  ) {
    val totalPaisa = items.sumOf { it.costPaisa }
    val shopper = _members.value.find { it.id == shopperId }
    val shopperName = shopper?.name ?: "Unknown"
    val bazarId = UUID.randomUUID().toString()

    var linkedDepositId: String? = null
    if (addToDeposit && totalPaisa > 0) {
      linkedDepositId = UUID.randomUUID().toString()
      val deposit = DepositRecord(
        id = linkedDepositId,
        periodId = _currentPeriod.value.id,
        memberId = shopperId,
        date = date,
        amountPaisa = totalPaisa,
        method = PaymentMethod.CASH,
        note = "Auto-credited from bazar on $date",
        linkedBazarId = bazarId,
        addedBy = "System (Bazar)"
      )
      _deposits.value = _deposits.value + deposit
    }

    val record = BazarRecord(
      id = bazarId,
      periodId = _currentPeriod.value.id,
      date = date,
      shopperId = shopperId,
      shopperName = shopperName,
      items = items,
      totalPaisa = totalPaisa,
      addToDeposit = addToDeposit,
      linkedDepositId = linkedDepositId
    )
    _bazarRecords.value = listOf(record) + _bazarRecords.value
    logAudit("Bazar", "$date by $shopperName", AuditAction.CREATE, "Total: ${CalculationEngine.formatPaisa(totalPaisa)}")
  }

  fun deleteBazar(id: String) {
    val bazar = _bazarRecords.value.find { it.id == id } ?: return
    // Remove linked deposit if any
    if (bazar.linkedDepositId != null) {
      _deposits.value = _deposits.value.filter { it.id != bazar.linkedDepositId }
    }
    _bazarRecords.value = _bazarRecords.value.filter { it.id != id }
    logAudit("Bazar", id, AuditAction.DELETE, "Deleted bazar of ${CalculationEngine.formatPaisa(bazar.totalPaisa)}")
  }

  fun addDeposit(
    memberId: String,
    date: String,
    amountPaisa: Long,
    method: PaymentMethod,
    note: String
  ) {
    val record = DepositRecord(
      periodId = _currentPeriod.value.id,
      memberId = memberId,
      date = date,
      amountPaisa = amountPaisa,
      method = method,
      note = note,
      addedBy = getCurrentMember()?.name ?: "Manager"
    )
    _deposits.value = listOf(record) + _deposits.value
    val member = _members.value.find { it.id == memberId }
    logAudit("Deposit", "${member?.name ?: memberId}", AuditAction.CREATE, "Amount: ${CalculationEngine.formatPaisa(amountPaisa)}")
  }

  fun deleteDeposit(id: String) {
    val dep = _deposits.value.find { it.id == id } ?: return
    _deposits.value = _deposits.value.filter { it.id != id }
    logAudit("Deposit", id, AuditAction.DELETE, "Deleted deposit of ${CalculationEngine.formatPaisa(dep.amountPaisa)}")
  }

  fun addBill(
    type: BillType,
    name: String,
    amountPaisa: Long,
    splitMode: SplitMode,
    participantIds: List<String>
  ) {
    val bill = BillRecord(
      periodId = _currentPeriod.value.id,
      type = type,
      name = name,
      amountPaisa = amountPaisa,
      splitMode = splitMode,
      participantIds = participantIds
    )
    _bills.value = listOf(bill) + _bills.value
    logAudit("Bill", name, AuditAction.CREATE, "Amount: ${CalculationEngine.formatPaisa(amountPaisa)}")
  }

  fun deleteBill(id: String) {
    _bills.value = _bills.value.filter { it.id != id }
    logAudit("Bill", id, AuditAction.DELETE, "Deleted bill")
  }

  fun addExtraCost(
    reason: String,
    amountPaisa: Long,
    participantIds: List<String>,
    date: String
  ) {
    val cost = ExtraCostRecord(
      periodId = _currentPeriod.value.id,
      reason = reason,
      amountPaisa = amountPaisa,
      participantIds = participantIds,
      date = date,
      createdBy = getCurrentMember()?.name ?: "Manager"
    )
    _extraCosts.value = listOf(cost) + _extraCosts.value
    logAudit("Extra Cost", reason, AuditAction.CREATE, "Amount: ${CalculationEngine.formatPaisa(amountPaisa)}")
  }

  fun deleteExtraCost(id: String) {
    _extraCosts.value = _extraCosts.value.filter { it.id != id }
    logAudit("Extra Cost", id, AuditAction.DELETE, "Deleted extra cost")
  }

  fun addNotice(title: String, description: String, pinned: Boolean) {
    val notice = Notice(
      title = title,
      description = description,
      pinned = pinned,
      createdAt = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
      createdBy = getCurrentMember()?.name ?: "Manager",
      readCount = 1
    )
    _notices.value = listOf(notice) + _notices.value
    logAudit("Notice", title, AuditAction.CREATE, "Published notice")
  }

  fun updateMenuDay(dayIndex: Int, breakfast: String, lunch: String, dinner: String) {
    _menu.value = _menu.value.map {
      if (it.dayIndex == dayIndex) {
        it.copy(breakfast = breakfast, lunch = lunch, dinner = dinner)
      } else it
    }
    logAudit("Menu", "Day $dayIndex", AuditAction.UPDATE, "Updated meal menu")
  }

  fun updateRoutine(dayIndex: Int, memberId: String) {
    val member = _members.value.find { it.id == memberId } ?: return
    _routine.value = _routine.value.map {
      if (it.dayIndex == dayIndex) {
        it.copy(memberId = memberId, memberName = member.name)
      } else it
    }
    logAudit("Routine", "Day $dayIndex", AuditAction.UPDATE, "Assigned to ${member.name}")
  }

  fun updateMessConfig(newConfig: MessConfig) {
    _config.value = newConfig
    logAudit("Settings", "Mess Config", AuditAction.UPDATE, "Updated mess preferences")
  }

  fun closeAndArchiveMonth(
    archiveName: String,
    carryOverBalances: Boolean
  ) {
    val summary = getPeriodSummary()
    val period = _currentPeriod.value

    val snapshot = MonthSnapshot(
      periodId = period.id,
      periodName = archiveName,
      closedAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()),
      totalMeals = summary.totalMeals,
      totalBazarPaisa = summary.totalBazarPaisa,
      totalBillsPaisa = summary.totalBillsPaisa,
      mealRatePaisa = summary.mealRatePaisa.toLong(),
      memberSummaries = summary.memberSummaries.values.toList()
    )

    _snapshots.value = listOf(snapshot) + _snapshots.value

    // Create next period
    val nextPeriodId = "period_" + UUID.randomUUID().toString().take(6)
    val nextPeriod = Period(
      id = nextPeriodId,
      name = "Next Month Period",
      startDate = "2026-12-01",
      endDate = "2026-12-31",
      isOpen = true,
      carryOverBalance = carryOverBalances
    )
    _currentPeriod.value = nextPeriod
    _allPeriods.value = listOf(nextPeriod) + _allPeriods.value

    // If carryOverBalances is enabled, set openingBalancePaisa for each member
    if (carryOverBalances) {
      _members.value = _members.value.map { member ->
        val finalBal = summary.memberSummaries[member.id]?.finalBalancePaisa ?: 0L
        member.copy(openingBalancePaisa = finalBal)
      }
    }

    logAudit("Period", archiveName, AuditAction.CLOSE_MONTH, "Archived month with carry over: $carryOverBalances")
  }

  fun reopenMonth(snapshotPeriodId: String) {
    val snap = _snapshots.value.find { it.periodId == snapshotPeriodId }
    if (snap != null) {
      _snapshots.value = _snapshots.value.filter { it.periodId != snapshotPeriodId }
      val reopened = Period(
        id = snap.periodId,
        name = snap.periodName,
        startDate = "2026-11-01",
        endDate = "2026-11-30",
        isOpen = true
      )
      _currentPeriod.value = reopened
      logAudit("Period", snap.periodName, AuditAction.UPDATE, "Reopened closed month")
    }
  }

  private fun logAudit(entity: String, entityId: String, action: AuditAction, details: String) {
    val log = AuditLog(
      entity = entity,
      entityId = entityId,
      action = action,
      details = details,
      byUser = getCurrentMember()?.name ?: "Manager"
    )
    _auditLogs.value = listOf(log) + _auditLogs.value.take(99)
  }

  fun seedDefaultData() {
    val m1 = Member(
      id = "mem_1",
      name = "Tanvir Ahmed",
      phone = "01711223344",
      email = "tanvir@example.com",
      role = Role.MANAGER,
      active = true,
      openingBalancePaisa = 150000L, // 1500.00 ৳ previous carry
      avatarColorHex = 0xFF009688
    )
    val m2 = Member(
      id = "mem_2",
      name = "Rahim Chowdhury",
      phone = "01722334455",
      email = "rahim@example.com",
      role = Role.SUB_MANAGER,
      active = true,
      openingBalancePaisa = 50000L, // 500.00 ৳
      avatarColorHex = 0xFF3F51B5
    )
    val m3 = Member(
      id = "mem_3",
      name = "Sakib Al Hasan",
      phone = "01733445566",
      email = "sakib@example.com",
      role = Role.MEMBER,
      active = true,
      openingBalancePaisa = -45000L, // -450.00 ৳ due from last month
      avatarColorHex = 0xFFE91E63
    )
    val m4 = Member(
      id = "mem_4",
      name = "Mahmudul Hasan",
      phone = "01744556677",
      email = "mahmud@example.com",
      role = Role.MEMBER,
      active = true,
      openingBalancePaisa = 0L,
      avatarColorHex = 0xFFFF9800
    )
    val m5 = Member(
      id = "mem_5",
      name = "Anik Sarker",
      phone = "01755667788",
      email = "anik@example.com",
      role = Role.MEMBER,
      active = true,
      openingBalancePaisa = 20000L,
      avatarColorHex = 0xFF4CAF50
    )
    val m6 = Member(
      id = "mem_6",
      name = "Kamrul Islam",
      phone = "01766778899",
      email = "kamrul@example.com",
      role = Role.MEMBER,
      active = true,
      openingBalancePaisa = -10000L,
      avatarColorHex = 0xFF9C27B0
    )

    _members.value = listOf(m1, m2, m3, m4, m5, m6)
    _currentUserId.value = m1.id

    // Seed 30 days of meals
    val sampleMeals = mutableListOf<MealEntry>()
    val membersList = listOf(m1, m2, m3, m4, m5, m6)
    for (day in 1..25) {
      val dateStr = String.format(Locale.US, "2026-11-%02d", day)
      membersList.forEachIndexed { idx, mem ->
        // realistic meals: 1 B, 1 L, 1 D, with occasional 0.5 or 0 on weekends
        val isWeekend = (day % 7 == 5 || day % 7 == 6)
        val b = if (isWeekend && idx % 2 == 0) 0.0 else 1.0
        val l = if (isWeekend) 1.0 else 1.0
        val d = 1.0
        sampleMeals.add(
          MealEntry(
            periodId = _currentPeriod.value.id,
            memberId = mem.id,
            date = dateStr,
            breakfast = b,
            lunch = l,
            dinner = d,
            guests = if (day == 10 && mem.id == m1.id) 2.0 else 0.0
          )
        )
      }
    }
    _meals.value = sampleMeals

    // Seed Bazar records
    val b1 = BazarRecord(
      periodId = _currentPeriod.value.id,
      date = "2026-11-01",
      shopperId = m1.id,
      shopperName = m1.name,
      items = listOf(
        BazarItem("Rui Fish", "2.5", "kg", 85000L),
        BazarItem("Vegetables (Potato, Cauliflower)", "5", "kg", 32000L),
        BazarItem("Soybean Oil", "2", "litres", 38000L)
      ),
      totalPaisa = 155000L, // 1550.00 ৳
      addToDeposit = true,
      linkedDepositId = "dep_bazar_1"
    )

    val b2 = BazarRecord(
      periodId = _currentPeriod.value.id,
      date = "2026-11-04",
      shopperId = m2.id,
      shopperName = m2.name,
      items = listOf(
        BazarItem("Broiler Chicken", "3", "kg", 66000L),
        BazarItem("Miniket Rice", "25", "kg", 185000L),
        BazarItem("Spices & Onion", "2", "kg", 24000L)
      ),
      totalPaisa = 275000L, // 2750.00 ৳
      addToDeposit = true,
      linkedDepositId = "dep_bazar_2"
    )

    val b3 = BazarRecord(
      periodId = _currentPeriod.value.id,
      date = "2026-11-08",
      shopperId = m3.id,
      shopperName = m3.name,
      items = listOf(
        BazarItem("Beef", "2", "kg", 150000L),
        BazarItem("Eggs", "30", "pcs", 42000L),
        BazarItem("Lentils (Dal)", "2", "kg", 26000L)
      ),
      totalPaisa = 218000L, // 2180.00 ৳
      addToDeposit = false
    )

    val b4 = BazarRecord(
      periodId = _currentPeriod.value.id,
      date = "2026-11-12",
      shopperId = m4.id,
      shopperName = m4.name,
      items = listOf(
        BazarItem("Hilsha Fish", "1.2", "kg", 120000L),
        BazarItem("Green Chili & Coriander", "500", "g", 15000L),
        BazarItem("Mustard Oil", "1", "litre", 28000L)
      ),
      totalPaisa = 163000L, // 1630.00 ৳
      addToDeposit = true,
      linkedDepositId = "dep_bazar_4"
    )

    _bazarRecords.value = listOf(b4, b3, b2, b1)

    // Seed Deposits
    val d1 = DepositRecord(
      id = "dep_bazar_1",
      periodId = _currentPeriod.value.id,
      memberId = m1.id,
      date = "2026-11-01",
      amountPaisa = 155000L,
      method = PaymentMethod.CASH,
      note = "Auto-credited from bazar on 2026-11-01",
      linkedBazarId = b1.id
    )
    val d2 = DepositRecord(
      id = "dep_bazar_2",
      periodId = _currentPeriod.value.id,
      memberId = m2.id,
      date = "2026-11-04",
      amountPaisa = 275000L,
      method = PaymentMethod.CASH,
      note = "Auto-credited from bazar on 2026-11-04",
      linkedBazarId = b2.id
    )
    val d3 = DepositRecord(
      periodId = _currentPeriod.value.id,
      memberId = m3.id,
      date = "2026-11-02",
      amountPaisa = 300000L, // 3000.00 ৳
      method = PaymentMethod.BKASH,
      note = "bKash TrxID: 9XKJ32"
    )
    val d4 = DepositRecord(
      periodId = _currentPeriod.value.id,
      memberId = m4.id,
      date = "2026-11-02",
      amountPaisa = 350000L, // 3500.00 ৳
      method = PaymentMethod.NAGAD,
      note = "Nagad TrxID: NG8832"
    )
    val d5 = DepositRecord(
      periodId = _currentPeriod.value.id,
      memberId = m5.id,
      date = "2026-11-03",
      amountPaisa = 400000L, // 4000.00 ৳
      method = PaymentMethod.BANK,
      note = "Bank transfer deposit"
    )
    val d6 = DepositRecord(
      periodId = _currentPeriod.value.id,
      memberId = m6.id,
      date = "2026-11-05",
      amountPaisa = 250000L, // 2500.00 ৳
      method = PaymentMethod.CASH,
      note = "Cash hand deposit"
    )
    val d7 = DepositRecord(
      id = "dep_bazar_4",
      periodId = _currentPeriod.value.id,
      memberId = m4.id,
      date = "2026-11-12",
      amountPaisa = 163000L,
      method = PaymentMethod.CASH,
      note = "Auto-credited from bazar on 2026-11-12",
      linkedBazarId = b4.id
    )
    _deposits.value = listOf(d7, d6, d5, d4, d3, d2, d1)

    // Seed Bills
    val bill1 = BillRecord(
      periodId = _currentPeriod.value.id,
      type = BillType.GAS,
      name = "Gas Cylinder Bill",
      amountPaisa = 145000L, // 1450.00 ৳
      splitMode = SplitMode.SHARED_EQUALLY
    )
    val bill2 = BillRecord(
      periodId = _currentPeriod.value.id,
      type = BillType.ELECTRICITY,
      name = "DESCO Electricity Bill",
      amountPaisa = 280000L, // 2800.00 ৳
      splitMode = SplitMode.SHARED_EQUALLY
    )
    val bill3 = BillRecord(
      periodId = _currentPeriod.value.id,
      type = BillType.MAID,
      name = "Bua / Cook Salary",
      amountPaisa = 420000L, // 4200.00 ৳
      splitMode = SplitMode.SHARED_EQUALLY
    )
    val bill4 = BillRecord(
      periodId = _currentPeriod.value.id,
      type = BillType.INTERNET,
      name = "Fiber Optical WiFi",
      amountPaisa = 80000L, // 800.00 ৳
      splitMode = SplitMode.SHARED_EQUALLY
    )
    _bills.value = listOf(bill1, bill2, bill3, bill4)

    // Seed Extra Costs (Feast)
    val extra1 = ExtraCostRecord(
      periodId = _currentPeriod.value.id,
      reason = "Friday Special Mutton Kacchi Biryani",
      amountPaisa = 180000L, // 1800.00 ৳
      participantIds = listOf(m1.id, m2.id, m3.id, m4.id, m5.id),
      date = "2026-11-06"
    )
    _extraCosts.value = listOf(extra1)

    // Seed Notices
    val n1 = Notice(
      title = "Bazar Schedule for November updated",
      description = "Everyone please check your bazar routine day. In case of emergency exam or absence, arrange a swap with another roommate in advance.",
      pinned = true,
      createdAt = "01 Nov 2026",
      createdBy = "Tanvir Ahmed (Manager)",
      readCount = 6
    )
    val n2 = Notice(
      title = "Electricity bill payment due date: 15 Nov",
      description = "Please deposit your minimum advance 3000 ৳ before 10th of this month to ensure smooth bazar and utility payments.",
      pinned = false,
      createdAt = "03 Nov 2026",
      createdBy = "Tanvir Ahmed (Manager)",
      readCount = 5
    )
    _notices.value = listOf(n1, n2)

    // Seed Weekly Menu
    _menu.value = listOf(
      MenuDay(1, "Monday", "Roti, Dal, Fried Egg", "Rice, Rui Fish, Lentils", "Rice, Chicken Curry, Salad"),
      MenuDay(2, "Tuesday", "Khichuri, Pickles, Fried Egg", "Rice, Tilapia Fish, Mixed Veg", "Rice, Beef Bhuna, Dal"),
      MenuDay(3, "Wednesday", "Paratha, Potato Curry (Aloo Dum)", "Rice, Egg Curry, Spinach (Palong Shak)", "Rice, Chicken Curry, Salad"),
      MenuDay(4, "Thursday", "Roti, Dal, Boiled Egg", "Rice, Pangas/Hilsha, Veggies", "Rice, Beef Curry, Lentils"),
      MenuDay(5, "Friday", "Special Biryani / Polao", "Rice, Mutton Rezala, Borhani", "Rice, Fried Fish, Dal"),
      MenuDay(6, "Saturday", "Khichuri, Begun Bhaja", "Rice, Chicken Roast, Veggies", "Rice, Rui Fish Curry"),
      MenuDay(7, "Sunday", "Paratha, Egg Omelet, Tea", "Rice, Beef Kala Bhuna, Dal", "Rice, Chicken Stew, Salad")
    )

    // Seed Routine
    _routine.value = listOf(
      BazarRoutine(1, "Monday", m1.id, m1.name),
      BazarRoutine(2, "Tuesday", m2.id, m2.name),
      BazarRoutine(3, "Wednesday", m3.id, m3.name),
      BazarRoutine(4, "Thursday", m4.id, m4.name),
      BazarRoutine(5, "Friday", m5.id, m5.name),
      BazarRoutine(6, "Saturday", m6.id, m6.name),
      BazarRoutine(7, "Sunday", m1.id, m1.name)
    )

    // Seed Audit Logs
    _auditLogs.value = listOf(
      AuditLog(entity = "System", entityId = "Init", action = AuditAction.CREATE, details = "Smart Mess v2 initialized with 6 members", byUser = "System"),
      AuditLog(entity = "Bazar", entityId = "b1", action = AuditAction.CREATE, details = "Tanvir added Bazar 1550.00 ৳ (linked to deposit)", byUser = "Tanvir Ahmed"),
      AuditLog(entity = "Deposit", entityId = "d3", action = AuditAction.CREATE, details = "Sakib deposited 3000.00 ৳ via bKash", byUser = "Manager")
    )

    // Seed Notifications (matching Screenshot 2)
    _notifications.value = listOf(
      MessNotification(
        title = "Meal Update",
        message = "07 Oct, 2026 তারিখের জন্য আপনার 1.5 টি মিল অন করা হয়েছে।",
        category = "Meal Updates",
        timestamp = "10:19 PM, 29 Oct, 2026",
        isRead = false
      ),
      MessNotification(
        title = "Meal Update",
        message = "আজকের জন্য আপনার 1 টি মিল অন করা হয়েছে।",
        category = "Meal Updates",
        timestamp = "10:19 PM, 29 Oct, 2026",
        isRead = false
      ),
      MessNotification(
        title = "Meal Update",
        message = "06 Oct, 2026 তারিখের জন্য আপনার 1 টি মিল অন করা হয়েছে।",
        category = "Meal Updates",
        timestamp = "9:59 PM, 29 Oct, 2026",
        isRead = false
      ),
      MessNotification(
        title = "Meal Update",
        message = "আজকের জন্য আপনার মিল অফ রাখা হয়েছে।",
        category = "Meal Updates",
        timestamp = "9:59 PM, 29 Oct, 2026",
        isRead = false
      )
    )
  }
}
