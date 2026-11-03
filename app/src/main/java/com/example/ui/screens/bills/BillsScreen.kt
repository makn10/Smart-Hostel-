package com.example.ui.screens.bills

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calc.CalculationEngine
import com.example.core.localization.AppStrings
import com.example.core.model.BillRecord
import com.example.core.model.BillType
import com.example.core.model.ExtraCostRecord
import com.example.core.model.Member
import com.example.core.model.SplitMode
import com.example.ui.MessViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillsScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val bills by viewModel.bills.collectAsState()
  val extraCosts by viewModel.extraCosts.collectAsState()
  val members by viewModel.members.collectAsState()
  val config by viewModel.config.collectAsState()
  val language by viewModel.language.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  var showAddBillDialog by remember { mutableStateOf(false) }
  var showAddExtraDialog by remember { mutableStateOf(false) }

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
            text = AppStrings.bills(language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          val totalBillsPaisa = bills.sumOf { it.amountPaisa }
          val totalExtraPaisa = extraCosts.sumOf { it.amountPaisa }
          Text(
            text = "Bills: ${CalculationEngine.formatPaisa(totalBillsPaisa, config.currencySymbol)} • Extra: ${CalculationEngine.formatPaisa(totalExtraPaisa, config.currencySymbol)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = {
            if (selectedTab == 0) showAddBillDialog = true else showAddExtraDialog = true
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("add_bill_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (selectedTab == 0) "Add Bill" else "Add Extra", fontSize = 12.sp)
        }
      }

      // Tabs: Fixed Utility Bills / Extra Personal Costs
      TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
      ) {
        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Utility Bills") })
        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Extra Costs / Feast") })
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        // Utility Bills List
        if (bills.isEmpty()) {
          EmptyStateView(
            title = "No Monthly Bills",
            message = "Add gas, electricity, internet or cook maid salary.",
            icon = Icons.Default.Receipt
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(bills, key = { it.id }) { bill ->
              val icon = when (bill.type) {
                BillType.GAS -> Icons.Default.LocalGasStation
                BillType.ELECTRICITY -> Icons.Default.ElectricBolt
                BillType.INTERNET -> Icons.Default.Wifi
                else -> Icons.Default.Receipt
              }

              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("bill_item_${bill.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                      shape = RoundedCornerShape(10.dp),
                      color = MaterialTheme.colorScheme.primaryContainer,
                      modifier = Modifier.size(40.dp)
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                      }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(bill.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                      Text(
                        "${bill.type.displayName} • ${bill.splitMode.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = CalculationEngine.formatPaisa(bill.amountPaisa, config.currencySymbol),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = RedExpense
                    )
                    IconButton(onClick = { viewModel.deleteBill(bill.id) }) {
                      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        // Extra Costs List
        if (extraCosts.isEmpty()) {
          EmptyStateView(
            title = "No Extra Costs",
            message = "Special feasts or shared costs among selected members appear here.",
            icon = Icons.Default.RestaurantMenu
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(extraCosts, key = { it.id }) { extra ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(extra.reason, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                      "${extra.date} • Split among ${if (extra.participantIds.isEmpty()) "All members" else "${extra.participantIds.size} members"}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = CalculationEngine.formatPaisa(extra.amountPaisa, config.currencySymbol, showSign = true),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (extra.amountPaisa >= 0) RedExpense else TealPrimary
                    )
                    IconButton(onClick = { viewModel.deleteExtraCost(extra.id) }) {
                      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (showAddBillDialog) {
      AddBillDialog(
        members = members,
        onDismiss = { showAddBillDialog = false },
        onSave = { type, name, amountPaisa, splitMode, pIds ->
          viewModel.addBill(type, name, amountPaisa, splitMode, pIds)
          showAddBillDialog = false
        }
      )
    }

    if (showAddExtraDialog) {
      AddExtraCostDialog(
        members = members,
        onDismiss = { showAddExtraDialog = false },
        onSave = { reason, amountPaisa, pIds, date ->
          viewModel.addExtraCost(reason, amountPaisa, pIds, date)
          showAddExtraDialog = false
        }
      )
    }
  }
}

@Composable
fun AddBillDialog(
  members: List<Member>,
  onDismiss: () -> Unit,
  onSave: (BillType, String, Long, SplitMode, List<String>) -> Unit
) {
  var selectedType by remember { mutableStateOf(BillType.ELECTRICITY) }
  var name by remember { mutableStateOf("DESCO Electricity") }
  var amountStr by remember { mutableStateOf("") }
  var splitMode by remember { mutableStateOf(SplitMode.SHARED_EQUALLY) }
  val selectedMembers = remember { mutableStateListOf<String>() }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Utility Bill") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Bill Description") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text("Total Bill Amount (৳)") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Bill Category:", style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf(BillType.GAS, BillType.ELECTRICITY, BillType.MAID, BillType.INTERNET).forEach { type ->
            Button(
              onClick = {
                selectedType = type
                name = when (type) {
                  BillType.GAS -> "Gas Cylinder Bill"
                  BillType.ELECTRICITY -> "Electricity Bill"
                  BillType.MAID -> "Maid / Bua Salary"
                  BillType.INTERNET -> "WiFi Internet Bill"
                  else -> "Utility Bill"
                }
              },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(36.dp)
            ) {
              Text(type.displayName.split(" ").first(), fontSize = 9.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull()
          if (amt != null && name.isNotBlank()) {
            val amountPaisa = (amt * 100).toLong()
            onSave(selectedType, name, amountPaisa, splitMode, selectedMembers.toList())
          }
        }
      ) {
        Text("Save Bill")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun AddExtraCostDialog(
  members: List<Member>,
  onDismiss: () -> Unit,
  onSave: (String, Long, List<String>, String) -> Unit
) {
  var reason by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
  val selectedMembers = remember { mutableStateListOf<String>().apply { addAll(members.map { it.id }) } }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Personal Extra Cost / Feast") },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason (e.g. Friday Kacchi Biryani)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = amountStr,
            onValueChange = { amountStr = it },
            label = { Text("Total Amount (৳) [Negative for correction]") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = dateStr,
            onValueChange = { dateStr = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          Text("Select Participating Members:", style = MaterialTheme.typography.labelMedium)
        }
        items(members.filter { it.active }) { mem ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                if (selectedMembers.contains(mem.id)) selectedMembers.remove(mem.id) else selectedMembers.add(mem.id)
              }
              .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = selectedMembers.contains(mem.id),
              onCheckedChange = {
                if (it) selectedMembers.add(mem.id) else selectedMembers.remove(mem.id)
              }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(mem.name)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = amountStr.toDoubleOrNull()
          if (amt != null && reason.isNotBlank()) {
            val amountPaisa = (amt * 100).toLong()
            onSave(reason, amountPaisa, selectedMembers.toList(), dateStr)
          }
        }
      ) {
        Text("Save Extra Cost")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
