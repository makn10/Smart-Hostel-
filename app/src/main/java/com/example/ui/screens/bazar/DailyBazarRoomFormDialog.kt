package com.example.ui.screens.bazar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calc.CalculationEngine
import com.example.core.model.Member
import com.example.ui.components.UserAvatar
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyBazarRoomFormDialog(
  members: List<Member>,
  onDismiss: () -> Unit,
  onSave: (date: String, itemDescription: String, category: String, amountPaisa: Long, shopperId: String, shopperName: String, addToDeposit: Boolean) -> Unit
) {
  var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
  var itemDescription by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Vegetables") }
  var amountStr by remember { mutableStateOf("") }
  var selectedShopperId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
  var addToDeposit by remember { mutableStateOf(true) }

  val categories = listOf(
    "🐟 Fish",
    "🥩 Meat",
    "🥬 Vegetables",
    "🛒 Groceries",
    "🥛 Milk & Eggs",
    "🧂 Spices & Oil",
    "🍎 Fruits",
    "📦 Other"
  )

  val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
  val amountPaisa = (amountDouble * 100).toLong()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = TealContainer,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = TealDark, modifier = Modifier.size(20.dp))
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Record Daily Bazar Expense", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Text("Persists to local Room database", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Date
        item {
          OutlinedTextField(
            value = dateStr,
            onValueChange = { dateStr = it },
            label = { Text("Date (YYYY-MM-DD) *") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("bazar_room_date_input")
          )
        }

        // Item Description
        item {
          OutlinedTextField(
            value = itemDescription,
            onValueChange = { itemDescription = it },
            label = { Text("Item Description * (e.g. Hilsha, Potatoes)") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("bazar_room_desc_input")
          )
        }

        // Category Selection
        item {
          Text("Expense Category:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(4.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(categories) { cat ->
              val cleanName = cat.substringAfter(" ")
              val isSelected = selectedCategory == cleanName
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) TealPrimary else Color(0xFFF1F5F9),
                modifier = Modifier
                  .clickable { selectedCategory = cleanName }
                  .testTag("cat_chip_$cleanName")
              ) {
                Text(
                  text = cat,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else Color(0xFF1E293B),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // Total Amount
        item {
          OutlinedTextField(
            value = amountStr,
            onValueChange = { amountStr = it },
            label = { Text("Total Amount (৳) *") },
            modifier = Modifier.fillMaxWidth().testTag("bazar_room_amount_input")
          )
        }

        // Shopper Selector
        item {
          Text("Shopper (Buyer):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(4.dp))
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(members.filter { it.active }) { mem ->
              val isSelected = selectedShopperId == mem.id
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) TealPrimary else Color(0xFFF1F5F9),
                modifier = Modifier.clickable { selectedShopperId = mem.id }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  UserAvatar(name = mem.name, colorHex = mem.avatarColorHex, size = 22)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = mem.name.split(" ").first(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF1E293B)
                  )
                }
              }
            }
          }
        }

        // Running Total Preview & Add to Deposit
        item {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = TealContainer.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Total Expense:", fontWeight = FontWeight.Bold)
              Text(
                CalculationEngine.formatPaisa(amountPaisa),
                fontWeight = FontWeight.Bold,
                color = TealDark,
                style = MaterialTheme.typography.titleMedium
              )
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
              text = "Add this amount to shopper's deposit balance",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (itemDescription.isNotBlank() && amountPaisa > 0 && selectedShopperId.isNotBlank()) {
            val shopper = members.find { it.id == selectedShopperId }
            onSave(
              dateStr,
              itemDescription,
              selectedCategory,
              amountPaisa,
              selectedShopperId,
              shopper?.name ?: "Member",
              addToDeposit
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
        modifier = Modifier.testTag("save_room_bazar_btn")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Save to Room DB")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
