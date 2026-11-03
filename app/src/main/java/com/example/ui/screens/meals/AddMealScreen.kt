package com.example.ui.screens.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Member
import com.example.ui.MessViewModel
import com.example.ui.components.UserAvatar
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddMealScreen(
  viewModel: MessViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val members by viewModel.members.collectAsState()
  val meals by viewModel.meals.collectAsState()
  val activeMembers = remember(members) { members.filter { it.active } }

  // 0 = All Members, 1 = Single Member
  var selectedTab by remember { mutableIntStateOf(0) }

  // Date selection
  val dateSdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
  val displaySdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.US) }
  var selectedDateStr by remember { mutableStateOf(dateSdf.format(Date())) }
  var showDatePickerDialog by remember { mutableStateOf(false) }

  // Auto-submit modal
  var showAutoSubmitSetupDialog by remember { mutableStateOf(false) }

  // Default values for "All Members"
  var defaultBreakfast by remember { mutableDoubleStateOf(1.0) }
  var defaultLunch by remember { mutableDoubleStateOf(1.0) }
  var defaultDinner by remember { mutableDoubleStateOf(1.0) }

  // Per-member overrides state: memberId -> Triple(B, L, D)
  val memberMealOverrides = remember { mutableStateMapOf<String, Triple<Double, Double, Double>>() }

  // Single member state
  var singleSelectedMemberId by remember { mutableStateOf(activeMembers.firstOrNull()?.id ?: "") }
  var singleBreakfast by remember { mutableDoubleStateOf(1.0) }
  var singleLunch by remember { mutableDoubleStateOf(1.0) }
  var singleDinner by remember { mutableDoubleStateOf(1.0) }
  var singleGuests by remember { mutableDoubleStateOf(0.0) }

  // Sync member overrides when date changes or default changes
  LaunchedEffect(selectedDateStr, activeMembers) {
    activeMembers.forEach { mem ->
      val existing = meals.find { it.memberId == mem.id && it.date == selectedDateStr }
      if (existing != null) {
        memberMealOverrides[mem.id] = Triple(existing.breakfast, existing.lunch, existing.dinner)
      } else {
        memberMealOverrides[mem.id] = Triple(defaultBreakfast, defaultLunch, defaultDinner)
      }
    }
    // Update single member initial values
    val singleExisting = meals.find { it.memberId == singleSelectedMemberId && it.date == selectedDateStr }
    if (singleExisting != null) {
      singleBreakfast = singleExisting.breakfast
      singleLunch = singleExisting.lunch
      singleDinner = singleExisting.dinner
      singleGuests = singleExisting.guests
    }
  }

  // Update all when default steppers change
  fun applyDefaultToAll(b: Double, l: Double, d: Double) {
    defaultBreakfast = b
    defaultLunch = l
    defaultDinner = d
    activeMembers.forEach { mem ->
      memberMealOverrides[mem.id] = Triple(b, l, d)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFB))
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier.testTag("add_meal_back_btn")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onBackground
        )
      }
      Spacer(modifier = Modifier.weight(0.7f))
      Text(
        text = "Add Meal",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.weight(1f))
    }

    // Two Top Toggle Pills: [ All Members ] [ Single Member ]
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFFF1F5F9),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .padding(3.dp)
      ) {
        Row(modifier = Modifier.fillMaxSize()) {
          // All Members Tab
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .clip(RoundedCornerShape(26.dp))
              .background(if (selectedTab == 0) TealPrimary else Color.Transparent)
              .clickable { selectedTab = 0 }
              .testTag("tab_all_members"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "All Members",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (selectedTab == 0) Color.White else Color(0xFF475569)
            )
          }

          // Single Member Tab
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .clip(RoundedCornerShape(26.dp))
              .background(if (selectedTab == 1) TealPrimary else Color.Transparent)
              .clickable { selectedTab = 1 }
              .testTag("tab_single_member"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Single Member",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = if (selectedTab == 1) Color.White else Color(0xFF475569)
            )
          }
        }
      }
    }

    // Scrollable Content
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Selected Date Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePickerDialog = true }
            .testTag("selected_date_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = TealContainer,
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.CalendarMonth,
                  contentDescription = null,
                  tint = TealDark,
                  modifier = Modifier.size(22.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Selected Date",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
              )
              val formattedDisplay = try {
                val parsed = dateSdf.parse(selectedDateStr)
                if (parsed != null) displaySdf.format(parsed) else selectedDateStr
              } catch (e: Exception) {
                selectedDateStr
              }
              Text(
                text = formattedDisplay,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
              )
            }

            IconButton(onClick = { showDatePickerDialog = true }) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Date",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      if (selectedTab == 0) {
        // === ALL MEMBERS SECTION ===

        // 2. Set Default for All Card
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F7F5)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFB2DFDB))),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("set_default_card")
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Set Default for All",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TealDark
              )
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                MealCapsuleStepper(
                  iconEmoji = "☕",
                  title = "Breakfast",
                  value = defaultBreakfast,
                  onValueChange = { applyDefaultToAll(it, defaultLunch, defaultDinner) },
                  modifier = Modifier.weight(1f),
                  testTag = "default_breakfast_stepper"
                )
                MealCapsuleStepper(
                  iconEmoji = "🍛",
                  title = "Lunch",
                  value = defaultLunch,
                  onValueChange = { applyDefaultToAll(defaultBreakfast, it, defaultDinner) },
                  modifier = Modifier.weight(1f),
                  testTag = "default_lunch_stepper"
                )
                MealCapsuleStepper(
                  iconEmoji = "🍽️",
                  title = "Dinner",
                  value = defaultDinner,
                  onValueChange = { applyDefaultToAll(defaultBreakfast, defaultLunch, it) },
                  modifier = Modifier.weight(1f),
                  testTag = "default_dinner_stepper"
                )
              }
            }
          }
        }

        // 3. Member List with per-member overrides
        items(activeMembers, key = { it.id }) { member ->
          val currentTriple = memberMealOverrides[member.id] ?: Triple(defaultBreakfast, defaultLunch, defaultDinner)
          val (b, l, d) = currentTriple
          val totalMeal = b + l + d

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("member_meal_card_${member.id}")
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              // Member Header Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = Color(member.avatarColorHex).copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(member.avatarColorHex),
                        modifier = Modifier.size(22.dp)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = member.name,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF1E293B)
                    )
                    Text(
                      text = "Role: ${member.role.label.lowercase(Locale.US)}",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFF64748B)
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFFF1F5F9)
                ) {
                  Text(
                    text = "Total: ${if (totalMeal % 1.0 == 0.0) "${totalMeal.toInt()}.0" else "$totalMeal"}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // 3 Meal Capsule Steppers Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                MealCapsuleStepper(
                  iconEmoji = "☕",
                  title = "Breakfast",
                  value = b,
                  onValueChange = { memberMealOverrides[member.id] = Triple(it, l, d) },
                  modifier = Modifier.weight(1f)
                )
                MealCapsuleStepper(
                  iconEmoji = "🍛",
                  title = "Lunch",
                  value = l,
                  onValueChange = { memberMealOverrides[member.id] = Triple(b, it, d) },
                  modifier = Modifier.weight(1f)
                )
                MealCapsuleStepper(
                  iconEmoji = "🍽️",
                  title = "Dinner",
                  value = d,
                  onValueChange = { memberMealOverrides[member.id] = Triple(b, l, it) },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }

        // 4. Prominent "Submit For All" Button
        item {
          Button(
            onClick = {
              viewModel.saveAllMemberMeals(selectedDateStr, memberMealOverrides)
              onNavigateBack()
            },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("submit_for_all_btn")
          ) {
            Text(
              text = "Submit For All",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      } else {
        // === SINGLE MEMBER SECTION ===

        // Member Selector
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Select Member",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
              )
              Spacer(modifier = Modifier.height(8.dp))

              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(activeMembers) { member ->
                  val isSelected = member.id == singleSelectedMemberId
                  Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) TealPrimary else Color(0xFFF1F5F9),
                    modifier = Modifier
                      .clickable {
                        singleSelectedMemberId = member.id
                        val existing = meals.find { it.memberId == member.id && it.date == selectedDateStr }
                        if (existing != null) {
                          singleBreakfast = existing.breakfast
                          singleLunch = existing.lunch
                          singleDinner = existing.dinner
                          singleGuests = existing.guests
                        }
                      }
                      .testTag("select_single_member_${member.id}")
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      UserAvatar(name = member.name, colorHex = member.avatarColorHex, size = 26)
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = member.name.split(" ").first(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF1E293B)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Selected Member Details & Steppers Card
        item {
          val selectedMember = activeMembers.find { it.id == singleSelectedMemberId } ?: activeMembers.firstOrNull()
          val singleTotal = singleBreakfast + singleLunch + singleDinner + singleGuests

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  UserAvatar(
                    name = selectedMember?.name ?: "Member",
                    colorHex = selectedMember?.avatarColorHex ?: 0xFF009688,
                    size = 40
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = selectedMember?.name ?: "Select Member",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "Role: ${selectedMember?.role?.label?.lowercase() ?: ""}",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color(0xFF64748B)
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = TealContainer
                ) {
                  Text(
                    text = "Total: ${if (singleTotal % 1.0 == 0.0) "${singleTotal.toInt()}.0" else "$singleTotal"}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealDark,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Stepper Capsules
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                MealCapsuleStepper(
                  iconEmoji = "☕",
                  title = "Breakfast",
                  value = singleBreakfast,
                  onValueChange = { singleBreakfast = it },
                  modifier = Modifier.weight(1f)
                )
                MealCapsuleStepper(
                  iconEmoji = "🍛",
                  title = "Lunch",
                  value = singleLunch,
                  onValueChange = { singleLunch = it },
                  modifier = Modifier.weight(1f)
                )
                MealCapsuleStepper(
                  iconEmoji = "🍽️",
                  title = "Dinner",
                  value = singleDinner,
                  onValueChange = { singleDinner = it },
                  modifier = Modifier.weight(1f)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Guest Meals Stepper
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "👥 Guest Meals",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                  )
                  Text(
                    text = "Extra visitors hosted by this member",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = Color.White,
                  border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
                  modifier = Modifier.height(36.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(enabled = singleGuests > 0) { if (singleGuests >= 0.5) singleGuests -= 0.5 },
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = "−", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (singleGuests > 0) TealPrimary else Color.LightGray)
                    }
                    Text(
                      text = if (singleGuests % 1.0 == 0.0) "${singleGuests.toInt()}" else "$singleGuests",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF1E293B),
                      modifier = Modifier.width(32.dp),
                      textAlign = TextAlign.Center
                    )
                    Box(
                      modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { singleGuests += 0.5 },
                      contentAlignment = Alignment.Center
                    ) {
                      Text(text = "+", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                    }
                  }
                }
              }
            }
          }
        }

        // Single Member Submit Button
        item {
          Button(
            onClick = {
              if (singleSelectedMemberId.isNotBlank()) {
                viewModel.setMeal(
                  memberId = singleSelectedMemberId,
                  date = selectedDateStr,
                  breakfast = singleBreakfast,
                  lunch = singleLunch,
                  dinner = singleDinner,
                  guests = singleGuests
                )
                onNavigateBack()
              }
            },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("submit_single_member_btn")
          ) {
            Text(
              text = "Submit Member Meal",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      // 5. Auto-Submit Banner Card at bottom (matching Screenshot 1)
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.8f)),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC8E6C9))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "প্রতিদিন সাবমিট করতে বিরক্ত?",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1B5E20)
                )
                Text(
                  text = "অটো-সাবমিট চালু করুন 🤖",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF2E7D32)
                )
              }
            }

            Button(
              onClick = { showAutoSubmitSetupDialog = true },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
              modifier = Modifier
                .height(34.dp)
                .testTag("setup_autosubmit_btn")
            ) {
              Text(
                text = "Setup",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
      DatePickerSelectorDialog(
        currentDate = selectedDateStr,
        onDismiss = { showDatePickerDialog = false },
        onDateSelected = { newDate ->
          selectedDateStr = newDate
          showDatePickerDialog = false
        }
      )
    }

    // Auto-Submit Setup Dialog
    if (showAutoSubmitSetupDialog) {
      AutoSubmitSetupDialog(
        onDismiss = { showAutoSubmitSetupDialog = false },
        onEnable = { cutoffHour ->
          viewModel.showMessage("Auto-submit scheduled daily at $cutoffHour:00 PM with default meals!")
          showAutoSubmitSetupDialog = false
        }
      )
    }
  }
}

