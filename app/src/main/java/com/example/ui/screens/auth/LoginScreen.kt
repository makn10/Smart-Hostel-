package com.example.ui.screens.auth

import android.app.Activity
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.firebase.GoogleAuthManager
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealPrimary
import com.google.firebase.auth.FirebaseUser

@Composable
fun LoginScreen(
  onAuthSuccess: (FirebaseUser) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var isSignUpMode by remember { mutableStateOf(false) }
  var fullName by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showForgotPasswordDialog by remember { mutableStateOf(false) }
  var forgotEmail by remember { mutableStateOf("") }

  val scrollState = rememberScrollState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFF9FBFA))
  ) {
    // Decorative curved background gradient at top
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFFE0F2F1),
              Color(0xFFF9FBFA)
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(28.dp))

      // Logo circular container
      Surface(
        shape = CircleShape,
        color = Color(0xFFE0F2F1),
        modifier = Modifier.size(90.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(68.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = "Smart Mess Logo",
                tint = TealPrimary,
                modifier = Modifier.size(36.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Header Texts
      Text(
        text = if (isSignUpMode) "Create Account" else "Welcome Back!",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B)
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (isSignUpMode) "স্মার্ট মেস ম্যানেজারে নতুন একাউন্ট খুলুন" else "আপনার একাউন্টে লগইন করুন",
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF64748B)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Error banner
      if (errorMessage != null) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFFEBEE),
          modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
        ) {
          Text(
            text = errorMessage ?: "",
            color = Color(0xFFD32F2F),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
          )
        }
      }

      // Sign-Up Name field
      if (isSignUpMode) {
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it },
          label = { Text("Full Name") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = Color(0xFFCBD5E1)
          ),
          modifier = Modifier.fillMaxWidth().testTag("signup_name_field")
        )
        Spacer(modifier = Modifier.height(14.dp))
      }

      // Email Address field
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email Address") },
        leadingIcon = {
          Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = TealPrimary,
          unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        modifier = Modifier.fillMaxWidth().testTag("login_email_field")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Password field
      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B))
        },
        trailingIcon = {
          IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
            Icon(
              imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
              tint = Color(0xFF64748B)
            )
          }
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = TealPrimary,
          unfocusedBorderColor = Color(0xFFCBD5E1)
        ),
        modifier = Modifier.fillMaxWidth().testTag("login_password_field")
      )

      // Confirm Password in sign up
      if (isSignUpMode) {
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
          value = confirmPassword,
          onValueChange = { confirmPassword = it },
          label = { Text("Confirm Password") },
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B))
          },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = Color(0xFFCBD5E1)
          ),
          modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password")
        )
      } else {
        // Forgot password link
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          contentAlignment = Alignment.CenterEnd
        ) {
          Text(
            text = "পাসওয়ার্ড ভুলে গেছেন?",
            color = TealDark,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
              .clickable { showForgotPasswordDialog = true }
              .padding(4.dp)
              .testTag("forgot_password_btn")
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // LOG IN Button
      Button(
        onClick = {
          if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter your email and password, or use Google Sign-In below."
          } else if (activity != null) {
            isLoading = true
            errorMessage = null
            GoogleAuthManager.signInWithGoogle(
              activity = activity,
              credentialManager = credentialManager,
              scope = scope,
              onAuthSuccess = { user ->
                isLoading = false
                onAuthSuccess(user)
              },
              onAuthError = { err ->
                isLoading = false
                errorMessage = err
              },
              onAuthCancelled = {
                isLoading = false
              }
            )
          }
        },
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("login_submit_btn")
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(24.dp)
          )
        } else {
          Text(
            text = if (isSignUpMode) "SIGN UP" else "LOG IN",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // OR Divider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
        Text(
          text = "OR",
          color = Color(0xFF94A3B8),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 14.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Continue with Google Button
      OutlinedButton(
        onClick = {
          if (activity != null) {
            isLoading = true
            errorMessage = null
            GoogleAuthManager.signInWithGoogle(
              activity = activity,
              credentialManager = credentialManager,
              scope = scope,
              onAuthSuccess = { user ->
                isLoading = false
                onAuthSuccess(user)
              },
              onAuthError = { err ->
                isLoading = false
                errorMessage = err
              },
              onAuthCancelled = {
                isLoading = false
              }
            )
          }
        },
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("continue_with_google_btn")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          // Google Colorful G representation
          Surface(
            shape = CircleShape,
            color = Color(0xFFEA4335),
            modifier = Modifier.size(20.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Continue with Google",
            color = Color(0xFF1E293B),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Toggle Login / Sign Up
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = if (isSignUpMode) "আগে থেকেই একাউন্ট আছে? " else "কোনো একাউন্ট নেই? ",
          color = Color(0xFF64748B),
          fontSize = 13.sp
        )
        Text(
          text = if (isSignUpMode) "লগইন করুন" else "নতুন একাউন্ট খুলুন",
          color = TealDark,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          modifier = Modifier
            .clickable {
              isSignUpMode = !isSignUpMode
              errorMessage = null
            }
            .padding(4.dp)
            .testTag("toggle_signup_btn")
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 3 Quick Action Social / Help Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Facebook Page
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9).copy(alpha = 0.8f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Surface(shape = CircleShape, color = Color(0xFF1877F2), modifier = Modifier.size(34.dp)) {
              Box(contentAlignment = Alignment.Center) {
                Text("f", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "Follow Our Facebook Page",
              fontSize = 10.sp,
              textAlign = TextAlign.Center,
              color = Color(0xFF475569),
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Web Version
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9).copy(alpha = 0.8f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Surface(shape = CircleShape, color = Color(0xFF00897B), modifier = Modifier.size(34.dp)) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Laptop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "For iPhone/Laptop user Web Version",
              fontSize = 10.sp,
              textAlign = TextAlign.Center,
              color = Color(0xFF475569),
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Video Tutorial
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9).copy(alpha = 0.8f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Surface(shape = CircleShape, color = Color(0xFFFF0000), modifier = Modifier.size(34.dp)) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "ভিডিও টিউটোরিয়াল এখানে দেখুন",
              fontSize = 10.sp,
              textAlign = TextAlign.Center,
              color = Color(0xFF475569),
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Bottom Security Badge
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          Icons.Default.Security,
          contentDescription = null,
          tint = TealPrimary,
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            "নিরাপদ, দ্রুত এবং সহজ",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF1E293B)
          )
          Text(
            "আপনার মেস ব্যবস্থাপনার সেরা সমাধান",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Forgot password dialog
  if (showForgotPasswordDialog) {
    AlertDialog(
      onDismissRequest = { showForgotPasswordDialog = false },
      title = { Text("Password Reset") },
      text = {
        Column {
          Text("আপনার একাউন্টের ইমেইল দিন, পাসওয়ার্ড রিসেট লিংক পাঠানো হবে।", fontSize = 13.sp)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = forgotEmail,
            onValueChange = { forgotEmail = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(onClick = { showForgotPasswordDialog = false }) {
          Text("Send Reset Link")
        }
      },
      dismissButton = {
        TextButton(onClick = { showForgotPasswordDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
