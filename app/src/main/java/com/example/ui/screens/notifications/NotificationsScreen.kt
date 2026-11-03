package com.example.ui.screens.notifications

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.MessViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

@Composable
fun NotificationsScreen(
  viewModel: MessViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val notifications by viewModel.notifications.collectAsState()
  var selectedFilter by remember { mutableStateOf("All") }

  val filteredNotifications = remember(notifications, selectedFilter) {
    if (selectedFilter == "All") notifications else notifications.filter { it.category == selectedFilter }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFB))
  ) {
    // Top Bar (matching Screenshot 2)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onBackground
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Notifications",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Stay updated with your mess activity",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF64748B)
        )
      }

      IconButton(
        onClick = { viewModel.clearAllNotifications() },
        modifier = Modifier.testTag("clear_notifications_btn")
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFFEBEE),
          modifier = Modifier.size(38.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Clear All",
              tint = Color(0xFFE53935),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }

    // Filter Chips Row: [All (4)] [Meal Updates] [Deposit] [System]
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val categories = listOf("All", "Meal Updates", "Deposit", "System")
      items(categories) { cat ->
        val count = if (cat == "All") notifications.size else notifications.count { it.category == cat }
        val isSelected = selectedFilter == cat

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isSelected) TealPrimary else Color.White,
          border = if (!isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))) else null,
          modifier = Modifier
            .clickable { selectedFilter = cat }
            .testTag("notif_filter_$cat")
        ) {
          Text(
            text = if (cat == "All") "All  $count" else cat,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Notification Cards List
    if (filteredNotifications.isEmpty()) {
      Box(modifier = Modifier.weight(1f)) {
        EmptyStateView(
          title = "No Notifications",
          message = "You're all caught up with your mess activities!",
          icon = Icons.Default.Campaign
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredNotifications, key = { it.id }) { notif ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEDF2F7))),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.Top
            ) {
              // Unread Blue Dot
              Box(
                modifier = Modifier
                  .padding(top = 10.dp, end = 8.dp)
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (!notif.isRead) Color(0xFF1E88E5) else Color.Transparent)
              )

              // Speaker Icon
              Surface(
                shape = CircleShape,
                color = TealContainer,
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = notif.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                  )
                  Text(
                    text = notif.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = notif.message,
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFF1E293B),
                  lineHeight = 20.sp
                )
              }
            }
          }
        }
      }
    }

    // Bottom Security & Mark All As Read Bar (matching Screenshot 2)
    Surface(
      color = Color.White,
      shadowElevation = 4.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = Color(0xFF009688),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "All your notifications are secure.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
          )
        }

        TextButton(
          onClick = { viewModel.markAllNotificationsAsRead() },
          colors = ButtonDefaults.textButtonColors(contentColor = TealPrimary)
        ) {
          Text(
            text = "Mark all as read",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
