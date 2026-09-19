package com.example.data.firebase

import android.net.Uri
import android.util.Log
import com.example.data.models.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Enterprise Firebase Manager for Iraq Citizen Portal (سامي الفلاحي)
 * Implements real-time synchronization, Firebase Auth, Cloud Firestore (13 collections),
 * Firebase Storage, and graceful fallback when offline or before google-services.json setup.
 */
class FirebasePortalManager {

    companion object {
        const val PRIMARY_ADMIN_EMAIL = "abraheem630@gmail.com"
        private const val TAG = "FirebasePortalManager"

        // Collections specified in architecture:
        const val COL_USERS = "users"
        const val COL_REQUESTS = "requests"
        const val COL_COMPLAINTS = "complaints"
        const val COL_REPORTS = "reports"
        const val COL_JOBS = "jobs"
        const val COL_JOB_APPLICATIONS = "jobApplications"
        const val COL_SOCIAL_SERVICES = "socialServices"
        const val COL_NEWS = "news"
        const val COL_NOTIFICATIONS = "notifications"
        const val COL_MESSAGES = "messages"
        const val COL_AUDIT_LOGS = "auditLogs"
        const val COL_SETTINGS = "settings"
        const val COL_ADMIN_USERS = "adminUsers"
    }

    /**
     * Safely checks if Firebase is initialized in the runtime
     */
    val isFirebaseReady: Boolean
        get() = try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    private val auth: FirebaseAuth?
        get() = if (isFirebaseReady) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseReady) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null

    private val storage: FirebaseStorage?
        get() = if (isFirebaseReady) {
            try { FirebaseStorage.getInstance() } catch (e: Exception) { null }
        } else null

    val currentFirebaseUser: FirebaseUser?
        get() = auth?.currentUser

    // ----------------------------------------------------
    // 1. Authentication & Role Validation
    // ----------------------------------------------------

    suspend fun isUserAdmin(email: String): Boolean {
        if (email.trim().equals(PRIMARY_ADMIN_EMAIL, ignoreCase = true)) return true
        val db = firestore ?: return false
        return try {
            val doc = db.collection(COL_ADMIN_USERS).document(email.trim().lowercase()).get().await()
            doc.exists() && (doc.getBoolean("active") ?: false)
        } catch (e: Exception) {
            Log.w(TAG, "Failed checking admin in Firestore: ${e.message}")
            false
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser?> {
        val fbAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth ليس مهيئاً بعد. يرجى إضافة google-services.json"))
        return try {
            val res = fbAuth.signInWithEmailAndPassword(email.trim(), pass).await()
            Result.success(res.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpCitizen(email: String, pass: String): Result<FirebaseUser?> {
        val fbAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth ليس مهيئاً بعد."))
        return try {
            val res = fbAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
            Result.success(res.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureAdminRegistered(email: String = PRIMARY_ADMIN_EMAIL) {
        val db = firestore ?: return
        try {
            val data = mapOf(
                "email" to email.trim().lowercase(),
                "role" to "owner",
                "active" to true,
                "assignedAt" to System.currentTimeMillis()
            )
            db.collection(COL_ADMIN_USERS).document(email.trim().lowercase())
                .set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error registering admin in adminUsers: ${e.message}")
        }
    }

    fun signOut() {
        auth?.signOut()
    }

    // ----------------------------------------------------
    // 2. Cloud Firestore Real-time Streams
    // ----------------------------------------------------

    fun streamRequests(): Flow<List<CitizenRequestEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COL_REQUESTS)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) {
                        return@addSnapshotListener
                    }
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(CitizenRequestEntity::class.java)
                    }.sortedByDescending { it.createdAt }
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming requests: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    fun streamComplaints(): Flow<List<ComplaintEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COL_COMPLAINTS)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ComplaintEntity::class.java)
                    }.sortedByDescending { it.createdAt }
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming complaints: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    fun streamReports(): Flow<List<ProblemReportEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COL_REPORTS)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ProblemReportEntity::class.java)
                    }.sortedByDescending { it.createdAt }
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming reports: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    fun streamNews(): Flow<List<NewsEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COL_NEWS)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(NewsEntity::class.java)
                    }.sortedByDescending { it.createdAt }
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming news: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    fun streamJobs(): Flow<List<JobEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COL_JOBS)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(JobEntity::class.java)
                    }.sortedByDescending { it.createdAt }
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming jobs: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    fun streamMessages(): Flow<List<MessageEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = db.collection(COL_MESSAGES)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MessageEntity::class.java)
                    }.sortedByDescending { it.createdAt }
                    trySend(list)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming messages: ${e.message}")
        }

        awaitClose { listener?.remove() }
    }

    // ----------------------------------------------------
    // 3. Document Mutations (Writes to Firestore)
    // ----------------------------------------------------

    suspend fun saveUser(user: UserEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_USERS).document(user.id).set(user, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user in Firestore: ${e.message}")
        }
    }

    suspend fun saveRequest(request: CitizenRequestEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_REQUESTS).document(request.id).set(request, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save request in Firestore: ${e.message}")
        }
    }

    suspend fun updateRequestStatus(
        requestId: String,
        newStatus: String,
        adminNotes: String,
        actorEmail: String,
        updatedTimeline: String
    ) {
        val db = firestore ?: return
        try {
            val updates = mapOf(
                "status" to newStatus,
                "adminNotes" to adminNotes,
                "updatedAt" to System.currentTimeMillis(),
                "timeline" to updatedTimeline
            )
            db.collection(COL_REQUESTS).document(requestId).update(updates).await()

            // Record in audit logs
            recordAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    actorEmail = actorEmail,
                    action = "تحديث حالة الطلب إلى: $newStatus",
                    entityType = "Request",
                    entityId = requestId,
                    details = "ملاحظات الإدارة: $adminNotes"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update request status in Firestore: ${e.message}")
        }
    }

    suspend fun saveComplaint(complaint: ComplaintEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_COMPLAINTS).document(complaint.id).set(complaint, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save complaint in Firestore: ${e.message}")
        }
    }

    suspend fun updateComplaintStatus(complaintId: String, newStatus: String, adminNotes: String, actorEmail: String) {
        val db = firestore ?: return
        try {
            val updates = mapOf(
                "status" to newStatus,
                "adminNotes" to adminNotes,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COL_COMPLAINTS).document(complaintId).update(updates).await()

            recordAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    actorEmail = actorEmail,
                    action = "تحديث حالة الشكوى إلى: $newStatus",
                    entityType = "Complaint",
                    entityId = complaintId,
                    details = adminNotes
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update complaint in Firestore: ${e.message}")
        }
    }

    suspend fun saveReport(report: ProblemReportEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_REPORTS).document(report.id).set(report, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save report in Firestore: ${e.message}")
        }
    }

    suspend fun updateReportStatus(reportId: String, newStatus: String, actorEmail: String) {
        val db = firestore ?: return
        try {
            val updates = mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COL_REPORTS).document(reportId).update(updates).await()

            recordAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    actorEmail = actorEmail,
                    action = "تحديث حالة البلاغ إلى: $newStatus",
                    entityType = "ProblemReport",
                    entityId = reportId,
                    details = "الحالة المحدثة: $newStatus"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update report in Firestore: ${e.message}")
        }
    }

    suspend fun saveJob(job: JobEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_JOBS).document(job.id).set(job, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save job in Firestore: ${e.message}")
        }
    }

    suspend fun deleteJob(jobId: String) {
        val db = firestore ?: return
        try {
            db.collection(COL_JOBS).document(jobId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete job in Firestore: ${e.message}")
        }
    }

    suspend fun saveJobApplication(app: JobApplicationEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_JOB_APPLICATIONS).document(app.id).set(app, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save jobApplication in Firestore: ${e.message}")
        }
    }

    suspend fun saveSocialService(service: SocialServiceEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_SOCIAL_SERVICES).document(service.id).set(service, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save socialService in Firestore: ${e.message}")
        }
    }

    suspend fun saveNews(news: NewsEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_NEWS).document(news.id).set(news, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save news in Firestore: ${e.message}")
        }
    }

    suspend fun deleteNews(newsId: String) {
        val db = firestore ?: return
        try {
            db.collection(COL_NEWS).document(newsId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete news in Firestore: ${e.message}")
        }
    }

    suspend fun sendNotification(notification: NotificationEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_NOTIFICATIONS).document(notification.id).set(notification, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification in Firestore: ${e.message}")
        }
    }

    suspend fun saveMessage(msg: MessageEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_MESSAGES).document(msg.id).set(msg, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save message in Firestore: ${e.message}")
        }
    }

    suspend fun replyToMessage(msgId: String, reply: String, actorEmail: String) {
        val db = firestore ?: return
        try {
            val updates = mapOf(
                "reply" to reply,
                "status" to "تم الرد",
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COL_MESSAGES).document(msgId).update(updates).await()

            recordAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    actorEmail = actorEmail,
                    action = "الرد على استفسار المواطن",
                    entityType = "Message",
                    entityId = msgId,
                    details = "نص الرد: $reply"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reply to message in Firestore: ${e.message}")
        }
    }

    suspend fun recordAuditLog(log: AuditLogEntity) {
        val db = firestore ?: return
        try {
            db.collection(COL_AUDIT_LOGS).document(log.id).set(log).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record audit log in Firestore: ${e.message}")
        }
    }

    // ----------------------------------------------------
    // 4. Firebase Storage (Private User Documents Upload)
    // ----------------------------------------------------

    /**
     * Uploads a citizen's document or problem image securely into Firebase Storage.
     * Path structure: uploads/{userId}/{folderName}/{uuid}_{filename}
     * Complies with storage security rules (no public world access).
     */
    suspend fun uploadFileToStorage(
        userId: String,
        folderName: String,
        fileName: String,
        fileUri: Uri
    ): Result<String> {
        val fbStorage = storage ?: return Result.failure(IllegalStateException("Firebase Storage ليس مهيئاً بعد."))
        return try {
            val safeUserId = if (userId.isNotBlank()) userId else "guest"
            val uniquePath = "uploads/$safeUserId/$folderName/${UUID.randomUUID()}_$fileName"
            val storageRef = fbStorage.reference.child(uniquePath)

            storageRef.putFile(fileUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file to Firebase Storage: ${e.message}")
            Result.failure(e)
        }
    }
}
