package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.JobEntity
import com.example.data.models.NewsEntity
import com.example.data.models.SocialServiceEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 1. فرَص العمل والتدريب المهني
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsAndTrainingScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val jobs by viewModel.allJobs.collectAsState()
    var selectedJobForApply by remember { mutableStateOf<JobEntity?>(null) }

    val user by viewModel.currentUser.collectAsState()

    // Application Dialog
    if (selectedJobForApply != null) {
        val job = selectedJobForApply!!
        var applicantName by remember { mutableStateOf(user?.fullName ?: "") }
        var applicantPhone by remember { mutableStateOf(user?.phone ?: "") }
        var applicantEmail by remember { mutableStateOf(user?.email ?: "") }
        var education by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedJobForApply = null },
            title = {
                Text(
                    text = "التقديم على: ${job.title}",
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "الجهة المعلنة: ${job.organization} - ${job.province}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("الاسم الرباعي *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = applicantPhone,
                        onValueChange = { applicantPhone = it },
                        label = { Text("رقم الهاتف *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = applicantEmail,
                        onValueChange = { applicantEmail = it },
                        label = { Text("البريد الإلكتروني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = education,
                        onValueChange = { education = it },
                        label = { Text("المؤهل العلمي / التخصص *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("الخبرات السابقة وسنوات العمل") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (applicantName.isNotBlank() && applicantPhone.isNotBlank() && education.isNotBlank()) {
                            isSubmitting = true
                            viewModel.submitJobApplication(
                                jobId = job.id,
                                jobTitle = job.title,
                                name = applicantName.trim(),
                                phone = applicantPhone.trim(),
                                email = applicantEmail.trim(),
                                education = education.trim(),
                                notes = notes.trim(),
                                onSuccess = {
                                    isSubmitting = false
                                    selectedJobForApply = null
                                }
                            )
                        } else {
                            viewModel.showMessage("يرجى إكمال الحقول الإلزامية")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    Text("إرسال طلب التقديم", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedJobForApply = null }) {
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GoldLight, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WorkOutline, contentDescription = null, tint = GoldDark)
                    }
                    Column {
                        Text(
                            text = "فرص العمل والتأهيل المهني",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        )
                        Text(
                            text = "مبادرة مكتب سامي الفلاحي لدعم توظيف وتدريب الشباب والكوادر.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        if (jobs.isEmpty()) {
            item {
                EmptyStateView(
                    message = "لا توجد فرص عمل معلنة حاليًا، يرجى المتابعة لاحقًا.",
                    icon = Icons.Outlined.WorkOutline
                )
            }
        } else {
            items(jobs, key = { it.id }) { job ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = job.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                            )
                            Surface(
                                color = GoldLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = job.jobType,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldDark,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${job.organization} - ${job.location} (${job.province})",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = job.description,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextDark, lineHeight = 20.sp)
                        )

                        if (job.qualifications.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = NavyContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "المؤهلات والشروط المطلوبة:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = job.qualifications,
                                        style = MaterialTheme.typography.bodySmall.copy(color = NavyDark)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "فترة التقديم: ${job.startDate} حتى ${job.endDate}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )

                            Button(
                                onClick = { selectedJobForApply = job },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("التقديم الآن", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. الخدمات والمساعدات الاجتماعية
// ==========================================
@Composable
fun SocialServicesScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val services by viewModel.allSocialServices.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GoldLight, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = GoldDark)
                    }
                    Column {
                        Text(
                            text = "برامج الرعاية والمساعدات الاجتماعية",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        )
                        Text(
                            text = "شروط ووثائق التقديم على شبكات الرعاية والإعانات الطبية والإنسانية.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        items(services, key = { it.id }) { service ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDark, lineHeight = 20.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = BorderLight)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Target groups & conditions
                    Text("الفئات المستهدفة:", style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Bold))
                    Text(service.targetGroups, style = MaterialTheme.typography.bodySmall.copy(color = TextDark))

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("الشروط والضوابط:", style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Bold))
                    Text(service.conditions, style = MaterialTheme.typography.bodySmall.copy(color = TextDark))

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("الوثائق والمستمسكات المطلوبة:", style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Bold))
                    Text(service.requiredDocs, style = MaterialTheme.typography.bodySmall.copy(color = TextDark))

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.SubmitRequest) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("تقديم طلب شمول بهذه الخدمة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. الأخبار والإعلانات الرسمية
// ==========================================
@Composable
fun NewsAndAnnouncementsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val newsList by viewModel.allNews.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GoldLight, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = GoldDark)
                    }
                    Column {
                        Text(
                            text = "المركز الإعلامي والبيانات الرسمية",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        )
                        Text(
                            text = "متابعة أخبار المبادرات والجولات الميدانية وتطورات المشاريع الخدمية.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        items(newsList, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = GoldLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "بيان رسمي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldDark,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = item.date,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.content.ifBlank { item.description },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextDark,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. التواصل المباشر مع فريق المتابعة
// ==========================================
@Composable
fun MessagesScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()

    var senderName by remember { mutableStateOf(user?.fullName ?: "") }
    var senderPhone by remember { mutableStateOf(user?.phone ?: "") }
    var requestNumber by remember { mutableStateOf("") }
    var messageContent by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val myMessages = remember(allMessages, user) {
        if (user == null) allMessages
        else allMessages.filter { it.userId == user?.id || it.senderPhone == user?.phone }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(GoldLight, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ContactSupport, contentDescription = null, tint = GoldDark)
                    }
                    Column {
                        Text(
                            text = "التواصل المباشر مع فريق المتابعة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        )
                        Text(
                            text = "أرسل استفسارك بخصوص أي معاملة وسيقوم الفريق المختص بالرد عليك.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        // New Message Input Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "إرسال استفسار أو رسالة جديدة",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    )

                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("الاسم *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = senderPhone,
                        onValueChange = { senderPhone = it },
                        label = { Text("رقم الهاتف *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = requestNumber,
                        onValueChange = { requestNumber = it },
                        label = { Text("رقم الطلب المرتبط (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = messageContent,
                        onValueChange = { messageContent = it },
                        label = { Text("نص الرسالة أو الاستفسار *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (senderName.isNotBlank() && senderPhone.isNotBlank() && messageContent.isNotBlank()) {
                                isSending = true
                                viewModel.sendMessage(
                                    name = senderName.trim(),
                                    phone = senderPhone.trim(),
                                    requestNumber = requestNumber.trim(),
                                    content = messageContent.trim(),
                                    onSuccess = {
                                        isSending = false
                                        messageContent = ""
                                    }
                                )
                            } else {
                                viewModel.showMessage("يرجى تعبئة الاسم ورقم الهاتف ومحتوى الرسالة")
                            }
                        },
                        enabled = !isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال الرسالة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Previous messages list
        item {
            Text(
                text = "الرسائل السابقة والردود (${myMessages.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (myMessages.isEmpty()) {
            item {
                EmptyStateView(
                    message = "لا توجد مراسلات سابقة.",
                    icon = Icons.Outlined.ChatBubbleOutline
                )
            }
        } else {
            items(myMessages, key = { it.id }) { msg ->
                val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(msg.createdAt))
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
                            Text(
                                text = "رسالة من: ${msg.senderName}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                            )
                            Surface(
                                color = if (msg.status == "تم الرد") Color(0xFFD1FAE5) else GoldLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = msg.status,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (msg.status == "تم الرد") Color(0xFF065F46) else GoldDark,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (msg.requestNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("بخصوص الطلب رقم: ${msg.requestNumber}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(msg.content, style = MaterialTheme.typography.bodyMedium.copy(color = TextDark))

                        // Reply section if exists
                        if (msg.reply.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = NavyContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "رد فريق المتابعة:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(msg.reply, style = MaterialTheme.typography.bodySmall.copy(color = NavyDark))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(dateFormatted, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. عن التطبيق والمنظومة
// ==========================================
@Composable
fun AboutAppScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldAccent, CircleShape)
                        .background(NavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = settings.appName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
                )
                Text(
                    text = "الإصدار الرسمي 2.0 - جمهورية العراق",
                    style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Candidate Official Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, GoldAccent, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.candidate),
                            contentDescription = "المرشح سامي الفلاحي",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(
                            text = "المرشح سامي الفلاحي",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "صوت المواطن وراعي المبادرة الخدمية",
                            style = MaterialTheme.typography.bodySmall.copy(color = GoldDark, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "الرؤية والهدف",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                )
                Text(
                    text = "تأسست هذه المنظومة الرقمية بمبادرة وإشراف الأستاذ سامي الفلاحي لمد جسور التواصل الفعلي والدائم مع أهلنا الكرام في كافة المحافظات، وتمكين المواطن من رفع معاملاته وشكاواه ومتابعة مسار حلها بكل شفافية وتوثيق دقيق لكل مرحلة.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextDark, lineHeight = 22.sp)
                )

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "بيانات الاتصال ومقر المتابعة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                )
                Text("العنوان: ${settings.officeAddress}", style = MaterialTheme.typography.bodyMedium.copy(color = TextDark))
                Text("الخط الساخن: ${settings.contactPhone}", style = MaterialTheme.typography.bodyMedium.copy(color = TextDark))
                Text("البريد الإلكتروني: ${settings.contactEmail}", style = MaterialTheme.typography.bodyMedium.copy(color = TextDark))

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "سياسة الخصوصية وسرية البيانات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                )
                Text(
                    text = "تلتزم إدارة المنظومة بحماية خصوصية كافة المواطنين وسرية المعلومات والوثائق المقدمة، ولا يتم استخدامها إلا لغرض إنجاز المعاملة ومتابعتها مع الجهات الرسمية ذات الصلة.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, lineHeight = 18.sp)
                )
            }
        }
    }
}
