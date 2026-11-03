package com.example.ui.screens.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MessViewModel
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyMealHistoryScreen(
  viewModel: MessViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentUser = viewModel.getCurrentMember()
  val meals by viewModel.meals.collectAsState()
  val userMeals = meals.filter { it.memberId == currentUser?.id }.sortedByDescending { it.date }

  val inSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  val daySdf = SimpleDateFormat("dd MMM", Locale.US)
  val weekdaySdf = SimpleDateFormat("EEEE", Locale.US)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFB))
  ) {
    // Green Header Bar (matching Screenshot 3)
    Surface(
      color = TealPrimary,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.weight(0.7f))
        Text(
          text = "My Meal History",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
      }
    }

    // List of Day Cards
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(userMeals, key = { it.id }) { meal ->
        val total = meal.rawMealCount()
        val isOn = total > 0.0

        val (formattedDate, weekday) = try {
          val d = inSdf.parse(meal.date)
          if (d != null) Pair(daySdf.format(d), weekdaySdf.format(d)) else Pair(meal.date, "")
        } catch (e: Exception) {
          Pair(meal.date, "")
        }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(
              width = 1.5.dp,
              color = if (isOn) Color(0xFF4CAF50).copy(alpha = 0.6f) else Color(0xFFE0E0E0),
              shape = RoundedCornerShape(16.dp)
            )
            .testTag("meal_history_${meal.date}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isOn) Color(0xFFE8F5E9) else Color(0xFFF1F5F9),
                modifier = Modifier.size(44.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (isOn) Color(0xFF2E7D32) else Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column {
                Text(
                  text = formattedDate,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1E293B)
                )
                Text(
                  text = weekday,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF94A3B8)
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${if (total % 1.0 == 0.0) "${total.toInt()}" else "$total"} Meal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isOn) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
              ) {
                Text(
                  text = if (isOn) "ON" else "OFF",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isOn) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
