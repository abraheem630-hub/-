package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PortalDatabase
import com.example.data.models.*
import com.example.data.repository.PortalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen(val title: String) {
    object Home : Screen("الرئيسية")
    object SubmitRequest : Screen("تقديم طلب")
    data class TrackRequest(val initialNumber: String = "") : Screen("متابعة الطلب")
    object SubmitComplaint : Screen("تقديم شكوى")
    object ReportProblem : Screen("الإبلاغ عن مشكلة")
    object CitizenProfile : Screen("ملف المواطن")
    object JobsAndTraining : Screen("فرص العمل والتدريب")
    object SocialServices : Screen("الخدمات والمساعدات الاجتماعية")
    object NewsAndAnnouncements : Screen("الأخبار والإعلانات")
    object Messages : Screen("التواصل مع فريق المتابعة")
    object AboutApp : Screen("عن التطبيق")

    // Admin Screens
    object AdminLogin : Screen("تسجيل الدخول للإدارة")
    object AdminDashboard : Screen("لوحة تحكم المالك")
    object AdminRequests : Screen("إدارة الطلبات")
    object AdminComplaints : Screen("إدارة الشكاوى")
    object AdminReports : Screen("إدارة البلاغات")
    object AdminCitizens : Screen("إدارة المواطنين")
    object AdminJobs : Screen("إدارة فرص العمل")
    object AdminSocialServices : Screen("إدارة الخدمات الاجتماعية")
    object AdminNews : Screen("إدارة الأخبار")
    object AdminNotifications : Screen("إدارة الإشعارات")
    object AdminMessages : Screen("صندوق الرسائل")
    object AdminAuditLogs : Screen("سجل العمليات")
    object AdminSettings : Screen("إعدادات المنصة")
}

data class RequestTrackingResult(
    val isLoading: Boolean = false,
    val request: CitizenRequestEntity? = null,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)

class PortalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PortalRepository

    init {
        val db = PortalDatabase.getDatabase(application)
        repository = PortalRepository(db.portalDao())
    }

    // --- Navigation & User Session ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenBackStack = mutableListOf<Screen>()

    private val _currentUser = MutableStateFlow<UserEntity?>(
        // Provide a default registered citizen for instant convenience while allowing switching
        UserEntity(
            id = "citizen_sample_1",
            fullName = "حيدر علي الكرخي",
            phone = "07801234567",
            email = "haider@example.iq",
            province = "الأنبار",
            role = "citizen"
        )
    )
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val isAdmin: StateFlow<Boolean> = _currentUser.map { user ->
        user != null && user.email.trim().equals(repository.primaryAdminEmail, ignoreCase = true)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Tracking state
    private val _trackingResult = MutableStateFlow(RequestTrackingResult())
    val trackingResult: StateFlow<RequestTrackingResult> = _trackingResult.asStateFlow()

    // Data streams (Cached eagerly for instantaneous UI responsiveness)
    val allRequests: StateFlow<List<CitizenRequestEntity>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allComplaints: StateFlow<List<ComplaintEntity>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allReports: StateFlow<List<ProblemReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allJobApplications: StateFlow<List<JobApplicationEntity>> = repository.allJobApplications
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allSocialServices: StateFlow<List<SocialServiceEntity>> = repository.allSocialServices
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allNews: StateFlow<List<NewsEntity>> = repository.allNews
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allMessages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val appSettings: StateFlow<AppSettingsEntity> = repository.appSettings
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSettingsEntity()
        )

    val notifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "guest"
        repository.getNotificationsForUser(userId)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unreadNotificationsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        val userId = user?.id ?: "guest"
        repository.getUnreadNotificationsCount(userId)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Filter states for admin requests
    val adminSearchQuery = MutableStateFlow("")
    val adminStatusFilter = MutableStateFlow("الكل")
    val adminProvinceFilter = MutableStateFlow("الكل")
    val adminTypeFilter = MutableStateFlow("الكل")

    val filteredAdminRequests: StateFlow<List<CitizenRequestEntity>> = combine(
        allRequests,
        adminSearchQuery,
        adminStatusFilter,
        adminProvinceFilter,
        adminTypeFilter
    ) { list, query, status, province, type ->
        list.filter { item ->
            val matchQuery = query.isBlank() ||
                    item.requestNumber.contains(query, ignoreCase = true) ||
                    item.fullName.contains(query, ignoreCase = true) ||
                    item.phone.contains(query, ignoreCase = true)
            val matchStatus = status == "الكل" || item.status == status
            val matchProvince = province == "الكل" || item.province == province
            val matchType = type == "الكل" || item.requestType == type
            matchQuery && matchStatus && matchProvince && matchType
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // UI Toast or SnackBar message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Navigation
    fun navigateTo(screen: Screen) {
        // Protect admin routes
        if (screen is Screen.AdminDashboard ||
            screen is Screen.AdminRequests ||
            screen is Screen.AdminComplaints ||
            screen is Screen.AdminReports ||
            screen is Screen.AdminCitizens ||
            screen is Screen.AdminJobs ||
            screen is Screen.AdminSocialServices ||
            screen is Screen.AdminNews ||
            screen is Screen.AdminNotifications ||
            screen is Screen.AdminMessages ||
            screen is Screen.AdminAuditLogs ||
            screen is Screen.AdminSettings
        ) {
            if (!isAdmin.value) {
                _currentScreen.value = Screen.AdminLogin
                showMessage("يرجى تسجيل الدخول بحساب المالك الرئيسي للوصول إلى لوحة التحكم")
                return
            }
        }
        _screenBackStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun goBack(): Boolean {
        return if (_screenBackStack.isNotEmpty()) {
            _currentScreen.value = _screenBackStack.removeAt(_screenBackStack.size - 1)
            true
        } else {
            if (_currentScreen.value != Screen.Home) {
                _currentScreen.value = Screen.Home
                true
            } else {
                false
            }
        }
    }

    // Authentication
    fun loginCitizen(fullName: String, phone: String, email: String, province: String) {
        viewModelScope.launch {
            val user = repository.registerCitizen(fullName, phone, email, province)
            _currentUser.value = user
            showMessage("أهلاً بك يا ${user.fullName}، تم تسجيل الدخول بنجاح")
            _currentScreen.value = Screen.CitizenProfile
        }
    }

    fun loginAdmin(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (email.trim().equals(repository.primaryAdminEmail, ignoreCase = true)) {
                if (pass.length >= 6) {
                    val adminUser = UserEntity(
                        id = "admin_owner",
                        fullName = "سامي الفلاحي - المالك والمشرف العام",
                        phone = "07701234567",
                        email = repository.primaryAdminEmail,
                        province = "بغداد",
                        role = "admin"
                    )
                    _currentUser.value = adminUser
                    showMessage("مرحباً بك في لوحة تحكم المالك")
                    _currentScreen.value = Screen.AdminDashboard
                    onSuccess()
                } else {
                    onError("كلمة المرور يجب أن لا تقل عن 6 أحرف أو أرقام")
                }
            } else {
                onError("البريد الإلكتروني غير مصرح له بالدخول كمالك للوحة التحكم")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Home
        showMessage("تم تسجيل الخروج بنجاح")
    }

    // Actions
    fun submitRequest(
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
        attachments: String,
        onSuccess: (CitizenRequestEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.id ?: "guest_${System.currentTimeMillis()}"
                val req = repository.submitRequest(
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
                    attachments = attachments
                )
                onSuccess(req)
            } catch (e: Exception) {
                onError("تعذر إرسال الطلب حالياً، يرجى التحقق من الاتصال والمحاولة لاحقاً.")
            }
        }
    }

    fun trackRequest(number: String, phone: String) {
        viewModelScope.launch {
            _trackingResult.value = RequestTrackingResult(isLoading = true, hasSearched = true)
            try {
                val req = if (phone.isNotBlank()) {
                    repository.findRequestByNumberAndPhone(number, phone)
                } else {
                    repository.findRequestByNumber(number)
                }
                if (req != null) {
                    _trackingResult.value = RequestTrackingResult(
                        isLoading = false,
                        request = req,
                        hasSearched = true
                    )
                } else {
                    _trackingResult.value = RequestTrackingResult(
                        isLoading = false,
                        errorMessage = "لم يتم العثور على أي طلب مطابق لرقم الطلب ورقم الهاتف المدخلين.",
                        hasSearched = true
                    )
                }
            } catch (e: Exception) {
                _trackingResult.value = RequestTrackingResult(
                    isLoading = false,
                    errorMessage = "حدث خطأ أثناء الاستعلام، يرجى المحاولة مرة أخرى.",
                    hasSearched = true
                )
            }
        }
    }

    fun submitComplaint(
        fullName: String,
        phone: String,
        province: String,
        district: String,
        subdistrict: String,
        complaintType: String,
        description: String,
        attachments: String,
        onSuccess: (ComplaintEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.id ?: "guest_${System.currentTimeMillis()}"
                val cmp = repository.submitComplaint(
                    userId = userId,
                    fullName = fullName,
                    phone = phone,
                    province = province,
                    district = district,
                    subdistrict = subdistrict,
                    complaintType = complaintType,
                    description = description,
                    attachments = attachments
                )
                onSuccess(cmp)
            } catch (e: Exception) {
                onError("تعذر تسجيل الشكوى حالياً، يرجى المحاولة لاحقاً.")
            }
        }
    }

    fun submitReport(
        problemType: String,
        province: String,
        district: String,
        subdistrict: String,
        address: String,
        description: String,
        phone: String,
        onSuccess: (ProblemReportEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.id ?: "guest_${System.currentTimeMillis()}"
                val rep = repository.submitReport(
                    userId = userId,
                    problemType = problemType,
                    province = province,
                    district = district,
                    subdistrict = subdistrict,
                    address = address,
                    description = description,
                    phone = phone
                )
                onSuccess(rep)
            } catch (e: Exception) {
                onError("تعذر إرسال البلاغ حالياً.")
            }
        }
    }

    fun submitJobApplication(
        jobId: String,
        jobTitle: String,
        name: String,
        phone: String,
        email: String,
        education: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.submitJobApplication(jobId, jobTitle, name, phone, email, education, notes)
            showMessage("تم استلام طلب التقديم بنجاح وحفظه في المنظومة")
            onSuccess()
        }
    }

    fun sendMessage(
        name: String,
        phone: String,
        requestNumber: String,
        content: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val userId = _currentUser.value?.id ?: "guest_${System.currentTimeMillis()}"
            repository.sendMessage(userId, name, phone, requestNumber, content)
            showMessage("تم إرسال رسالتك إلى فريق المتابعة بنجاح")
            onSuccess()
        }
    }

    // Admin Operations
    fun adminUpdateRequestStatus(requestId: String, status: String, note: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.updateRequestStatus(requestId, status, note, adminEmail)
            showMessage("تم تحديث حالة الطلب وتسجيل العملية في سجل الرقابة")
        }
    }

    fun adminDeleteRequest(requestId: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.deleteRequest(requestId, adminEmail)
            showMessage("تم حذف الطلب بنجاح")
        }
    }

    fun adminUpdateComplaintStatus(complaintId: String, status: String, note: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.updateComplaintStatus(complaintId, status, note, adminEmail)
            showMessage("تم تحديث حالة الشكوى بنجاح")
        }
    }

    fun adminUpdateReportStatus(reportId: String, status: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.updateReportStatus(reportId, status, adminEmail)
            showMessage("تم تحديث حالة البلاغ")
        }
    }

    fun adminAddJob(job: JobEntity) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.addJob(job, adminEmail)
            showMessage("تم نشر فرصة العمل بنجاح")
        }
    }

    fun adminDeleteJob(jobId: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.deleteJob(jobId, adminEmail)
            showMessage("تم حذف فرصة العمل")
        }
    }

    fun adminAddNews(news: NewsEntity) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.addNews(news, adminEmail)
            showMessage("تم نشر الخبر بنجاح")
        }
    }

    fun adminDeleteNews(newsId: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.deleteNews(newsId, adminEmail)
            showMessage("تم حذف الخبر")
        }
    }

    fun adminAddSocialService(service: SocialServiceEntity) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.addSocialService(service, adminEmail)
            showMessage("تمت إضافة الخدمة الاجتماعية بنجاح")
        }
    }

    fun adminDeleteSocialService(id: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.deleteSocialService(id, adminEmail)
            showMessage("تم حذف الخدمة الاجتماعية")
        }
    }

    fun adminReplyMessage(messageId: String, reply: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.replyToMessage(messageId, reply, adminEmail)
            showMessage("تم إرسال الرد للمواطن وإشعاره")
        }
    }

    fun adminSendNotification(title: String, body: String, target: String) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.sendNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    targetUserId = target,
                    title = title,
                    body = body,
                    type = "announcement"
                ),
                adminEmail
            )
            showMessage("تم بث الإشعار بنجاح")
        }
    }

    fun adminUpdateSettings(settings: AppSettingsEntity) {
        viewModelScope.launch {
            val adminEmail = _currentUser.value?.email ?: repository.primaryAdminEmail
            repository.updateSettings(settings, adminEmail)
            showMessage("تم حفظ إعدادات المنصة بنجاح")
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }
}
