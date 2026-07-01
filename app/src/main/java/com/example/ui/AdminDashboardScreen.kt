package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.data.CscPartner
import com.example.viewmodel.ReportRow
import com.example.viewmodel.PartnerCommissionStats
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onNavigateToAppointmentDetail: (Int) -> Unit
) {
    val centreName by viewModel.centreName.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val partners by viewModel.partners.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Appointments, 1 = Partners, 2 = Commissions, 3 = Settings/Reports

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ASK Admin Panel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = centreName,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AadhaarBlue)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    icon = { Icon(Icons.Default.CalendarMonth, "Appointments") },
                    label = { Text("Appointments", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_appointments")
                )
                NavigationBarItem(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    icon = { Icon(Icons.Default.Storefront, "Partners") },
                    label = { Text("CSC Partners", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_partners")
                )
                NavigationBarItem(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    icon = { Icon(Icons.Default.Payments, "Commissions") },
                    label = { Text("Commissions", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_commissions")
                )
                NavigationBarItem(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    icon = { Icon(Icons.Default.Analytics, "Reports") },
                    label = { Text("Reports/Settings", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGrayBg)
                .padding(innerPadding)
        ) {
            when (activeSubTab) {
                0 -> AdminAppointmentsTab(viewModel, appointments, onNavigateToAppointmentDetail)
                1 -> AdminPartnersTab(viewModel, partners, appointments)
                2 -> AdminCommissionsTab(viewModel, partners, appointments)
                3 -> AdminSettingsAndReportsTab(viewModel)
            }
        }
    }
}

// ==========================================
// APPOINTMENTS TAB
// ==========================================
@Composable
fun AdminAppointmentsTab(
    viewModel: MainViewModel,
    appointments: List<Appointment>,
    onNavigateToAppointmentDetail: (Int) -> Unit
) {
    val searchQuery by viewModel.appointmentSearchQuery.collectAsState()
    val selectedFilter by viewModel.selectedStatusFilter.collectAsState()

    val filteredAppointments = remember(appointments, searchQuery, selectedFilter) {
        appointments.filter { app ->
            val matchesSearch = app.customerName.contains(searchQuery, ignoreCase = true) ||
                    app.mobileNumber.contains(searchQuery) ||
                    app.aadhaarMasked.contains(searchQuery) ||
                    app.partnerId.contains(searchQuery, ignoreCase = true) ||
                    app.appointmentId.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "PENDING" -> app.status == "PENDING"
                "APPROVED" -> app.status == "APPROVED"
                "REJECTED" -> app.status == "REJECTED"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalCount = appointments.size
            val pendingCount = appointments.count { it.status == "PENDING" }
            val approvedCount = appointments.count { it.status == "APPROVED" }

            StatCard(title = "Total", count = totalCount.toString(), color = AadhaarBlue, modifier = Modifier.weight(1f))
            StatCard(title = "Pending", count = pendingCount.toString(), color = StateAmber, modifier = Modifier.weight(1f))
            StatCard(title = "Approved", count = approvedCount.toString(), color = StateGreen, modifier = Modifier.weight(1f))
        }

        // Search Bar and Filters
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search Name, Mobile, Aadhaar, CSC ID...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_search_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val filters = listOf("ALL", "PENDING", "APPROVED", "REJECTED")
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { viewModel.updateStatusFilter(filter) },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_$filter"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (filter == "APPROVED") StateGreen else if (filter == "REJECTED") StateRed else if (filter == "PENDING") StateAmber else AadhaarBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Appointment List
        if (filteredAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, "No appointments", modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No appointments match your search/filter.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("appointments_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredAppointments, key = { it.id }) { app ->
                    AppointmentCard(app = app, onClick = { onNavigateToAppointmentDetail(app.id) })
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, fontSize = 20.sp, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AppointmentCard(app: Appointment, onClick: () -> Unit) {
    val statusColor = when (app.status) {
        "APPROVED" -> StateGreen
        "REJECTED" -> StateRed
        else -> StateAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("appointment_card_${app.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = app.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = app.status,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CSC ID: ${app.partnerId}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AadhaarBlue
                    )
                    Text(
                        text = "Aadhaar: ${app.aadhaarMasked}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Service: ${app.serviceType}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                if (app.status == "APPROVED") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Token: ${app.tokenNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronOrange
                        )
                        Text(
                            text = "Date: ${app.appointmentDate}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Details",
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==========================================
// CSC PARTNERS TAB
// ==========================================
@Composable
fun AdminPartnersTab(
    viewModel: MainViewModel,
    partners: List<CscPartner>,
    appointments: List<Appointment>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf<String?>(null) } // partnerId to reset

    var pId by remember { mutableStateOf("") }
    var pName by remember { mutableStateOf("") }
    var pPassword by remember { mutableStateOf("") }
    var dialogError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CSC Partners",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue)
            )
            Button(
                onClick = {
                    pId = ""
                    pName = ""
                    pPassword = ""
                    dialogError = null
                    showAddDialog = true
                },
                modifier = Modifier.testTag("add_partner_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
            ) {
                Icon(Icons.Default.Add, "Add")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add CSC ID")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (partners.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No partners registered yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("partners_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(partners, key = { it.partnerId }) { partner ->
                    val partnerApps = appointments.filter { it.partnerId == partner.partnerId }
                    val stats = viewModel.getPartnerCommissionStats(partner.partnerId)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_card_${partner.partnerId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = partner.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "CSC ID: ${partner.partnerId}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AadhaarBlue
                                    )
                                }

                                Surface(
                                    color = if (partner.isActive) StateGreen.copy(alpha = 0.15f) else StateRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (partner.isActive) "ACTIVE" else "SUSPENDED",
                                        color = if (partner.isActive) StateGreen else StateRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Performance metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PerformanceIndicator(label = "Appointments", value = partnerApps.size.toString())
                                PerformanceIndicator(label = "Approved (Earned)", value = "${partnerApps.count { it.status == "APPROVED" }} (₹${stats.totalEarned.toInt()})")
                                PerformanceIndicator(label = "Balance Due", value = "₹${stats.balanceDue.toInt()}", valueColor = if (stats.balanceDue > 0) SaffronOrange else StateGreen)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.togglePartnerStatus(partner.partnerId) },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (partner.isActive) StateRed else StateGreen
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (partner.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                        contentDescription = "Toggle Status",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (partner.isActive) "Suspend" else "Activate", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { showPasswordResetDialog = partner.partnerId },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Password, "Reset", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Pwd", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { viewModel.deletePartner(partner.partnerId) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                                        .testTag("delete_${partner.partnerId}")
                                ) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE PARTNER DIALOG
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create CSC Partner", fontWeight = FontWeight.Bold, color = AadhaarBlue) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pId,
                        onValueChange = { pId = it },
                        label = { Text("CSC Partner ID") },
                        placeholder = { Text("e.g. CSC2045") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_partner_id_input")
                    )
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Partner Name") },
                        placeholder = { Text("e.g. Rahul Patil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_partner_name_input")
                    )
                    OutlinedTextField(
                        value = pPassword,
                        onValueChange = { pPassword = it },
                        label = { Text("Default Password") },
                        placeholder = { Text("e.g. partner123") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_partner_password_input")
                    )

                    dialogError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createCscPartner(pId, pName, pPassword, onSuccess = {
                            showAddDialog = false
                        }, onError = {
                            dialogError = it
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                    modifier = Modifier.testTag("add_partner_submit")
                ) {
                    Text("CREATE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("CANCEL") }
            }
        )
    }

    // RESET PASSWORD DIALOG
    if (showPasswordResetDialog != null) {
        var newPword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = null },
            title = { Text("Reset CSC Partner Password") },
            text = {
                OutlinedTextField(
                    value = newPword,
                    onValueChange = { newPword = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reset_password_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPartnerPassword(showPasswordResetDialog!!, newPword) {
                            showPasswordResetDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AadhaarBlue),
                    modifier = Modifier.testTag("reset_password_submit")
                ) {
                    Text("RESET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = null }) { Text("CANCEL") }
            }
        )
    }
}

@Composable
fun PerformanceIndicator(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.ExtraBold)
    }
}


// ==========================================
// COMMISSION TAB
// ==========================================
@Composable
fun AdminCommissionsTab(
    viewModel: MainViewModel,
    partners: List<CscPartner>,
    appointments: List<Appointment>
) {
    val payments by viewModel.payments.collectAsState()
    val rate by viewModel.commissionAmount.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPartnerIdForPayment by remember { mutableStateOf("") }
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentRefInput by remember { mutableStateOf("") }
    var paymentRemarksInput by remember { mutableStateOf("") }
    var payError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Commission Manager",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue)
        )
        Text(
            text = "Rate: ₹${rate.toInt()} per approved appointment",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Commission Summary Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AadhaarBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Network Commission Stats", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                val totalApproved = appointments.count { it.status == "APPROVED" }
                val totalEarned = totalApproved * rate
                val totalPaid = payments.sumOf { it.amount }
                val totalDue = totalEarned - totalPaid

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Earned", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("₹${totalEarned.toInt()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text("Paid", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("₹${totalPaid.toInt()}", color = StateGreen, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text("Outstanding", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("₹${totalDue.toInt()}", color = SaffronOrange, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Partner Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue)
            )
            Button(
                onClick = {
                    if (partners.isNotEmpty()) {
                        selectedPartnerIdForPayment = partners.first().partnerId
                        val pStats = viewModel.getPartnerCommissionStats(selectedPartnerIdForPayment)
                        paymentAmountInput = pStats.balanceDue.coerceAtLeast(0.0).toInt().toString()
                        paymentRefInput = ""
                        paymentRemarksInput = ""
                        payError = null
                        showPaymentDialog = true
                    }
                },
                modifier = Modifier.testTag("record_payment_button"),
                colors = ButtonDefaults.buttonColors(containerColor = StateGreen)
            ) {
                Icon(Icons.Default.AddCard, "Pay")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Record Payment")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Partner Commission List
        partners.forEach { partner ->
            val stats = viewModel.getPartnerCommissionStats(partner.partnerId)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(partner.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("CSC ID: ${partner.partnerId}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Due: ₹${stats.balanceDue.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = if (stats.balanceDue > 0) SaffronOrange else StateGreen)
                        Text("Paid: ₹${stats.totalPaid.toInt()}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Payment History Log",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (payments.isEmpty()) {
            Text("No payments registered yet.", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
        } else {
            payments.forEach { payment ->
                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(payment.paymentDate))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Paid ₹${payment.amount.toInt()} to ${payment.partnerId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = StateGreen
                            )
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        if (!payment.referenceId.isNullOrEmpty()) {
                            Text("Ref ID: ${payment.referenceId}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        if (!payment.remarks.isNullOrEmpty()) {
                            Text("Remarks: ${payment.remarks}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // PAYMENT RECORD DIALOG
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Record Commission Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Partner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    // Simple simulated spinner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.1f))
                            .clickable {
                                // Rotate partners simple simulation
                                val currentIndex = partners.indexOfFirst { it.partnerId == selectedPartnerIdForPayment }
                                val nextIndex = (currentIndex + 1) % partners.size
                                selectedPartnerIdForPayment = partners[nextIndex].partnerId
                                val pStats = viewModel.getPartnerCommissionStats(selectedPartnerIdForPayment)
                                paymentAmountInput = pStats.balanceDue.coerceAtLeast(0.0).toInt().toString()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val currentPartnerName = partners.find { it.partnerId == selectedPartnerIdForPayment }?.name ?: ""
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("$currentPartnerName ($selectedPartnerIdForPayment)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, "Dropdown")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                    )

                    OutlinedTextField(
                        value = paymentRefInput,
                        onValueChange = { paymentRefInput = it },
                        label = { Text("Reference ID / Txn Number") },
                        placeholder = { Text("e.g. TXN9872") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("payment_ref_input")
                    )

                    OutlinedTextField(
                        value = paymentRemarksInput,
                        onValueChange = { paymentRemarksInput = it },
                        label = { Text("Remarks") },
                        placeholder = { Text("e.g. Monthly settlement") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    payError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountInput.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            payError = "Enter a valid amount"
                        } else {
                            viewModel.payCommission(
                                partnerId = selectedPartnerIdForPayment,
                                amount = amount,
                                referenceId = paymentRefInput,
                                remarks = paymentRemarksInput
                            )
                            showPaymentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StateGreen),
                    modifier = Modifier.testTag("payment_submit")
                ) {
                    Text("PAY")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("CANCEL") }
            }
        )
    }
}


// ==========================================
// REPORTS & SETTINGS TAB
// ==========================================
@Composable
fun AdminSettingsAndReportsTab(viewModel: MainViewModel) {
    val currentRate by viewModel.commissionAmount.collectAsState()
    val cName by viewModel.centreName.collectAsState()
    val cAddress by viewModel.centreAddress.collectAsState()
    val cPhone by viewModel.centrePhone.collectAsState()
    val cEmail by viewModel.centreEmail.collectAsState()
    val cLat by viewModel.centreLat.collectAsState()
    val cLng by viewModel.centreLng.collectAsState()

    var editRate by remember { mutableStateOf("") }
    var editName by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editLat by remember { mutableStateOf("") }
    var editLng by remember { mutableStateOf("") }

    // Synchronize inputs with state once on open
    LaunchedEffect(currentRate, cName, cAddress, cPhone, cEmail, cLat, cLng) {
        editRate = currentRate.toInt().toString()
        editName = cName
        editAddress = cAddress
        editPhone = cPhone
        editEmail = cEmail
        editLat = cLat.toString()
        editLng = cLng.toString()
    }

    var reportType by remember { mutableStateOf(0) } // 0 = Daily, 1 = Monthly

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // REPORTING SECTION
        Text(
            text = "Activity Reports",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Selector for report types
        TabRow(
            selectedTabIndex = reportType,
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
                .padding(2.dp)
        ) {
            Tab(selected = reportType == 0, onClick = { reportType = 0 }, text = { Text("Daily Summary", fontWeight = FontWeight.Bold) })
            Tab(selected = reportType == 1, onClick = { reportType = 1 }, text = { Text("Monthly Summary", fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(12.dp))

        val reportData = if (reportType == 0) viewModel.getDailyReport() else viewModel.getMonthlyReport()

        if (reportData.isEmpty()) {
            Text("No reporting data available.", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
        } else {
            // Render beautiful reporting table/cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Period", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), color = Color.Gray)
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray)
                        Text("Approved", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center, color = Color.Gray)
                        Text("Comm.", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    reportData.forEach { row ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.timeframe, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f))
                            Text(row.total.toString(), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(row.approved.toString(), fontSize = 12.sp, color = StateGreen, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                            Text("₹${row.commission.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SETTINGS SECTION
        Text(
            text = "Application Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue)
        )
        Text("Modify the Aadhaar Centre information and commission structures.", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = editRate,
                    onValueChange = { editRate = it },
                    label = { Text("CSC Commission per Approved Slip (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setting_rate_input")
                )

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("ASK Centre Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("setting_name_input")
                )

                OutlinedTextField(
                    value = editAddress,
                    onValueChange = { editAddress = it },
                    label = { Text("Centre Physical Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Support Mobile / Hotline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Support Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editLat,
                        onValueChange = { editLat = it },
                        label = { Text("Centre Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = editLng,
                        onValueChange = { editLng = it },
                        label = { Text("Centre Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val rateVal = editRate.toDoubleOrNull() ?: currentRate
                        val latVal = editLat.toDoubleOrNull() ?: cLat
                        val lngVal = editLng.toDoubleOrNull() ?: cLng
                        viewModel.updateApplicationSettings(
                            commission = rateVal,
                            name = editName.trim(),
                            address = editAddress.trim(),
                            phone = editPhone.trim(),
                            email = editEmail.trim(),
                            lat = latVal,
                            lng = lngVal
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("settings_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                ) {
                    Icon(Icons.Default.Save, "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE CONFIGURATION", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
