package com.example.ui.screens.members

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.calc.CalculationEngine
import com.example.core.localization.AppStrings
import com.example.core.model.Member
import com.example.core.model.Role
import com.example.ui.MessViewModel
import com.example.ui.components.BalanceChip
import com.example.ui.components.RoleBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun MembersScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val members by viewModel.members.collectAsState()
  val summary by viewModel.periodSummary.collectAsState()
  val config by viewModel.config.collectAsState()
  val language by viewModel.language.collectAsState()

  var selectedMemberForDetail by remember { mutableStateOf<Member?>(null) }
  var showAddMemberDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("members_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = AppStrings.members(language),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${members.count { it.active }} Active • ${members.size} Total",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Button(
            onClick = { showAddMemberDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("add_member_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Member", fontSize = 12.sp)
          }
        }
      }

      items(members, key = { it.id }) { member ->
        val memSummary = summary.memberSummaries[member.id]
        val balancePaisa = memSummary?.finalBalancePaisa ?: member.openingBalancePaisa

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedMemberForDetail = member }
            .testTag("member_card_${member.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            UserAvatar(name = member.name, colorHex = member.avatarColorHex, size = 44)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = member.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                RoleBadge(role = member.role)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${member.phone} • ${String.format(Locale.US, "%.1f", memSummary?.rawMeals ?: 0.0)} meals",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              BalanceChip(balancePaisa = balancePaisa, currencySymbol = config.currencySymbol)
              Spacer(modifier = Modifier.height(4.dp))
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }

    // Member Detail Dialog with 6 Tabs
    selectedMemberForDetail?.let { member ->
      MemberDetailDialog(
        member = member,
        viewModel = viewModel,
        onDismiss = { selectedMemberForDetail = null }
      )
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
      AddMemberDialog(
        onDismiss = { showAddMemberDialog = false },
        onSave = { newMember ->
          viewModel.addOrUpdateMember(newMember)
          showAddMemberDialog = false
        }
      )
    }
  }
}

@Composable
fun MemberDetailDialog(
  member: Member,
  viewModel: MessViewModel,
  onDismiss: () -> Unit
) {
  val summary by viewModel.periodSummary.collectAsState()
  val meals by viewModel.meals.collectAsState()
  val deposits by viewModel.deposits.collectAsState()
  val bazarList by viewModel.bazarRecords.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()
  val config by viewModel.config.collectAsState()
  val period by viewModel.currentPeriod.collectAsState()

  val memSummary = summary.memberSummaries[member.id]
  val memMeals = meals.filter { it.memberId == member.id }
  val memDeposits = deposits.filter { it.memberId == member.id }

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabTitles = listOf("Overview", "Meals", "Deposits", "Expenses", "Timeline", "Report")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(top = 24.dp)
        .testTag("member_detail_dialog"),
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Top Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(name = member.name, colorHex = member.avatarColorHex, size = 44)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = member.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                RoleBadge(role = member.role)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = member.phone,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        // Tabs
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          edgePadding = 16.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          tabTitles.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
            )
          }
        }

        // Tab Content
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(16.dp)
        ) {
          when (selectedTab) {
            0 -> OverviewTab(member, memSummary, config.currencySymbol, summary.mealRatePaisa)
            1 -> MealsTab(memMeals)
            2 -> DepositsTab(memDeposits, config.currencySymbol)
            3 -> ExpensesTab(memSummary, config.currencySymbol, summary.mealRatePaisa)
            4 -> TimelineTab(member.name, memMeals, memDeposits, bazarList.filter { it.shopperId == member.id })
            5 -> FinancialReportTab(member, memSummary, config.currencySymbol, period.name)
          }
        }
      }
    }
  }
}

@Composable
fun OverviewTab(
  member: Member,
  summary: com.example.core.calc.MemberCalculationSummary?,
  currencySymbol: String,
  mealRatePaisa: Double
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "Period Calculation Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Meals", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${String.format(Locale.US, "%.1f", summary?.rawMeals ?: 0.0)} meals", fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Applied Meal Rate", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(CalculationEngine.formatMealRate(mealRatePaisa, currencySymbol), fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Meal Cost", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(CalculationEngine.formatPaisa(summary?.mealCostPaisa ?: 0L, currencySymbol), fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Fixed Bills Share", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(CalculationEngine.formatPaisa(summary?.fixedBillsCostPaisa ?: 0L, currencySymbol), fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Extra Costs", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(CalculationEngine.formatPaisa(summary?.extraCostPaisa ?: 0L, currencySymbol), fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Expenses", fontWeight = FontWeight.SemiBold)
            Text(CalculationEngine.formatPaisa(summary?.totalExpensePaisa ?: 0L, currencySymbol), fontWeight = FontWeight.Bold, color = RedExpense)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Deposited", fontWeight = FontWeight.SemiBold)
            Text(CalculationEngine.formatPaisa(summary?.totalDepositPaisa ?: 0L, currencySymbol), fontWeight = FontWeight.Bold, color = GreenIncome)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Opening Balance Carry", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(CalculationEngine.formatPaisa(member.openingBalancePaisa, currencySymbol, showSign = true))
          }
          Spacer(modifier = Modifier.height(10.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if ((summary?.finalBalancePaisa ?: 0L) >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Final Balance:", fontWeight = FontWeight.Bold)
              BalanceChip(balancePaisa = summary?.finalBalancePaisa ?: 0L, currencySymbol = currencySymbol)
            }
          }
        }
      }
    }
  }
}

