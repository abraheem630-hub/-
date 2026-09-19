package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.NewsEntity
import com.example.ui.components.HeroSamiBanner
import com.example.ui.components.ServiceCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsState()
    val newsList by viewModel.allNews.collectAsState()
    var quickTrackNumber by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Sami Banner
        item {
            HeroSamiBanner(
                heroTitle = settings.heroTitle,
                heroSubtitle = settings.heroSubtitle,
                notice = settings.bannerNotice,
                onSubmitRequestClick = { viewModel.navigateTo(Screen.SubmitRequest) },
                onTrackRequestClick = { viewModel.navigateTo(Screen.TrackRequest()) }
            )
        }

        // Account / Login & Email Status Card
        item {
            val user by viewModel.currentUser.collectAsState()
            val isAdmin by viewModel.isAdmin.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isAdmin) NavyDark else SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdmin) GoldAccent else NavyLight.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isAdmin) Modifier.border(1.5.dp, GoldAccent, CircleShape)
                                    else Modifier.background(NavyPrimary)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAdmin) {
                                Image(
                                    painter = painterResource(id = R.drawable.candidate),
                                    contentDescription = "المرشح سامي الفلاحي",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = SurfaceWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isAdmin) "مسجل الدخول: المشرف العام" else if (user != null) "مرحباً: ${user?.fullName}" else "تسجيل الدخول وحساب المستخدم",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdmin) SurfaceWhite else NavyDark,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = if (isAdmin) "abraheem630@gmail.com" else if (user != null) (user?.email?.ifBlank { "رقم الهاتف: " + user?.phone } ?: "حساب مسجل") else "تسجيل دخول المشرف أو إدارة حساب المواطن",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isAdmin) GoldAccent else TextMuted,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (isAdmin) {
                                viewModel.navigateTo(Screen.AdminDashboard)
                            } else {
                                viewModel.navigateTo(Screen.AdminLogin)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdmin) GoldAccent else NavyPrimary,
                            contentColor = if (isAdmin) NavyDark else SurfaceWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isAdmin) "لوحة الإدارة" else "تسجيل الدخول",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Tracking Search Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = GoldDark
                        )
                        Text(
                            text = "استعلام سريع عن حالة طلب أو شكوى",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quickTrackNumber,
                            onValueChange = { quickTrackNumber = it },
                            placeholder = { Text("مثال: ANB-2026-000001", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (quickTrackNumber.isNotBlank()) {
                                    viewModel.navigateTo(Screen.TrackRequest(quickTrackNumber.trim()))
                                } else {
                                    viewModel.showMessage("يرجى إدخال رقم الطلب أولاً")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("استعلام", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section Title: الخدمات الرئيسية
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(GoldAccent)
                    )
                    Text(
                        text = "الخدمات الرئيسية للمواطنين",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark,
                            fontSize = 18.sp
                        )
                    )
                }
                Text(
                    text = "10 خدمات متكاملة",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // 10 Main Services Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: تقديم طلب & تقديم شكوى
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ServiceCard(
                        title = "1. تقديم طلب",
                        description = "إرسال المعاملات والطلبات الخدمية والإنسانية مع المرفقات",
                        icon = Icons.Default.PostAdd,
                        onClick = { viewModel.navigateTo(Screen.SubmitRequest) },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceCard(
                        title = "2. تقديم شكوى",
                        description = "تسجيل الشكاوى الرسمية حول التقصير الخدمي أو المعاملات",
                        icon = Icons.Default.ReportProblem,
                        onClick = { viewModel.navigateTo(Screen.SubmitComplaint) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: متابعة الطلب & ملف المواطن
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ServiceCard(
                        title = "3. متابعة الطلب",
                        description = "تتبع مراحل معالجة طلباتك والجدول الزمني للإنجاز برقم التتبع",
                        icon = Icons.Default.TrackChanges,
                        onClick = { viewModel.navigateTo(Screen.TrackRequest()) },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceCard(
                        title = "4. ملف المواطن",
                        description = "سجل طلباتك وشكاواك وبلاغاتك ومحادثاتك في حسابك",
                        icon = Icons.Default.Badge,
                        onClick = { viewModel.navigateTo(Screen.CitizenProfile) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: فرص العمل والتدريب & الخدمات الاجتماعية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ServiceCard(
                        title = "5. فرص العمل والتدريب",
                        description = "استعراض والتقديم على الوظائف والبرامج التأهيلية للشباب",
                        icon = Icons.Default.WorkOutline,
                        onClick = { viewModel.navigateTo(Screen.JobsAndTraining) },
                        badgeText = "جديد",
                        modifier = Modifier.weight(1f)
                    )
                    ServiceCard(
                        title = "6. الخدمات الاجتماعية",
                        description = "برامج الرعاية والمساعدات الطبية والإنسانية للأسر",
                        icon = Icons.Default.VolunteerActivism,
                        onClick = { viewModel.navigateTo(Screen.SocialServices) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: الإبلاغ عن مشكلة & معلومات وإرشادات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ServiceCard(
                        title = "7. الإبلاغ عن مشكلة",
                        description = "بلاغ عاجل عن أعطال الكهرباء والماء والطرق مع الموقع",
                        icon = Icons.Default.NotificationImportant,
                        onClick = { viewModel.navigateTo(Screen.ReportProblem) },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceCard(
                        title = "8. معلومات وإرشادات",
                        description = "دليل الاستخدام والشروط وسياسة الخصوصية للمنصة",
                        icon = Icons.Default.MenuBook,
                        onClick = { viewModel.navigateTo(Screen.AboutApp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 5: التواصل مع فريق المتابعة & الأخبار والإعلانات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ServiceCard(
                        title = "9. فريق المتابعة",
                        description = "مراسلة مباشرة مع مسؤولي المتابعة وإرسال الاستفسارات",
                        icon = Icons.Default.ContactSupport,
                        onClick = { viewModel.navigateTo(Screen.Messages) },
                        modifier = Modifier.weight(1f)
                    )
                    ServiceCard(
                        title = "10. الأخبار والإعلانات",
                        description = "آخر البيانات والقرارات والجولات الميدانية في المناطق",
                        icon = Icons.Default.Campaign,
                        onClick = { viewModel.navigateTo(Screen.NewsAndAnnouncements) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section: الأخبار والإعلانات
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(GoldAccent)
                    )
                    Text(
                        text = "آخر الأخبار والإعلانات",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark,
                            fontSize = 18.sp
                        )
                    )
                }
                TextButton(onClick = { viewModel.navigateTo(Screen.NewsAndAnnouncements) }) {
                    Text("عرض الكل", color = GoldDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(newsList.take(4), key = { it.id }) { news ->
                    NewsHorizontalCard(news = news) {
                        viewModel.navigateTo(Screen.NewsAndAnnouncements)
                    }
                }
            }
        }

        // Official Contact & Transparency Footer Card
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "مكتب خدمة ومتابعة شؤون المواطنين",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = SurfaceWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "هذه المنصة وسيلة إلكترونية مخصصة لتنظيم وتوثيق تواصل المواطنين الكرام مع مكتب الأستاذ سامي الفلاحي لتيسير متابعة القضايا الخدمية والإنسانية.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SurfaceWhite.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "الخط الساخن: ${settings.contactPhone}",
                            style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = settings.officeAddress,
                            style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsHorizontalCard(
    news: NewsEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = GoldLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "بيان رسمي",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = news.date,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = news.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    fontSize = 14.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = news.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "قراءة التفاصيل ←",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldDark,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
