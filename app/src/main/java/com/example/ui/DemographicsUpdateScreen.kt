package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AadhaarProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemographicsUpdateScreen(
    currentProfile: AadhaarProfile,
    onNavigateBack: () -> Unit,
    onSubmitUpdate: (field: String, oldValue: String, newValue: String, proofDoc: String) -> Unit,
    onOpenAvatarSelect: () -> Unit
) {
    var selectedField by remember { mutableStateOf("Address") }
    
    // Form fields initialized with current profile
    var nameEnglish by remember { mutableStateOf(currentProfile.nameEnglish) }
    var nameHindi by remember { mutableStateOf(currentProfile.nameHindi) }
    var dob by remember { mutableStateOf(currentProfile.dob) }
    var gender by remember { mutableStateOf(currentProfile.gender) }
    var addressEnglish by remember { mutableStateOf(currentProfile.addressEnglish) }
    var addressHindi by remember { mutableStateOf(currentProfile.addressHindi) }
    var pinCode by remember { mutableStateOf(currentProfile.pinCode) }
    var mobile by remember { mutableStateOf(currentProfile.mobileNumber) }

    var selectedProofDoc by remember { mutableStateOf("Voter ID Card") }
    var showProofDropdown by remember { mutableStateOf(false) }
    val proofOptions = listOf("Voter ID Card", "Passport", "Driving License", "Ration Card", "Electricity Bill", "Bank Passbook / Statement")

    var showSuccessDialog by remember { mutableStateOf(false) }
    var generatedUrn by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Online Demographics Update", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("SSUP Portal Demo Simulator", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info banner
            Surface(
                color = AadhaarSaffron.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AadhaarSaffron)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Simulate Aadhaar Profile Changes",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AadhaarNavy
                        )
                        Text(
                            "Select the demographic field you wish to update. A simulated URN (Update Request Number) will be generated for status tracking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Avatar & UID Summary Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AadhaarNavy.copy(alpha = 0.1f))
                                .clickable { onOpenAvatarSelect() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Change Photo", tint = AadhaarNavy, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("UID: ${currentProfile.aadhaarNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                            Text("Tap photo icon to simulate avatar update", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    OutlinedButton(onClick = onOpenAvatarSelect, shape = RoundedCornerShape(8.dp)) {
                        Text("Avatar", fontSize = 11.sp)
                    }
                }
            }

            // Field Selection Tabs
            Text("Select Field to Update:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val fields = listOf("Address", "Name", "DOB & Gender", "Contact")
                fields.forEach { f ->
                    val isSelected = selectedField == f
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedField = f },
                        label = { Text(f, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AadhaarNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Dynamic Form area based on selection
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedField) {
                        "Name" -> {
                            Text("Update Name Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = nameEnglish,
                                onValueChange = { nameEnglish = it },
                                label = { Text("Full Name (English)") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = nameHindi,
                                onValueChange = { nameHindi = it },
                                label = { Text("पूरा नाम (हिंदी / Regional)") },
                                leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        "DOB & Gender" -> {
                            Text("Update Date of Birth & Gender", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("Date of Birth (DD/MM/YYYY)") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = { Text("Gender (Male / Female / Other)") },
                                leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        "Address" -> {
                            Text("Update Residential Address", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = addressEnglish,
                                onValueChange = { addressEnglish = it },
                                label = { Text("Complete Address (English)") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = addressHindi,
                                onValueChange = { addressHindi = it },
                                label = { Text("पूरा पता (हिंदी / Regional)") },
                                leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null) },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { if (it.length <= 6) pinCode = it },
                                label = { Text("PIN Code (6 digits)") },
                                leadingIcon = { Icon(Icons.Default.PinDrop, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        "Contact" -> {
                            Text("Update Mobile Number & Email", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { if (it.length <= 10) mobile = it },
                                label = { Text("Linked Mobile Number (10 digits)") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                prefix = { Text("+91 ") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = currentProfile.email,
                                onValueChange = { },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Document Upload Simulator
                    Text("Select Supporting Document Proof:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                    
                    Box {
                        OutlinedTextField(
                            value = selectedProofDoc,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Valid POA / POI Document") },
                            leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null, tint = AadhaarGreen) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProofDropdown = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = showProofDropdown,
                            onDismissRequest = { showProofDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            proofOptions.forEach { doc ->
                                DropdownMenuItem(
                                    text = { Text(doc) },
                                    onClick = { selectedProofDoc = doc; showProofDropdown = false }
                                )
                            }
                        }
                    }

                    Text(
                        "💡 Note: In this demo simulator, document uploading is simulated instantly without requiring file picker access.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit Button
            Button(
                onClick = {
                    val (field, oldVal, newVal) = when (selectedField) {
                        "Name" -> Triple("Name", currentProfile.nameEnglish, nameEnglish)
                        "DOB & Gender" -> Triple("DOB/Gender", "${currentProfile.dob} (${currentProfile.gender})", "$dob ($gender)")
                        "Contact" -> Triple("Mobile Number", currentProfile.mobileNumber, mobile)
                        else -> Triple("Residential Address", currentProfile.addressEnglish, addressEnglish)
                    }
                    val urn = "URN-" + (100000000..999999999).random().toString()
                    generatedUrn = urn
                    onSubmitUpdate(field, oldVal, newVal, selectedProofDoc)
                    showSuccessDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AadhaarNavy),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulate Demographics Update & Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onNavigateBack() },
            icon = { Icon(Icons.Default.Verified, contentDescription = null, tint = AadhaarGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("Update Request Submitted!", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your demographic update request has been recorded in the UIDAI Demo Simulator.")
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Update Request Number (URN):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(generatedUrn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                        }
                    }
                    Text("You can track the status of this URN in the Update History screen.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false; onNavigateBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = AadhaarNavy)
                ) {
                    Text("Done & Return to Dashboard")
                }
            }
        )
    }
}
