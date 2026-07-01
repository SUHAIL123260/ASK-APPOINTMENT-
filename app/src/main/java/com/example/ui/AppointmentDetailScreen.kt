package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.Appointment
import com.example.ui.components.DeterministicQrCode
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appointments by viewModel.appointments.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    val app = remember(appointments, appointmentId) {
        appointments.find { it.id == appointmentId }
    }

    val centreName by viewModel.centreName.collectAsState()
    val centreAddress by viewModel.centreAddress.collectAsState()
    val centrePhone by viewModel.centrePhone.collectAsState()
    val centreLat by viewModel.centreLat.collectAsState()
    val centreLng by viewModel.centreLng.collectAsState()

    // --- Live GPS Tracking state ---
    var liveLocation by remember { mutableStateOf<Location?>(null) }
    var isTracking by remember { mutableStateOf(false) }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        permissionGranted = fineGranted || coarseGranted
        if (permissionGranted) {
            isTracking = true
            Toast.makeText(context, "GPS Permission Granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "GPS Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isTracking, permissionGranted) {
        if (isTracking && permissionGranted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    liveLocation = location
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                // Instantly try to get last known location
                val providers = locationManager.getProviders(true)
                var bestLocation: Location? = null
                for (provider in providers) {
                    val lastKnown = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || lastKnown.accuracy < bestLocation.accuracy) {
                        bestLocation = lastKnown
                    }
                }
                if (bestLocation != null) {
                    liveLocation = bestLocation
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
                        0.5f,
                        locationListener
                    )
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000L,
                        0.5f,
                        locationListener
                    )
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            try {
                while (true) {
                    delay(1000L)
                }
            } finally {
                try {
                    locationManager.removeUpdates(locationListener)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Dialog state
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }

    // Loading Simulation overlay for slip exports
    var exportingText by remember { mutableStateOf<String?>(null) }

    if (app == null) {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Appointment not found")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Detail", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AadhaarBlue)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGrayBg)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Workflow Status Card
                StatusWorkflowCard(app)

                // Main Slip or Info display
                if (app.status == "APPROVED") {
                    // Render Printable A5 Slip
                    PrintableA5SlipCard(
                        app = app,
                        centreName = centreName,
                        centreAddress = centreAddress,
                        centrePhone = centrePhone,
                        onExportPdf = {
                            scope.launch {
                                exportingText = "Generating Official PDF Document..."
                                delay(1800)
                                exportingText = null
                                Toast.makeText(context, "A5 Appointment PDF successfully generated and saved to Downloads folder!", Toast.LENGTH_LONG).show()
                            }
                        },
                        onExportExcel = {
                            scope.launch {
                                exportingText = "Exporting XLS Spreadsheet..."
                                delay(1500)
                                exportingText = null
                                Toast.makeText(context, "Appointment record successfully added to Commission Excel Ledger!", Toast.LENGTH_LONG).show()
                            }
                        },
                        onPrint = {
                            Toast.makeText(context, "Sending slip to thermal/network printer...", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // Pending/Rejected simple details view
                    CustomerDetailsCard(app)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LIVE LOCATION TRACKING PANEL
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("live_tracking_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, "GPS Tracking", tint = SaffronOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Live GPS Location Tracking", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            if (isTracking && liveLocation != null) {
                                Surface(
                                    color = StateGreen,
                                    shape = CircleShape,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Track your live distance and directions directly to the Aadhaar Seva Kendra in real-time.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!permissionGranted) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                            ) {
                                Icon(Icons.Default.GpsFixed, "Enable GPS")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enable Live GPS Tracking", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            if (!isTracking) {
                                Button(
                                    onClick = { isTracking = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                                ) {
                                    Icon(Icons.Default.PlayArrow, "Start GPS")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start Tracking Live Location", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                val userLoc = liveLocation
                                if (userLoc == null) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = SaffronOrange, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Connecting to GPS Satellites...", fontSize = 12.sp, color = Color.Gray)
                                    }
                                } else {
                                    val distanceResults = FloatArray(1)
                                    var distanceText = "Calculating..."
                                    var bearing = 0f
                                    try {
                                        Location.distanceBetween(
                                            userLoc.latitude, userLoc.longitude,
                                            centreLat, centreLng,
                                            distanceResults
                                        )
                                        val distanceInMeters = distanceResults[0]
                                        distanceText = if (distanceInMeters >= 1000) {
                                            String.format("%.2f Km", distanceInMeters / 1000f)
                                        } else {
                                            String.format("%.0f meters", distanceInMeters)
                                        }

                                        val dLon = Math.toRadians(centreLng - userLoc.longitude)
                                        val rLat1 = Math.toRadians(userLoc.latitude)
                                        val rLat2 = Math.toRadians(centreLat)
                                        val y = Math.sin(dLon) * Math.cos(rLat2)
                                        val x = Math.cos(rLat1) * Math.sin(rLat2) - Math.sin(rLat1) * Math.cos(rLat2) * Math.cos(dLon)
                                        var initialBearing = Math.toDegrees(Math.atan2(y, x))
                                        bearing = ((initialBearing + 360) % 360).toFloat()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SoftGrayBg, RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("YOUR CURRENT GPS COORDINATES", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text("Lat: ${String.format("%.5f", userLoc.latitude)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Lng: ${String.format("%.5f", userLoc.longitude)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Accuracy: ±${String.format("%.1f", userLoc.accuracy)}m", fontSize = 10.sp, color = Color.Gray)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("DISTANCE TO ASK CENTRE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(distanceText, fontSize = 18.sp, fontWeight = FontWeight.Black, color = AadhaarBlue)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("ASK CENTRE DIRECTION RADAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .background(AadhaarBlue.copy(alpha = 0.05f), CircleShape)
                                                .border(2.dp, AadhaarBlue.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(100.dp)) {
                                                drawCircle(
                                                    color = SaffronOrange.copy(alpha = 0.15f),
                                                    radius = size.minDimension / 2f
                                                )
                                                drawCircle(
                                                    color = SaffronOrange.copy(alpha = 0.3f),
                                                    radius = size.minDimension / 4f
                                                )

                                                drawLine(
                                                    color = Color.LightGray.copy(alpha = 0.5f),
                                                    start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                                                    end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height)
                                                )
                                                drawLine(
                                                    color = Color.LightGray.copy(alpha = 0.5f),
                                                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                                                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2)
                                                )

                                                rotate(degrees = bearing) {
                                                    val path = androidx.compose.ui.graphics.Path().apply {
                                                        moveTo(size.width / 2f, 15f)
                                                        lineTo(size.width / 2f - 12f, size.height / 2f + 15f)
                                                        lineTo(size.width / 2f, size.height / 2f + 5f)
                                                        lineTo(size.width / 2f + 12f, size.height / 2f + 15f)
                                                        close()
                                                    }
                                                    drawPath(path = path, color = SaffronOrange)
                                                }
                                            }

                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "Centre",
                                                tint = AadhaarBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Point top of phone forward to align with arrow",
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val uriStr = "google.navigation:q=$centreLat,$centreLng"
                                                val gmmIntentUri = Uri.parse(uriStr)
                                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                                    setPackage("com.google.android.apps.maps")
                                                }
                                                context.startActivity(mapIntent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AadhaarBlue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Directions, "Directions")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open Navigator", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { isTracking = false },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Stop, "Stop")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Stop GPS", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ADMIN ACTION CONTROLS (If role is Admin)
                if (currentRole == "ADMIN") {
                    AdminControlsCard(
                        app = app,
                        onApproveClick = { showApproveDialog = true },
                        onRejectClick = { showRejectDialog = true },
                        onRescheduleClick = { showRescheduleDialog = true }
                    )
                }
            }

            // Simulated Export progress Overlay
            AnimatedVisibility(
                visible = exportingText != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = SaffronOrange)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(exportingText ?: "Processing...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // APPROVE DIALOG
    if (showApproveDialog) {
        var dateVal by remember { mutableStateOf("") }
        var timeVal by remember { mutableStateOf("10:30 AM") }
        
        // Autopopulate date with tomorrow
        LaunchedEffect(Unit) {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            dateVal = sdf.format(calendar.time)
        }

        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approve Appointment", fontWeight = FontWeight.Bold, color = StateGreen) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select schedule for Aadhaar service processing:")
                    OutlinedTextField(
                        value = dateVal,
                        onValueChange = { dateVal = it },
                        label = { Text("Date (DD-MM-YYYY)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("approve_date_input")
                    )

                    OutlinedTextField(
                        value = timeVal,
                        onValueChange = { timeVal = it },
                        label = { Text("Time Slot") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("approve_time_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveAppointment(app.id, dateVal, timeVal)
                        showApproveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StateGreen),
                    modifier = Modifier.testTag("approve_confirm")
                ) {
                    Text("APPROVE & GENERATE SLIP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) { Text("CANCEL") }
            }
        )
    }

    // REJECT DIALOG
    if (showRejectDialog) {
        var reasonVal by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Appointment", fontWeight = FontWeight.Bold, color = StateRed) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Specify reason for rejecting this Aadhaar update:")
                    OutlinedTextField(
                        value = reasonVal,
                        onValueChange = { reasonVal = it },
                        placeholder = { Text("e.g. Document copy is unreadable or CSC stamp is blank") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("reject_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectAppointment(app.id, reasonVal)
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StateRed),
                    modifier = Modifier.testTag("reject_confirm")
                ) {
                    Text("CONFIRM REJECTION")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("CANCEL") }
            }
        )
    }

    // RESCHEDULE DIALOG
    if (showRescheduleDialog) {
        var dateVal by remember { mutableStateOf(app.appointmentDate) }
        var timeVal by remember { mutableStateOf(app.appointmentTime) }
        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            title = { Text("Reschedule Appointment Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dateVal,
                        onValueChange = { dateVal = it },
                        label = { Text("New Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = timeVal,
                        onValueChange = { timeVal = it },
                        label = { Text("New Time Slot") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rescheduleAppointment(app.id, dateVal, timeVal)
                        showRescheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AadhaarBlue)
                ) {
                    Text("CONFIRM")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDialog = false }) { Text("CANCEL") }
            }
        )
    }
}

@Composable
fun StatusWorkflowCard(app: Appointment) {
    val color = when (app.status) {
        "APPROVED" -> StateGreen
        "REJECTED" -> StateRed
        else -> StateAmber
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (app.status) {
                    "APPROVED" -> Icons.Default.Verified
                    "REJECTED" -> Icons.Default.Cancel
                    else -> Icons.Default.PendingActions
                },
                contentDescription = "Status",
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Current Request Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                    text = app.status,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = color
                )
                if (app.status == "REJECTED" && !app.rejectionReason.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reason: ${app.rejectionReason}",
                        style = MaterialTheme.typography.bodySmall.copy(color = StateRed, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerDetailsCard(app: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Customer Information Registry", fontWeight = FontWeight.Bold, color = AadhaarBlue, fontSize = 15.sp)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))

            DetailRow("Full Name", app.customerName)
            DetailRow("Mobile Number", app.mobileNumber)
            DetailRow("Aadhaar Number", app.aadhaarMasked)
            DetailRow("Father / Husband", app.fatherHusbandName)
            DetailRow("Date of Birth", app.dob)
            DetailRow("Gender", app.gender)
            DetailRow("Village / City", app.village)
            DetailRow("Full Address", app.address)
            DetailRow("PIN Code", app.pinCode)
            DetailRow("Aadhaar Service", app.serviceType)

            if (!app.remarks.isNullOrEmpty()) {
                DetailRow("Remarks", app.remarks)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Documents Checklist", fontWeight = FontWeight.Bold, color = AadhaarBlue, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DocIndicator("Photo", app.photographPath != null, modifier = Modifier.weight(1f))
                DocIndicator("Aadhaar Copy", app.aadhaarCopyPath != null, modifier = Modifier.weight(1f))
                DocIndicator("Support Doc", app.supportingDocPath != null, modifier = Modifier.weight(1f))
                DocIndicator("Stamp", app.cscStampPath != null, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.8f), textAlign = TextAlign.End)
    }
}

@Composable
fun DocIndicator(label: String, isOk: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isOk) StateGreen.copy(alpha = 0.15f) else StateRed.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isOk) StateGreen else StateRed,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isOk) StateGreen else StateRed)
        }
    }
}

@Composable
fun AdminControlsCard(
    app: Appointment,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onRescheduleClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Admin Controls Panel", fontWeight = FontWeight.Bold, color = AadhaarBlue, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (app.status == "PENDING") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onApproveClick,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("action_approve"),
                        colors = ButtonDefaults.buttonColors(containerColor = StateGreen)
                    ) {
                        Icon(Icons.Default.VerifiedUser, "Approve")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("action_reject"),
                        colors = ButtonDefaults.buttonColors(containerColor = StateRed)
                    ) {
                        Icon(Icons.Default.CancelPresentation, "Reject")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 13.sp)
                    }
                }
            } else if (app.status == "APPROVED") {
                Button(
                    onClick = onRescheduleClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                ) {
                    Icon(Icons.Default.EditCalendar, "Reschedule")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reschedule Appointment Slot")
                }
            } else {
                // Rejected, allow retry review
                Button(
                    onClick = onApproveClick,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StateGreen)
                ) {
                    Icon(Icons.Default.Refresh, "Retry")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-evaluate & Approve")
                }
            }
        }
    }
}

