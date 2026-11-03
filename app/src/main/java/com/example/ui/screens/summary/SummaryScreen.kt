package com.example.ui.screens.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.MessViewModel
import com.example.ui.components.BalanceChip
import com.example.ui.screens.charts.ExpensesVsDepositsVisualizer
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun SummaryScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val summary by viewModel.periodSummary.collectAsState()
  val period by viewModel.currentPeriod.collectAsState()
  val config by viewModel.config.collectAsState()
  val language by viewModel.language.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var sortByBalanceAsc by remember { mutableStateOf<Boolean?>(null) }
  var showExportDialog by remember { mutableStateOf(false) }

  val filteredSummaries = remember(summary, searchQuery, sortByBalanceAsc) {
    var list = summary.memberSummaries.values.toList()
    if (searchQuery.isNotBlank()) {
      list = list.filter { it.memberName.contains(searchQuery, ignoreCase = true) }
    }
    when (sortByBalanceAsc) {
      true -> list.sortedBy { it.finalBalancePaisa }
      false -> list.sortedByDescending { it.finalBalancePaisa }
      null -> list
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("summary_screen_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = AppStrings.summary(language),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${period.name} • Master Calculation",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Button(
            onClick = { showExportDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("export_statement_btn")
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export", fontSize = 12.sp)
          }
        }
      }

      // Visualizer: Expenses vs Deposits Comparison
      item {
        ExpensesVsDepositsVisualizer(
          summary = summary,
          currencySymbol = config.currencySymbol
        )
      }

      // Master KPI Overview Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Period Totals & Rates", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Meals Consumed:")
              Text(String.format(Locale.US, "%.1f", summary.totalMeals), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Bazar Expenditure:")
              Text(CalculationEngine.formatPaisa(summary.totalBazarPaisa, config.currencySymbol), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Utility Bills:")
              Text(CalculationEngine.formatPaisa(summary.totalBillsPaisa, config.currencySymbol), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Extra Costs:")
              Text(CalculationEngine.formatPaisa(summary.totalExtraPaisa, config.currencySymbol), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Total Member Deposits:")
              Text(CalculationEngine.formatPaisa(summary.totalDepositPaisa, config.currencySymbol), fontWeight = FontWeight.Bold, color = GreenIncome)
            }
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Live Meal Rate:", fontWeight = FontWeight.Bold)
                Text(CalculationEngine.formatMealRate(summary.mealRatePaisa, config.currencySymbol), fontWeight = FontWeight.Bold, color = TealPrimary)
              }
            }
          }
        }
      }

      // Search and Sort controls
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search member...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f)
          )

          OutlinedButton(
            onClick = {
              sortByBalanceAsc = when (sortByBalanceAsc) {
                null -> false
                false -> true
                true -> null
              }
            },
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              when (sortByBalanceAsc) {
                true -> "Due first"
                false -> "Credit first"
                null -> "Default"
              },
              fontSize = 11.sp
            )
          }
        }
      }

      // Members Breakdown Table
      items(filteredSummaries, key = { it.memberId }) { mem ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(mem.memberName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              BalanceChip(balancePaisa = mem.finalBalancePaisa, currencySymbol = config.currencySymbol)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Meals: ${String.format(Locale.US, "%.1f", mem.rawMeals)}", style = MaterialTheme.typography.bodySmall)
              Text("Meal Cost: ${CalculationEngine.formatPaisa(mem.mealCostPaisa, config.currencySymbol)}", style = MaterialTheme.typography.bodySmall)
              Text("Bills: ${CalculationEngine.formatPaisa(mem.fixedBillsCostPaisa, config.currencySymbol)}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Deposit: ${CalculationEngine.formatPaisa(mem.totalDepositPaisa, config.currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = GreenIncome, fontWeight = FontWeight.SemiBold)
              Text("Total Spent: ${CalculationEngine.formatPaisa(mem.totalExpensePaisa, config.currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = RedExpense, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }

    if (showExportDialog) {
      val textStatement = viewModel.generateMonthStatementText()
      AlertDialog(
        onDismissRequest = { showExportDialog = false },
        title = { Text("Export Month Statement") },
        text = {
          Column {
            Text("Full formatted summary statement for ${period.name}:", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
              LazyColumn(modifier = Modifier.padding(10.dp)) {
                item {
                  Text(textStatement, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                }
              }
            }
          }
        },
        confirmButton = {
          Button(onClick = {
            viewModel.showMessage("Statement copied to clipboard!")
            showExportDialog = false
          }) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Copy / Share")
          }
        },
        dismissButton = {
          OutlinedButton(onClick = { showExportDialog = false }) { Text("Close") }
        }
      )
    }
  }
}
