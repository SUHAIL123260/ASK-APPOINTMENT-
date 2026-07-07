package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PvcOrder
import com.example.data.UpdateRequest
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateHistoryScreen(
    updateRequests: List<UpdateRequest>,
    pvcOrders: List<PvcOrder>,
    onNavigateBack: () -> Unit,
    onDeleteRequest: (UpdateRequest) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, DEMO, PVC, SUCCESS, IN_PROGRESS
    var selectedRequestForDetails by remember { mutableStateOf<UpdateRequest?>(null) }
    var selectedPvcForDetails by remember { mutableStateOf<PvcOrder?>(null) }

    // Combined or filtered items
    val filteredUpdates = remember(updateRequests, searchQuery, selectedFilter) {
        updateRequests.filter { req ->
            val matchesSearch = req.urnNumber.contains(searchQuery, ignoreCase = true) ||
                    req.fieldUpdated.contains(searchQuery, ignoreCase = true) ||
                    req.updateType.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "DEMO" -> !req.updateType.contains("PVC", ignoreCase = true)
                "PVC" -> req.updateType.contains("PVC", ignoreCase = true)
                "SUCCESS" -> req.status == "SUCCESS"
                "IN_PROGRESS" -> req.status == "IN_PROGRESS"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("URN Status & Update History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Track Demographics & PVC Orders", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by URN, Field, or Status...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AadhaarNavy) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AadhaarNavy
                )
            )

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("ALL" to "All", "DEMO" to "Demographics", "PVC" to "PVC Card", "IN_PROGRESS" to "In Progress", "SUCCESS" to "Completed")
                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AadhaarNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Summary statistics bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Requests", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${updateRequests.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Completed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${updateRequests.count { it.status == "SUCCESS" }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AadhaarGreen)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("In Progress", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${updateRequests.count { it.status == "IN_PROGRESS" }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                }
            }

            // List of Update Requests
            if (filteredUpdates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ManageSearch, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching URN requests found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Try clearing your search filters or create a new update simulation.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredUpdates) { req ->
                        UrnHistoryCard(
                            request = req,
                            onClick = { selectedRequestForDetails = req },
                            onDelete = { onDeleteRequest(req) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }

    // URN Details Modal
    selectedRequestForDetails?.let { req ->
        AlertDialog(
            onDismissRequest = { selectedRequestForDetails = null },
            icon = {
                Icon(
                    imageVector = if (req.status == "SUCCESS") Icons.Default.Verified else Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = if (req.status == "SUCCESS") AadhaarGreen else Color(0xFFE65100),
                    modifier = Modifier.size(44.dp)
                )
            },
            title = { Text("URN Tracking Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("URN: ${req.urnNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                            Text("Status: ${req.status}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (req.status == "SUCCESS") AadhaarGreen else Color(0xFFE65100))
                        }
                    }
                    Text("Type: ${req.updateType}", fontWeight = FontWeight.SemiBold)
                    Text("Field: ${req.fieldUpdated}", color = Color.DarkGray)
                    Text("Old Value: ${req.oldValue}", style = MaterialTheme.typography.bodySmall)
                    Text("New Value: ${req.newValue}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                    Text("Supporting Proof: ${req.proofDocument}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("Date: ${req.requestDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("UIDAI Verification Timeline (Simulation)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    TimelineStep(step = "1. Request Submitted via SSUP", isDone = true)
                    TimelineStep(step = "2. Proof Document Verification", isDone = true)
                    TimelineStep(step = "3. UIDAI Registry Sync", isDone = req.status == "SUCCESS")
                }
            },
            confirmButton = {
                Button(onClick = { selectedRequestForDetails = null }, colors = ButtonDefaults.buttonColors(containerColor = AadhaarNavy)) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun UrnHistoryCard(
    request: UpdateRequest,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    shape = CircleShape,
                    color = if (request.updateType.contains("PVC", true)) MaterialTheme.colorScheme.secondaryContainer else AadhaarNavy.copy(alpha = 0.1f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (request.updateType.contains("PVC", true)) "💳" else "📝", fontSize = 20.sp)
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
                        text = "${request.updateType}: ${request.fieldUpdated}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color.DarkGray),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Updated to: ${request.newValue}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun TimelineStep(step: String, isDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isDone) AadhaarGreen else Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(step, style = MaterialTheme.typography.labelSmall, color = if (isDone) Color.Black else Color.Gray)
    }
}
