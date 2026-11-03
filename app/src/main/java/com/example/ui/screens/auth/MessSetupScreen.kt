package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessSetupScreen(
  onMessCreated: (name: String, mealSystem: String, ratePaisa: Long) -> Unit,
  onMessJoined: (code: String) -> Unit,
  onLogout: () -> Unit,
  isProcessing: Boolean = false,
  errorMessage: String? = null,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Create Mess, 1: Join Mess

  // Create Mess state
  var messName by remember { mutableStateOf("") }
  var mealSystem by remember { mutableStateOf("shared") } // "fixed" or "shared"
  var fixedMealRateStr by remember { mutableStateOf("60") }

  // Join Mess state
  var joinCode by remember { mutableStateOf("") }

  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Mess Setup",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
          )
        },
        actions = {
          IconButton(onClick = onLogout, modifier = Modifier.testTag("setup_logout_btn")) {
            Icon(
              Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = "Log out",
              tint = Color.White
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary)
      )
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(Color(0xFFF9FBFA))
    ) {
      // Top TabRow matching screenshots
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = TealPrimary,
        contentColor = Color.White,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = Color.White,
            height = 3.dp
          )
        }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Text(
              "Create Mess",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
              fontSize = 15.sp,
              color = Color.White
            )
          },
          modifier = Modifier.testTag("tab_create_mess")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              "Join Mess",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
              fontSize = 15.sp,
              color = Color.White
            )
          },
          modifier = Modifier.testTag("tab_join_mess")
        )
      }

      // Content Body
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (errorMessage != null) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFEBEE),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
          ) {
            Text(
              text = errorMessage,
              color = Color(0xFFD32F2F),
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(12.dp),
              textAlign = TextAlign.Center
            )
          }
        }

        if (selectedTab == 0) {
          // --- TAB 1: CREATE MESS ---
          Spacer(modifier = Modifier.height(10.dp))

          // Building with plus icon in Teal
          Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(64.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "আপনি কি ম্যানেজার?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "নতুন মেস খুলুন এবং কোড শেয়ার করুন",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
          )

          Spacer(modifier = Modifier.height(24.dp))

          // Mess Name Input
          OutlinedTextField(
            value = messName,
            onValueChange = { messName = it },
            label = { Text("Mess Name") },
            placeholder = { Text("eikhane mess name") },
            leadingIcon = {
              Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF1E293B))
            },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TealPrimary,
              unfocusedBorderColor = Color(0xFF00796B),
              focusedLabelColor = TealPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("setup_mess_name_input")
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Meal System Section
          Column(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "Meal System",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color(0xFF00695C)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Fixed Rate radio
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clickable { mealSystem = "fixed" }
                  .padding(end = 24.dp)
              ) {
                RadioButton(
                  selected = mealSystem == "fixed",
                  onClick = { mealSystem = "fixed" },
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Fixed Rate", fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
              }

              // Shared Cost radio
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { mealSystem = "shared" }
              ) {
                RadioButton(
                  selected = mealSystem == "shared",
                  onClick = { mealSystem = "shared" },
                  colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Shared Cost", fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
              }
            }
          }

          // Meal Rate input (when Fixed Rate is selected)
          if (mealSystem == "fixed") {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
              value = fixedMealRateStr,
              onValueChange = { fixedMealRateStr = it },
              label = { Text("Meal Rate (Tk)") },
              leadingIcon = {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF1E293B))
              },
              shape = RoundedCornerShape(10.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Color(0xFF00796B),
                focusedLabelColor = TealPrimary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("setup_meal_rate_input")
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          // CREATE MESS Button
          Button(
            onClick = {
              if (messName.isNotBlank()) {
                val ratePaisa = ((fixedMealRateStr.toDoubleOrNull() ?: 60.0) * 100).toLong()
                onMessCreated(messName, mealSystem, ratePaisa)
              }
            },
            enabled = !isProcessing && messName.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("create_mess_btn")
          ) {
            if (isProcessing) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
              Text("CREATE MESS", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Informative Explanation Card matching screenshots
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1).copy(alpha = 0.5f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF80CBC4))),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Lightbulb,
                  contentDescription = null,
                  tint = TealPrimary,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (mealSystem == "shared") "শেয়ার্ড কস্ট (Shared Cost) কী?" else "ফিক্সড রেট (Fixed Rate) কী?",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color(0xFF004D40)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = if (mealSystem == "shared")
                  "মাসের মোট বাজার খরচকে মোট মিল দিয়ে ভাগ করে প্রতিদিন অটোমেটিক মিল রেট বের হবে।"
                else
                  "মিল রেট আগে থেকেই নির্দিষ্ট থাকবে। বাজারের খরচের ওপর ভিত্তি করে এই রেট পরিবর্তন হবে না।",
                fontSize = 13.sp,
                color = Color(0xFF37474F),
                lineHeight = 18.sp
              )

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = if (mealSystem == "shared")
                  "💡 উদাহরণ: মেসে মোট বাজার ৫০০০ টাকা এবং মোট মিল ১০০ হলে, মিল রেট হবে ৫০ টাকা। সাধারণ ব্যাচেলর মেসে এই সিস্টেমটিই সবচেয়ে বেশি ব্যবহার করা হয়।"
                else
                  "💡 উদাহরণ: আপনি যদি রেট ৬০ টাকা সেট করেন, তবে সারা মাস সবার হিসাব এই রেটেই হবে। কমার্শিয়াল হোস্টেল বা নির্দিষ্ট ভাড়ার মেসের জন্য এটি পারফেক্ট।",
                fontSize = 12.sp,
                color = Color(0xFF00695C),
                lineHeight = 17.sp
              )
            }
          }

        } else {
          // --- TAB 2: JOIN MESS (Screenshot 4) ---
          Spacer(modifier = Modifier.height(20.dp))

          // User with plus icon in Blue
          Icon(
            imageVector = Icons.Default.GroupAdd,
            contentDescription = null,
            tint = Color(0xFF3F51B5),
            modifier = Modifier.size(64.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "আপনি কি মেম্বার?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "ম্যানেজারের দেওয়া কোডটি নিচে দিন",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
          )

          Spacer(modifier = Modifier.height(28.dp))

          // Mess Code Input
          OutlinedTextField(
            value = joinCode,
            onValueChange = { if (it.length <= 6) joinCode = it },
            label = { Text("Mess Code") },
            placeholder = { Text("e.g. 654321") },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color(0xFF3F51B5),
              unfocusedBorderColor = Color(0xFF64748B),
              focusedLabelColor = Color(0xFF3F51B5)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("join_mess_code_input")
          )

          Spacer(modifier = Modifier.height(24.dp))

          // JOIN MESS Button
          Button(
            onClick = {
              if (joinCode.isNotBlank()) {
                onMessJoined(joinCode.trim())
              }
            },
            enabled = !isProcessing && joinCode.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("join_mess_btn")
          ) {
            if (isProcessing) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
              Text("JOIN MESS", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }
        }
      }
    }
  }
}
