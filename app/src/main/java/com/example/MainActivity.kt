package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.theme.AadhaarTheme
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AadhaarTheme {
                MainAadhaarApp()
            }
        }
    }
}

@Composable
fun MainAadhaarApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    // State flows from ViewModel
    val currentProfile by viewModel.currentProfile.collectAsState()
    val portalSetting by viewModel.portalSetting.collectAsState()
    val updateRequests by viewModel.updateRequests.collectAsState()
    val pvcOrders by viewModel.pvcOrders.collectAsState()
    val isBiometricLocked by viewModel.isBiometricLocked.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val showCardModal by viewModel.showCardModal.collectAsState()
    val selectedAvatar by viewModel.selectedAvatar.collectAsState()
    val isCardBackSide by viewModel.isCardBackSide.collectAsState()
    val isNumberMasked by viewModel.isNumberMasked.collectAsState()

    // Local Dialog States
    var showOtpSimulator by remember { mutableStateOf(false) }
    var showAvatarSelect by remember { mutableStateOf(false) }

    // Auto clear action message after 4 seconds
    LaunchedEffect(actionMessage) {
        if (actionMessage != null) {
            delay(4000)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            AnimatedVisibility(
                visible = actionMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                actionMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1B5E20),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = msg,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. AADHAAR DASHBOARD
                composable("dashboard") {
                    AadhaarDashboardScreen(
                        profile = currentProfile,
                        setting = portalSetting,
                        recentUpdates = updateRequests.take(5),
                        isBiometricLocked = isBiometricLocked,
                        isSyncing = isSyncing,
                        onNavigateToUpdateDemographics = {
                            navController.navigate("demographics")
                        },
                        onNavigateToHistory = {
                            navController.navigate("history")
                        },
                        onNavigateToBiometricLock = {
                            navController.navigate("biometrics")
                        },
                        onOpenOtpSimulator = {
                            showOtpSimulator = true
                        },
                        onNavigateToPvcOrder = {
                            navController.navigate("pvc_order")
                        },
                        onNavigateToSettings = {
                            navController.navigate("settings")
                        },
                        onTriggerCloudSync = {
                            viewModel.syncWithUidai()
                        },
                        onPreviewCard = {
                            viewModel.openCardModal()
                        },
                        onDownloadPdf = {
                            viewModel.downloadEAadhaar()
                        },
                        onShareProfile = {
                            viewModel.shareProfileDetails()
                        }
                    )
                }

                // 2. DEMOGRAPHICS UPDATE (SSUP) SCREEN
                composable("demographics") {
                    DemographicsUpdateScreen(
                        currentProfile = currentProfile,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onSubmitUpdate = { field, oldVal, newVal, proofDoc ->
                            viewModel.submitDemographicsUpdate(field, oldVal, newVal, proofDoc)
                            navController.popBackStack()
                        },
                        onOpenAvatarSelect = {
                            showAvatarSelect = true
                        }
                    )
                }

                // 3. URN TRACKING & UPDATE HISTORY SCREEN
                composable("history") {
                    UpdateHistoryScreen(
                        updateRequests = updateRequests,
                        pvcOrders = pvcOrders,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onDeleteRequest = { req ->
                            viewModel.deleteUpdateRequest(req)
                        }
                    )
                }

                // 4. BIOMETRIC LOCK & VID SCREEN
                composable("biometrics") {
                    BiometricLockScreen(
                        currentProfile = currentProfile,
                        isBiometricLocked = isBiometricLocked,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onToggleLock = { newState ->
                            viewModel.toggleBiometricLock(newState)
                        },
                        onGenerateNewVid = {
                            viewModel.generateNewVirtualId()
                        }
                    )
                }

                // 5. PVC CARD ORDER SCREEN
                composable("pvc_order") {
                    PvcCardOrderScreen(
                        currentProfile = currentProfile,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onOrderPlaced = { srn ->
                            viewModel.orderPvcCard(srn)
                        }
                    )
                }

                // 6. PORTAL SETTINGS SCREEN
                composable("settings") {
                    AadhaarSettingsScreen(
                        currentSetting = portalSetting,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onSaveSettings = { setting ->
                            viewModel.updatePortalSetting(setting)
                        },
                        onResetDemoData = {
                            viewModel.resetDemoData()
                        }
                    )
                }
            }
        }

        // --- Modals & Dialogs ---
        if (showOtpSimulator) {
            OtpVerificationDialog(
                mobileNumber = currentProfile.mobileNumber,
                onDismiss = { showOtpSimulator = false },
                onVerified = {
                    showOtpSimulator = false
                    viewModel.syncWithUidai()
                }
            )
        }

        if (showAvatarSelect) {
            AvatarSelectDialog(
                currentAvatar = selectedAvatar,
                onDismiss = { showAvatarSelect = false },
                onSelectAvatar = { avatarId ->
                    viewModel.updateProfileAvatar(avatarId)
                }
            )
        }

        if (showCardModal) {
            AadhaarCardPreviewModal(
                profile = currentProfile,
                isMasked = isNumberMasked,
                isBackSide = isCardBackSide,
                onToggleSide = { viewModel.toggleCardSide() },
                onToggleMask = { viewModel.toggleNumberMask() },
                onDownloadPdf = { viewModel.downloadEAadhaar() },
                onPrintPdf = { viewModel.printEaadhaar() },
                onDismiss = { viewModel.closeCardModal() }
            )
        }
    }
}
