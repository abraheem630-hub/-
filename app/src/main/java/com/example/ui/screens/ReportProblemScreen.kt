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
import com.example.data.models.ProblemReportEntity
import com.example.ui.components.IraqiProvinceDropdown
import com.example.ui.components.PROBLEM_TYPES
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ReportProblemScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var problemType by remember { mutableStateOf(PROBLEM_TYPES.first()) }
    var province by remember { mutableStateOf(currentUser?.province ?: "الأنبار") }
    var district by remember { mutableStateOf("") }
    var subdistrict by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var hasPhoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            hasPhoto = true
            viewModel.showMessage("تم اختيار صورة المشكلة الميدانية بنجاح")
        }
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var submittedReport by remember { mutableStateOf<ProblemReportEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var typeExpanded by remember { mutableStateOf(false) }

    if (submittedReport != null) {
        val rep = submittedReport!!
        AlertDialog(
            onDismissRequest = { submittedReport = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(GoldContainer, RoundedCornerShape(27.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = GoldDark, modifier = Modifier.size(32.dp))
                }
            },
            title = {
                Text(
                    text = "تم إرسال البلاغ بنجاح",
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
                        text = "شكرًا لمساهمتك، تم تحويل البلاغ إلى فرق الصيانة والمتابعة الميدانية للتعامل معه فوراً.",
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
                            Text("رقم البلاغ المرجعي:", style = MaterialTheme.typography.labelSmall.copy(color = NavyDark))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rep.reportNumber,
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
                            clipboard.setPrimaryClip(ClipData.newPlainText("Report Number", rep.reportNumber))
                            viewModel.showMessage("تم نسخ رقم البلاغ: ${rep.reportNumber}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نسخ رقم البلاغ", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        submittedReport = null
                        viewModel.navigateTo(Screen.Home)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("العودة للرئيسية", color = SurfaceWhite, fontWeight = FontWeight.Bold)
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
                    Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = GoldDark)
                }
                Column {
                    Text(
                        text = "الإبلاغ عن خلل أو مشكلة خدمية",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                    )
                    Text(
                        text = "خدمة سريعة للتبليغ المباشر عن أعطال شبكات الكهرباء، الماء، والطرق.",
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = problemType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع المشكلة *") },
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
                        PROBLEM_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    problemType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

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

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("الموقع والعنوان الدقيق كنص يدوي *") },
                    placeholder = { Text("مثال: شارع 60، قرب جامع التقوى، الحي العسكري") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("شرح مختصر عن المشكلة أو حجم الخلل *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف للتواصل والمطابقة *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (hasPhoto) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = if (hasPhoto) Color(0xFF059669) else NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasPhoto) "تم إرفاق صورة الخلل الميداني بنجاح" else "إرفاق صورة لمكان المشكلة (اختياري)",
                        color = if (hasPhoto) Color(0xFF059669) else NavyPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (district.isBlank()) {
                            errorMessage = "يرجى كتابة القضاء"
                            return@Button
                        }
                        if (address.isBlank()) {
                            errorMessage = "يرجى تحديد الموقع كتابياً"
                            return@Button
                        }
                        if (description.isBlank()) {
                            errorMessage = "يرجى كتابة وصف للخلل"
                            return@Button
                        }
                        if (phone.isBlank() || phone.length < 10) {
                            errorMessage = "يرجى إدخال رقم هاتف صالح"
                            return@Button
                        }

                        errorMessage = null
                        isSubmitting = true

                        viewModel.submitReport(
                            problemType = problemType,
                            province = province,
                            district = district.trim(),
                            subdistrict = subdistrict.trim(),
                            address = address.trim(),
                            description = description.trim(),
                            phone = phone.trim(),
                            onSuccess = { created ->
                                isSubmitting = false
                                submittedReport = created
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
                        Text("إرسال البلاغ الميداني", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
