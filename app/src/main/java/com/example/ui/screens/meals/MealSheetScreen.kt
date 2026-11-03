package com.example.ui.screens.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.model.MealEntry
import com.example.core.model.Member
import com.example.ui.MessViewModel
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MealSheetScreen(
  viewModel: MessViewModel,
  onNavigateToAddMeal: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val members by viewModel.members.collectAsState()
  val meals by viewModel.meals.collectAsState()
  val period by viewModel.currentPeriod.collectAsState()
  val language by viewModel.language.collectAsState()

  // Matrix days (1..30)
  val daysCount = 30
  val horizontalScrollState = rememberScrollState()

  var selectedCellEntry by remember { mutableStateOf<Pair<Member, Int>?>(null) }
  var showAddMealDialog by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(bottom = 80.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = AppStrings.meals(language),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${period.name} • 30 Days Matrix",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { onNavigateToAddMeal() },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("add_meal_sheet_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(AppStrings.addMeal(language), fontSize = 12.sp)
        }
      }
    }

    // Legend
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(GreenIncome))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Full (1+)", style = MaterialTheme.typography.labelSmall)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(AmberSecondary))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Half (0.5)", style = MaterialTheme.typography.labelSmall)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(RedExpense.copy(alpha = 0.5f)))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Off (0)", style = MaterialTheme.typography.labelSmall)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Matrix View (Sticky Member Name + Horizontally Scrollable Days + Total Column)
    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
      // Sticky Left Column: Members
      Column(
        modifier = Modifier
          .width(110.dp)
          .background(MaterialTheme.colorScheme.surface)
          .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
      ) {
        // Header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.CenterStart
        ) {
          Text(
            text = "Member",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
          )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
          items(members.filter { it.active }) { member ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Text(
                text = member.name.split(" ").first(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
              )
            }
          }

          // Daily Total label row
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .padding(horizontal = 6.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Text(
                text = "Total Meals",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Scrollable Columns: Day 1..30 + Total Column
      Row(
        modifier = Modifier
          .weight(1f)
          .horizontalScroll(horizontalScrollState)
      ) {
        for (day in 1..daysCount) {
          val dateStr = String.format(Locale.US, "2026-11-%02d", day)
          val dayMeals = meals.filter { it.date == dateStr }
          val dayTotal = dayMeals.sumOf { it.rawMealCount() }

          Column(
            modifier = Modifier
              .width(52.dp)
              .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
          ) {
            // Day Header
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "$day",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }

            // Member Cells for this Day
            members.filter { it.active }.forEach { member ->
              val entry = dayMeals.find { it.memberId == member.id }
              val count = entry?.rawMealCount() ?: 0.0

              val (cellBg, cellText) = when {
                count >= 1.0 -> Pair(GreenContainer, GreenIncome)
                count > 0.0 -> Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary)
                else -> Pair(RedContainer.copy(alpha = 0.4f), RedExpense)
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .background(cellBg)
                  .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                  .clickable { selectedCellEntry = Pair(member, day) },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (count % 1.0 == 0.0) "${count.toInt()}" else "$count",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = cellText
                )
              }
            }

            // Day Total cell
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (dayTotal % 1.0 == 0.0) "${dayTotal.toInt()}" else String.format(Locale.US, "%.1f", dayTotal),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Final Member Totals Column
        Column(
          modifier = Modifier
            .width(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
          Box(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("Sum", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
          }

          members.filter { it.active }.forEach { member ->
            val totalMemberMeals = meals.filter { it.memberId == member.id }.sumOf { it.rawMealCount() }
            Box(
              modifier = Modifier.fillMaxWidth().height(48.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = String.format(Locale.US, "%.1f", totalMemberMeals),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TealPrimary
              )
            }
          }

          // Grand Total
          val grandTotalMeals = meals.sumOf { it.rawMealCount() }
          Box(
            modifier = Modifier.fillMaxWidth().height(44.dp).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = String.format(Locale.US, "%.0f", grandTotalMeals),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = TealPrimary
            )
          }
        }
      }
    }

    // Cell Edit Stepper Dialog
    selectedCellEntry?.let { (member, day) ->
      val dateStr = String.format(Locale.US, "2026-11-%02d", day)
      val existing = meals.find { it.memberId == member.id && it.date == dateStr }

      MealStepperDialog(
        memberName = member.name,
        dateStr = dateStr,
        initialBreakfast = existing?.breakfast ?: 1.0,
        initialLunch = existing?.lunch ?: 1.0,
        initialDinner = existing?.dinner ?: 1.0,
        initialGuests = existing?.guests ?: 0.0,
        onDismiss = { selectedCellEntry = null },
        onSave = { b, l, d, g ->
          viewModel.setMeal(member.id, dateStr, b, l, d, g)
          selectedCellEntry = null
        }
      )
    }

    // Add Meal Dialog
    if (showAddMealDialog) {
      AddMealDialog(
        members = members,
        onDismiss = { showAddMealDialog = false },
        onSaveAll = { date, b, l, d ->
          viewModel.setMealsForAll(date, b, l, d)
          showAddMealDialog = false
        },
        onSaveSingle = { memberId, date, b, l, d, g ->
          viewModel.setMeal(memberId, date, b, l, d, g)
          showAddMealDialog = false
        }
      )
    }
  }
}

