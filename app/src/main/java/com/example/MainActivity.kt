package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AdminDashboardScreen
import com.example.ui.AppointmentDetailScreen
import com.example.ui.LoginScreen
import com.example.ui.PartnerDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainNavigation()
      }
    }
  }
}

@Composable
fun MainNavigation() {
  val navController = rememberNavController()
  val viewModel: MainViewModel = viewModel()

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = "login",
      modifier = Modifier.padding(innerPadding)
    ) {
      // 1. LOGIN SCREEN
      composable("login") {
        LoginScreen(
          viewModel = viewModel,
          onLoginSuccess = {
            val role = viewModel.currentRole.value
            if (role == "ADMIN") {
              navController.navigate("admin_dashboard") {
                popUpTo("login") { inclusive = true }
              }
            } else if (role == "PARTNER") {
              navController.navigate("partner_dashboard") {
                popUpTo("login") { inclusive = true }
              }
            }
          }
        )
      }

      // 2. ADMIN DASHBOARD SCREEN
      composable("admin_dashboard") {
        AdminDashboardScreen(
          viewModel = viewModel,
          onLogout = {
            navController.navigate("login") {
              popUpTo("admin_dashboard") { inclusive = true }
            }
          },
          onNavigateToAppointmentDetail = { appointmentId ->
            navController.navigate("appointment_detail/$appointmentId")
          }
        )
      }

      // 3. CSC PARTNER DASHBOARD SCREEN
      composable("partner_dashboard") {
        PartnerDashboardScreen(
          viewModel = viewModel,
          onLogout = {
            navController.navigate("login") {
              popUpTo("partner_dashboard") { inclusive = true }
            }
          },
          onNavigateToAppointmentDetail = { appointmentId ->
            navController.navigate("appointment_detail/$appointmentId")
          }
        )
      }

      // 4. APPOINTMENT DETAIL / SLIP SCREEN
      composable(
        route = "appointment_detail/{appointmentId}",
        arguments = listOf(
          navArgument("appointmentId") { type = NavType.IntType }
        )
      ) { backStackEntry ->
        val appointmentId = backStackEntry.arguments?.getInt("appointmentId") ?: 0
        AppointmentDetailScreen(
          appointmentId = appointmentId,
          viewModel = viewModel,
          onBack = {
            navController.popBackStack()
          }
        )
      }
    }
  }
}
