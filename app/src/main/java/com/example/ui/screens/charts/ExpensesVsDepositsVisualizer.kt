package com.example.ui.screens.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calc.CalculationEngine
import com.example.core.calc.MessCalculationSummary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import kotlin.math.max

@Composable
fun ExpensesVsDepositsVisualizer(
  summary: MessCalculationSummary,
  currencySymbol: String = "৳",
  modifier: Modifier = Modifier
) {
  val totalDeposits = summary.totalDepositPaisa
  val totalExpenses = summary.totalExpensesPaisa
  val netDelta = totalDeposits - totalExpenses
  val isSurplus = netDelta >= 0

  val maxVal = max(totalDeposits, totalExpenses).coerceAtLeast(1L)
  val depositRatio = (totalDeposits.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)
  val expenseRatio = (totalExpenses.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)

  val animDepositRatio by animateFloatAsState(targetValue = depositRatio, animationSpec = tween(700), label = "depRatio")
  val animExpenseRatio by animateFloatAsState(targetValue = expenseRatio, animationSpec = tween(700), label = "expRatio")

  val coveragePct = if (totalExpenses > 0) ((totalDeposits.toDouble() / totalExpenses.toDouble()) * 100).toInt() else 100

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("expenses_vs_deposits_visualizer")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = if (isSurplus) GreenContainer else RedContainer,
            modifier = Modifier.size(38.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isSurplus) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isSurplus) GreenIncome else RedExpense,
                modifier = Modifier.size(22.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Expenses vs Deposits",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1E293B)
            )
            Text(
              text = "Monthly Financial Solvency & Liquidity",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF64748B)
            )
          }
        }

        // Net Delta Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isSurplus) GreenContainer else RedContainer
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isSurplus) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
              contentDescription = null,
              tint = if (isSurplus) GreenIncome else RedExpense,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = CalculationEngine.formatPaisa(netDelta, currencySymbol, showSign = true),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (isSurplus) GreenIncome else RedExpense
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Comparative Chart Bars (Styled after modern chart libraries like Recharts / Vico)
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Total Member Deposits Bar
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Total Deposits Collected", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Text(
              text = CalculationEngine.formatPaisa(totalDeposits, currencySymbol),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2E7D32)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(18.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(Color(0xFFF1F5F9))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(animDepositRatio)
                .fillMaxHeight()
                .clip(RoundedCornerShape(9.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF81C784), Color(0xFF2E7D32))))
            )
          }
        }

        // Total Mess Expenses Bar
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE53935)))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Total Mess Expenditures", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            Text(
              text = CalculationEngine.formatPaisa(totalExpenses, currencySymbol),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFE53935)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(18.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(Color(0xFFF1F5F9))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(animExpenseRatio)
                .fillMaxHeight()
                .clip(RoundedCornerShape(9.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFEF9A9A), Color(0xFFE53935))))
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Coverage & Solvency Meter
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Deposit Coverage Ratio",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF475569)
            )
            Text(
              text = "$coveragePct%",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (coveragePct >= 100) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          LinearProgressIndicator(
            progress = { (coveragePct / 150f).coerceIn(0.05f, 1f) },
            color = if (coveragePct >= 100) Color(0xFF2E7D32) else Color(0xFFE53935),
            trackColor = Color(0xFFE2E8F0),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (isSurplus) {
              "✔ Healthy cash cushion: Deposits currently exceed total expenses by ${CalculationEngine.formatPaisa(netDelta, currencySymbol)}."
            } else {
              "⚠ Liquidity deficit: Total expenses exceed deposits by ${CalculationEngine.formatPaisa(kotlin.math.abs(netDelta), currencySymbol)}. Prompt members for advances."
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSurplus) Color(0xFF2E7D32) else Color(0xFFD32F2F),
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Side-by-side component breakdown
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Deposits Breakdown
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF4FBF9),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFB2DFDB))),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Savings, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Deposits In", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealDark)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              CalculationEngine.formatPaisa(totalDeposits, currencySymbol),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Cash & mobile deposits", fontSize = 10.sp, color = Color(0xFF64748B))
          }
        }

        // Expenses Breakdown
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFF7F7),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFCDD2))),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = RedExpense, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Expenses Out", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RedExpense)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              CalculationEngine.formatPaisa(totalExpenses, currencySymbol),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = RedExpense
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Bazar: ${CalculationEngine.formatPaisa(summary.totalBazarPaisa)}", fontSize = 10.sp, color = Color(0xFF64748B))
          }
        }
      }
    }
  }
}
