package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AadhaarProfile
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricLockScreen(
    currentProfile: AadhaarProfile,
    isBiometricLocked: Boolean,
    onNavigateBack: () -> Unit,
    onToggleLock: (Boolean) -> Unit,
    onGenerateNewVid: () -> Unit
) {
    var isSimulatingScan by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) } // "SUCCESS" or "REJECTED"
    var showOtpDialog by remember { mutableStateOf(false) }
    var targetLockState by remember { mutableStateOf(!isBiometricLocked) }

    LaunchedEffect(isSimulatingScan) {
        if (isSimulatingScan) {
            scanResult = null
            delay(1800)
            scanResult = if (isBiometricLocked) "REJECTED" else "SUCCESS"
            isSimulatingScan = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Biometric Security & VID", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Lock/Unlock Fingerprint & Iris", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AadhaarNavy,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Main Lock Status Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBiometricLocked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = if (isBiometricLocked) MaterialTheme.colorScheme.error else AadhaarGreen,
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(if (isBiometricLocked) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else AadhaarGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBiometricLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock Status",
                            tint = if (isBiometricLocked) MaterialTheme.colorScheme.error else AadhaarGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isBiometricLocked) "Biometrics Currently LOCKED" else "Biometrics Currently UNLOCKED",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isBiometricLocked) Color(0xFFC62828) else Color(0xFF1B5E20),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBiometricLocked)
                            "Your fingerprint and iris authentication services are disabled by UIDAI security. This prevents any unauthorized biometric authentication."
                        else
                            "Your fingerprint and iris authentication services are active and can be used for Aadhaar verification across banks and govt services.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            targetLockState = !isBiometricLocked
                            showOtpDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBiometricLocked) AadhaarGreen else MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Icon(
                            imageVector = if (isBiometricLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBiometricLocked) "Unlock Biometrics (with OTP)" else "Lock Biometrics (Secure Mode)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // === Biometric Scanner Simulator ===
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Live Biometric Authentication Simulator", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                    Text("Test how UIDAI responds when an agency scans your fingerprint.", style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scanner Fingerprint Box
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .border(2.dp, if (isSimulatingScan) AadhaarSaffron else AadhaarNavy, CircleShape)
                            .clickable(enabled = !isSimulatingScan) { isSimulatingScan = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSimulatingScan) {
                            CircularProgressIndicator(color = AadhaarSaffron, strokeWidth = 3.dp, modifier = Modifier.size(64.dp))
                        } else {
                            Icon(Icons.Default.Fingerprint, contentDescription = "Scan", tint = AadhaarNavy, modifier = Modifier.size(56.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isSimulatingScan) "Scanning Fingerprint against UIDAI..." else "Tap scanner icon above to simulate verification",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSimulatingScan) AadhaarSaffron else Color.DarkGray
                    )

                    scanResult?.let { res ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = if (res == "SUCCESS") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (res == "SUCCESS") AadhaarGreen else MaterialTheme.colorScheme.error)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res == "SUCCESS") Icons.Default.VerifiedUser else Icons.Default.GppBad,
                                    contentDescription = null,
                                    tint = if (res == "SUCCESS") AadhaarGreen else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (res == "SUCCESS") "Authentication SUCCESS!" else "Authentication REJECTED!",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (res == "SUCCESS") Color(0xFF1B5E20) else Color(0xFFC62828)
                                    )
                                    Text(
                                        text = if (res == "SUCCESS") "Biometrics are unlocked. Identity verified." else "Error: Biometrics locked by resident. Unlock to verify.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // === Virtual ID (VID) Management Card ===
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Virtual ID (VID) Generator", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "VID is a temporary, revocable 16-digit random number mapped with your Aadhaar number. Use VID instead of Aadhaar to enhance privacy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Current Active VID (16-Digit):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(currentProfile.virtualId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = AadhaarNavy)
                            }
                            OutlinedButton(
                                onClick = onGenerateNewVid,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AadhaarNavy)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Regenerate", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showOtpDialog) {
        com.example.ui.components.OtpVerificationDialog(
            mobileNumber = currentProfile.mobileNumber,
            onDismiss = { showOtpDialog = false },
            onVerified = {
                showOtpDialog = false
                onToggleLock(targetLockState)
            }
        )
    }
}
