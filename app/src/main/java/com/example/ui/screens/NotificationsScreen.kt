package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    viewModel: PortalViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldDark)
                    }
                    Column {
                        Text(
                            text = "مركز الإشعارات والتنبيهات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                        )
                        Text(
                            text = "تحديثات فورية حول طلباتك والإعلانات الرسمية وفرص العمل.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SurfaceWhite.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                EmptyStateView(
                    message = "لا توجد إشعارات جديدة حالياً.",
                    icon = Icons.Outlined.Notifications
                )
            }
        } else {
            items(notifications) { notif ->
                val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(notif.createdAt))
                val isUnread = !notif.isRead

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.markNotificationAsRead(notif.id)
                            if (notif.referenceId.isNotBlank()) {
                                if (notif.referenceId.startsWith("ANB")) {
                                    viewModel.navigateTo(Screen.TrackRequest(notif.referenceId))
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnread) GoldLight.copy(alpha = 0.35f) else SurfaceWhite
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 3.dp else 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isUnread) GoldAccent else NavyContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (notif.type) {
                                    "status_update" -> Icons.Default.Sync
                                    "job" -> Icons.Default.WorkOutline
                                    "message" -> Icons.Default.Chat
                                    else -> Icons.Default.Campaign
                                },
                                contentDescription = null,
                                tint = if (isUnread) NavyDark else NavyPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark
                                    )
                                )
                                if (isUnread) {
                                    Surface(
                                        color = Color(0xFFDC2626),
                                        shape = CircleShape,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notif.body,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextDark, lineHeight = 18.sp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}
