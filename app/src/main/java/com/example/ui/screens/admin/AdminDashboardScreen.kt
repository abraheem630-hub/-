package com.example.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun AdminDashboardScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val requests by viewModel.allRequests.collectAsState()
    val complaints by viewModel.allComplaints.collectAsState()
    val reports by viewModel.allReports.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val messages by viewModel.allMessages.collectAsState()

    val completedRequests = remember(requests) { requests.count { it.status == "تم الإنجاز" } }
    val processingRequests = remember(requests) { requests.count { it.status == "قيد المعالجة" || it.status == "قيد المتابعة" } }
    val receivedRequests = remember(requests) { requests.count { it.status == "تم استلام الطلب" || it.status == "قيد المراجعة" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Admin Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, GoldAccent, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.candidate),
                                    contentDescription = "سامي الفلاحي",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column {
                                Text(
                                    text = "لوحة تحكم المشرف العام والمالك",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SurfaceWhite
                                    )
                                )
                                Text(
                                    text = "سامي الفلاحي (abraheem630@gmail.com)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent)
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Logout, contentDescription = "خروج", tint = Color.Red)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "إشراف ومتابعة كافة العمليات والمعاملات الواردة من المواطنين مع التوثيق الرقابي الفوري.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite.copy(alpha = 0.8f))
                    )
                }
            }
        }

        // Section: إحصائيات عامة
        item {
            Text(
                text = "الإحصائيات والتحليلات المباشرة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
            )
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "إجمالي الطلبات",
                        value = "${requests.size}",
                        icon = Icons.Default.Description,
                        color = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "تم الإنجاز",
                        value = "$completedRequests",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "قيد المعالجة والمتابعة",
                        value = "$processingRequests",
                        icon = Icons.Default.HourglassBottom,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "طلبات جديدة مستلمة",
                        value = "$receivedRequests",
                        icon = Icons.Default.Inbox,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminStatCard(
                        title = "الشكاوى المسجلة",
                        value = "${complaints.size}",
                        icon = Icons.Default.ReportProblem,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        title = "البلاغات الميدانية",
                        value = "${reports.size}",
                        icon = Icons.Default.NotificationImportant,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Completion Rate Visual Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val total = requests.size.coerceAtLeast(1)
                    val percent = (completedRequests * 100) / total

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نسبة إنجاز الطلبات العامة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                        )
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldDark)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = GoldAccent,
                        trackColor = NavyContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تم إنجاز $completedRequests معاملة من أصل ${requests.size} طلباً مقدماً عبر المنصة.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            }
        }

        // Section: الإدارة والأقسام
        item {
            Text(
                text = "إدارة المنظومة وقواعد البيانات",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminNavigationRow(
                    title = "إدارة طلبات المواطنين وتحديث الحالات",
                    subtitle = "فرز، مراجعة، تحديث الحالات إلى منجز، وإضافة ملاحظات (${requests.size})",
                    icon = Icons.Default.Assignment,
                    badgeCount = requests.size,
                    onClick = { viewModel.navigateTo(Screen.AdminRequests) }
                )

                AdminNavigationRow(
                    title = "إدارة الشكاوى الرسمية",
                    subtitle = "مراجعة شكاوى المواطنين والرد الإداري عليها (${complaints.size})",
                    icon = Icons.Default.ReportProblem,
                    badgeCount = complaints.size,
                    onClick = { viewModel.navigateTo(Screen.AdminComplaints) }
                )

                AdminNavigationRow(
                    title = "إدارة البلاغات الميدانية",
                    subtitle = "أعطال شبكات الكهرباء والماء والطرق (${reports.size})",
                    icon = Icons.Default.NotificationImportant,
                    badgeCount = reports.size,
                    onClick = { viewModel.navigateTo(Screen.AdminReports) }
                )

                AdminNavigationRow(
                    title = "إدارة المواطنين المسجلين",
                    subtitle = "سجل المواطنين والمحافظات وبيانات التواصل (${users.size})",
                    icon = Icons.Default.People,
                    badgeCount = users.size,
                    onClick = { viewModel.navigateTo(Screen.AdminCitizens) }
                )

                AdminNavigationRow(
                    title = "إدارة فرص العمل والتدريب",
                    subtitle = "نشر وتعديل وحذف الفرص الوظيفية للشباب",
                    icon = Icons.Default.Work,
                    onClick = { viewModel.navigateTo(Screen.AdminJobs) }
                )

                AdminNavigationRow(
                    title = "إدارة برامج الرعاية الاجتماعية",
                    subtitle = "تحديث ضوابط وشروط المساعدات الإنسانية والطبية",
                    icon = Icons.Default.VolunteerActivism,
                    onClick = { viewModel.navigateTo(Screen.AdminSocialServices) }
                )

                AdminNavigationRow(
                    title = "إدارة الأخبار والبيانات الرسمية",
                    subtitle = "نشر البيانات الصحفية وجولات السيد سامي الفلاحي",
                    icon = Icons.Default.Campaign,
                    onClick = { viewModel.navigateTo(Screen.AdminNews) }
                )

                AdminNavigationRow(
                    title = "إرسال الإشعارات والتنبيهات العامة",
                    subtitle = "بث إشعارات فورية لجميع المواطنين في التطبيق",
                    icon = Icons.Default.NotificationsActive,
                    onClick = { viewModel.navigateTo(Screen.AdminNotifications) }
                )

                AdminNavigationRow(
                    title = "صندوق رسائل واستفسارات المواطنين",
                    subtitle = "الرد المباشر على استفسارات المتابعة (${messages.size})",
                    icon = Icons.Default.Chat,
                    badgeCount = messages.size,
                    onClick = { viewModel.navigateTo(Screen.AdminMessages) }
                )

                AdminNavigationRow(
                    title = "سجل الرقابة والعمليات الإدارية (Audit Logs)",
                    subtitle = "توثيق كل تعديل وحذف وحالة مع التوقيت والبريد",
                    icon = Icons.Default.History,
                    onClick = { viewModel.navigateTo(Screen.AdminAuditLogs) }
                )

                AdminNavigationRow(
                    title = "إعدادات المنظومة وهوية المكتب",
                    subtitle = "العناوين، أرقام الهاتف، الهيدر، ونصوص الترحيب",
                    icon = Icons.Default.Settings,
                    onClick = { viewModel.navigateTo(Screen.AdminSettings) }
                )
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun AdminNavigationRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    badgeCount: Int? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1
                )
            }

            if (badgeCount != null) {
                Surface(
                    color = GoldLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
