package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onNavigateToAppointmentDetail: (Int) -> Unit
) {
    val currentPartnerId by viewModel.currentPartnerId.collectAsState()
    val currentPartnerName by viewModel.currentPartnerName.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    val partnerIdStr = currentPartnerId ?: ""
    val partnerNameStr = currentPartnerName ?: ""

    // Filter appointments for this partner
    val myAppointments = remember(appointments, partnerIdStr) {
        appointments.filter { it.partnerId == partnerIdStr }
    }

    var activeSubTab by remember { mutableStateOf(0) } // 0 = New Appointment, 1 = History, 2 = Commission

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CSC Partner Panel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = "$partnerNameStr ($partnerIdStr)",
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
                        modifier = Modifier.testTag("partner_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaffronOrange)
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
                    icon = { Icon(Icons.Default.PostAdd, "New Request") },
                    label = { Text("New Slip", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("partner_nav_new")
                )
                NavigationBarItem(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    icon = { Icon(Icons.Default.History, "History") },
                    label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("partner_nav_history")
                )
                NavigationBarItem(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, "Earnings") },
                    label = { Text("Earnings", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("partner_nav_earnings")
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
                0 -> PartnerNewAppointmentTab(viewModel, onNavigateToAppointmentDetail)
                1 -> PartnerHistoryTab(myAppointments, onNavigateToAppointmentDetail)
                2 -> PartnerEarningsTab(viewModel, partnerIdStr)
            }
        }
    }
}

