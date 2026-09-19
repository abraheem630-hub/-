package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

val IRAQI_PROVINCES = listOf(
    "بغداد",
    "الأنبار",
    "البصرة",
    "نينوى",
    "أربيل",
    "النجف الأشرف",
    "كربلاء المقدسة",
    "كركوك",
    "بابل",
    "ذي قار",
    "ديالى",
    "ميسان",
    "المثنى",
    "القادسية",
    "واسط",
    "صلاح الدين",
    "دهوك",
    "السليمانية"
)

val REQUEST_TYPES = listOf(
    "طلب مساعدة إنسانية ورعاية اجتماعية",
    "طلب توظيف وتدريب مهني",
    "معاملة بلدية وخدمات ماء ومجاري",
    "تأهيل شبكات الكهرباء والإنارة",
    "طلب علاج ومساعدة طبية مستعجلة",
    "تظلم ومتابعة معاملة إدارية",
    "صيانة طرق وتبليط شوارع",
    "أخرى"
)

val PROBLEM_TYPES = listOf(
    "انقطاع وتذبذب التيار الكهربائي",
    "شح المياه أو كسر أنبوب رئيسي",
    "تضرر وتخسف في الطرق العامة",
    "طفح مجاري وصرف صحي",
    "تراكم النفايات والمخلفات",
    "أخرى"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    isAdminLoggedIn: Boolean,
    onAdminClick: () -> Unit,
    unreadCount: Int = 0,
    onNotificationsClick: () -> Unit = {}
) {
    Surface(
        color = NavyDark,
        shadowElevation = 4.dp
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, GoldAccent, CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "جمهورية العراق | المنظومة الرقمية لخدمة المواطن",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldAccent.copy(alpha = 0.85f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            },
            navigationIcon = {
                if (canNavigateBack) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = SurfaceWhite
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = SurfaceWhite
                                ) {
                                    Text(text = if (unreadCount > 99) "99+" else "$unreadCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "الإشعارات",
                            tint = if (unreadCount > 0) GoldAccent else SurfaceWhite
                        )
                    }
                }

                // Direct login button & status
                Surface(
                    onClick = onAdminClick,
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAdminLoggedIn) GoldAccent else NavyLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdminLoggedIn) Icons.Filled.VerifiedUser else Icons.Default.Login,
                            contentDescription = if (isAdminLoggedIn) "لوحة المالك" else "تسجيل الدخول",
                            tint = if (isAdminLoggedIn) NavyDark else GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAdminLoggedIn) "المالك متصل" else "تسجيل الدخول",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isAdminLoggedIn) NavyDark else SurfaceWhite,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = NavyDark,
                titleContentColor = SurfaceWhite
            )
        )
    }
}

