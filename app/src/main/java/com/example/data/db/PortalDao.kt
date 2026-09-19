package com.example.data.db

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PortalDao {

    // --- Requests ---
    @Query("SELECT * FROM requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<CitizenRequestEntity>>

    @Query("SELECT * FROM requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): CitizenRequestEntity?

    @Query("SELECT * FROM requests WHERE requestNumber = :number AND phone = :phone LIMIT 1")
    suspend fun findRequestByNumberAndPhone(number: String, phone: String): CitizenRequestEntity?

    @Query("SELECT * FROM requests WHERE requestNumber = :number LIMIT 1")
    suspend fun findRequestByNumber(number: String): CitizenRequestEntity?

    @Query("SELECT * FROM requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRequestsByUserId(userId: String): Flow<List<CitizenRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: CitizenRequestEntity)

    @Update
    suspend fun updateRequest(request: CitizenRequestEntity)

    @Query("DELETE FROM requests WHERE id = :id")
    suspend fun deleteRequestById(id: String)

    @Query("SELECT COUNT(*) FROM requests")
    fun getRequestsCount(): Flow<Int>

    // --- Complaints ---
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id LIMIT 1")
    suspend fun getComplaintById(id: String): ComplaintEntity?

    @Query("SELECT * FROM complaints WHERE complaintNumber = :number LIMIT 1")
    suspend fun findComplaintByNumber(number: String): ComplaintEntity?

    @Query("SELECT * FROM complaints WHERE userId = :userId ORDER BY createdAt DESC")
    fun getComplaintsByUserId(userId: String): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    @Query("DELETE FROM complaints WHERE id = :id")
    suspend fun deleteComplaintById(id: String)

    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ProblemReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): ProblemReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ProblemReportEntity)

    @Update
    suspend fun updateReport(report: ProblemReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    // --- Users ---
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    // --- Jobs & Applications ---
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJobById(id: String)

    @Query("SELECT * FROM job_applications ORDER BY createdAt DESC")
    fun getAllJobApplications(): Flow<List<JobApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobApplication(app: JobApplicationEntity)

    // --- Social Services ---
    @Query("SELECT * FROM social_services")
    fun getAllSocialServices(): Flow<List<SocialServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocialService(service: SocialServiceEntity)

    @Update
    suspend fun updateSocialService(service: SocialServiceEntity)

    @Query("DELETE FROM social_services WHERE id = :id")
    suspend fun deleteSocialService(id: String)

    // --- News ---
    @Query("SELECT * FROM news ORDER BY createdAt DESC")
    fun getAllNews(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(news: NewsEntity)

    @Update
    suspend fun updateNews(news: NewsEntity)

    @Query("DELETE FROM news WHERE id = :id")
    suspend fun deleteNewsById(id: String)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE targetUserId = :userId OR targetUserId = 'all' ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE (targetUserId = :userId OR targetUserId = 'all') AND isRead = 0")
    fun getUnreadNotificationsCount(userId: String): Flow<Int>

    // --- Messages ---
    @Query("SELECT * FROM messages ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMessagesByUserId(userId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 'portal_settings' LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAppSettings(settings: AppSettingsEntity)
}
