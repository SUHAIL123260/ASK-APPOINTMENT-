package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AadhaarSetting
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AadhaarSettingsScreen(
    currentSetting: AadhaarSetting,
    onNavigateBack: () -> Unit,
    onSaveSettings: (AadhaarSetting) -> Unit,
    onResetDemoData: () -> Unit
) {
    var portalName by remember { mutableStateOf(currentSetting.portalName) }
    var defaultLanguage by remember { mutableStateOf(currentSetting.defaultLanguage) }
    var autoLockBiometrics by remember { mutableStateOf(currentSetting.autoLockBiometrics) }
    var maskAadhaarByDefault by remember { mutableStateOf(currentSetting.maskAadhaarByDefault) }
    var enableSmsAlerts by remember { mutableStateOf(currentSetting.enableSmsAlerts) }
    var allowOfflineVerification by remember { mutableStateOf(currentSetting.allowOfflineVerification) }

    var showResetConfirm by remember { mutableStateOf(false) }
    var showSavedToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aadhaar Portal Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Security, Privacy & Preferences", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val updated = currentSetting.copy(
                                portalName = portalName,
                                defaultLanguage = defaultLanguage,
                                autoLockBiometrics = autoLockBiometrics,
                                maskAadhaarByDefault = maskAadhaarByDefault,
                                enableSmsAlerts = enableSmsAlerts,
                                allowOfflineVerification = allowOfflineVerification
                            )
                            onSaveSettings(updated)
                            showSavedToast = true
                        }
                    ) {
                        Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            if (showSavedToast) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AadhaarGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AadhaarGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Preferences saved successfully!", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                }
            }

            // Portal Branding & Display
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Portal Display & Language", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                    OutlinedTextField(
                        value = portalName,
                        onValueChange = { portalName = it },
                        label = { Text("Portal Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Default Display Language", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val langs = listOf("English", "Hindi (हिंदी)", "Bilingual")
                        langs.forEach { lang ->
                            val selected = defaultLanguage == lang
                            FilterChip(
                                selected = selected,
                                onClick = { defaultLanguage = lang },
                                label = { Text(lang, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AadhaarNavy, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
            }

            // Security & Biometric Defaults
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Security & Privacy Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)

                    SwitchRow(
                        title = "Mask Aadhaar Number by Default",
                        subtitle = "Display only last 4 digits (XXXX XXXX 1234) on e-Aadhaar and dashboard cards.",
                        checked = maskAadhaarByDefault,
                        onCheckedChange = { maskAadhaarByDefault = it }
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    SwitchRow(
                        title = "Auto-Lock Biometrics after 30 mins",
                        subtitle = "Automatically lock fingerprint & iris after inactivity to protect against unauthorized authentication.",
                        checked = autoLockBiometrics,
                        onCheckedChange = { autoLockBiometrics = it }
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    SwitchRow(
                        title = "SMS & Email Verification Alerts",
                        subtitle = "Receive instant simulated alerts whenever an agency performs authentication using your UID.",
                        checked = enableSmsAlerts,
                        onCheckedChange = { enableSmsAlerts = it }
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    SwitchRow(
                        title = "Allow Offline Paperless KYC (XML)",
                        subtitle = "Enable password-protected Offline Aadhaar XML sharing without UIDAI server queries.",
                        checked = allowOfflineVerification,
                        onCheckedChange = { allowOfflineVerification = it }
                    )
                }
            }

            // Reset Demo Data Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Simulator Data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    Text(
                        "Restore default sample profile (Rajesh Sharma), clear update history, and reset biometric lock states to initial demo settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Default Demo Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Demo Simulator?") },
            text = { Text("Are you sure you want to clear all simulated demographics updates and restore the default sample profile?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetDemoData()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AadhaarNavy
            )
        )
    }
}
