package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.data.firebase.GoogleAuthManager
import com.example.ui.MainScreen
import com.example.ui.MessViewModel
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.MessSetupScreen
import com.example.ui.theme.SmartMessTheme

class MainActivity : ComponentActivity() {
  private val viewModel: MessViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SmartMessTheme {
        AppContent(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun AppContent(viewModel: MessViewModel) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val currentUser by viewModel.currentUser.collectAsState()
  val currentMessId by viewModel.currentMessId.collectAsState()
  val isAuthProcessing by viewModel.isAuthProcessing.collectAsState()
  val authErrorMessage by viewModel.authErrorMessage.collectAsState()

  // Attempt silent auto-sign in on initial launch
  LaunchedEffect(Unit) {
    if (currentUser == null) {
      val credentialManager = CredentialManager.create(context)
      GoogleAuthManager.attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        scope = scope,
        onAuthSuccess = { user ->
          viewModel.onUserAuthenticated(user)
        },
        onUnauthenticated = {
          // Stay on LoginScreen
        }
      )
    }
  }

  if (currentUser == null) {
    LoginScreen(
      onAuthSuccess = { user ->
        viewModel.onUserAuthenticated(user)
      }
    )
  } else if (currentMessId.isNullOrBlank()) {
    MessSetupScreen(
      onMessCreated = { name, mealSystem, ratePaisa ->
        viewModel.createMess(name, mealSystem, ratePaisa)
      },
      onMessJoined = { code ->
        viewModel.joinMess(code)
      },
      onLogout = {
        viewModel.signOut()
      },
      isProcessing = isAuthProcessing,
      errorMessage = authErrorMessage
    )
  } else {
    MainScreen(viewModel = viewModel)
  }
}
