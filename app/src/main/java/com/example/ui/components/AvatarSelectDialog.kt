package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class AvatarOption(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AvatarSelectDialog(
    currentAvatar: String,
    onDismiss: () -> Unit,
    onSelectAvatar: (String) -> Unit
) {
    val options = remember {
        listOf(
            AvatarOption("default_male", "Rajesh (Male)", Icons.Default.Person, AadhaarNavy),
            AvatarOption("default_female", "Priya (Female)", Icons.Default.Face3, AadhaarSaffron),
            AvatarOption("student_boy", "Rahul (Student)", Icons.Default.School, AadhaarGreen),
            AvatarOption("student_girl", "Ananya (Student)", Icons.Default.AutoStories, Color(0xFF9C27B0)),
            AvatarOption("senior_male", "Sharma Ji (Senior)", Icons.Default.Elderly, Color(0xFFE65100)),
            AvatarOption("senior_female", "Devi Ji (Senior)", Icons.Default.ElderlyWoman, Color(0xFF00838F)),
            AvatarOption("business_pro", "Executive Pro", Icons.Default.Work, Color(0xFF2E7D32)),
            AvatarOption("verified_citizen", "Verified ID", Icons.Default.VerifiedUser, Color(0xFF1565C0))
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AadhaarNavy, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Profile Avatar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AadhaarNavy)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Choose a simulated photo avatar for your Aadhaar card demo preview.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    items(options) { opt ->
                        val isSelected = currentAvatar == opt.id
                        val borderColor = if (isSelected) AadhaarNavy else Color.Transparent
                        val bgColor = if (isSelected) AadhaarNavy.copy(alpha = 0.1f) else opt.color.copy(alpha = 0.08f)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(if (isSelected) 2.dp else 1.dp, if (isSelected) AadhaarNavy else Color.LightGray, RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectAvatar(opt.id)
                                    onDismiss()
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(opt.color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = opt.icon, contentDescription = null, tint = opt.color, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = opt.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AadhaarNavy else Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