/**
 * Capsule Stepper matching Screenshot 1 exactly:
 * Shows emoji + title on top, and pill container below with [- 1.0 +]
 */
@Composable
fun MealCapsuleStepper(
  iconEmoji: String,
  title: String,
  value: Double,
  onValueChange: (Double) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "meal_capsule_stepper"
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "$iconEmoji $title",
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF64748B),
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1
    )
    Spacer(modifier = Modifier.height(4.dp))

    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color.White,
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .testTag(testTag)
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clickable(enabled = value > 0) {
              if (value >= 0.5) onValueChange(value - 0.5)
            },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "−",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = if (value > 0) TealPrimary else Color.LightGray
          )
        }

        Text(
          text = if (value % 1.0 == 0.0) "${value.toInt()}.0" else "$value",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1E293B)
        )

        Box(
          modifier = Modifier
            .size(32.dp)
            .clickable { onValueChange(value + 0.5) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "+",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = TealPrimary
          )
        }
      }
    }
  }
}

@Composable
fun DatePickerSelectorDialog(
  currentDate: String,
  onDismiss: () -> Unit,
  onDateSelected: (String) -> Unit
) {
  var textDate by remember { mutableStateOf(currentDate) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select Date for Meals") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Quick Day Selector (November 2026):", style = MaterialTheme.typography.bodySmall)

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(30) { dayIdx ->
            val day = dayIdx + 1
            val dStr = String.format(Locale.US, "2026-11-%02d", day)
            val isSelected = dStr == textDate

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isSelected) TealPrimary else Color(0xFFF1F5F9),
              modifier = Modifier
                .clickable { textDate = dStr }
                .padding(vertical = 4.dp)
            ) {
              Text(
                text = "$day",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
              )
            }
          }
        }

        OutlinedTextField(
          value = textDate,
          onValueChange = { textDate = it },
          label = { Text("Custom Date (YYYY-MM-DD)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = { onDateSelected(textDate) }) {
        Text("Done")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun AutoSubmitSetupDialog(
  onDismiss: () -> Unit,
  onEnable: (Int) -> Unit
) {
  var cutoffHour by remember { mutableIntStateOf(22) } // 10 PM

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Daily Auto-Submit Setup")
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Every day before the cutoff time, meals will automatically be recorded with default counts for all active members unless a member requests meal OFF.",
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Cut-off time: $cutoffHour:00 PM (Every Night)",
          fontWeight = FontWeight.Bold,
          color = TealDark
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(20, 21, 22, 23).forEach { hour ->
            Button(
              onClick = { cutoffHour = hour },
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (cutoffHour == hour) TealPrimary else Color(0xFFF1F5F9)
              ),
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "${hour - 12} PM",
                color = if (cutoffHour == hour) Color.White else Color(0xFF1E293B),
                fontSize = 11.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = { onEnable(cutoffHour) }) {
        Text("Enable Auto-Submit")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
