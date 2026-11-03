package com.example.ui.screens.bazar

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calc.CalculationEngine
import com.example.core.localization.AppStrings
import com.example.core.model.BazarItem
import com.example.core.model.BazarRecord
import com.example.core.model.Member
import com.example.ui.MessViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UserAvatar
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BazarScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val bazarRecords by viewModel.bazarRecords.collectAsState()
  val members by viewModel.members.collectAsState()
  val routine by viewModel.routine.collectAsState()
  val config by viewModel.config.collectAsState()
  val language by viewModel.language.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  var showAddBazarDialog by remember { mutableStateOf(false) }
  var showRoomBazarDialog by remember { mutableStateOf(false) }
  val roomBazarExpenses by viewModel.roomBazarExpenses.collectAsState()

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = AppStrings.bazar(language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          val totalBazarPaisa = bazarRecords.sumOf { it.totalPaisa }
          Text(
            text = "Total Spent: ${CalculationEngine.formatPaisa(totalBazarPaisa, config.currencySymbol)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = { showRoomBazarDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("record_room_bazar_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Daily Bazar (Room)", fontSize = 12.sp)
          }
        }
      }

      // Tabs: Bazar History / Room DB / Routine
      TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
      ) {
        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Shopping") })
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Room DB (${roomBazarExpenses.size})") }
        )
        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Routine") })
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        // Shopping History List
        if (bazarRecords.isEmpty()) {
          EmptyStateView(
            title = "No Bazar Records",
            message = "Add daily bazar shopping records to track expenditures.",
            icon = Icons.Default.ShoppingCart
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(bazarRecords, key = { it.id }) { record ->
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("bazar_card_${record.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      UserAvatar(name = record.shopperName, size = 32)
                      Spacer(modifier = Modifier.width(8.dp))
                      Column {
                        Text(record.shopperName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(record.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = CalculationEngine.formatPaisa(record.totalPaisa, config.currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                      )
                      IconButton(onClick = { viewModel.deleteBazar(record.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  // Items List
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                      .padding(10.dp)
                  ) {
                    record.items.forEach { item ->
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(
                          text = "${item.name} ${if (item.qty.isNotBlank()) "(${item.qty} ${item.unit})" else ""}",
                          style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                          text = CalculationEngine.formatPaisa(item.costPaisa, config.currencySymbol),
                          style = MaterialTheme.typography.bodySmall,
                          fontWeight = FontWeight.SemiBold
                        )
                      }
                    }
                  }

                  if (record.addToDeposit) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "✔ Auto-credited to shopper's deposit balance",
                      style = MaterialTheme.typography.labelSmall,
                      color = GreenIncome,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }
          }
        }
      } else if (selectedTab == 1) {
        // Local Room DB Bazar Expenses Tab
        if (roomBazarExpenses.isEmpty()) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp)
            ) {
              Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = TealPrimary.copy(alpha = 0.5f)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text("No Room DB Expenses Yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                "Record daily bazar expenses with date, item description, category, and total amount persisted into your local Room database.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showRoomBazarDialog = true },
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record Daily Bazar Expense")
              }
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            item {
              val roomTotalPaisa = roomBazarExpenses.sumOf { it.totalAmountPaisa }
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text("Local Room Database", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("${roomBazarExpenses.size} offline-first entries", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  Text(
                    text = CalculationEngine.formatPaisa(roomTotalPaisa, config.currencySymbol),
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    style = MaterialTheme.typography.titleMedium
                  )
                }
              }
            }

            items(roomBazarExpenses, key = { it.id }) { expense ->
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("room_bazar_card_${expense.id}")
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      UserAvatar(name = expense.shopperName, size = 32)
                      Spacer(modifier = Modifier.width(8.dp))
                      Column {
                        Text(expense.shopperName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(expense.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = CalculationEngine.formatPaisa(expense.totalAmountPaisa, config.currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                      )
                      IconButton(onClick = { viewModel.deleteRoomBazarExpense(expense.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  // Item description and category
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = expense.itemDescription,
                          fontWeight = FontWeight.SemiBold,
                          style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                          text = "Category: ${expense.category}",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }

                      Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealPrimary.copy(alpha = 0.15f)
                      ) {
                        Text(
                          text = expense.category,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold,
                          color = TealPrimary,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        // Bazar Routine Tab
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(routine) { item ->
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(item.dayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                  Text("Assigned Member", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  UserAvatar(name = item.memberName, size = 28)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(item.memberName, fontWeight = FontWeight.SemiBold, color = TealPrimary)
                }
              }
            }
          }
        }
      }
    }

    if (showAddBazarDialog) {
      AddBazarDialog(
        members = members,
        onDismiss = { showAddBazarDialog = false },
        onSave = { date, shopperId, items, addToDeposit ->
          viewModel.addBazar(date, shopperId, items, addToDeposit)
          showAddBazarDialog = false
        }
      )
    }

    if (showRoomBazarDialog) {
      DailyBazarRoomFormDialog(
        members = members,
        onDismiss = { showRoomBazarDialog = false },
        onSave = { date, itemDescription, category, amountPaisa, shopperId, shopperName, addToDeposit ->
          viewModel.saveDailyBazarExpense(
            date = date,
            itemDescription = itemDescription,
            category = category,
            totalAmountPaisa = amountPaisa,
            shopperId = shopperId,
            shopperName = shopperName,
            addToDeposit = addToDeposit
          )
          showRoomBazarDialog = false
        }
      )
    }
  }
}

@Composable
fun AddBazarDialog(
  members: List<Member>,
  onDismiss: () -> Unit,
  onSave: (String, String, List<BazarItem>, Boolean) -> Unit
) {
  var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
  var selectedShopperId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
  var addToDeposit by remember { mutableStateOf(true) }

  // Dynamic Item Rows
  data class TempItem(var name: String, var qty: String, var unit: String, var costStr: String)
  val itemsList = remember {
    mutableStateListOf(
      TempItem("Vegetables", "3", "kg", "150"),
      TempItem("Chicken", "2", "kg", "450")
    )
  }

  val runningTotalPaisa = itemsList.sumOf {
    val taka = it.costStr.toDoubleOrNull() ?: 0.0
    (taka * 100).toLong()
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Daily Bazar Entry", fontWeight = FontWeight.Bold) },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          OutlinedTextField(
            value = dateStr,
            onValueChange = { dateStr = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          Text("Shopper:", style = MaterialTheme.typography.labelMedium)
          Column {
            members.filter { it.active }.forEach { mem ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { selectedShopperId = mem.id }
                  .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (selectedShopperId == mem.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.size(16.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(mem.name, fontWeight = if (selectedShopperId == mem.id) FontWeight.Bold else FontWeight.Normal)
              }
            }
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Items:", fontWeight = FontWeight.Bold)
            Button(
              onClick = { itemsList.add(TempItem("", "1", "kg", "")) },
              modifier = Modifier.height(36.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add Item", fontSize = 11.sp)
            }
          }
        }

        items(itemsList.size) { index ->
          val item = itemsList[index]
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                  value = item.name,
                  onValueChange = { item.name = it; itemsList[index] = item.copy(name = it) },
                  label = { Text("Item Name") },
                  modifier = Modifier.weight(1f)
                )
                if (itemsList.size > 1) {
                  IconButton(onClick = { itemsList.removeAt(index) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                  }
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                  value = item.qty,
                  onValueChange = { item.qty = it; itemsList[index] = item.copy(qty = it) },
                  label = { Text("Qty") },
                  modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                  value = item.unit,
                  onValueChange = { item.unit = it; itemsList[index] = item.copy(unit = it) },
                  label = { Text("Unit") },
                  modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                  value = item.costStr,
                  onValueChange = { item.costStr = it; itemsList[index] = item.copy(costStr = it) },
                  label = { Text("Cost (৳)") },
                  modifier = Modifier.weight(1.2f)
                )
              }
            }
          }
        }

        item {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Running Total:", fontWeight = FontWeight.Bold)
              Text(CalculationEngine.formatPaisa(runningTotalPaisa), fontWeight = FontWeight.Bold, color = TealPrimary)
            }
          }
        }

        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { addToDeposit = !addToDeposit },
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(checked = addToDeposit, onCheckedChange = { addToDeposit = it })
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "Add this amount to shopper's deposit balance",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val validItems = itemsList.filter { it.name.isNotBlank() }.map {
            val costDouble = it.costStr.toDoubleOrNull() ?: 0.0
            BazarItem(
              name = it.name,
              qty = it.qty,
              unit = it.unit,
              costPaisa = (costDouble * 100).toLong()
            )
          }
          if (validItems.isNotEmpty() && selectedShopperId.isNotBlank()) {
            onSave(dateStr, selectedShopperId, validItems, addToDeposit)
          }
        }
      ) {
        Text("Save All Items")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
