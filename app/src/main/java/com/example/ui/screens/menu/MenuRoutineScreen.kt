package com.example.ui.screens.menu

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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestaurantMenu
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.model.MenuDay
import com.example.core.model.Notice
import com.example.ui.MessViewModel
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.TealPrimary

@Composable
fun MenuRoutineScreen(
  viewModel: MessViewModel,
  modifier: Modifier = Modifier
) {
  val menu by viewModel.menu.collectAsState()
  val notices by viewModel.notices.collectAsState()
  val language by viewModel.language.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  var showPostNoticeDialog by remember { mutableStateOf(false) }
  var editingMenuDay by remember { mutableStateOf<MenuDay?>(null) }

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
            text = if (selectedTab == 0) AppStrings.menu(language) else AppStrings.notices(language),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (selectedTab == 0) "Weekly Mess Meal Menu" else "Important Mess Announcements",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (selectedTab == 1) {
          Button(
            onClick = { showPostNoticeDialog = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("post_notice_btn")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Post Notice", fontSize = 12.sp)
          }
        }
      }

      TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
      ) {
        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Weekly Menu") })
        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Notice Board") })
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        // Weekly Menu List
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(menu) { day ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(day.dayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TealPrimary)
                  IconButton(onClick = { editingMenuDay = day }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Menu", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                  }
                }

                Spacer(modifier = Modifier.height(6.dp))
                MenuMealItem(title = "Breakfast", content = day.breakfast)
                MenuMealItem(title = "Lunch", content = day.lunch)
                MenuMealItem(title = "Dinner", content = day.dinner)
              }
            }
          }
        }
      } else {
        // Notice Board List
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(notices, key = { it.id }) { notice ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (notice.pinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
              ),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(notice.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                  if (notice.pinned) {
                    Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = AmberSecondary, modifier = Modifier.size(18.dp))
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(notice.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Posted by ${notice.createdBy} on ${notice.createdAt}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // Post Notice Dialog
    if (showPostNoticeDialog) {
      PostNoticeDialog(
        onDismiss = { showPostNoticeDialog = false },
        onPost = { title, desc, pinned ->
          viewModel.postNotice(title, desc, pinned)
          showPostNoticeDialog = false
        }
      )
    }

    // Edit Menu Dialog
    editingMenuDay?.let { day ->
      EditMenuDialog(
        day = day,
        onDismiss = { editingMenuDay = null },
        onSave = { b, l, d ->
          viewModel.updateMenuDay(day.dayIndex, b, l, d)
          editingMenuDay = null
        }
      )
    }
  }
}

@Composable
fun MenuMealItem(title: String, content: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "$title: ",
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.width(75.dp)
    )
    Text(
      text = content,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun PostNoticeDialog(
  onDismiss: () -> Unit,
  onPost: (String, String, Boolean) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var pinned by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Publish Notice") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Notice Title *") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description / Details *") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { pinned = !pinned },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(checked = pinned, onCheckedChange = { pinned = it })
          Spacer(modifier = Modifier.width(6.dp))
          Text("Pin to top of board")
        }
      }
    },
    confirmButton = {
      Button(onClick = {
        if (title.isNotBlank() && description.isNotBlank()) {
          onPost(title, description, pinned)
        }
      }) {
        Text("Publish")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun EditMenuDialog(
  day: MenuDay,
  onDismiss: () -> Unit,
  onSave: (String, String, String) -> Unit
) {
  var b by remember { mutableStateOf(day.breakfast) }
  var l by remember { mutableStateOf(day.lunch) }
  var d by remember { mutableStateOf(day.dinner) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit ${day.dayName} Menu") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = b,
          onValueChange = { b = it },
          label = { Text("Breakfast") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = l,
          onValueChange = { l = it },
          label = { Text("Lunch") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = d,
          onValueChange = { d = it },
          label = { Text("Dinner") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = { onSave(b, l, d) }) { Text("Save Menu") }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