@Composable
fun MealsTab(meals: List<com.example.core.model.MealEntry>) {
  if (meals.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No meals recorded for this period")
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(meals.sortedByDescending { it.date }) { entry ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(entry.date, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Text("B: ${entry.breakfast}")
              Text("L: ${entry.lunch}")
              Text("D: ${entry.dinner}")
              if (entry.guests > 0) Text("G: +${entry.guests}", color = TealPrimary)
            }
            Text(
              "${entry.rawMealCount()} total",
              fontWeight = FontWeight.Bold,
              color = TealPrimary
            )
          }
        }
      }
    }
  }
}

@Composable
fun DepositsTab(deposits: List<com.example.core.model.DepositRecord>, currencySymbol: String) {
  if (deposits.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No deposits recorded yet")
    }
  } else {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(deposits.sortedByDescending { it.date }) { dep ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(dep.date, fontWeight = FontWeight.Bold)
              Text("${dep.method.label} • ${dep.note.ifEmpty { "Deposit" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
              CalculationEngine.formatPaisa(dep.amountPaisa, currencySymbol, showSign = true),
              fontWeight = FontWeight.Bold,
              color = if (dep.amountPaisa >= 0) GreenIncome else RedExpense
            )
          }
        }
      }
    }
  }
}

@Composable
fun ExpensesTab(
  summary: com.example.core.calc.MemberCalculationSummary?,
  currencySymbol: String,
  mealRatePaisa: Double
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Meal Expense Formula", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "${String.format(Locale.US, "%.1f", summary?.rawMeals ?: 0.0)} meals × ${CalculationEngine.formatMealRate(mealRatePaisa, currencySymbol)} = ${CalculationEngine.formatPaisa(summary?.mealCostPaisa ?: 0L, currencySymbol)}",
            style = MaterialTheme.typography.bodyMedium,
            color = TealPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Fixed Utility Bills Breakdown", fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          summary?.billBreakdown?.forEach { (billName, sharePaisa) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(billName, style = MaterialTheme.typography.bodyMedium)
              Text(CalculationEngine.formatPaisa(sharePaisa, currencySymbol), fontWeight = FontWeight.SemiBold)
            }
          }
          if (summary?.billBreakdown.isNullOrEmpty()) {
            Text("No bill shares assigned.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
fun TimelineTab(
  memberName: String,
  meals: List<com.example.core.model.MealEntry>,
  deposits: List<com.example.core.model.DepositRecord>,
  bazars: List<com.example.core.model.BazarRecord>
) {
  // Combine all events sorted by date
  data class TimelineEvent(val date: String, val title: String, val detail: String)

  val events = mutableListOf<TimelineEvent>()
  meals.forEach {
    events.add(TimelineEvent(it.date, "Meal entry", "${it.rawMealCount()} meals (B:${it.breakfast} L:${it.lunch} D:${it.dinner})"))
  }
  deposits.forEach {
    events.add(TimelineEvent(it.date, "Deposit (${it.method.label})", "${CalculationEngine.formatPaisa(it.amountPaisa)}"))
  }
  bazars.forEach {
    events.add(TimelineEvent(it.date, "Bazar shopping", "${CalculationEngine.formatPaisa(it.totalPaisa)} - ${it.items.size} items"))
  }

  val sorted = events.sortedByDescending { it.date }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(sorted) { event ->
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text(event.date, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(event.title, fontWeight = FontWeight.SemiBold)
            Text(event.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
fun FinancialReportTab(
  member: Member,
  summary: com.example.core.calc.MemberCalculationSummary?,
  currencySymbol: String,
  periodName: String
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text("Financial Statement - $periodName", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Member: ${member.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Total Deposited:")
          Text(CalculationEngine.formatPaisa(summary?.totalDepositPaisa ?: 0L, currencySymbol), fontWeight = FontWeight.Bold, color = GreenIncome)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Total Expended:")
          Text(CalculationEngine.formatPaisa(summary?.totalExpensePaisa ?: 0L, currencySymbol), fontWeight = FontWeight.Bold, color = RedExpense)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Opening Balance Carry:")
          Text(CalculationEngine.formatPaisa(member.openingBalancePaisa, currencySymbol, showSign = true))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Final Balance:", fontWeight = FontWeight.Bold)
            BalanceChip(balancePaisa = summary?.finalBalancePaisa ?: 0L, currencySymbol = currencySymbol)
          }
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { /* share handled via clipboard */ },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Share WhatsApp")
      }
    }
  }
}

@Composable
fun AddMemberDialog(
  onDismiss: () -> Unit,
  onSave: (Member) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var openingBalanceStr by remember { mutableStateOf("0") }
  var selectedRole by remember { mutableStateOf(Role.MEMBER) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Member Manually") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name *") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number *") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Email (Optional)") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = openingBalanceStr,
          onValueChange = { openingBalanceStr = it },
          label = { Text("Opening Balance (৳, - for due)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && phone.isNotBlank()) {
            val balanceDouble = openingBalanceStr.toDoubleOrNull() ?: 0.0
            val balancePaisa = (balanceDouble * 100).toLong()
            onSave(
              Member(
                name = name,
                phone = phone,
                email = email,
                role = selectedRole,
                openingBalancePaisa = balancePaisa
              )
            )
          }
        }
      ) {
        Text("Save Member")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
