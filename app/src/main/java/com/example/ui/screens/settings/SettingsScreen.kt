package com.example.ui.screens.settings

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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.model.MealSystem
import com.example.core.model.MessConfig
import com.example.core.model.Role
import com.example.ui.MessViewModel
import com.example.ui.components.RoleBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.TealPrimary

@Composable
fun SettingsScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val config by viewModel.config.collectAsState()
  val period by viewModel.currentPeriod.collectAsState()
  val members by viewModel.members.collectAsState()
  val currentUser = viewModel.getCurrentMember()
  val language by viewModel.language.collectAsState()
  val snapshots by viewModel.snapshots.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()

  var showRoleSwitcherDialog by remember { mutableStateOf(false) }
  var showCloseMonthDialog by remember { mutableStateOf(false) }
  var showMessSettingsDialog by remember { mutableStateOf(false) }
  var showAuditLogsDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("settings_screen_list"),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text(
        text = AppStrings.settings(language),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Preferences, Month Archive & Management",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Google Cloud Account & Full System Save Card
    item {
      val authUser by viewModel.currentUser.collectAsState()
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("cloud_sync_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Cloud Database Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Text(
                if (authUser != null) "Logged in as ${authUser?.displayName ?: authUser?.email}" else "Sign in to backup data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            if (authUser != null) {
              OutlinedButton(
                onClick = { viewModel.signOut() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("settings_logout_btn")
              ) {
                Text("Log Out", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { viewModel.saveFullSystemToCloud() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier.fillMaxWidth().testTag("save_full_system_cloud_btn")
          ) {
            Icon(Icons.Default.Cached, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Full System to Cloud", fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "Syncs all members, daily meals, bazar records, deposits and notices to Firebase Cloud Firestore.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Current User Profile Card & Role Switcher
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              UserAvatar(name = currentUser?.name ?: "User", colorHex = currentUser?.avatarColorHex ?: 0xFF009688, size = 44)
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(currentUser?.name ?: "User", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                currentUser?.let { RoleBadge(role = it.role) }
              }
            }

            Button(
              onClick = { showRoleSwitcherDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("settings_switch_role_btn")
            ) {
              Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Switch Demo User", fontSize = 11.sp)
            }
          }
        }
      }
    }

    // Language Preference
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.toggleLanguage() }
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Language, contentDescription = null, tint = TealPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("App Language", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
              Text(
                if (language == AppLanguage.BANGLA) "বাংলা (Bangla active)" else "English (English active)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          OutlinedButton(onClick = { viewModel.toggleLanguage() }) {
            Text(if (language == AppLanguage.ENGLISH) "Switch to বাংলা" else "Switch to English")
          }
        }
      }
    }

    // Mess Settings (Only Manager can edit)
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Mess Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Button(
              onClick = { showMessSettingsDialog = true },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Edit Settings", fontSize = 11.sp)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text("Mess Name: ${config.name}", style = MaterialTheme.typography.bodyMedium)
          Text("Join Code: ${config.joinCode}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TealPrimary)
          Text("Meal System: ${config.mealSystem.title}", style = MaterialTheme.typography.bodyMedium)
          Text(
            if (config.mealSystem == MealSystem.SHARED_COST) AppStrings.sharedCostExplainer(language) else AppStrings.fixedRateExplainer(language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text("Weights: B:${config.breakfastWeight} | L:${config.lunchWeight} | D:${config.dinnerWeight}", style = MaterialTheme.typography.bodySmall)
        }
      }
    }

    // Month Close & Archive Wizard
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(AppStrings.closeMonth(language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
              Text("Freeze calculations and open new period", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
              onClick = { showCloseMonthDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("close_month_btn")
            ) {
              Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Close Month", fontSize = 11.sp)
            }
          }

          if (snapshots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Archived Snapshots (${snapshots.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            snapshots.forEach { snap ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(snap.periodName, fontWeight = FontWeight.SemiBold)
                  Text("Rate: ${CalculationEngine.formatPaisa(snap.mealRatePaisa)} • Closed: ${snap.closedAt}", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                  onClick = { viewModel.reopenMonth(snap.periodId) },
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text("Reopen", fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    }

    // Activity & Audit Log
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showAuditLogsDialog = true }
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = TealPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(AppStrings.auditTrail(language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
              Text("${auditLogs.size} recorded system actions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          OutlinedButton(onClick = { showAuditLogsDialog = true }) {
            Text("View Log")
          }
        }
      }
    }

    // Reset / Reload Demo Data
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.resetSeedData() }
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Reload Demo Dataset", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
              Text("Re-seeds 6 members, 25 days of meals, bills & bazar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Button(onClick = { viewModel.resetSeedData() }, shape = RoundedCornerShape(8.dp)) {
            Text("Reset", fontSize = 11.sp)
          }
        }
      }
    }
  }

  // Dialogs
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

  if (showCloseMonthDialog) {
    CloseMonthWizardDialog(
      currentPeriodName = period.name,
      onDismiss = { showCloseMonthDialog = false },
      onConfirm = { archiveName, carryOver ->
        viewModel.closeMonth(archiveName, carryOver)
        showCloseMonthDialog = false
      }
    )
  }

  if (showMessSettingsDialog) {
    MessSettingsDialog(
      currentConfig = config,
      onDismiss = { showMessSettingsDialog = false },
      onSave = {
        viewModel.updateConfig(it)
        showMessSettingsDialog = false
      }
    )
  }

  if (showAuditLogsDialog) {
    AuditLogsDialog(
      logs = auditLogs,
      onDismiss = { showAuditLogsDialog = false }
    )
  }
}

@Composable
fun RoleSwitcherDialog(
  members: List<com.example.core.model.Member>,
  currentMemberId: String,
  onDismiss: () -> Unit,
  onSelectMember: (String) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Switch Demo User (Role Testing)") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Select a member to preview their dashboard, permissions, and expenses:", style = MaterialTheme.typography.bodySmall)
        members.forEach { mem ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectMember(mem.id) }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = mem.id == currentMemberId, onClick = { onSelectMember(mem.id) })
            Spacer(modifier = Modifier.width(6.dp))
            UserAvatar(name = mem.name, colorHex = mem.avatarColorHex, size = 28)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(mem.name, fontWeight = FontWeight.Bold)
              RoleBadge(role = mem.role)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) { Text("Done") }
    }
  )
}

