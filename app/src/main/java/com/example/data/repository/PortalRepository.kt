package com.example.data.repository

import android.net.Uri
import com.example.data.db.PortalDao
import com.example.data.firebase.FirebasePortalManager
import com.example.data.models.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Unified Repository managing local cache (Room) and cloud sync (Cloud Firestore, Auth, Storage).
 * Maintains offline-first reliability while syncing all 13 collections directly to Firestore.
 */
class PortalRepository(
    private val portalDao: PortalDao,
    val firebaseManager: FirebasePortalManager = FirebasePortalManager()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // Primary Owner & Administrator
    val primaryAdminEmail = FirebasePortalManager.PRIMARY_ADMIN_EMAIL

    init {
        // Automatically sync initial cloud listeners if Firebase is configured
        if (firebaseManager.isFirebaseReady) {
            scope.launch {
                firebaseManager.ensureAdminRegistered(primaryAdminEmail)
            }
        }
    }

    // --- Authentication & Verification ---
    val currentFirebaseUser: FirebaseUser?
        get() = firebaseManager.currentFirebaseUser

    suspend fun verifyIsAdmin(email: String): Boolean {
        return firebaseManager.isUserAdmin(email)
    }

    suspend fun loginAdmin(email: String, pass: String): Result<FirebaseUser?> {
        return firebaseManager.signInWithEmail(email, pass)
    }

    suspend fun signUpCitizenAccount(email: String, pass: String): Result<FirebaseUser?> {
        return firebaseManager.signUpCitizen(email, pass)
    }

    fun logout() {
        firebaseManager.signOut()
    }

    // --- File & Document Upload (Firebase Storage) ---
    suspend fun uploadDocument(userId: String, folder: String, name: String, uri: Uri): Result<String> {
        return firebaseManager.uploadFileToStorage(userId, folder, name, uri)
    }

    // --- 1. Requests ---
    val allRequests: Flow<List<CitizenRequestEntity>> = portalDao.getAllRequests()

    fun getRequestsByUser(userId: String): Flow<List<CitizenRequestEntity>> =
        portalDao.getRequestsByUserId(userId)

    suspend fun getRequestById(id: String): CitizenRequestEntity? =
        portalDao.getRequestById(id)

    suspend fun findRequestByNumberAndPhone(number: String, phone: String): CitizenRequestEntity? =
        portalDao.findRequestByNumberAndPhone(number.trim(), phone.trim())

    suspend fun findRequestByNumber(number: String): CitizenRequestEntity? =
        portalDao.findRequestByNumber(number.trim())

    suspend fun submitRequest(
        userId: String,
        fullName: String,
        phone: String,
        email: String,
        province: String,
        district: String,
        subdistrict: String,
        area: String,
        address: String,
        requestType: String,
        description: String,
        attachments: String
    ): CitizenRequestEntity {
        val count = (portalDao.getAllRequests().firstOrNull()?.size ?: 0) + 1
        val reqNumber = String.format(Locale.US, "ANB-2026-%06d", count)
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(now))
        val initialTimeline = "✓ تم استلام الطلب|$formattedDate|تم تسجيل طلبكم بنجاح في المنظومة وإحالته لفرز البيانات"

        val entity = CitizenRequestEntity(
            id = id,
            requestNumber = reqNumber,
            userId = userId,
            fullName = fullName,
            phone = phone,
            email = email,
            province = province,
            district = district,
            subdistrict = subdistrict,
            area = area,
            address = address,
            requestType = requestType,
            description = description,
            attachments = attachments,
            status = "تم استلام الطلب",
            createdAt = now,
            updatedAt = now,
            timeline = initialTimeline,
            adminNotes = ""
        )
        portalDao.insertRequest(entity)

        // Sync to Cloud Firestore
        firebaseManager.saveRequest(entity)

        // In-app & cloud notification
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = userId,
            title = "تم تسجيل طلبك رقم $reqNumber",
            body = "تم استقبال طلبك ($requestType) وهو الآن قيد المتابعة لدى الإدارة.",
            type = "status_update",
            referenceId = reqNumber
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)

        return entity
    }

    suspend fun updateRequestStatus(
        requestId: String,
        newStatus: String,
        note: String,
        adminEmail: String
    ) {
        val request = portalDao.getRequestById(requestId) ?: return
        val now = System.currentTimeMillis()
        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(now))
        val newStep = "● $newStatus|$formattedDate|${if (note.isNotBlank()) note else "تم تحديث الحالة بواسطة فريق المتابعة"}"
        val updatedTimeline = if (request.timeline.isBlank()) newStep else "${request.timeline}\n$newStep"

        val updated = request.copy(
            status = newStatus,
            updatedAt = now,
            timeline = updatedTimeline,
            adminNotes = if (note.isNotBlank()) "${request.adminNotes}\n[$formattedDate] $note".trim() else request.adminNotes
        )
        portalDao.updateRequest(updated)

        // Sync updates to Cloud Firestore
        firebaseManager.updateRequestStatus(requestId, newStatus, note, adminEmail, updatedTimeline)

        // Audit Log
        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "تحديث حالة الطلب",
            entityType = "request",
            entityId = request.requestNumber,
            details = "تغيير الحالة إلى: $newStatus | ملاحظات: $note"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)

        // Citizen Notification
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = request.userId,
            title = "تحديث على طلبك: ${request.requestNumber}",
            body = "أصبح طلبك الآن في حالة: $newStatus",
            type = "status_update",
            referenceId = request.requestNumber
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)
    }

    suspend fun deleteRequest(id: String, adminEmail: String) {
        val req = portalDao.getRequestById(id)
        portalDao.deleteRequestById(id)
        if (req != null) {
            val audit = AuditLogEntity(
                id = UUID.randomUUID().toString(),
                actorEmail = adminEmail,
                action = "حذف طلب",
                entityType = "request",
                entityId = req.requestNumber,
                details = "تم حذف الطلب المقدم من ${req.fullName}"
            )
            portalDao.insertAuditLog(audit)
            firebaseManager.recordAuditLog(audit)
        }
    }

    // --- 2. Complaints ---
    val allComplaints: Flow<List<ComplaintEntity>> = portalDao.getAllComplaints()

    fun getComplaintsByUser(userId: String): Flow<List<ComplaintEntity>> =
        portalDao.getComplaintsByUserId(userId)

    suspend fun submitComplaint(
        userId: String,
        fullName: String,
        phone: String,
        province: String,
        district: String,
        subdistrict: String,
        complaintType: String,
        description: String,
        attachments: String
    ): ComplaintEntity {
        val count = (portalDao.getAllComplaints().firstOrNull()?.size ?: 0) + 1
        val cmpNumber = String.format(Locale.US, "CMP-2026-%06d", count)
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = ComplaintEntity(
            id = id,
            complaintNumber = cmpNumber,
            userId = userId,
            fullName = fullName,
            phone = phone,
            province = province,
            district = district,
            subdistrict = subdistrict,
            complaintType = complaintType,
            description = description,
            attachments = attachments,
            status = "تم استلام الشكوى",
            createdAt = now,
            updatedAt = now
        )
        portalDao.insertComplaint(entity)
        firebaseManager.saveComplaint(entity)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = userId,
            title = "تم تسجيل شكواك رقم $cmpNumber",
            body = "تم استلام الشكوى حول ($complaintType) وأُحيلت لقسم الشكاوى للمتابعة.",
            type = "status_update",
            referenceId = cmpNumber
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)

        return entity
    }

    suspend fun updateComplaintStatus(
        complaintId: String,
        status: String,
        note: String,
        adminEmail: String
    ) {
        val cmp = portalDao.getComplaintById(complaintId) ?: return
        val now = System.currentTimeMillis()
        val updated = cmp.copy(
            status = status,
            updatedAt = now,
            adminNotes = if (note.isNotBlank()) "${cmp.adminNotes}\n$note".trim() else cmp.adminNotes
        )
        portalDao.updateComplaint(updated)
        firebaseManager.updateComplaintStatus(complaintId, status, note, adminEmail)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "تحديث حالة شكوى",
            entityType = "complaint",
            entityId = cmp.complaintNumber,
            details = "الحالة الجديدة: $status | $note"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = cmp.userId,
            title = "تحديث على شكواك: ${cmp.complaintNumber}",
            body = "تم تحديث حالة الشكوى إلى: $status",
            type = "status_update",
            referenceId = cmp.complaintNumber
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)
    }

    // --- 3. Reports ---
    val allReports: Flow<List<ProblemReportEntity>> = portalDao.getAllReports()

    suspend fun submitReport(
        userId: String,
        problemType: String,
        province: String,
        district: String,
        subdistrict: String,
        address: String,
        description: String,
        phone: String
    ): ProblemReportEntity {
        val count = (portalDao.getAllReports().firstOrNull()?.size ?: 0) + 1
        val repNumber = String.format(Locale.US, "REP-2026-%06d", count)
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = ProblemReportEntity(
            id = id,
            reportNumber = repNumber,
            userId = userId,
            problemType = problemType,
            province = province,
            district = district,
            subdistrict = subdistrict,
            address = address,
            description = description,
            phone = phone,
            status = "بلاغ جديد",
            createdAt = now,
            updatedAt = now
        )
        portalDao.insertReport(entity)
        firebaseManager.saveReport(entity)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = userId,
            title = "تم تسجيل بلاغك رقم $repNumber",
            body = "تم استلام البلاغ الميداني عن ($problemType) بنجاح.",
            type = "status_update",
            referenceId = repNumber
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)

        return entity
    }

    suspend fun updateReportStatus(reportId: String, status: String, adminEmail: String) {
        val rep = portalDao.getReportById(reportId) ?: return
        val updated = rep.copy(
            status = status,
            updatedAt = System.currentTimeMillis()
        )
        portalDao.updateReport(updated)
        firebaseManager.updateReportStatus(reportId, status, adminEmail)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "تحديث حالة بلاغ ميداني",
            entityType = "report",
            entityId = rep.reportNumber,
            details = "الحالة المحدثة: $status"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = rep.userId,
            title = "تحديث على بلاغك: ${rep.reportNumber}",
            body = "تم تحديث حالة البلاغ إلى: $status",
            type = "status_update",
            referenceId = rep.reportNumber
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)
    }

    // --- 4. Users ---
    val allUsers: Flow<List<UserEntity>> = portalDao.getAllUsers()

    suspend fun getUserById(id: String): UserEntity? = portalDao.getUserById(id)

    suspend fun saveUser(user: UserEntity) {
        portalDao.insertUser(user)
        firebaseManager.saveUser(user)
    }

    suspend fun registerCitizen(fullName: String, phone: String, email: String, province: String): UserEntity {
        val id = UUID.randomUUID().toString()
        val user = UserEntity(
            id = id,
            fullName = fullName,
            phone = phone,
            email = email,
            province = province,
            role = "citizen"
        )
        portalDao.insertUser(user)
        firebaseManager.saveUser(user)
        return user
    }

    // --- 5. Jobs & Applications ---
    val allJobs: Flow<List<JobEntity>> = portalDao.getAllJobs()
    val allJobApplications: Flow<List<JobApplicationEntity>> = portalDao.getAllJobApplications()

    suspend fun postJob(job: JobEntity, adminEmail: String) {
        portalDao.insertJob(job)
        firebaseManager.saveJob(job)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "نشر فرصة عمل جديدة",
            entityType = "job",
            entityId = job.id,
            details = "المسمى: ${job.title} | الجهة: ${job.organization}"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = "all",
            title = "فرصة عمل جديدة: ${job.title}",
            body = "تم الإعلان عن فرصة عمل جديدة لدى (${job.organization}) في ${job.province}.",
            type = "job",
            referenceId = job.id
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)
    }

    suspend fun addJob(job: JobEntity, adminEmail: String) = postJob(job, adminEmail)

    suspend fun deleteJob(jobId: String, adminEmail: String) {
        portalDao.deleteJobById(jobId)
        firebaseManager.deleteJob(jobId)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "حذف فرصة عمل",
            entityType = "job",
            entityId = jobId,
            details = "تم حذف فرصة العمل من المنظومة"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)
    }

    suspend fun submitJobApplication(
        jobId: String,
        jobTitle: String,
        applicantName: String,
        applicantPhone: String,
        applicantEmail: String,
        education: String,
        notes: String
    ): JobApplicationEntity {
        val app = JobApplicationEntity(
            id = UUID.randomUUID().toString(),
            jobId = jobId,
            jobTitle = jobTitle,
            applicantName = applicantName,
            applicantPhone = applicantPhone,
            applicantEmail = applicantEmail,
            education = education,
            notes = notes
        )
        portalDao.insertJobApplication(app)
        firebaseManager.saveJobApplication(app)
        return app
    }

    // --- 6. Social Services ---
    val allSocialServices: Flow<List<SocialServiceEntity>> = portalDao.getAllSocialServices()

    suspend fun saveSocialService(service: SocialServiceEntity, adminEmail: String) {
        portalDao.insertSocialService(service)
        firebaseManager.saveSocialService(service)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "إضافة برنامج رعاية ومساعدات",
            entityType = "social_service",
            entityId = service.id,
            details = "اسم البرنامج: ${service.name}"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)
    }

    suspend fun addSocialService(service: SocialServiceEntity, adminEmail: String) = saveSocialService(service, adminEmail)

    suspend fun deleteSocialService(id: String, adminEmail: String) {
        portalDao.deleteSocialService(id)
        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "حذف برنامج رعاية ومساعدات",
            entityType = "social_service",
            entityId = id,
            details = "تم حذف الخدمة الاجتماعية من المنظومة"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)
    }

    // --- 7. News & Announcements ---
    val allNews: Flow<List<NewsEntity>> = portalDao.getAllNews()

    suspend fun publishNews(news: NewsEntity, adminEmail: String) {
        portalDao.insertNews(news)
        firebaseManager.saveNews(news)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "نشر بيان إخباري أو إعلان رسمي",
            entityType = "news",
            entityId = news.id,
            details = "العنوان: ${news.title}"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = "all",
            title = "بيان رسمي جديد",
            body = news.title,
            type = "announcement",
            referenceId = news.id
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)
    }

    suspend fun addNews(news: NewsEntity, adminEmail: String) = publishNews(news, adminEmail)

    suspend fun deleteNews(newsId: String, adminEmail: String) {
        portalDao.deleteNewsById(newsId)
        firebaseManager.deleteNews(newsId)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "حذف خبر أو بيان",
            entityType = "news",
            entityId = newsId,
            details = "تم حذف البيان من المنصة"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)
    }

    // --- 8. Notifications ---
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> =
        portalDao.getNotificationsForUser(userId)

    fun getUnreadNotificationsCount(userId: String): Flow<Int> =
        portalDao.getUnreadNotificationsCount(userId)

    suspend fun markNotificationAsRead(id: String) {
        portalDao.markNotificationAsRead(id)
    }

    suspend fun sendNotification(notification: NotificationEntity, adminEmail: String) {
        portalDao.insertNotification(notification)
        firebaseManager.sendNotification(notification)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "بث إشعار إداري",
            entityType = "notification",
            entityId = notification.id,
            details = "العنوان: ${notification.title}"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)
    }

    suspend fun broadcastNotification(title: String, body: String, type: String, adminEmail: String) {
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = "all",
            title = title,
            body = body,
            type = type
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "بث إشعار عام للمواطنين",
            entityType = "notification",
            entityId = notif.id,
            details = "العنوان: $title"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)
    }

    // --- 9. Messages ---
    val allMessages: Flow<List<MessageEntity>> = portalDao.getAllMessages()

    suspend fun sendMessage(
        userId: String,
        senderName: String,
        senderPhone: String,
        requestNumber: String,
        content: String
    ): MessageEntity {
        val msg = MessageEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            senderName = senderName,
            senderPhone = senderPhone,
            requestNumber = requestNumber,
            content = content,
            status = "جديدة"
        )
        portalDao.insertMessage(msg)
        firebaseManager.saveMessage(msg)
        return msg
    }

    suspend fun replyToMessage(messageId: String, reply: String, adminEmail: String) {
        val messages = portalDao.getAllMessages().firstOrNull() ?: emptyList()
        val msg = messages.find { it.id == messageId } ?: return
        val updated = msg.copy(
            reply = reply,
            status = "تم الرد",
            updatedAt = System.currentTimeMillis()
        )
        portalDao.updateMessage(updated)
        firebaseManager.replyToMessage(messageId, reply, adminEmail)

        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "الرد على استفسار مواطن",
            entityType = "message",
            entityId = messageId,
            details = "الرد على رسالة: ${msg.senderName} (${msg.senderPhone})"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            targetUserId = msg.userId,
            title = "رد جديد من فريق المتابعة",
            body = "قام فريق المتابعة بالرد على استفسارك: $reply",
            type = "message",
            referenceId = messageId
        )
        portalDao.insertNotification(notif)
        firebaseManager.sendNotification(notif)
    }

    // --- 10. Audit Logs ---
    val allAuditLogs: Flow<List<AuditLogEntity>> = portalDao.getAllAuditLogs()

    // --- 11. App Settings ---
    val appSettings: Flow<AppSettingsEntity?> = portalDao.getAppSettings()

    suspend fun updateSettings(settings: AppSettingsEntity, adminEmail: String) {
        portalDao.updateAppSettings(settings)
        val audit = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            actorEmail = adminEmail,
            action = "تحديث إعدادات المنصة",
            entityType = "settings",
            entityId = settings.id,
            details = "تم تعديل بيانات الاتصال والهيدر والإشعارات العامة"
        )
        portalDao.insertAuditLog(audit)
        firebaseManager.recordAuditLog(audit)
    }
}
