package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ComplaintEntity
import com.example.ui.components.IraqiProvinceDropdown
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen

val COMPLAINT_TYPES = listOf(
    "شكوى سوء خدمة أو انقطاع دائم",
    "شكوى تأخير أو عرقلة معاملة رسمية",
    "شكوى فساد إداري أو مالي",
    "تجاوز على ممتلكات عامة أو بيئية",
    "شكوى بخصوص شبكة مياه أو مجاري",
    "أخرى"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitComplaintScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var province by remember { mutableStateOf(currentUser?.province ?: "الأنبار") }
    var district by remember { mutableStateOf("") }
    var subdistrict by remember { mutableStateOf("") }
    var complaintType by remember { mutableStateOf(COMPLAINT_TYPES.first()) }
    var description by remember { mutableStateOf("") }
    var attachmentsList by remember { mutableStateOf(listOf<String>()) }

    val complaintPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = "وثيقة_شكوى_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
            attachmentsList = attachmentsList + fileName
            viewModel.showMessage("تم إرفاق: $fileName")
        }
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var submittedComplaint by remember { mutableStateOf<ComplaintEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }

    if (submittedComplaint != null) {
        val cmp = submittedComplaint!!
        AlertDialog(
            onDismissRequest = { submittedComplaint = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(GoldContainer, RoundedCornerShape(27.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = GoldDark, modifier = Modifier.size(32.dp))
                }
            },
            title = {
                Text(
                    text = "تم تسجيل الشكوى بنجاح",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تم استلام الشكوى وتوجيهها لقسم المتابعة والرقابة للتدقيق واتخاذ اللازم.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = NavyContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("رقم الشكوى المرجعي:", style = MaterialTheme.typography.labelSmall.copy(color = NavyDark))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cmp.complaintNumber,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Complaint Number", cmp.complaintNumber))
                            viewModel.showMessage("تم نسخ رقم الشكوى: ${cmp.complaintNumber}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نسخ رقم الشكوى", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        submittedComplaint = null
                        viewModel.navigateTo(Screen.Home)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تم والعودة للرئيسية", color = SurfaceWhite, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceWhite
        )
    }

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
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = GoldDark)
                }
                Column {
                    Text(
                        text = "نافذة تقديم الشكاوى والملاحظات",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                    )
                    Text(
                        text = "تصل شكواك مباشرة للمكتب الإداري وتُعامل بسرية واهتمام تام.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SurfaceWhite.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }

        if (errorMessage != null) {
            Surface(
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626))
                    Text(text = errorMessage!!, color = Color(0xFF991B1B), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                IraqiProvinceDropdown(
                    selectedProvince = province,
                    onProvinceSelected = { province = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("القضاء *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = subdistrict,
                        onValueChange = { subdistrict = it },
                        label = { Text("الناحية") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = complaintType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الشكوى *") },
                        trailingIcon = {
                            IconButton(onClick = { typeExpanded = !typeExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        COMPLAINT_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    complaintType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف الشكوى والملاحظات بدقة *") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Attachments button
                OutlinedButton(
                    onClick = {
                        complaintPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إرفاق صور أو وثائق تثبت الشكوى (${attachmentsList.size})")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (fullName.isBlank()) {
                            errorMessage = "يرجى إدخال الاسم"
                            return@Button
                        }
                        if (phone.isBlank() || phone.length < 10) {
                            errorMessage = "يرجى إدخال رقم هاتف صحيح"
                            return@Button
                        }
                        if (district.isBlank()) {
                            errorMessage = "يرجى تحديد القضاء"
                            return@Button
                        }
                        if (description.isBlank() || description.length < 15) {
                            errorMessage = "يرجى كتابة تفاصيل الشكوى بوضوح (15 حرفاً على الأقل)"
                            return@Button
                        }

                        errorMessage = null
                        isSubmitting = true

                        viewModel.submitComplaint(
                            fullName = fullName.trim(),
                            phone = phone.trim(),
                            province = province,
                            district = district.trim(),
                            subdistrict = subdistrict.trim(),
                            complaintType = complaintType,
                            description = description.trim(),
                            attachments = attachmentsList.joinToString(","),
                            onSuccess = { created ->
                                isSubmitting = false
                                submittedComplaint = created
                            },
                            onError = { err ->
                                isSubmitting = false
                                errorMessage = err
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = NavyDark)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إرسال الشكوى رسمياً", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