@Composable
fun CloseMonthWizardDialog(
  currentPeriodName: String,
  onDismiss: () -> Unit,
  onConfirm: (String, Boolean) -> Unit
) {
  var archiveName by remember { mutableStateOf(currentPeriodName) }
  var carryOver by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Close & Archive Period", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("This will freeze all meal rates, bills, and balances into an immutable snapshot.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
          value = archiveName,
          onValueChange = { archiveName = it },
          label = { Text("Archive Name (e.g. November 2026)") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { carryOver = !carryOver },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(checked = carryOver, onCheckedChange = { carryOver = it })
          Spacer(modifier = Modifier.width(6.dp))
          Text("Carry over ± balances as next month opening balance", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    confirmButton = {
      Button(onClick = { onConfirm(archiveName, carryOver) }) {
        Text("Confirm & Archive")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun MessSettingsDialog(
  currentConfig: MessConfig,
  onDismiss: () -> Unit,
  onSave: (MessConfig) -> Unit
) {
  var name by remember { mutableStateOf(currentConfig.name) }
  var mealSystem by remember { mutableStateOf(currentConfig.mealSystem) }
  var fixedRateStr by remember { mutableStateOf((currentConfig.fixedRatePaisa / 100.0).toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Mess Configuration") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Mess Name") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Meal System Mode:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = { mealSystem = MealSystem.SHARED_COST },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (mealSystem == MealSystem.SHARED_COST) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.weight(1f)
          ) {
            Text("Shared Cost", fontSize = 11.sp)
          }

          Button(
            onClick = { mealSystem = MealSystem.FIXED_RATE },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (mealSystem == MealSystem.FIXED_RATE) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.weight(1f)
          ) {
            Text("Fixed Rate", fontSize = 11.sp)
          }
        }

        if (mealSystem == MealSystem.FIXED_RATE) {
          OutlinedTextField(
            value = fixedRateStr,
            onValueChange = { fixedRateStr = it },
            label = { Text("Fixed Meal Rate (৳)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = {
        val fixedRate = ((fixedRateStr.toDoubleOrNull() ?: 65.0) * 100).toLong()
        onSave(currentConfig.copy(name = name, mealSystem = mealSystem, fixedRatePaisa = fixedRate))
      }) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun AuditLogsDialog(
  logs: List<com.example.core.model.AuditLog>,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Activity & Audit Log") },
    text = {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().height(300.dp)
      ) {
        LazyColumn(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(logs) { log ->
            Column {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${log.entity} • ${log.action.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)
                Text(log.byUser, style = MaterialTheme.typography.labelSmall)
              }
              Text(log.details, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) { Text("Close") }
    }
  )
}
