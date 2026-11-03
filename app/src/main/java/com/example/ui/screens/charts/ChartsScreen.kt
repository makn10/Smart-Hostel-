package com.example.ui.screens.charts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calc.CalculationEngine
import com.example.core.localization.AppStrings
import com.example.core.model.BillType
import com.example.ui.MessViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.util.Locale

@Composable
fun ChartsScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val summary by viewModel.periodSummary.collectAsState()
  val bazarRecords by viewModel.bazarRecords.collectAsState()
  val bills by viewModel.bills.collectAsState()
  val config by viewModel.config.collectAsState()
  val language by viewModel.language.collectAsState()

  // Calculate expense categories
  val totalBazar = summary.totalBazarPaisa
  val gasBills = bills.filter { it.type == BillType.GAS }.sumOf { it.amountPaisa }
  val currentBills = bills.filter { it.type == BillType.ELECTRICITY }.sumOf { it.amountPaisa }
  val maidBills = bills.filter { it.type == BillType.MAID }.sumOf { it.amountPaisa }
  val otherBills = bills.filter { it.type != BillType.GAS && it.type != BillType.ELECTRICITY && it.type != BillType.MAID }.sumOf { it.amountPaisa }
  val extraCosts = summary.totalExtraPaisa

  val grandTotalSpent = totalBazar + summary.totalBillsPaisa + extraCosts

  // Member balance counts
  val positiveBalances = summary.memberSummaries.values.filter { it.finalBalancePaisa > 0 }
  val negativeBalances = summary.memberSummaries.values.filter { it.finalBalancePaisa < 0 }
  val zeroBalances = summary.memberSummaries.values.filter { it.finalBalancePaisa == 0L }

  // Highest spending bazar
  val highestBazar = bazarRecords.maxByOrNull { it.totalPaisa }
  val avgBazar = if (bazarRecords.isNotEmpty()) totalBazar / bazarRecords.size else 0L

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("charts_screen_list"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Text(
        text = AppStrings.charts(language),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Financial Visualizations & Spend Analysis",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Hero Visualizer: Monthly Expenses vs Member Deposits
    item {
      ExpensesVsDepositsVisualizer(
        summary = summary,
        currencySymbol = config.currencySymbol
      )
    }

    // Category Distribution Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Expense Categories Distribution", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Text("Total Outflow: ${CalculationEngine.formatPaisa(grandTotalSpent, config.currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(14.dp))

          ExpenseCategoryBar(
            title = "Bazar Expenses",
            amountPaisa = totalBazar,
            totalPaisa = grandTotalSpent,
            color = TealPrimary,
            currencySymbol = config.currencySymbol
          )
          Spacer(modifier = Modifier.height(10.dp))
          ExpenseCategoryBar(
            title = "Electricity / Current Bill",
            amountPaisa = currentBills,
            totalPaisa = grandTotalSpent,
            color = AmberSecondary,
            currencySymbol = config.currencySymbol
          )
          Spacer(modifier = Modifier.height(10.dp))
          ExpenseCategoryBar(
            title = "Maid / Bua Salary",
            amountPaisa = maidBills,
            totalPaisa = grandTotalSpent,
            color = Color(0xFF673AB7),
            currencySymbol = config.currencySymbol
          )
          Spacer(modifier = Modifier.height(10.dp))
          ExpenseCategoryBar(
            title = "Gas Utility Bill",
            amountPaisa = gasBills,
            totalPaisa = grandTotalSpent,
            color = Color(0xFF00ACC1),
            currencySymbol = config.currencySymbol
          )
          Spacer(modifier = Modifier.height(10.dp))
          ExpenseCategoryBar(
            title = "Other Utilities & Extra Costs",
            amountPaisa = otherBills + extraCosts,
            totalPaisa = grandTotalSpent,
            color = Color(0xFFE91E63),
            currencySymbol = config.currencySymbol
          )
        }
      }
    }

    // Daily Bazar Spend Trend (Scale strictly from 0 - Bug #5 Fixed)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Bazar Cost Trend (Scales from 0 ৳)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Text("Recent shopping entries compared from baseline 0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(14.dp))

          val recentBazar = bazarRecords.take(6).reversed()
          val maxCost = recentBazar.maxOfOrNull { it.totalPaisa } ?: 1L

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recentBazar.forEach { b ->
              val progress = (b.totalPaisa.toFloat() / maxCost.toFloat()).coerceIn(0f, 1f)
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("${b.date} • ${b.shopperName}", style = MaterialTheme.typography.labelSmall)
                  Text(CalculationEngine.formatPaisa(b.totalPaisa, config.currencySymbol), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                  progress = { progress },
                  modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                  color = TealPrimary,
                  trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // Member Balance Status Overview
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Member Balance Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("${positiveBalances.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = GreenIncome)
              Text("Credit (Mess Owes)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("${negativeBalances.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = RedExpense)
              Text("Due (Member Owes)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("${zeroBalances.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
              Text("Settled (0 ৳)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }

    // Insights Highlights
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Quick Insights", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
          Spacer(modifier = Modifier.height(6.dp))
          if (highestBazar != null) {
            Text(
              "• Highest spending trip: ${CalculationEngine.formatPaisa(highestBazar.totalPaisa, config.currencySymbol)} on ${highestBazar.date} by ${highestBazar.shopperName}",
              style = MaterialTheme.typography.bodySmall
            )
          }
          Text(
            "• Average bazar trip spend: ${CalculationEngine.formatPaisa(avgBazar, config.currencySymbol)}",
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }
  }
}

@Composable
fun ExpenseCategoryBar(
  title: String,
  amountPaisa: Long,
  totalPaisa: Long,
  color: Color,
  currencySymbol: String
) {
  val pct = if (totalPaisa > 0) (amountPaisa.toFloat() / totalPaisa.toFloat() * 100).toInt() else 0
  val progress = if (totalPaisa > 0) (amountPaisa.toFloat() / totalPaisa.toFloat()).coerceIn(0f, 1f) else 0f

  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
      }
      Text(
        "${CalculationEngine.formatPaisa(amountPaisa, currencySymbol)} ($pct%)",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
      color = color,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
  }
}