@Composable
fun MealStepperDialog(
  memberName: String,
  dateStr: String,
  initialBreakfast: Double,
  initialLunch: Double,
  initialDinner: Double,
  initialGuests: Double,
  onDismiss: () -> Unit,
  onSave: (Double, Double, Double, Double) -> Unit
) {
  var breakfast by remember { mutableDoubleStateOf(initialBreakfast) }
  var lunch by remember { mutableDoubleStateOf(initialLunch) }
  var dinner by remember { mutableDoubleStateOf(initialDinner) }
  var guests by remember { mutableDoubleStateOf(initialGuests) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Meals • $dateStr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Member: $memberName", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        MealStepperRow(title = "Breakfast", value = breakfast, onValueChange = { breakfast = it })
        MealStepperRow(title = "Lunch", value = lunch, onValueChange = { lunch = it })
        MealStepperRow(title = "Dinner", value = dinner, onValueChange = { dinner = it })
        MealStepperRow(title = "Guest Meals", value = guests, onValueChange = { guests = it })

        val total = breakfast + lunch + dinner + guests
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Day Total:", fontWeight = FontWeight.Bold)
            Text("$total meals", fontWeight = FontWeight.Bold, color = TealPrimary)
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = { onSave(breakfast, lunch, dinner, guests) }) {
        Text("Save")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun MealStepperRow(
  title: String,
  value: Double,
  onValueChange: (Double) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = { if (value >= 0.5) onValueChange(value - 0.5) },
        enabled = value > 0
      ) {
        Icon(Icons.Default.RemoveCircle, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary)
      }
      Text(
        text = if (value % 1.0 == 0.0) "${value.toInt()}" else "$value",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(36.dp),
        textAlign = TextAlign.Center
      )
      IconButton(
        onClick = { onValueChange(value + 0.5) }
      ) {
        Icon(Icons.Default.AddCircle, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

@Composable
fun AddMealDialog(
  members: List<Member>,
  onDismiss: () -> Unit,
  onSaveAll: (String, Double, Double, Double) -> Unit,
  onSaveSingle: (String, String, Double, Double, Double, Double) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
  var breakfast by remember { mutableDoubleStateOf(1.0) }
  var lunch by remember { mutableDoubleStateOf(1.0) }
  var dinner by remember { mutableDoubleStateOf(1.0) }
  var guests by remember { mutableDoubleStateOf(0.0) }
  var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add / Record Meals") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
          Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All Members") })
          Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Single Member") })
        }

        OutlinedTextField(
          value = dateStr,
          onValueChange = { dateStr = it },
          label = { Text("Date (YYYY-MM-DD)") },
          modifier = Modifier.fillMaxWidth()
        )

        if (selectedTab == 1) {
          Text("Select Member:", style = MaterialTheme.typography.labelMedium)
          Column {
            members.filter { it.active }.forEach { mem ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedMemberId = mem.id }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (selectedMemberId == mem.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.size(16.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(mem.name, fontWeight = if (selectedMemberId == mem.id) FontWeight.Bold else FontWeight.Normal)
              }
            }
          }
        }

        MealStepperRow("Breakfast", breakfast) { breakfast = it }
        MealStepperRow("Lunch", lunch) { lunch = it }
        MealStepperRow("Dinner", dinner) { dinner = it }
        if (selectedTab == 1) {
          MealStepperRow("Guest Meals", guests) { guests = it }
        }
      }
    },
    confirmButton = {
      Button(onClick = {
        if (selectedTab == 0) {
          onSaveAll(dateStr, breakfast, lunch, dinner)
        } else {
          onSaveSingle(selectedMemberId, dateStr, breakfast, lunch, dinner, guests)
        }
      }) {
        Text("Submit")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
