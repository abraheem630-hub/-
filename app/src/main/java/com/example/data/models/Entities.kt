package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val province: String,
    val role: String = "citizen", // "citizen" or "admin"
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "requests")
data class CitizenRequestEntity(
    @PrimaryKey val id: String,
    val requestNumber: String, // e.g. ANB-2026-000001
    val userId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val province: String,
    val district: String,
    val subdistrict: String,
    val area: String,
    val address: String,
    val requestType: String,
    val description: String,
    val attachments: String, // comma-separated filenames
    val status: String = "تم استلام الطلب",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val timeline: String = "", // formatted JSON or delimiter-separated timeline steps
    val adminNotes: String = ""
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val complaintNumber: String, // CMP-2026-000001
    val userId: String,
    val fullName: String,
    val phone: String,
    val province: String,
    val district: String,
    val subdistrict: String,
    val complaintType: String,
    val description: String,
    val attachments: String,
    val status: String = "تم استلام الشكوى",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val adminNotes: String = ""
)

@Entity(tableName = "reports")
data class ProblemReportEntity(
    @PrimaryKey val id: String,
    val reportNumber: String, // REP-2026-000001
    val userId: String,
    val problemType: String,
    val province: String,
    val district: String,
    val subdistrict: String,
    val address: String,
    val description: String,
    val phone: String,
    val status: String = "بلاغ جديد",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val organization: String,
    val province: String,
    val location: String,
    val jobType: String, // دوام كامل, عقد, تدريب وتأهيل
    val description: String,
    val qualifications: String = "",
    val conditions: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val jobTitle: String,
    val applicantName: String,
    val applicantPhone: String,
    val applicantEmail: String,
    val education: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "social_services")
data class SocialServiceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val targetGroups: String = "",
    val conditions: String = "",
    val requiredDocs: String = "",
    val applyMethod: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val description: String,
    val content: String,
    val imageUrl: String = "",
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val targetUserId: String, // "all" or specific user
    val title: String,
    val body: String,
    val type: String, // "status_update", "announcement", "job", "message", "general"
    val referenceId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val senderName: String,
    val senderPhone: String,
    val requestNumber: String = "",
    val content: String,
    val reply: String = "",
    val status: String = "جديدة", // جديدة, تم الرد, مغلقة
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val actorEmail: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: String = "portal_settings",
    val appName: String = "سامي الفلاحي | خدمات المواطن",
    val heroTitle: String = "سامي الفلاحي",
    val heroSubtitle: String = "خدمة أقرب للمواطن",
    val contactPhone: String = "07700000000",
    val contactEmail: String = "info@alfalahi-portal.iq",
    val officeAddress: String = "العراق - مكتب خدمة المواطنين والمتابعة",
    val bannerNotice: String = "أهلاً بكم في المنصة الموحدة لتلقي ومتابعة طلبات وشكاوى المواطنين الكرام"
)
