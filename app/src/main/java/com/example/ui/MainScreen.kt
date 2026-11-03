package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.core.localization.AppStrings
import com.example.ui.screens.bazar.AddBazarDialog
import com.example.ui.screens.bazar.BazarScreen
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.charts.ChartsScreen
import com.example.ui.screens.deposits.AddDepositDialog
import com.example.ui.screens.deposits.DepositsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.meals.AddMealScreen
import com.example.ui.screens.meals.MealSheetScreen
import com.example.ui.screens.meals.MyMealHistoryScreen
import com.example.ui.screens.members.MembersScreen
import com.example.ui.screens.menu.MenuRoutineScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.settings.RoleSwitcherDialog
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.summary.SummaryScreen

@Composable
fun MainScreen(viewModel: MessViewModel) {
  var currentTab by remember { mutableStateOf("home") }
  val language by viewModel.language.collectAsState()
  val members by viewModel.members.collectAsState()
  val currentUser = viewModel.getCurrentMember()

  val snackbarHostState = remember { SnackbarHostState() }
  val snackbarMsg by viewModel.snackbarMessage.collectAsState()

  LaunchedEffect(snackbarMsg) {
    snackbarMsg?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbar()
    }
  }

  // Quick Action Dialog States
  var showQuickBazarDialog by remember { mutableStateOf(false) }
  var showQuickDepositDialog by remember { mutableStateOf(false) }
  var showRoleSwitcherDialog by remember { mutableStateOf(false) }

  val isFullScreen = currentTab in listOf("add_meal", "notifications", "my_meal_history")

  if (isFullScreen) {
    BackHandler {
      currentTab = "home"
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (!isFullScreen) {
        NavigationBar(
          modifier = Modifier
            .navigationBarsPadding()
            .testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = AppStrings.home(language)) },
            label = { Text(AppStrings.home(language)) },
            selected = currentTab == "home",
            onClick = { currentTab = "home" },
            modifier = Modifier.testTag("nav_item_home")
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.People, contentDescription = AppStrings.members(language)) },
            label = { Text(AppStrings.members(language)) },
            selected = currentTab == "members",
            onClick = { currentTab = "members" },
            modifier = Modifier.testTag("nav_item_members")
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.Restaurant, contentDescription = AppStrings.meals(language)) },
            label = { Text(AppStrings.meals(language)) },
            selected = currentTab == "meals",
            onClick = { currentTab = "meals" },
            modifier = Modifier.testTag("nav_item_meals")
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = AppStrings.bazar(language)) },
            label = { Text(AppStrings.bazar(language)) },
            selected = currentTab == "bazar",
            onClick = { currentTab = "bazar" },
            modifier = Modifier.testTag("nav_item_bazar")
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = AppStrings.bills(language)) },
            label = { Text(AppStrings.bills(language)) },
            selected = currentTab == "bills",
            onClick = { currentTab = "bills" },
            modifier = Modifier.testTag("nav_item_bills")
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = AppStrings.charts(language)) },
            label = { Text(AppStrings.charts(language)) },
            selected = currentTab == "charts",
            onClick = { currentTab = "charts" },
            modifier = Modifier.testTag("nav_item_charts")
          )
          NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = AppStrings.settings(language)) },
            label = { Text(AppStrings.settings(language)) },
            selected = currentTab == "settings",
            onClick = { currentTab = "settings" },
            modifier = Modifier.testTag("nav_item_settings")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(if (!isFullScreen) innerPadding else androidx.compose.foundation.layout.PaddingValues())
    ) {
      when (currentTab) {
        "home" -> HomeScreen(
          viewModel = viewModel,
          onNavigateToTab = { currentTab = it },
          onOpenQuickAction = { action ->
            when (action) {
              "meal" -> currentTab = "add_meal"
              "bazar" -> showQuickBazarDialog = true
              "deposit" -> showQuickDepositDialog = true
            }
          },
          onOpenUserSwitcher = { showRoleSwitcherDialog = true }
        )
        "add_meal" -> AddMealScreen(
          viewModel = viewModel,
          onNavigateBack = { currentTab = "home" }
        )
        "notifications" -> NotificationsScreen(
          viewModel = viewModel,
          onNavigateBack = { currentTab = "home" }
        )
        "my_meal_history" -> MyMealHistoryScreen(
          viewModel = viewModel,
          onNavigateBack = { currentTab = "home" }
        )
        "members" -> MembersScreen(viewModel = viewModel)
        "meals" -> MealSheetScreen(
          viewModel = viewModel,
          onNavigateToAddMeal = { currentTab = "add_meal" }
        )
        "bazar" -> BazarScreen(viewModel = viewModel)
        "deposits" -> DepositsScreen(viewModel = viewModel)
        "bills" -> BillsScreen(viewModel = viewModel)
        "summary" -> SummaryScreen(viewModel = viewModel)
        "charts" -> ChartsScreen(viewModel = viewModel)
        "menu" -> MenuRoutineScreen(viewModel = viewModel)
        "settings" -> SettingsScreen(viewModel = viewModel)
        else -> HomeScreen(
          viewModel = viewModel,
          onNavigateToTab = { currentTab = it },
          onOpenQuickAction = {},
          onOpenUserSwitcher = { showRoleSwitcherDialog = true }
        )
      }
    }

    if (showQuickBazarDialog) {
      AddBazarDialog(
        members = members,
        onDismiss = { showQuickBazarDialog = false },
        onSave = { date, shopperId, items, addToDeposit ->
          viewModel.addBazar(date, shopperId, items, addToDeposit)
          showQuickBazarDialog = false
        }
      )
    }

    if (showQuickDepositDialog) {
      AddDepositDialog(
        members = members,
        onDismiss = { showQuickDepositDialog = false },
        onSave = { memberId, date, amountPaisa, method, note ->
          viewModel.addDeposit(memberId, date, amountPaisa, method, note)
          showQuickDepositDialog = false
        }
      )
    }

    if (showRoleSwitcherDialog) {
      RoleSwitcherDialog(
        members = members,
        currentMemberId = currentUser?.id ?: "",
        onDismiss = { showRoleSwitcherDialog = false },
        onSelectMember = {
          viewModel.switchUser(it)
          showRoleSwitcherDialog = false
        }
      )
    }
  }
}
