package com.example.ui.screens.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calc.CalculationEngine
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.model.Role
import com.example.ui.MessViewModel
import com.example.ui.components.BalanceChip
import com.example.ui.components.RoleBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: MessViewModel,
  onNavigateToTab: (String) -> Unit,
  onOpenQuickAction: (String) -> Unit,
  onOpenUserSwitcher: () -> Unit,
  modifier: Modifier = Modifier
) {
  val config by viewModel.config.collectAsState()
  val period by viewModel.currentPeriod.collectAsState()
  val summary by viewModel.periodSummary.collectAsState()
  val currentUser = viewModel.getCurrentMember()
  val language by viewModel.language.collectAsState()
  val notices by viewModel.notices.collectAsState()
  val meals by viewModel.meals.collectAsState()

  // Calculate today's meals
  val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
  val todayMeals = meals.filter { it.date == todayStr }
  val todayBreakfast = todayMeals.sumOf { it.breakfast }
  val todayLunch = todayMeals.sumOf { it.lunch }
  val todayDinner = todayMeals.sumOf { it.dinner }

  val mySummary = currentUser?.let { summary.memberSummaries[it.id] }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen_content"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar & Welcome Header
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = config.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "${period.name} • Join Code: ${config.joinCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              // Notifications bell
              val unreadCount = viewModel.notifications.collectAsState().value.count { !it.isRead }
              IconButton(
                onClick = { onNavigateToTab("notifications") },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("notif_bell_btn")
              ) {
                Box {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                  if (unreadCount > 0) {
                    Box(
                      modifier = Modifier
                        .size(9.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(4.dp))

              // Language toggle button
              IconButton(
                onClick = { viewModel.toggleLanguage() },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("lang_toggle_btn")
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                  modifier = Modifier.size(32.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = if (language == AppLanguage.ENGLISH) "বাং" else "EN",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = TealPrimary
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(6.dp))

              // User Switcher (Role Testing)
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                  .clickable { onOpenUserSwitcher() }
                  .testTag("demo_user_switcher_btn")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  currentUser?.let {
                    UserAvatar(name = it.name, colorHex = it.avatarColorHex, size = 24)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = it.role.label,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch user",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Today's Meal Overview
    item {
      Column {
        SectionHeader(
          title = AppStrings.todayOverview(language),
          actionText = AppStrings.meals(language),
          onActionClick = { onNavigateToTab("meals") }
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatCard(
            title = AppStrings.breakfast(language),
            value = String.format(Locale.US, "%.1f", todayBreakfast),
            icon = Icons.Default.Fastfood,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.weight(1f),
            testTag = "today_breakfast_card"
          )
          StatCard(
            title = AppStrings.lunch(language),
            value = String.format(Locale.US, "%.1f", todayLunch),
            icon = Icons.Default.Restaurant,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.weight(1f),
            testTag = "today_lunch_card"
          )
          StatCard(
            title = AppStrings.dinner(language),
            value = String.format(Locale.US, "%.1f", todayDinner),
            icon = Icons.Default.Restaurant,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.weight(1f),
            testTag = "today_dinner_card"
          )
        }
      }
    }

    // My Summary Card
    if (currentUser != null && mySummary != null) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("my_summary_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(name = currentUser.name, colorHex = currentUser.avatarColorHex, size = 36)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${currentUser.name} (${AppStrings.mySummary(language)})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  RoleBadge(role = currentUser.role)
                }
              }

              BalanceChip(balancePaisa = mySummary.finalBalancePaisa, currencySymbol = config.currencySymbol)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = AppStrings.myMeals(language),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = String.format(Locale.US, "%.1f", mySummary.rawMeals),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Column {
                Text(
                  text = AppStrings.myCost(language),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = CalculationEngine.formatPaisa(mySummary.totalExpensePaisa, config.currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = RedExpense
                )
              }
              Column {
                Text(
                  text = AppStrings.myDeposit(language),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = CalculationEngine.formatPaisa(mySummary.totalDepositPaisa, config.currencySymbol),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = GreenIncome
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (mySummary.finalBalancePaisa >= 0) {
                  "✔ ${AppStrings.messOwesYou(language)}: ${CalculationEngine.formatPaisa(mySummary.finalBalancePaisa, config.currencySymbol)}"
                } else {
                  "⚠ ${AppStrings.youOweMess(language)}: ${CalculationEngine.formatPaisa(kotlin.math.abs(mySummary.finalBalancePaisa), config.currencySymbol)}"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (mySummary.finalBalancePaisa >= 0) GreenIncome else RedExpense,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = TealContainer.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToTab("my_meal_history") }
                .testTag("view_my_meal_history_btn")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = TealDark,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "View My Meal History",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TealDark
                  )
                }
                Icon(
                  imageVector = Icons.Default.ArrowForward,
                  contentDescription = null,
                  tint = TealDark,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }

    // Mess Overall Totals (6 Key KPIs)
    item {
      Column {
        SectionHeader(
          title = "Mess Overview (${period.name})",
          actionText = AppStrings.summary(language),
          onActionClick = { onNavigateToTab("summary") }
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = AppStrings.mealRate(language),
            value = CalculationEngine.formatMealRate(summary.mealRatePaisa, config.currencySymbol),
            subtitle = config.mealSystem.title,
            icon = Icons.Default.Restaurant,
            containerColor = TealContainer,
            contentColor = TealDark,
            modifier = Modifier.weight(1f),
            testTag = "kpi_meal_rate"
          )
          StatCard(
            title = AppStrings.totalMeals(language),
            value = String.format(Locale.US, "%.1f", summary.totalMeals),
            subtitle = "All active members",
            icon = Icons.Default.People,
            containerColor = AmberContainer,
            contentColor = AmberSecondary,
            modifier = Modifier.weight(1f),
            testTag = "kpi_total_meals"
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = AppStrings.bazarCost(language),
            value = CalculationEngine.formatPaisa(summary.totalBazarPaisa, config.currencySymbol),
            icon = Icons.Default.ShoppingCart,
            modifier = Modifier.weight(1f),
            testTag = "kpi_bazar_cost"
          )
          StatCard(
            title = AppStrings.totalDeposit(language),
            value = CalculationEngine.formatPaisa(summary.totalDepositPaisa, config.currencySymbol),
            icon = Icons.Default.AttachMoney,
            modifier = Modifier.weight(1f),
            testTag = "kpi_total_deposit"
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = AppStrings.totalBills(language),
            value = CalculationEngine.formatPaisa(summary.totalBillsPaisa, config.currencySymbol),
            icon = Icons.Default.Receipt,
            modifier = Modifier.weight(1f),
            testTag = "kpi_total_bills"
          )
          StatCard(
            title = AppStrings.messBalance(language),
            value = CalculationEngine.formatPaisa(summary.messCashInHandPaisa, config.currencySymbol),
            subtitle = "Deposit − Bazar − Bills",
            icon = Icons.Default.AccountBalance,
            containerColor = if (summary.messCashInHandPaisa >= 0) GreenContainer else RedContainer,
            contentColor = if (summary.messCashInHandPaisa >= 0) GreenIncome else RedExpense,
            modifier = Modifier.weight(1f),
            testTag = "kpi_cash_in_hand"
          )
        }
      }
    }

    // Quick Action Shortcuts
    item {
      Column {
        SectionHeader(title = "Quick Actions")
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { onNavigateToTab("add_meal") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
              .weight(1f)
              .testTag("quick_action_meal")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(AppStrings.addMeal(language), fontSize = 12.sp, maxLines = 1)
          }

          Button(
            onClick = { onOpenQuickAction("bazar") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier
              .weight(1f)
              .testTag("quick_action_bazar")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(AppStrings.addBazar(language), fontSize = 12.sp, maxLines = 1)
          }

          Button(
            onClick = { onOpenQuickAction("deposit") },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenIncome),
            modifier = Modifier
              .weight(1f)
              .testTag("quick_action_deposit")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(AppStrings.addDeposit(language), fontSize = 12.sp, maxLines = 1)
          }
        }
      }
    }

    // Notices banner
    if (notices.isNotEmpty()) {
      item {
        val topNotice = notices.first()
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToTab("menu") },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = topNotice.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = topNotice.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.Default.ArrowForward,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
