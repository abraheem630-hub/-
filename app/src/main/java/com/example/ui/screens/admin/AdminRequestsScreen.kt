package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.models.CitizenRequestEntity
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import java.text.SimpleDateFormat
import java.util.*

val ALL_STATUSES = listOf(
    "تم استلام الطلب",
    "قيد المراجعة",
    "قيد المعالجة",
    "قيد المتابعة",
    "تم الإنجاز",
    "مغلق",
    "بحاجة إلى معلومات إضافية"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val requests by viewModel.filteredAdminRequests.collectAsState()
    val searchQuery by viewModel.adminSearchQuery.collectAsState()
    val statusFilter by viewModel.adminStatusFilter.collectAsState()

    var selectedRequestForEdit by remember { mutableStateOf<CitizenRequestEntity?>(null) }
    var requestToDelete by remember { mutableStateOf<CitizenRequestEntity?>(null) }

    // Status / Note Edit Dialog
    if (selectedRequestForEdit != null) {
        val req = selectedRequestForEdit!!
        var newStatus by remember { mutableStateOf(req.status) }
        var newNote by remember { mutableStateOf("") }
        var statusDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedRequestForEdit = null },
            title = {
                Text(
                    text = "تعديل حالة الطلب: ${req.requestNumber}",
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("المواطن: ${req.fullName} (${req.phone})", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    Text("النوع: ${req.requestType}", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    Text("المحافظة: ${req.province} - ${req.area}", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))

                    Divider(color = BorderLight)

                    // Status Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الحالة الجديدة للطلب *") },
                            trailingIcon = {
                                IconButton(onClick = { statusDropdownExpanded = !statusDropdownExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            ALL_STATUSES.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st) },
                                    onClick = {
                                        newStatus = st
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newNote,
                        onValueChange = { newNote = it },
                        label = { Text("إضافة ملاحظة أو توجيه للمواطن") },
                        placeholder = { Text("مثال: تم التنسيق مع مديرية البلدية لتنفيذ العمل") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (req.adminNotes.isNotBlank()) {
                        Surface(
                            color = BackgroundLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("الملاحظات الإدارية السابقة:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextMuted))
                                Text(req.adminNotes, style = MaterialTheme.typography.bodySmall.copy(color = TextDark))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdateRequestStatus(req.id, newStatus, newNote.trim())
                        selectedRequestForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                ) {
                    Text("حفظ وتحديث الحالة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRequestForEdit = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = SurfaceWhite
        )
    }

    // Delete Confirmation Dialog
    ConfirmationDialog(
        show = requestToDelete != null,
        title = "حذف الطلب نهائياً",
        message = "هل أنت متأكد من حذف الطلب رقم ${requestToDelete?.requestNumber} الخاص بالمواطن ${requestToDelete?.fullName}؟ لا يمكن التراجع عن هذا الإجراء.",
        onConfirm = {
            requestToDelete?.let { viewModel.adminDeleteRequest(it.id) }
            requestToDelete = null
        },
        onDismiss = { requestToDelete = null }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Filter Bar
        Surface(
            color = SurfaceWhite,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.adminSearchQuery.value = it },
                    placeholder = { Text("بحث برقم الطلب، اسم المواطن، أو رقم الهاتف...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Status quick filter chips
                val filterOptions = listOf("الكل") + ALL_STATUSES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = filterOptions.indexOf(statusFilter).coerceAtLeast(0),
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        indicator = {}
                    ) {
                        filterOptions.forEach { opt ->
                            val isSelected = statusFilter == opt
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.adminStatusFilter.value = opt },
                                label = { Text(opt, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = SurfaceWhite
                                ),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Requests List
        if (requests.isEmpty()) {
            EmptyStateView(
                message = "لا توجد طلبات مطابقة لمعايير البحث الحالية.",
                icon = Icons.Default.SearchOff
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(requests, key = { it.id }) { req ->
                    val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(req.createdAt))

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
                                Column {
                                    Text(
                                        text = req.requestNumber,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = NavyDark
                                        )
                                    )
                                    Text(
                                        text = dateFormatted,
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                    )
                                }
                                StatusChip(status = req.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "المواطن: ${req.fullName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark)
                                )
                                Text(
                                    text = req.phone,
                                    style = MaterialTheme.typography.bodySmall.copy(color = GoldDark, fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "الموقع: ${req.province} - ${req.district} (${req.area})",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "النوع: ${req.requestType}",
                                style = MaterialTheme.typography.bodySmall.copy(color = NavyPrimary, fontWeight = FontWeight.Medium)
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = req.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDark),
                                maxLines = 2
                            )

                            if (req.adminNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = NavyContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ملاحظات الإدارة: ${req.adminNotes}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = NavyDark),
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: تغيير الحالة / الملاحظات & حذف
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { selectedRequestForEdit = req },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تحديث الحالة والملاحظة", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { requestToDelete = req },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
