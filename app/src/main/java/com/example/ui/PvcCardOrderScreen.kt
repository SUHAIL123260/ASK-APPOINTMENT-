package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AadhaarProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvcCardOrderScreen(
    currentProfile: AadhaarProfile,
    onNavigateBack: () -> Unit,
    onOrderPlaced: (srn: String) -> Unit
) {
    var isProcessingPayment by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedSrn by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Order Aadhaar PVC Card", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Official UIDAI Plastic Card • ₹50", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Hero PVC Card Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, AadhaarSaffron, RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE3F2FD), Color(0xFFFFF3E0), Color(0xFFE8F5E9))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("भारत सरकार • GOVERNMENT OF INDIA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                            Surface(color = AadhaarSaffron, shape = RoundedCornerShape(6.dp)) {
                                Text("PVC PRO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, AadhaarNavy, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(40.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(currentProfile.nameEnglish, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("DOB: ${currentProfile.dob} | ${currentProfile.gender}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(currentProfile.aadhaarNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = Color(0xFFD32F2F))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("✨ Hologram & Micro Text Security", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AadhaarNavy)
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            // Features List
            Text("Why Order Aadhaar PVC Card?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureRow(icon = Icons.Default.Security, title = "Secure QR Code & Hologram", desc = "Includes digital signature, ghost image, and guilloche pattern.")
                FeatureRow(icon = Icons.Default.WaterDrop, title = "Weather & Water Resistant", desc = "Durable plastic card that fits conveniently into your wallet.")
                FeatureRow(icon = Icons.Default.LocalShipping, title = "Speed Post Delivery", desc = "Delivered directly to your registered address via India Post.")
            }

            // Delivery Address Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delivery Address (From Aadhaar)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentProfile.addressEnglish, style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("PIN: ${currentProfile.pinCode} | Mobile: ${currentProfile.mobileNumber}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
            }

            // Price Breakdown Box
            Surface(
                color = AadhaarGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AadhaarGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Payable Amount:", style = MaterialTheme.typography.labelMedium, color = Color.DarkGray)
                        Text("₹ 50.00", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                    }
                    Text("Inclusive of Speed Post\ncharges & GST", fontSize = 11.sp, color = Color(0xFF1B5E20), textAlign = TextAlign.End, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Order Button
            Button(
                onClick = {
                    isProcessingPayment = true
                    val srn = "SRN-" + (10000000..99999999).random().toString()
                    generatedSrn = srn
                    onOrderPlaced(srn)
                    isProcessingPayment = false
                    showSuccessDialog = true
                },
                enabled = !isProcessingPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AadhaarNavy),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isProcessingPayment) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Processing ₹50 Payment...", fontSize = 16.sp)
                } else {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulate ₹50 Payment & Place Order", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onNavigateBack() },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AadhaarGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("PVC Card Ordered Successfully!", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your payment of ₹50 was simulated successfully. Your Aadhaar PVC card order has been dispatched for printing.")
                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Service Request Number (SRN):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(generatedSrn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                        }
                    }
                    Text("Expected Delivery: 5–7 business days via India Post Speed Post.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false; onNavigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = AadhaarNavy)) {
                    Text("Return to Dashboard")
                }
            }
        )
    }
}

@Composable
fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Surface(shape = CircleShape, color = AadhaarNavy.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
