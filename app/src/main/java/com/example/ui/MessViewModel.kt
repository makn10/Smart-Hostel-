package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.calc.CalculationEngine
import com.example.core.calc.MessCalculationSummary
import com.example.core.localization.AppLanguage
import com.example.core.model.BazarItem
import com.example.core.model.BillType
import com.example.core.model.Member
import com.example.core.model.MessConfig
import com.example.core.model.PaymentMethod
import com.example.core.model.SplitMode
import com.example.data.firebase.FirestoreService
import com.example.data.local.BazarExpenseEntity
import com.example.data.local.MessRoomDatabase
import com.example.data.repository.MessRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MessViewModel(
  application: Application
) : AndroidViewModel(application) {

  val firestoreService = FirestoreService(application)
  private val roomDb = MessRoomDatabase.getDatabase(application)
  val repository = MessRepository(bazarExpenseDao = roomDb.bazarExpenseDao())

  private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _currentMessId = MutableStateFlow<String?>("mess_default")
  val currentMessId: StateFlow<String?> = _currentMessId.asStateFlow()

  private val _isAuthProcessing = MutableStateFlow(false)
  val isAuthProcessing: StateFlow<Boolean> = _isAuthProcessing.asStateFlow()

  private val _authErrorMessage = MutableStateFlow<String?>(null)
  val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

  init {
    val authListener = FirebaseAuth.AuthStateListener { auth ->
      val user = auth.currentUser
      _currentUser.value = user
      if (user != null) {
        viewModelScope.launch {
          firestoreService.saveUserProfile(user)
          val messId = firestoreService.getUserCurrentMessId(user.uid)
          if (!messId.isNullOrBlank()) {
            _currentMessId.value = messId
          }
        }
      } else {
        _currentMessId.value = null
      }
    }
    Firebase.auth.addAuthStateListener(authListener)
  }

  fun onUserAuthenticated(user: FirebaseUser) {
    _currentUser.value = user
    _authErrorMessage.value = null
    viewModelScope.launch {
      firestoreService.saveUserProfile(user)
      val messId = firestoreService.getUserCurrentMessId(user.uid)
      _currentMessId.value = messId
      showMessage("Welcome, ${user.displayName ?: "Member"}!")
    }
  }

  fun createMess(name: String, mealSystem: String, ratePaisa: Long) {
    val user = _currentUser.value ?: return
    viewModelScope.launch {
      _isAuthProcessing.value = true
      _authErrorMessage.value = null
      val result = firestoreService.createMess(
        name = name,
        mealSystem = mealSystem,
        mealRatePaisa = ratePaisa,
        managerUid = user.uid,
        managerName = user.displayName ?: "Manager"
      )
      _isAuthProcessing.value = false
      result.onSuccess { messId ->
        _currentMessId.value = messId
        val mealSysEnum = if (mealSystem == "fixed") com.example.core.model.MealSystem.FIXED_RATE else com.example.core.model.MealSystem.SHARED_COST
        repository.updateMessConfig(
          config.value.copy(
            id = messId,
            name = name,
            mealSystem = mealSysEnum,
            fixedRatePaisa = ratePaisa
          )
        )
        showMessage("Mess '$name' created successfully!")
      }.onFailure { err ->
        _authErrorMessage.value = err.localizedMessage ?: "Failed to create mess"
      }
    }
  }

  fun joinMess(code: String) {
    val user = _currentUser.value ?: return
    viewModelScope.launch {
      _isAuthProcessing.value = true
      _authErrorMessage.value = null
      val result = firestoreService.joinMessByCode(
        code = code,
        userUid = user.uid,
        userName = user.displayName ?: "Member"
      )
      _isAuthProcessing.value = false
      result.onSuccess { messId ->
        _currentMessId.value = messId
        showMessage("Joined mess successfully!")
      }.onFailure { err ->
        _authErrorMessage.value = err.localizedMessage ?: "Failed to join mess"
      }
    }
  }

  fun signOut() {
    Firebase.auth.signOut()
    _currentUser.value = null
    _currentMessId.value = null
    showMessage("Logged out")
  }

  fun saveFullSystemToCloud() {
    val messId = _currentMessId.value ?: "mess_default"
    val user = _currentUser.value
    if (user == null) {
      showMessage("Please log in with Google to sync data to Cloud Firestore")
      return
    }

    viewModelScope.launch {
      try {
        // Sync members
        val currentMembers = members.value
        // Sync meals
        val currentMeals = meals.value
        currentMeals.forEach { m ->
          firestoreService.saveDailyMeal(
            messId = messId,
            date = m.date,
            memberId = m.memberId,
            memberName = currentMembers.find { it.id == m.memberId }?.name ?: "Member",
            breakfast = m.breakfast,
            lunch = m.lunch,
            dinner = m.dinner,
            guests = m.guests
          )
        }

        // Sync bazar records
        bazarRecords.value.forEach { b ->
          firestoreService.saveBazarRecord(messId, b)
        }

        // Sync deposits
        deposits.value.forEach { d ->
          firestoreService.saveDepositRecord(messId, d)
        }

        // Sync notices
        notices.value.forEach { n ->
          firestoreService.saveNotice(messId, n.title, n.description, n.pinned, n.createdBy)
        }

        showMessage("All data successfully saved to Cloud Firestore!")
      } catch (e: Exception) {
        showMessage("Sync failed: ${e.message}")
      }
    }
  }

  val roomBazarExpenses: StateFlow<List<BazarExpenseEntity>> = roomDb.bazarExpenseDao().getAllExpenses()
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

  fun saveDailyBazarExpense(
    date: String,
    itemDescription: String,
    category: String,
    totalAmountPaisa: Long,
    shopperId: String,
    shopperName: String,
    addToDeposit: Boolean
  ) {
    viewModelScope.launch {
      val entity = BazarExpenseEntity(
        date = date,
        itemDescription = itemDescription,
        category = category,
        totalAmountPaisa = totalAmountPaisa,
        shopperId = shopperId,
        shopperName = shopperName
      )
      repository.insertRoomBazarExpense(entity, addToDeposit)
      val formatted = CalculationEngine.formatPaisa(totalAmountPaisa)
      showMessage("Saved $itemDescription ($formatted) into Room database")
    }
  }

  fun deleteRoomBazarExpense(id: String) {
    viewModelScope.launch {
      repository.deleteRoomBazarExpense(id)
      showMessage("Deleted expense from Room database")
    }
  }

  val config = repository.config
  val currentPeriod = repository.currentPeriod
  val allPeriods = repository.allPeriods
  val members = repository.members
  val meals = repository.meals
  val bazarRecords = repository.bazarRecords
  val deposits = repository.deposits
  val bills = repository.bills
  val extraCosts = repository.extraCosts
  val notices = repository.notices
  val menu = repository.menu
  val routine = repository.routine
  val auditLogs = repository.auditLogs
  val snapshots = repository.snapshots
  val notifications = repository.notifications
  val language = repository.language
  val currentUserId = repository.currentUserId

  fun markAllNotificationsAsRead() {
    repository.markAllNotificationsAsRead()
    showMessage("All notifications marked as read")
  }

  fun clearAllNotifications() {
    repository.clearAllNotifications()
    showMessage("All notifications cleared")
  }

  fun saveAllMemberMeals(date: String, memberMeals: Map<String, Triple<Double, Double, Double>>) {
    repository.saveAllMemberMeals(date, memberMeals)
    showMessage("Saved meals for all members on $date")
  }

  // Reactive summary combining all components
  val periodSummary: StateFlow<MessCalculationSummary> = combine(
    config,
    members,
    meals,
    bazarRecords,
    deposits,
    bills,
    extraCosts
  ) { _ ->
    repository.getPeriodSummary()
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    repository.getPeriodSummary()
  )

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }

  fun showMessage(msg: String) {
    _snackbarMessage.value = msg
  }

  fun getCurrentMember(): Member? {
    return repository.getCurrentMember()
  }

  fun switchUser(memberId: String) {
    repository.setCurrentUserId(memberId)
    val member = members.value.find { it.id == memberId }
    showMessage("Switched active view to ${member?.name} (${member?.role?.label})")
  }

  fun toggleLanguage() {
    val next = if (language.value == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
    repository.setLanguage(next)
    showMessage(if (next == AppLanguage.BANGLA) "ভাষা পরিবর্তন করা হয়েছে: বাংলা" else "Language switched to English")
  }

  fun setLanguage(lang: AppLanguage) {
    repository.setLanguage(lang)
  }

  fun addOrUpdateMember(member: Member) {
    repository.addOrUpdateMember(member)
    showMessage("Member '${member.name}' saved")
  }

  fun toggleMemberActive(memberId: String) {
    repository.toggleMemberActive(memberId)
  }

  fun setMeal(
    memberId: String,
    date: String,
    breakfast: Double,
    lunch: Double,
    dinner: Double,
    guests: Double = 0.0
  ) {
    repository.setMeal(memberId, date, breakfast, lunch, dinner, guests)
  }

  fun setMealsForAll(
    date: String,
    breakfast: Double,
    lunch: Double,
    dinner: Double
  ) {
    repository.setMealsForAll(date, breakfast, lunch, dinner)
    showMessage("Meals updated for all members on $date")
  }

  fun addBazar(
    date: String,
    shopperId: String,
    items: List<BazarItem>,
    addToDeposit: Boolean
  ) {
    repository.addBazar(date, shopperId, items, addToDeposit)
    val totalPaisa = items.sumOf { it.costPaisa }
    val formatted = CalculationEngine.formatPaisa(totalPaisa)
    showMessage("Bazar record of $formatted saved")
  }

  fun deleteBazar(id: String) {
    repository.deleteBazar(id)
    showMessage("Bazar record deleted")
  }

  fun addDeposit(
    memberId: String,
    date: String,
    amountPaisa: Long,
    method: PaymentMethod,
    note: String
  ) {
    repository.addDeposit(memberId, date, amountPaisa, method, note)
    val formatted = CalculationEngine.formatPaisa(amountPaisa)
    showMessage("Deposit of $formatted recorded")
  }

  fun deleteDeposit(id: String) {
    repository.deleteDeposit(id)
    showMessage("Deposit record deleted")
  }

  fun addBill(
    type: BillType,
    name: String,
    amountPaisa: Long,
    splitMode: SplitMode,
    participantIds: List<String>
  ) {
    repository.addBill(type, name, amountPaisa, splitMode, participantIds)
    showMessage("Bill '$name' saved")
  }

  fun deleteBill(id: String) {
    repository.deleteBill(id)
    showMessage("Bill removed")
  }

  fun addExtraCost(
    reason: String,
    amountPaisa: Long,
    participantIds: List<String>,
    date: String
  ) {
    repository.addExtraCost(reason, amountPaisa, participantIds, date)
    showMessage("Extra cost '$reason' saved")
  }

  fun deleteExtraCost(id: String) {
    repository.deleteExtraCost(id)
    showMessage("Extra cost removed")
  }

  fun postNotice(title: String, description: String, pinned: Boolean) {
    repository.addNotice(title, description, pinned)
    showMessage("Notice published")
  }

  fun updateMenuDay(dayIndex: Int, breakfast: String, lunch: String, dinner: String) {
    repository.updateMenuDay(dayIndex, breakfast, lunch, dinner)
    showMessage("Weekly menu updated")
  }

  fun updateRoutine(dayIndex: Int, memberId: String) {
    repository.updateRoutine(dayIndex, memberId)
    showMessage("Bazar routine duty updated")
  }

  fun updateConfig(newConfig: MessConfig) {
    repository.updateMessConfig(newConfig)
    showMessage("Mess settings saved")
  }

  fun closeMonth(archiveName: String, carryOver: Boolean) {
    repository.closeAndArchiveMonth(archiveName, carryOver)
    showMessage("Month '$archiveName' successfully archived!")
  }

  fun reopenMonth(periodId: String) {
    repository.reopenMonth(periodId)
    showMessage("Archived month reopened")
  }

  fun resetSeedData() {
    repository.seedDefaultData()
    showMessage("Demo data loaded with 6 members and 1 month activity")
  }

  fun generateMonthStatementText(): String {
    val summary = periodSummary.value
    val period = currentPeriod.value
    val configVal = config.value

    val sb = StringBuilder()
    sb.appendLine("==========================================")
    sb.appendLine("     SMART MESS STATEMENT - ${period.name.uppercase()}")
    sb.appendLine("==========================================")
    sb.appendLine("Mess Name: ${configVal.name}")
    sb.appendLine("Total Meals: ${summary.totalMeals}")
    sb.appendLine("Total Bazar Cost: ${CalculationEngine.formatPaisa(summary.totalBazarPaisa)}")
    sb.appendLine("Total Utility Bills: ${CalculationEngine.formatPaisa(summary.totalBillsPaisa)}")
    sb.appendLine("Live Meal Rate: ${CalculationEngine.formatMealRate(summary.mealRatePaisa)}")
    sb.appendLine("Mess Cash in Hand: ${CalculationEngine.formatPaisa(summary.messCashInHandPaisa)}")
    sb.appendLine("------------------------------------------")
    sb.appendLine("MEMBER BREAKDOWN:")
    summary.memberSummaries.values.forEach { m ->
      sb.appendLine("${m.memberName}:")
      sb.appendLine("  Meals: ${m.rawMeals} (Weighted: ${m.weightedMeals})")
      sb.appendLine("  Meal Cost: ${CalculationEngine.formatPaisa(m.mealCostPaisa)}")
      sb.appendLine("  Bills Share: ${CalculationEngine.formatPaisa(m.fixedBillsCostPaisa)}")
      sb.appendLine("  Extra Cost: ${CalculationEngine.formatPaisa(m.extraCostPaisa)}")
      sb.appendLine("  Total Expense: ${CalculationEngine.formatPaisa(m.totalExpensePaisa)}")
      sb.appendLine("  Total Deposited: ${CalculationEngine.formatPaisa(m.totalDepositPaisa)}")
      sb.appendLine("  Opening Carry: ${CalculationEngine.formatPaisa(m.openingBalancePaisa)}")
      val status = if (m.finalBalancePaisa >= 0) "REFUND/CREDIT" else "DUE/OWING"
      sb.appendLine("  Final Balance: ${CalculationEngine.formatPaisa(m.finalBalancePaisa)} [$status]")
      sb.appendLine()
    }
    sb.appendLine("Generated by Smart Mess Manager v2")
    return sb.toString()
  }
}
