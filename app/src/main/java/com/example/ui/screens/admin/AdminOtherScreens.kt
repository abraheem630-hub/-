package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 1. إدارة الشكاوى (Admin Complaints)
// ==========================================
@Composable
fun AdminComplaintsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val complaints by viewModel.allComplaints.collectAsState()
    var selectedComplaint by remember { mutableStateOf<ComplaintEntity?>(null) }

    if (selectedComplaint != null) {
        val cmp = selectedComplaint!!
        var status by remember { mutableStateOf(cmp.status) }
        var replyNote by remember { mutableStateOf("") }
        val statusOptions = listOf("تم استلام الشكوى", "قيد المراجعة والتحقيق", "تمت المعالجة والإغلاق", "مرفوضة لعدم الاختصاص")

        AlertDialog(
            onDismissRequest = { selectedComplaint = null },
            title = { Text("معالجة الشكوى رقم: ${cmp.complaintNumber}", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الشاكي: ${cmp.fullName} (${cmp.phone})", style = MaterialTheme.typography.bodySmall)
                    Text("الموضوع: ${cmp.complaintType}", style = MaterialTheme.typography.bodySmall)
                    Text("الوصف: ${cmp.description}", style = MaterialTheme.typography.bodySmall)

                    Divider(color = BorderLight)

                    Text("تحديد حالة الشكوى:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    statusOptions.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = status == opt, onClick = { status = opt })
                            Text(opt, fontSize = 13.sp)
                        }
                    }

                    OutlinedTextField(
                        value = replyNote,
                        onValueChange = { replyNote = it },
                        label = { Text("رد الإدارة أو الإجراء المتخذ") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdateComplaintStatus(cmp.id, status, replyNote.trim())
                        selectedComplaint = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("حفظ التحديث", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedComplaint = null }) { Text("إلغاء", color = TextMuted) }
            },
            containerColor = SurfaceWhite
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (complaints.isEmpty()) {
            item { EmptyStateView(message = "لا توجد شكاوى مسجلة حالياً.") }
        } else {
            items(complaints, key = { it.id }) { cmp ->
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
                            Text(cmp.complaintNumber, fontWeight = FontWeight.Bold, color = NavyDark)
                            StatusChip(status = cmp.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("الشاكي: ${cmp.fullName} - ${cmp.phone}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("النوع: ${cmp.complaintType}", color = NavyPrimary, fontSize = 12.sp)
                        Text(cmp.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)

                        if (cmp.adminNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(color = NavyContainer, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                Text("الإجراء: ${cmp.adminNotes}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { selectedComplaint = cmp },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("الرد وتحديث الحالة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. إدارة البلاغات الميدانية (Admin Reports)
// ==========================================
@Composable
fun AdminReportsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.allReports.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (reports.isEmpty()) {
            item { EmptyStateView(message = "لا توجد بلاغات ميدانية مسجلة.") }
        } else {
            items(reports, key = { it.id }) { rep ->
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
                            Text(rep.reportNumber, fontWeight = FontWeight.Bold, color = NavyDark)
                            StatusChip(status = rep.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(rep.problemType, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), fontSize = 14.sp)
                        Text("الموقع: ${rep.province} - ${rep.district} (${rep.address})", style = MaterialTheme.typography.bodySmall)
                        Text("الهاتف: ${rep.phone}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("الشرح: ${rep.description}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.adminUpdateReportStatus(rep.id, "قيد المعالجة الميدانية") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("إحالة للصيانة", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.adminUpdateReportStatus(rep.id, "تم الإصلاح والإنجاز") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تم الإصلاح", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. إدارة المواطنين (Admin Citizens)
// ==========================================
@Composable
fun AdminCitizensScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.allUsers.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.People, contentDescription = null, tint = GoldAccent)
                    Column {
                        Text("قاعدة بيانات المواطنين المسجلين", fontWeight = FontWeight.Bold, color = SurfaceWhite)
                        Text("إجمالي المسجلين: ${users.size} مواطناً", color = SurfaceWhite.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
        }

        items(users, key = { it.id }) { u ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(if (u.role == "admin") GoldLight else NavyContainer, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (u.role == "admin") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (u.role == "admin") GoldDark else NavyPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(u.fullName, fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 14.sp)
                            if (u.role == "admin") {
                                Surface(color = GoldLight, shape = RoundedCornerShape(4.dp)) {
                                    Text("مشرف", color = GoldDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text("الهاتف: ${u.phone}", style = MaterialTheme.typography.bodySmall, color = TextDark)
                        Text("البريد: ${u.email}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("المحافظة: ${u.province}", style = MaterialTheme.typography.labelSmall, color = GoldDark)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. إدارة فرص العمل (Admin Jobs)
// ==========================================
@Composable
fun AdminJobsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.allJobs.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var org by remember { mutableStateOf("") }
        var prov by remember { mutableStateOf("الأنبار") }
        var loc by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("دوام كامل") }
        var desc by remember { mutableStateOf("") }
        var quals by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة فرصة عمل / تدريب جديدة", fontWeight = FontWeight.Bold, color = NavyDark) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الوظيفة / البرنامج *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = org, onValueChange = { org = it }, label = { Text("الجهة المعلنة / الشركة *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = prov, onValueChange = { prov = it }, label = { Text("المحافظة") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("مكان العمل التفصيلي") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("نوع الدوام (دوام كامل / تدريب)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف والمهام *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = quals, onValueChange = { quals = it }, label = { Text("المؤهلات والشروط") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && org.isNotBlank()) {
                            viewModel.adminAddJob(
                                JobEntity(
                                    id = UUID.randomUUID().toString(),
                                    title = title.trim(),
                                    organization = org.trim(),
                                    province = prov.trim(),
                                    location = loc.trim(),
                                    jobType = type.trim(),
                                    description = desc.trim(),
                                    qualifications = quals.trim(),
                                    conditions = "",
                                    startDate = "2026-09-20",
                                    endDate = "2026-10-20"
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    Text("نشر الفرصة فوراً", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء", color = TextMuted) }
            },
            containerColor = SurfaceWhite
        )
    }

    Column(modifier = modifier.fillMaxSize().background(BackgroundLight)) {
        PaddingValues(14.dp).let {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة فرصة عمل أو برنامج تدريبي جديد", fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(jobs, key = { it.id }) { j ->
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
                            Text(j.title, fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 15.sp)
                            IconButton(onClick = { viewModel.adminDeleteJob(j.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                            }
                        }
                        Text("${j.organization} - ${j.province}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(j.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. إدارة الأخبار (Admin News)
// ==========================================
@Composable
fun AdminNewsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val news by viewModel.allNews.collectAsState()
    var showAddNewsDialog by remember { mutableStateOf(false) }

    if (showAddNewsDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNewsDialog = false },
            title = { Text("نشر بيان أو خبر رسمي جديد", fontWeight = FontWeight.Bold, color = NavyDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان البيان أو الخبر *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("موجز الخبر *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("التفاصيل الكاملة للبيان") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            viewModel.adminAddNews(
                                NewsEntity(
                                    id = UUID.randomUUID().toString(),
                                    title = title.trim(),
                                    date = today,
                                    description = desc.trim(),
                                    content = content.trim()
                                )
                            )
                            showAddNewsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    Text("نشر وتعميم الخبر", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNewsDialog = false }) { Text("إلغاء", color = TextMuted) }
            },
            containerColor = SurfaceWhite
        )
    }

    Column(modifier = modifier.fillMaxSize().background(BackgroundLight)) {
        Button(
            onClick = { showAddNewsDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("نشر بيان أو إعلان رسمي جديد", fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(news, key = { it.id }) { item ->
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
                            Text(item.title, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.adminDeleteNews(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                            }
                        }
                        Text(item.date, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. إرسال الإشعارات الجماعية (Admin Notifications)
// ==========================================
@Composable
fun AdminNotificationsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إرسال إشعار عام لكافة المواطنين", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 16.sp)
                Text("يصل هذا الإشعار مباشرة إلى جميع مستخدمي التطبيق في قائمة التنبيهات.", style = MaterialTheme.typography.bodySmall, color = TextMuted)

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الإشعار *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("نص الإشعار والتنبيه *") }, minLines = 3, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            viewModel.adminSendNotification(title.trim(), body.trim(), "all")
                            title = ""
                            body = ""
                        } else {
                            viewModel.showMessage("يرجى كتابة العنوان والنص")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بث الإشعار الآن", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 7. صندوق رسائل المواطنين (Admin Messages)
// ==========================================
@Composable
fun AdminMessagesScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.allMessages.collectAsState()
    var selectedMsg by remember { mutableStateOf<MessageEntity?>(null) }

    if (selectedMsg != null) {
        val msg = selectedMsg!!
        var replyText by remember { mutableStateOf(msg.reply) }

        AlertDialog(
            onDismissRequest = { selectedMsg = null },
            title = { Text("الرد على المواطن: ${msg.senderName}", fontWeight = FontWeight.Bold, color = NavyDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("رقم الهاتف: ${msg.senderPhone}", style = MaterialTheme.typography.bodySmall)
                    if (msg.requestNumber.isNotBlank()) {
                        Text("الطلب: ${msg.requestNumber}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("نص الرسالة: ${msg.content}", style = MaterialTheme.typography.bodyMedium, color = TextDark)
                    Divider(color = BorderLight)
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("نص رد فريق المتابعة") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            viewModel.adminReplyMessage(msg.id, replyText.trim())
                            selectedMsg = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("إرسال الرد للمواطن", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMsg = null }) { Text("إلغاء", color = TextMuted) }
            },
            containerColor = SurfaceWhite
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (messages.isEmpty()) {
            item { EmptyStateView(message = "صندوق الرسائل فارغ حالياً.") }
        } else {
            items(messages, key = { it.id }) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(msg.senderName, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text(msg.senderPhone, style = MaterialTheme.typography.bodySmall, color = GoldDark)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(msg.content, style = MaterialTheme.typography.bodySmall, color = TextDark)

                        if (msg.reply.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(color = NavyContainer, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                                Text("ردكم: ${msg.reply}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(6.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { selectedMsg = msg },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (msg.status == "تم الرد") "تعديل الرد" else "الرد على الرسالة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. سجل الرقابة والعمليات (Audit Logs)
// ==========================================
@Composable
fun AdminAuditLogsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.allAuditLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = GoldAccent)
                    Column {
                        Text("سجل الرقابة والعمليات الإدارية (Audit Logs)", fontWeight = FontWeight.Bold, color = SurfaceWhite)
                        Text("توثيق زمني غير قابل للتعديل لجميع أنشطة المشرفين.", color = SurfaceWhite.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
        }

        items(logs, key = { it.id }) { log ->
            val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(log.action, fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 13.sp)
                        Text(dateFormatted, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("المسؤول: ${log.actorEmail}", style = MaterialTheme.typography.labelSmall, color = GoldDark)
                    Text("الهدف: ${log.entityType} [${log.entityId}]", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(log.details, style = MaterialTheme.typography.bodySmall, color = TextDark)
                }
            }
        }
    }
}

// ==========================================
// 9. إعدادات المنصة (Admin Settings)
// ==========================================
@Composable
fun AdminSettingsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsState()

    var heroTitle by remember(settings) { mutableStateOf(settings.heroTitle) }
    var heroSubtitle by remember(settings) { mutableStateOf(settings.heroSubtitle) }
    var contactPhone by remember(settings) { mutableStateOf(settings.contactPhone) }
    var contactEmail by remember(settings) { mutableStateOf(settings.contactEmail) }
    var address by remember(settings) { mutableStateOf(settings.officeAddress) }
    var notice by remember(settings) { mutableStateOf(settings.bannerNotice) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إعدادات هوية المنصة وبيانات التواصل", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 16.sp)

                OutlinedTextField(value = heroTitle, onValueChange = { heroTitle = it }, label = { Text("الاسم في الهيدر الرئيسي") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = heroSubtitle, onValueChange = { heroSubtitle = it }, label = { Text("الشعار النصي (Slogan)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notice, onValueChange = { notice = it }, label = { Text("شريط الإعلان والتنبيه العام") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("رقم الخط الساخن") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = { Text("البريد الإلكتروني الرسمي") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("مقر وعنوان المكتب") }, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = {
                        val updated = settings.copy(
                            heroTitle = heroTitle.trim(),
                            heroSubtitle = heroSubtitle.trim(),
                            bannerNotice = notice.trim(),
                            contactPhone = contactPhone.trim(),
                            contactEmail = contactEmail.trim(),
                            officeAddress = address.trim()
                        )
                        viewModel.adminUpdateSettings(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ وتحديث الإعدادات", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 10. إدارة الخدمات الاجتماعية (Admin Social Services)
// ==========================================
@Composable
fun AdminSocialServicesScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val services by viewModel.allSocialServices.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("") }
        var conds by remember { mutableStateOf("") }
        var docs by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة خدمة / برنامج رعاية جديد", fontWeight = FontWeight.Bold, color = NavyDark) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم البرنامج / الخدمة *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف العام *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("الفئات المشمولة") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = conds, onValueChange = { conds = it }, label = { Text("الشروط والضوابط") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = docs, onValueChange = { docs = it }, label = { Text("المستمسكات المطلوبة") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.adminAddSocialService(
                                SocialServiceEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    description = desc.trim(),
                                    targetGroups = target.trim(),
                                    conditions = conds.trim(),
                                    requiredDocs = docs.trim()
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    Text("إضافة ونشر", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء", color = TextMuted) }
            },
            containerColor = SurfaceWhite
        )
    }

    Column(modifier = modifier.fillMaxSize().background(BackgroundLight)) {
        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("إضافة برنامج رعاية أو مساعدة جديد", fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(services) { s ->
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
                            Text(s.name, fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.adminDeleteSocialService(s.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                            }
                        }
                        Text(s.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
