package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RequestTimelineView
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrackRequestScreen(
    viewModel: PortalViewModel,
    initialNumber: String = "",
    modifier: Modifier = Modifier
) {
    val trackingState by viewModel.trackingResult.collectAsState()
    var requestNumberInput by remember { mutableStateOf(initialNumber) }
    var phoneInput by remember { mutableStateOf("") }

    // Auto-search if initialNumber passed
    LaunchedEffect(initialNumber) {
        if (initialNumber.isNotBlank()) {
            requestNumberInput = initialNumber
            viewModel.trackRequest(initialNumber, "")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(GoldLight, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = GoldDark
                        )
                    }
                    Column {
                        Text(
                            text = "نظام متابعة الطلبات الإلكتروني",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        )
                        Text(
                            text = "أدخل رقم الطلب ورقم الهاتف للاستعلام المباشر عن مسار المعاملة.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SurfaceWhite.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search inputs
                OutlinedTextField(
                    value = requestNumberInput,
                    onValueChange = { requestNumberInput = it },
                    label = { Text("رقم الطلب (مثال: ANB-2026-000001)", color = SurfaceWhite.copy(alpha = 0.8f)) },
                    leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = GoldAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SurfaceWhite,
                        unfocusedTextColor = SurfaceWhite,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = SurfaceWhite.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("رقم الهاتف المسجل (اختياري للتحقق)", color = SurfaceWhite.copy(alpha = 0.8f)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldAccent) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SurfaceWhite,
                        unfocusedTextColor = SurfaceWhite,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = SurfaceWhite.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (requestNumberInput.isNotBlank()) {
                            viewModel.trackRequest(requestNumberInput.trim(), phoneInput.trim())
                        } else {
                            viewModel.showMessage("يرجى إدخال رقم الطلب أولاً")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    if (trackingState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NavyDark, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("البحث وتتبع المسار", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Search Results
        if (trackingState.hasSearched) {
            val req = trackingState.request

            if (req != null) {
                val createdFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(req.createdAt))
                val updatedFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(req.updatedAt))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "طلب رقم:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                                Text(
                                    text = req.requestNumber,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                )
                            }
                            StatusChip(status = req.status)
                        }

                        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 12.dp))

                        // Details Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("مقدم الطلب", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                                Text(req.fullName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("المحافظة والمنطقة", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                                Text("${req.province} - ${req.area}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("نوع المعاملة", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                                Text(req.requestType, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تاريخ التقديم", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                                Text(createdFormatted, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("شرح الطلب:", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        Surface(
                            color = BackgroundLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = req.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDark, lineHeight = 18.sp),
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        if (req.attachments.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("المستندات والمرفقات:", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                req.attachments.split(",").forEach { file ->
                                    Surface(
                                        color = GoldLight,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = GoldDark, modifier = Modifier.size(12.dp))
                                            Text(file, style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontSize = 11.sp))
                                        }
                                    }
                                }
                            }
                        }

                        if (req.adminNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = NavyContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Comment, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                                        Text("ملاحظات وتوجيهات الإدارة:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 12.sp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(req.adminNotes, style = MaterialTheme.typography.bodySmall.copy(color = NavyDark, fontSize = 12.sp))
                                }
                            }
                        }

                        Divider(color = BorderLight, modifier = Modifier.padding(vertical = 14.dp))

                        // Timeline Section
                        Text(
                            text = "المسار الزمني لمعالجة الطلب (Timeline):",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        RequestTimelineView(timelineText = req.timeline)
                    }
                }
            } else if (trackingState.errorMessage != null) {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = trackingState.errorMessage!!,
                            color = Color(0xFF991B1B),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}
