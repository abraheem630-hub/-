package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CitizenProfileScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()
    val allComplaints by viewModel.allComplaints.collectAsState()
    val allReports by viewModel.allReports.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("طلباتي", "شكاواي", "بلاغاتي")

    var showEditProfileDialog by remember { mutableStateOf(false) }

    // User's filtered items
    val myRequests = remember(allRequests, user) {
        if (user == null) allRequests.take(2)
        else allRequests.filter { it.userId == user?.id || it.phone == user?.phone }
    }

    val myComplaints = remember(allComplaints, user) {
        if (user == null) allComplaints.take(2)
        else allComplaints.filter { it.userId == user?.id || it.phone == user?.phone }
    }

    val myReports = remember(allReports, user) {
        if (user == null) allReports.take(2)
        else allReports.filter { it.userId == user?.id || it.phone == user?.phone }
    }

    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(user?.fullName ?: "") }
        var phoneInput by remember { mutableStateOf(user?.phone ?: "") }
        var emailInput by remember { mutableStateOf(user?.email ?: "") }
        var provinceInput by remember { mutableStateOf(user?.province ?: "الأنبار") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("تعديل أو تسجيل بيانات المواطن", fontWeight = FontWeight.Bold, color = NavyDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("الاسم الكامل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("رقم الهاتف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                            viewModel.loginCitizen(nameInput.trim(), phoneInput.trim(), emailInput.trim(), provinceInput)
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("حفظ البيانات", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = SurfaceWhite
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, GoldAccent, CircleShape)
                                .background(NavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.fullName ?: "مواطن كريم (زائر)",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "الهاتف: ${user?.phone ?: "غير مسجل"}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "البريد الإلكتروني: ${if (user?.email.isNullOrBlank()) "غير مضاف" else user?.email}",
                                style = MaterialTheme.typography.bodySmall.copy(color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "المحافظة: ${user?.province ?: "العراق"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = NavyPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.Messages) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("صندوق الرسائل", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تبديل الحساب", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Direct Admin Portal Login shortcut
                    Surface(
                        onClick = { viewModel.navigateTo(Screen.AdminLogin) },
                        color = NavyLight.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("تسجيل دخول المالك والإدارة المركزية", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyDark)
                                    Text("abraheem630@gmail.com", fontSize = 11.sp, color = GoldDark)
                                }
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = NavyDark)
                        }
                    }
                }
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceWhite,
                contentColor = NavyPrimary,
                indicator = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = "$title (${if (index == 0) myRequests.size else if (index == 1) myComplaints.size else myReports.size})",
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NavyPrimary else TextMuted
                            )
                        }
                    )
                }
            }
        }

        // List Content based on Selected Tab
        when (selectedTab) {
            0 -> {
                if (myRequests.isEmpty()) {
                    item {
                        EmptyStateView(
                            message = "لا توجد طلبات مسجلة في ملفك حتى الآن.",
                            icon = Icons.Outlined.Assignment,
                            actionButtonText = "تقديم أول طلب الآن",
                            onActionClick = { viewModel.navigateTo(Screen.SubmitRequest) }
                        )
                    }
                } else {
                    items(myRequests, key = { it.id }) { req ->
                        val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(req.createdAt))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.navigateTo(Screen.TrackRequest(req.requestNumber)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = req.requestNumber,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyDark
                                        )
                                    )
                                    StatusChip(status = req.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = req.requestType,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = req.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "بتاريخ: $dateFormatted",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                    )
                                    Text(
                                        text = "تتبع المسار والردود ←",
                                        style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                if (myComplaints.isEmpty()) {
                    item {
                        EmptyStateView(
                            message = "لا توجد شكاوى مسجلة في ملفك.",
                            icon = Icons.Outlined.ReportProblem,
                            actionButtonText = "تسجيل شكوى جديدة",
                            onActionClick = { viewModel.navigateTo(Screen.SubmitComplaint) }
                        )
                    }
                } else {
                    items(myComplaints, key = { it.id }) { cmp ->
                        val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(cmp.createdAt))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cmp.complaintNumber,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyDark
                                        )
                                    )
                                    StatusChip(status = cmp.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = cmp.complaintType,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cmp.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                    maxLines = 2
                                )
                                if (cmp.adminNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = NavyContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "رد الإدارة: ${cmp.adminNotes}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NavyDark),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "تاريخ التسجيل: $dateFormatted",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                if (myReports.isEmpty()) {
                    item {
                        EmptyStateView(
                            message = "لا توجد بلاغات مسجلة.",
                            icon = Icons.Outlined.NotificationImportant,
                            actionButtonText = "الإبلاغ عن خلل خدمي",
                            onActionClick = { viewModel.navigateTo(Screen.ReportProblem) }
                        )
                    }
                } else {
                    items(myReports, key = { it.id }) { rep ->
                        val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(rep.createdAt))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = rep.reportNumber,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyDark
                                        )
                                    )
                                    StatusChip(status = rep.status)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = rep.problemType,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "الموقع: ${rep.province} - ${rep.district} (${rep.address})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "تاريخ الإبلاغ: $dateFormatted",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