@Composable
fun HeroSamiBanner(
    heroTitle: String,
    heroSubtitle: String,
    notice: String,
    onSubmitRequestClick: () -> Unit,
    onTrackRequestClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NavyLight.copy(alpha = 0.6f),
                            NavyDark
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Official Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GoldDark.copy(alpha = 0.25f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "البوابة المعتمدة لمتابعة قضايا المواطنين",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldLight,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Portrait and Titles Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Portrait Image
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, GoldAccent, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_sami_portrait),
                            contentDescription = "سامي الفلاحي",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = heroTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite,
                                fontSize = 22.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = heroSubtitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "نستمع إليكم ونسعى لتلبية احتياجاتكم وإيصال صوتكم للجهات المختصة بكل شفافية ومصداقية.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SurfaceWhite.copy(alpha = 0.85f),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                if (notice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SurfaceWhite.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = notice,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SurfaceWhite.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: تقديم طلب & متابعة طلب
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSubmitRequestClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = NavyDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تقديم طلب",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onTrackRequestClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SurfaceWhite
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(GoldAccent, SurfaceWhite))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "متابعة طلب",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (badgeText != null) {
                    Surface(
                        color = NavyContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    fontSize = 15.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "دخول الخدمة",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = GoldDark,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor, icon) = when (status) {
        "تم استلام الطلب", "تم استلام الشكوى" -> Triple(StatusReceived.copy(alpha = 0.12f), StatusReceived, Icons.Default.Inbox)
        "قيد المراجعة" -> Triple(StatusUnderReview.copy(alpha = 0.12f), StatusUnderReview, Icons.Default.HourglassTop)
        "قيد المعالجة" -> Triple(StatusProcessing.copy(alpha = 0.12f), StatusProcessing, Icons.Default.Engineering)
        "قيد المتابعة" -> Triple(StatusFollowUp.copy(alpha = 0.12f), StatusFollowUp, Icons.Default.Sync)
        "تم الإنجاز" -> Triple(StatusCompleted.copy(alpha = 0.12f), StatusCompleted, Icons.Default.CheckCircle)
        "مغلق" -> Triple(StatusClosed.copy(alpha = 0.12f), StatusClosed, Icons.Default.Lock)
        "بحاجة إلى معلومات إضافية" -> Triple(StatusNeedsInfo.copy(alpha = 0.12f), StatusNeedsInfo, Icons.Default.Warning)
        else -> Triple(NavyContainer, NavyDark, Icons.Default.Info)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun RequestTimelineView(timelineText: String) {
    val steps = remember(timelineText) {
        if (timelineText.isBlank()) emptyList()
        else timelineText.split("\n").filter { it.isNotBlank() }.map { line ->
            val parts = line.split("|")
            Triple(
                parts.getOrElse(0) { "تحديث" },
                parts.getOrElse(1) { "" },
                parts.getOrElse(2) { "" }
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Timeline indicator column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = NavyDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(42.dp)
                                .background(GoldAccent.copy(alpha = 0.4f))
                        )
                    }
                }

                // Details column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (index < steps.size - 1) 16.dp else 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = step.first,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = step.second,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                    if (step.third.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = step.third,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextDark,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    message: String,
    icon: ImageVector = Icons.Outlined.FolderOpen,
    actionButtonText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GoldLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldDark,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        )
        if (actionButtonText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text(text = actionButtonText, color = SurfaceWhite)
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title, fontWeight = FontWeight.Bold, color = NavyDark) },
            text = { Text(text = message, color = TextDark) },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("تأكيد الحذف", color = SurfaceWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = SurfaceWhite
        )
    }
}

@Composable
fun IraqiProvinceDropdown(
    selectedProvince: String,
    onProvinceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedProvince,
            onValueChange = {},
            readOnly = true,
            label = { Text("المحافظة") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = NavyPrimary
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            IRAQI_PROVINCES.forEach { prov ->
                DropdownMenuItem(
                    text = { Text(prov) },
                    onClick = {
                        onProvinceSelected(prov)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PortalBottomNavigationBar(
    currentScreen: String,
    onHomeClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    unreadCount: Int = 0
) {
    NavigationBar(
        containerColor = NavyDark,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "الرئيسية",
            onClick = onHomeClick,
            icon = {
                Icon(
                    imageVector = if (currentScreen == "الرئيسية") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "الرئيسية"
                )
            },
            label = { Text("الرئيسية") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = SurfaceWhite.copy(alpha = 0.7f),
                unselectedTextColor = SurfaceWhite.copy(alpha = 0.7f)
            )
        )

        NavigationBarItem(
            selected = currentScreen == "متابعة الطلب",
            onClick = onRequestsClick,
            icon = {
                Icon(
                    imageVector = if (currentScreen == "متابعة الطلب") Icons.Filled.Assignment else Icons.Outlined.Assignment,
                    contentDescription = "طلباتي"
                )
            },
            label = { Text("الطلبات") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = SurfaceWhite.copy(alpha = 0.7f),
                unselectedTextColor = SurfaceWhite.copy(alpha = 0.7f)
            )
        )

        NavigationBarItem(
            selected = currentScreen == "الإشعارات",
            onClick = onNotificationsClick,
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFDC2626),
                                contentColor = SurfaceWhite
                            ) {
                                Text(if (unreadCount > 9) "9+" else "$unreadCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (unreadCount > 0) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "الإشعارات"
                    )
                }
            },
            label = { Text("الإشعارات") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = SurfaceWhite.copy(alpha = 0.7f),
                unselectedTextColor = SurfaceWhite.copy(alpha = 0.7f)
            )
        )

        NavigationBarItem(
            selected = currentScreen == "ملف المواطن",
            onClick = onProfileClick,
            icon = {
                Icon(
                    imageVector = if (currentScreen == "ملف المواطن") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "الملف الشخصي"
                )
            },
            label = { Text("الملف") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = SurfaceWhite.copy(alpha = 0.7f),
                unselectedTextColor = SurfaceWhite.copy(alpha = 0.7f)
            )
        )
    }
}
