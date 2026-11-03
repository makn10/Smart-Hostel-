package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calc.CalculationEngine
import com.example.core.model.Role
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedContainer
import com.example.ui.theme.RedExpense
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector? = null,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  modifier: Modifier = Modifier,
  testTag: String = "stat_card"
) {
  Card(
    modifier = modifier.testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = contentColor.copy(alpha = 0.8f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = contentColor.copy(alpha = 0.7f),
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun RoleBadge(
  role: Role,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor) = when (role) {
    Role.MANAGER -> Pair(TealContainer, TealPrimary)
    Role.SUB_MANAGER -> Pair(AmberContainer, AmberSecondary)
    Role.MEMBER -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
  }

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = bgColor
  ) {
    Text(
      text = role.label,
      style = MaterialTheme.typography.labelSmall,
      color = textColor,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    )
  }
}

@Composable
fun BalanceChip(
  balancePaisa: Long,
  currencySymbol: String = "৳",
  modifier: Modifier = Modifier
) {
  val isPositive = balancePaisa >= 0
  val bgColor = if (isPositive) GreenContainer else RedContainer
  val textColor = if (isPositive) GreenIncome else RedExpense
  val label = if (isPositive) "Credit" else "Due"

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp),
    color = bgColor
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = CalculationEngine.formatPaisa(balancePaisa, currencySymbol, showSign = true),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = textColor
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "($label)",
        style = MaterialTheme.typography.labelSmall,
        color = textColor.copy(alpha = 0.85f),
        fontSize = 10.sp
      )
    }
  }
}

@Composable
fun UserAvatar(
  name: String,
  colorHex: Long = 0xFF009688,
  size: Int = 40,
  modifier: Modifier = Modifier
) {
  val initials = name.split(" ")
    .mapNotNull { it.firstOrNull()?.toString() }
    .take(2)
    .joinToString("")
    .uppercase()

  Box(
    modifier = modifier
      .size(size.dp)
      .clip(CircleShape)
      .background(Color(colorHex)),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initials.ifEmpty { "U" },
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = (size * 0.4).sp
    )
  }
}

@Composable
fun SectionHeader(
  title: String,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    if (actionText != null && onActionClick != null) {
      Text(
        text = actionText,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
          .clickable { onActionClick() }
          .padding(4.dp)
      )
    }
  }
}

@Composable
fun EmptyStateView(
  title: String,
  message: String,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.size(64.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