// ==========================================
// NEW APPOINTMENT FORM TAB
// ==========================================
@Composable
fun PartnerNewAppointmentTab(
    viewModel: MainViewModel,
    onNavigateToAppointmentDetail: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // Form states
    var custName by remember { mutableStateOf("") }
    var custMobile by remember { mutableStateOf("") }
    var custAadhaar by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var village by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var selectedService by remember { mutableStateOf("Mobile Update") }
    var remarks by remember { mutableStateOf("") }

    // Upload status states (we simulate uploading files)
    var isPhotoUploaded by remember { mutableStateOf(false) }
    var isAadhaarCopyUploaded by remember { mutableStateOf(false) }
    var isSupportDocUploaded by remember { mutableStateOf(false) }
    var isCscStampUploaded by remember { mutableStateOf(false) } // Mandatory!

    var formError by remember { mutableStateOf<String?>(null) }
    var newlyCreatedId by remember { mutableStateOf<Int?>(null) }

    val serviceTypes = listOf(
        "Mobile Update", "Address Update", "Name Update", "DOB Update", "Gender Update",
        "Photograph Update", "Fingerprint Update", "Iris Update", "Biometric Update",
        "Demographic Update", "Child Aadhaar", "New Aadhaar Enrollment", "Other"
    )

    if (newlyCreatedId != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("form_success_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(StateGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, "Success", tint = StateGreen, modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aadhaar Request Submitted!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = AadhaarBlue),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The Aadhaar update details and documents have been successfully submitted to the ASK Admin for verification and scheduling. Once approved, the official Token, Appointment ID, and secure QR Slip will be generated automatically for printing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onNavigateToAppointmentDetail(newlyCreatedId!!)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("partner_view_slip_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AadhaarBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Info, "Details")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VIEW REGISTRATION STATUS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        custName = ""
                        custMobile = ""
                        custAadhaar = ""
                        fatherName = ""
                        dob = ""
                        gender = "Male"
                        village = ""
                        address = ""
                        pinCode = ""
                        selectedService = "Mobile Update"
                        remarks = ""
                        isPhotoUploaded = false
                        isAadhaarCopyUploaded = false
                        isSupportDocUploaded = false
                        isCscStampUploaded = false
                        newlyCreatedId = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("partner_new_registration_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, "New")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REGISTER NEXT CUSTOMER", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Enrollment & Update Form",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue),
                modifier = Modifier.padding(bottom = 12.dp)
            )

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
                    Text("Customer Information", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)

                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("Customer Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("cust_name_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = custMobile,
                            onValueChange = { custMobile = it },
                            label = { Text("Mobile Number *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("cust_mobile_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = custAadhaar,
                            onValueChange = { custAadhaar = it },
                            label = { Text("Aadhaar Number *") },
                            placeholder = { Text("12 Digits") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("cust_aadhaar_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("Father / Husband Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("cust_father_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth *") },
                            placeholder = { Text("DD-MM-YYYY") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f).testTag("cust_dob_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Simple gender select row
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    gender = when (gender) {
                                        "Male" -> "Female"
                                        "Female" -> "Other"
                                        else -> "Male"
                                    }
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text("Gender *", fontSize = 10.sp, color = Color.Gray)
                                Text(gender, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Contact & Location Details", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text("Village / Town *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("cust_village_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("PIN Code *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("cust_pincode_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Full Address *") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("cust_address_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Aadhaar Service Configuration", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)

                    // Simulated Dropdown spinner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                val nextIdx = (serviceTypes.indexOf(selectedService) + 1) % serviceTypes.size
                                selectedService = serviceTypes[nextIdx]
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Required Aadhaar Service *", fontSize = 10.sp, color = Color.Gray)
                                Text(selectedService, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, "Select")
                        }
                    }

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Verification Uploads", fontWeight = FontWeight.Bold, color = SaffronOrange, fontSize = 14.sp)

                    // Photograph Selector row
                    DocumentUploadRow(
                        label = "Customer Photograph *",
                        isUploaded = isPhotoUploaded,
                        onClick = { isPhotoUploaded = !isPhotoUploaded },
                        tag = "upload_photo"
                    )

                    DocumentUploadRow(
                        label = "Aadhaar Copy File *",
                        isUploaded = isAadhaarCopyUploaded,
                        onClick = { isAadhaarCopyUploaded = !isAadhaarCopyUploaded },
                        tag = "upload_aadhaar"
                    )

                    DocumentUploadRow(
                        label = "Mandatory CSC Stamp *",
                        isUploaded = isCscStampUploaded,
                        onClick = { isCscStampUploaded = !isCscStampUploaded },
                        isMandatoryHighlight = true,
                        tag = "upload_stamp"
                    )

                    formError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.createAppointment(
                                customerName = custName,
                                mobileNumber = custMobile,
                                aadhaarNumber = custAadhaar,
                                fatherHusbandName = fatherName,
                                dob = dob,
                                gender = gender,
                                village = village,
                                address = address,
                                pinCode = pinCode,
                                serviceType = selectedService,
                                photographPath = if (isPhotoUploaded) "photo_path" else null,
                                aadhaarCopyPath = if (isAadhaarCopyUploaded) "aadhaar_path" else null,
                                supportingDocPath = null,
                                cscStampPath = if (isCscStampUploaded) "csc_stamp_path" else null,
                                remarks = remarks,
                                onSuccess = { insertedId ->
                                    newlyCreatedId = insertedId
                                    formError = null
                                },
                                onError = {
                                    formError = it
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_appointment_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                    ) {
                        Icon(Icons.Default.UploadFile, "Upload")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT APPOINTMENT SLIP", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentUploadRow(
    label: String,
    isUploaded: Boolean,
    onClick: () -> Unit,
    isMandatoryHighlight: Boolean = false,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(
            containerColor = if (isMandatoryHighlight && !isUploaded) StateRed.copy(alpha = 0.05f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isUploaded) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                    contentDescription = "Upload Icon",
                    tint = if (isUploaded) StateGreen else if (isMandatoryHighlight) StateRed else AadhaarBlue
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isMandatoryHighlight && !isUploaded) StateRed else Color.Unspecified
                )
            }

            Surface(
                color = if (isUploaded) StateGreen else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isUploaded) "ATTACHED" else "TAP TO CHOOSE",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}


// ==========================================
// HISTORY / STATUS TAB
// ==========================================
@Composable
fun PartnerHistoryTab(
    myAppointments: List<Appointment>,
    onNavigateToAppointmentDetail: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Your Appointment Entries",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue),
            modifier = Modifier.padding(16.dp)
        )

        if (myAppointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Feed, "Empty History", modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No appointments filed yet.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("partner_history_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(myAppointments, key = { it.id }) { app ->
                    AppointmentCard(app = app, onClick = { onNavigateToAppointmentDetail(app.id) })
                }
            }
        }
    }
}


// ==========================================
// MY COMMISSIONS TAB
// ==========================================
@Composable
fun PartnerEarningsTab(
    viewModel: MainViewModel,
    partnerId: String
) {
    val payments by viewModel.payments.collectAsState()
    val stats = viewModel.getPartnerCommissionStats(partnerId)

    // Filter payments for this partner
    val myPayments = remember(payments, partnerId) {
        payments.filter { it.partnerId == partnerId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your Commission Summary",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Stats boxes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AadhaarBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Commission Earnings", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Earned", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("₹${stats.totalEarned.toInt()}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text("Total Settled", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("₹${stats.totalPaid.toInt()}", color = StateGreen, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text("Balance Due", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text("₹${stats.balanceDue.toInt()}", color = SaffronOrange, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Earning History Log",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AadhaarBlue)
        )
        Text("Only successfully approved appointments earn commissions.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        // Settlements History
        if (myPayments.isEmpty()) {
            Text("No payments settled yet by Admin.", color = Color.Gray, fontSize = 12.sp)
        } else {
            myPayments.forEach { pay ->
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(pay.paymentDate))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Received: ₹${pay.amount.toInt()}", fontWeight = FontWeight.Bold, color = StateGreen, fontSize = 13.sp)
                            Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                        }
                        if (!pay.referenceId.isNullOrEmpty()) {
                            Text("Ref ID: ${pay.referenceId}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (!pay.remarks.isNullOrEmpty()) {
                            Text("Remarks: ${pay.remarks}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
