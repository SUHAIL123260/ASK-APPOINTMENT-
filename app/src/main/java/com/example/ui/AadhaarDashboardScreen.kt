package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AadhaarProfile
import com.example.data.AadhaarSetting
import com.example.data.UpdateRequest
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AadhaarDashboardScreen(
    profile: AadhaarProfile,
    setting: AadhaarSetting,
    recentUpdates: List<UpdateRequest>,
    isBiometricLocked: Boolean,
    isSyncing: Boolean,
    onNavigateToUpdateDemographics: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBiometricLock: () -> Unit,
    onOpenOtpSimulator: () -> Unit,
    onNavigateToPvcOrder: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTriggerCloudSync: () -> Unit,
    onPreviewCard: () -> Unit,
    onDownloadPdf: () -> Unit,
    onShareProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                color = AadhaarNavy,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_store_logo),
                                    contentDescription = "Aadhaar Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "मेरा आधार • Aadhaar Demo",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = setting.portalName.uppercase(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Cloud Sync & Online Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSyncing) AadhaarSaffron else AadhaarGreen,
                            modifier = Modifier.clickable { onTriggerCloudSync() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                                    contentDescription = "UIDAI Sync",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSyncing) "Syncing..." else "UIDAI Online",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // === Mini Aadhaar Preview Banner (Clickable to open 3D Modal) ===
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPreviewCard() }
                        .border(1.dp, AadhaarSaffron, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        // Tricolor top bar
                        Row(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(AadhaarSaffron))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(AadhaarGreen))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar box
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AadhaarNavy.copy(alpha = 0.1f))
                                    .border(1.dp, AadhaarNavy, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(42.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.nameEnglish,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.VerifiedUser, contentDescription = "Verified", tint = AadhaarGreen, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = profile.nameHindi,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "UID: ${profile.getMaskedAadhaar()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFFD32F2F)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR",
                                tint = AadhaarNavy,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Bottom action bar inside card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isBiometricLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (isBiometricLocked) MaterialTheme.colorScheme.error else AadhaarGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBiometricLocked) "Biometrics: LOCKED" else "Biometrics: ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBiometricLocked) MaterialTheme.colorScheme.error else AadhaarGreen
                                )
                            }
                            Text(
                                text = "Tap for e-Aadhaar 3D View →",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AadhaarNavy
                            )
                        }
                    }
                }
            }

            // Quick Actions Grid Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UIDAI SERVICES & DEMO MENU",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AadhaarNavy,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    TextButton(onClick = onOpenOtpSimulator) {
                        Icon(Icons.Default.LockClock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test OTP Simulator", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Cards Grid - Row 1
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = "Update Demographics",
                        subtitle = "Name, DOB, Address",
                        emojiOrIcon = "📝",
                        bgColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onNavigateToUpdateDemographics
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = "Order PVC Card",
                        subtitle = "Plastic Card ₹50",
                        emojiOrIcon = "💳",
                        bgColor = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onNavigateToPvcOrder
                    )
                }
            }

            // Cards Grid - Row 2
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = "Biometric Lock",
                        subtitle = if (isBiometricLocked) "Status: LOCKED" else "Status: UNLOCKED",
                        emojiOrIcon = if (isBiometricLocked) "🔒" else "🔓",
                        bgColor = if (isBiometricLocked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        textColor = if (isBiometricLocked) Color(0xFFC62828) else Color(0xFF1B5E20),
                        onClick = onNavigateToBiometricLock
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = "Update History",
                        subtitle = "Check URN Status",
                        emojiOrIcon = "📜",
                        bgColor = Color(0xFFFFF3E0),
                        textColor = Color(0xFFE65100),
                        onClick = onNavigateToHistory
                    )
                }
            }

            // Cards Grid - Row 3
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = "Download e-Aadhaar",
                        subtitle = "Password Protected PDF",
                        emojiOrIcon = "📥",
                        bgColor = Color(0xFFF3E5F5),
                        textColor = Color(0xFF4A148C),
                        onClick = onDownloadPdf
                    )
                    DashboardCard(
                        modifier = Modifier.weight(1f),
                        title = "Portal Settings",
                        subtitle = "Security & Language",
                        emojiOrIcon = "⚙️",
                        bgColor = Color(0xFFECEFF1),
                        textColor = Color(0xFF263238),
                        onClick = onNavigateToSettings
                    )
                }
            }

            // Recent Updates Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕒 RECENT UPDATE REQUESTS (URN)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AadhaarNavy,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("View All →", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Recent Updates List
            if (recentUpdates.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No Update Requests Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "When you simulate updating demographics or ordering a PVC card, URN status tracking will appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToUpdateDemographics,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AadhaarNavy)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulate Demographics Update")
                            }
                        }
                    }
                }
            } else {
                items(recentUpdates) { req ->
                    RecentUpdateCard(
                        request = req,
                        onClick = onNavigateToHistory
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    emojiOrIcon: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(106.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = emojiOrIcon,
                fontSize = 26.sp
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor.copy(alpha = 0.8f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentUpdateCard(
    request: UpdateRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AadhaarNavy.copy(alpha = 0.1f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (request.updateType.contains("PVC", true)) "💳" else "📝", fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = request.urnNumber,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = when (request.status) {
                                "SUCCESS" -> Color(0xFFE8F5E9)
                                "IN_PROGRESS" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFFEBEE)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = request.status,
                                color = when (request.status) {
                                    "SUCCESS" -> Color(0xFF2E7D32)
                                    "IN_PROGRESS" -> Color(0xFFE65100)
                                    else -> Color(0xFFC62828)
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${request.updateType} (${request.fieldUpdated})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color.DarkGray),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Requested: ${request.requestDate}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
