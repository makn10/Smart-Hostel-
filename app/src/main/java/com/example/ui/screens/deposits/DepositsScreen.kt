package com.example.ui.screens.deposits

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.core.model.DepositRecord
import com.example.core.model.Member
import com.example.core.model.PaymentMethod
import com.example.ui.MessViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UserAvatar
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DepositsScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val deposits by viewModel.deposits.collectAsState()
  val members by viewModel.members.collectAsState()
  val config by viewModel.config.collectAsState()
  val language by viewModel.language.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }

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
            text = AppStrings.deposits(language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          val totalPaisa = deposits.sumOf { it.amountPaisa }
          Text(
            text = "Total Collected: ${CalculationEngine.formatPaisa(totalPaisa, config.currencySymbol)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Button(
          onClick = { showAddDialog = true },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("add_deposit_screen_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(AppStrings.addDeposit(language), fontSize = 12.sp)
        }
      }

      if (deposits.isEmpty()) {
        EmptyStateView(
          title = "No Deposits",
          message = "No member deposits recorded for this period yet.",
          icon = Icons.Default.AttachMoney
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(deposits, key = { it.id }) { record ->
            val member = members.find { it.id == record.memberId }
            val memberName = member?.name ?: "Unknown"

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("deposit_card_${record.id}"),
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
                  UserAvatar(name = memberName, colorHex = member?.avatarColorHex ?: 0xFF009688, size = 38)
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                      ) {
                        Text(
                          text = record.method.label,
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.SemiBold,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(record.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (record.note.isNotBlank()) {
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(record.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = CalculationEngine.formatPaisa(record.amountPaisa, config.currencySymbol, showSign = true),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (record.amountPaisa >= 0) GreenIncome else RedExpense
                  )
                  IconButton(onClick = { viewModel.deleteDeposit(record.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
        }
      }
    }

    if (showAddDialog) {
      AddDepositDialog(
        members = members,
        onDismiss = { showAddDialog = false },
        onSave = { memberId, date, amountPaisa, method, note ->
          viewModel.addDeposit(memberId, date, amountPaisa, method, note)
          showAddDialog = false
        }
      )
    }
  }
}

@Composable
fun AddDepositDialog(
  members: List<Member>,
  onDismiss: () -> Unit,
  onSave: (String, String, Long, PaymentMethod, String) -> Unit
) {
  var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
  var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
  var amountStr by remember { mutableStateOf("") }
  var selectedMethod by remember { mutableStateOf(PaymentMethod.BKASH) }
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Record Member Deposit", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Select Member:", style = MaterialTheme.typography.labelMedium)
        Column {
          members.filter { it.active }.forEach { mem ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (selectedMemberId == mem.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .size(16.dp)
                  .testTag("select_deposit_member_${mem.id}")
              ) {}
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                mem.name,
                fontWeight = if (selectedMemberId == mem.id) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text("Amount (৳) [Negative allowed for correction]") },
          modifier = Modifier.fillMaxWidth().testTag("deposit_amount_input")
        )

        OutlinedTextField(
          value = dateStr,
          onValueChange = { dateStr = it },
          label = { Text("Date (YYYY-MM-DD)") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          PaymentMethod.values().forEach { method ->
            Button(
              onClick = { selectedMethod = method },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f).height(38.dp)
            ) {
              Text(method.label, fontSize = 10.sp)
            }
          }
        }

        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Note / TrxID (e.g. bKash TrxID)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amountDouble = amountStr.toDoubleOrNull()
          if (amountDouble != null && selectedMemberId.isNotBlank()) {
            val amountPaisa = (amountDouble * 100).toLong()
            onSave(selectedMemberId, dateStr, amountPaisa, selectedMethod, note)
          }
        }
      ) {
        Text("Save Deposit")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
