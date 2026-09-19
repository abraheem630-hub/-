package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.models.CitizenRequestEntity
import com.example.ui.components.IraqiProvinceDropdown
import com.example.ui.components.REQUEST_TYPES
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitRequestScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var province by remember { mutableStateOf(currentUser?.province ?: "الأنبار") }
    var district by remember { mutableStateOf("") }
    var subdistrict by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var requestType by remember { mutableStateOf(REQUEST_TYPES.first()) }
    var description by remember { mutableStateOf("") }
    var attachmentsList by remember { mutableStateOf(listOf<String>()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = "صورة_مرفقة_${System.currentTimeMillis().toString().takeLast(4)}.jpg"
            attachmentsList = attachmentsList + fileName
            viewModel.showMessage("تم اختيار الصورة: $fileName")
        }
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = "وثيقة_مرفقة_${System.currentTimeMillis().toString().takeLast(4)}.pdf"
            attachmentsList = attachmentsList + fileName
            viewModel.showMessage("تم اختيار الوثيقة: $fileName")
        }
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var submittedRequest by remember { mutableStateOf<CitizenRequestEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var typeExpanded by remember { mutableStateOf(false) }

    // If successfully submitted, show the success dialog with request number, copy button, and track button
    if (submittedRequest != null) {
        val req = submittedRequest!!
        AlertDialog(
            onDismissRequest = { submittedRequest = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(GoldContainer, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GoldDark,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "تم إرسال طلبك بنجاح",
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
                        text = "تم تسجيل طلبك في المنظومة وإحالته إلى لجنة المتابعة والتدقيق.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = NavyContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "رقم الطلب الخاص بك:",
                                style = MaterialTheme.typography.labelMedium.copy(color = NavyDark)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = req.requestNumber,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button: نسخ رقم الطلب
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Request Number", req.requestNumber))
                            viewModel.showMessage("تم نسخ رقم الطلب إلى الحافظة: ${req.requestNumber}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نسخ رقم الطلب", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val number = req.requestNumber
                        submittedRequest = null
                        viewModel.navigateTo(Screen.TrackRequest(number))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("متابعة الطلب الآن", fontWeight = FontWeight.Bold, color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        submittedRequest = null
                        viewModel.navigateTo(Screen.Home)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("العودة إلى الرئيسية", color = TextMuted)
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
            .padding(16.dp)
    ) {
        // Top Card description
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
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = GoldDark
                    )
                }
                Column {
                    Text(
                        text = "استمارة تقديم طلب رسمي",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                    )
                    Text(
                        text = "يرجى تعبئة الحقول بدقة لتحصل على رقم فريد للمتابعة الفورية.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SurfaceWhite.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFF991B1B),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Form Fields
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
                Text(
                    text = "بيانات مقدم الطلب",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                // الاسم الكامل
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل (ثلاثي أو رباعي) *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // رقم الهاتف
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف (الفعال للتواصل) *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    placeholder = { Text("07XXXXXXXXX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // البريد الإلكتروني (اختياري)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني (اختياري)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Divider(color = BorderLight)

                Text(
                    text = "الموقع الجغرافي والسكن",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                // المحافظة
                IraqiProvinceDropdown(
                    selectedProvince = province,
                    onProvinceSelected = { province = it }
                )

                // القضاء والناحية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("القضاء *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = subdistrict,
                        onValueChange = { subdistrict = it },
                        label = { Text("الناحية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // المنطقة والعنوان التفصيلي
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("المنطقة / الحي / المحلة *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("أقرب نقطة دالة أو تفاصيل العنوان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Divider(color = BorderLight)

                Text(
                    text = "تفاصيل الطلب والمرفقات",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                // نوع الطلب Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = requestType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("نوع الطلب *") },
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
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        REQUEST_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    requestType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // وصف الطلب
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("شرح وتفاصيل الطلب بالتفصيل *") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // قسم رفع الصور والمستندات (JPG, PNG, PDF)
                Surface(
                    color = GoldLight,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = GoldDark)
                            Text(
                                text = "المرفقات والوثائق الثبوتية",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الصيغ المقبولة: JPG, PNG, PDF (الحد الأقصى للملف: 10 ميغابايت)",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إرفاق صورة", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    docPickerLauncher.launch("application/pdf")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إرفاق PDF", fontSize = 12.sp)
                            }
                        }

                        // List of attached files
                        if (attachmentsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                attachmentsList.forEachIndexed { idx, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SurfaceWhite, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (item.endsWith(".pdf")) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                                contentDescription = null,
                                                tint = GoldDark,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(item, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                                        }
                                        IconButton(
                                            onClick = {
                                                attachmentsList = attachmentsList.filterIndexed { i, _ -> i != idx }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // زر إرسال الطلب
                Button(
                    onClick = {
                        // Validation
                        if (fullName.isBlank()) {
                            errorMessage = "يرجى كتابة الاسم الكامل"
                            return@Button
                        }
                        if (phone.isBlank() || phone.length < 10) {
                            errorMessage = "يرجى كتابة رقم هاتف صالح لا يقل عن 10 أرقام"
                            return@Button
                        }
                        if (district.isBlank()) {
                            errorMessage = "يرجى تحديد القضاء"
                            return@Button
                        }
                        if (area.isBlank()) {
                            errorMessage = "يرجى كتابة اسم المنطقة أو الحي"
                            return@Button
                        }
                        if (description.isBlank() || description.length < 15) {
                            errorMessage = "يرجى كتابة وصف تفصيلي للطلب (15 حرف على الأقل)"
                            return@Button
                        }

                        errorMessage = null
                        isSubmitting = true

                        val attachmentsString = attachmentsList.joinToString(",")

                        viewModel.submitRequest(
                            fullName = fullName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            province = province,
                            district = district.trim(),
                            subdistrict = subdistrict.trim(),
                            area = area.trim(),
                            address = address.trim(),
                            requestType = requestType,
                            description = description.trim(),
                            attachments = attachmentsString,
                            onSuccess = { created ->
                                isSubmitting = false
                                submittedRequest = created
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
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NavyDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إرسال الطلب واعتماد الرقم الفريد",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