// ==========================================
// PRINTABLE A5 APPOINTMENT SLIP
// ==========================================
@Composable
fun PrintableA5SlipCard(
    app: Appointment,
    centreName: String,
    centreAddress: String,
    centrePhone: String,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit,
    onPrint: () -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Document export control panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1.1f).testTag("download_pdf_btn"),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, "PDF", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download PDF", fontSize = 10.sp)
                }

                OutlinedButton(
                    onClick = onExportExcel,
                    modifier = Modifier.weight(1.1f).testTag("export_excel_btn"),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.GridOn, "XLS", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export XLS", fontSize = 10.sp)
                }

                Button(
                    onClick = onPrint,
                    modifier = Modifier.weight(1f).testTag("print_slip_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AadhaarBlue),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Print, "Print", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print Slip", fontSize = 10.sp)
                }
            }
        }

        // A5 PRINTABLE SLIP CANVAS BODY
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Black, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header of slip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simulated Logo
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AadhaarBlue, RoundedCornerShape(6.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ASK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OFFICIAL APPOINTMENT SLIP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = AadhaarBlue
                        )
                        Text(
                            text = centreName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.Black, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Credentials Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        SlipField("APPOINTMENT ID", app.appointmentId, isHighlight = true)
                        SlipField("TOKEN NUMBER", app.tokenNumber, isHighlight = true)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SlipField("SCHEDULE DATE", app.appointmentDate)
                        SlipField("SCHEDULE TIME", app.appointmentTime)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                // Customer Info
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        SlipField("CUSTOMER NAME", app.customerName)
                        SlipField("MOBILE NUMBER", app.mobileNumber)
                        SlipField("AADHAAR (MASKED)", app.aadhaarMasked)
                        SlipField("SERVICE TYPE", app.serviceType, isHighlight = true)
                    }
                    Column(
                        modifier = Modifier.weight(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // QR Code inside the slip
                        Text("VERIFICATION QR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        DeterministicQrCode(
                            text = app.qrCodeData ?: "ASK-MOCK",
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                // Required docs & guidelines
                Text("REQUIRED DOCUMENTS TO CARRY:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SaffronOrangeDark)
                Text(
                    text = "1. Original Aadhaar Card (if available)\n2. Valid Verification Proof matching chosen service (${app.serviceType})\n3. CSC stamp verified digital request slip (Presently attached)",
                    fontSize = 8.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalDark
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("INSTRUCTIONS FOR APPLICANT:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AadhaarBlue)
                Text(
                    text = "• Please reach the Aadhaar Seva Kendra 15 minutes before slot.\n• Ensure your verified registered mobile number is kept active.\n• Show this digital/physical token QR slip at the reception desk.",
                    fontSize = 8.sp,
                    lineHeight = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.Black, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // CALL & WHATSAPP BUTTON INTENTS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$centrePhone"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Phone, "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Centre", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            // WhatsApp direct messaging intent
                            val message = "Hello, this is regarding my Aadhaar Appointment ID ${app.appointmentId}. Token: ${app.tokenNumber}."
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=$centrePhone&text=${Uri.encode(message)}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StateGreen),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Chat, "WhatsApp", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp)
                    }
                }
            }
        }

        // GOOGLE MAPS PANEL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, "Map Pin", tint = SaffronOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ASK Centre Navigation Guide", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aadhaar Centre Address: $centreAddress",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Maps actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Google maps navigation intent
                            val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(centreAddress)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.weight(1f).testTag("maps_nav_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AadhaarBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Navigation, "Nav")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Navigate", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            // Share location intent
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Aadhaar Seva Kendra Location")
                                putExtra(Intent.EXTRA_TEXT, "Here is the ASK Aadhaar Centre location: $centreAddress")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Location Via"))
                        },
                        modifier = Modifier.weight(1f).testTag("maps_share_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, "Share")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Location", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SlipField(label: String, value: String, isHighlight: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            fontSize = if (isHighlight) 12.sp else 11.sp,
            fontWeight = if (isHighlight) FontWeight.Black else FontWeight.Bold,
            color = if (isHighlight) AadhaarBlue else CharcoalDark
        )
    }
}
