package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CitizenRequestEntity::class,
        ComplaintEntity::class,
        ProblemReportEntity::class,
        JobEntity::class,
        JobApplicationEntity::class,
        SocialServiceEntity::class,
        NewsEntity::class,
        NotificationEntity::class,
        MessageEntity::class,
        AuditLogEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PortalDatabase : RoomDatabase() {

    abstract fun portalDao(): PortalDao

    companion object {
        @Volatile
        private var INSTANCE: PortalDatabase? = null

        fun getDatabase(context: Context): PortalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortalDatabase::class.java,
                    "alfalahi_citizen_portal.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial base data asynchronously
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getDatabase(context).portalDao())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: PortalDao) {
            // Seed Owner Admin User
            dao.insertUser(
                UserEntity(
                    id = "admin_owner",
                    fullName = "سامي الفلاحي - الإدارة المركزية",
                    phone = "07701234567",
                    email = "abraheem630@gmail.com",
                    province = "بغداد",
                    role = "admin"
                )
            )

            // Seed App Settings
            dao.updateAppSettings(
                AppSettingsEntity(
                    id = "portal_settings",
                    appName = "سامي الفلاحي | خدمات المواطن",
                    heroTitle = "سامي الفلاحي",
                    heroSubtitle = "خدمة أقرب للمواطن",
                    contactPhone = "07701234567",
                    contactEmail = "abraheem630@gmail.com",
                    officeAddress = "العراق - مكتب المتابعة وخدمة المواطنين",
                    bannerNotice = "نرحب بكم في البوابة الرسمية لتلقي ومتابعة طلبات وشكاوى المواطنين الكرام."
                )
            )

            // Seed Social Services
            dao.insertSocialService(
                SocialServiceEntity(
                    id = "srv_1",
                    name = "دعم الرعاية الاجتماعية للأسر المتعففة",
                    description = "تسهيل ومتابعة شمول العوائل المستحقة براتب شبكة الحماية الاجتماعية بالتنسيق مع الجهات المعنية.",
                    targetGroups = "الأرامل، الأيتام، ذوي الدخل المحدود، كبار السن غير المشمولين براتب تقاعدي",
                    conditions = "أن لا يكون لدى المعيل أي راتب حكومي مسجل، وأن يكون المستفيد مقيماً في المحافظة",
                    requiredDocs = "البطاقة الوطنية الموحدة، بطاقة السكن، شهادة الوفاة إن وجدت، كتاب تأييد سكن",
                    applyMethod = "تقديم طلب عبر قسم الخدمات الاجتماعية في التطبيق أو الحضور لمكتب المتابعة"
                )
            )
            dao.insertSocialService(
                SocialServiceEntity(
                    id = "srv_2",
                    name = "متابعة الحالات الطبية والعلاجية الحرجة",
                    description = "استقبال التقارير الطبية الخاصة بالحالات المستعصية للتنسيق مع اللجان الطبية والمستشفيات التخصصية لتوفير العلاج أو الإخلاء الطبي.",
                    targetGroups = "المرضى من ذوي الأمراض المستعصية وذوي الاحتياجات الخاصة",
                    conditions = "تقرير طبي حديث صادر من مستشفى حكومي معتمد يثبت الحاجة للتدخل العاجل",
                    requiredDocs = "التقارير الطبية الرسمية، هوية الأحوال/البطاقة الوطنية، رقم الهاتف المعتمد",
                    applyMethod = "إرسال طلب عاجل عبر التطبيق مع إرفاق التقرير الطبي بصيغة صورة أو PDF"
                )
            )
            dao.insertSocialService(
                SocialServiceEntity(
                    id = "srv_3",
                    name = "تأهيل وتدريب الشباب الباحثين عن عمل",
                    description = "برامج ودورات تدريبية مهنية وتقنية مجانية لتطوير مهارات الشباب وربطهم بسوق العمل.",
                    targetGroups = "الخريجون والشباب من عمر 18 إلى 35 سنة",
                    conditions = "التفرغ التام خلال فترة الدورة التدريبية والالتزام بساعات الحضور",
                    requiredDocs = "السيرة الذاتية، وثيقة التخرج أو شهادة المرحلة الدراسية، البطاقة الوطنية",
                    applyMethod = "التقديم عبر نافذة فرص العمل والتدريب داخل التطبيق"
                )
            )

            // Seed Job Opportunities
            dao.insertJob(
                JobEntity(
                    id = "job_1",
                    title = "مهندس مدني / مراقب موقع مشاريع",
                    organization = "شركة الرافدين للمقاولات العامة",
                    province = "الأنبار",
                    location = "الرمادي - الفلوجة",
                    jobType = "دوام كامل",
                    description = "الإشراف الميداني على تنفيذ مشاريع تأهيل الطرق والمباني الخدمية والتأكد من مطابقة المواصفات الهندسية.",
                    qualifications = "بكالوريوس هندسة مدنية مع خبرة لا تقل عن سنتين في الإشراف الموقعي",
                    conditions = "إجادة برامج الرسم الهندسي وإعداد التقارير اليومية وتوافر وسيلة نقل",
                    startDate = "2026-09-20",
                    endDate = "2026-10-15",
                    isPublished = true
                )
            )
            dao.insertJob(
                JobEntity(
                    id = "job_2",
                    title = "أخصائي تقنية معلومات ودعم فني",
                    organization = "مركز الحلول الرقمية",
                    province = "بغداد",
                    location = "الكرخ - المنصور",
                    jobType = "دوام كامل",
                    description = "إدارة الشبكات وصيانة الأجهزة وتقديم الدعم الفني المباشر للمستخدمين والموظفين.",
                    qualifications = "دبلوم أو بكالوريوس في علوم الحاسوب أو هندسة تقنيات الحاسوب",
                    conditions = "خبرة عملية سنة واحدة وإلمام بإدارة خوادم ويندوز والشبكات",
                    startDate = "2026-09-18",
                    endDate = "2026-10-10",
                    isPublished = true
                )
            )
            dao.insertJob(
                JobEntity(
                    id = "job_3",
                    title = "برنامج التدريب المهني في التمديدات الكهربائية والصيانة",
                    organization = "معهد التدريب والتطوير المهني",
                    province = "الأنبار",
                    location = "الرمادي",
                    jobType = "تدريب وتأهيل",
                    description = "دورة تدريبية مكثفة لمدة 6 أسابيع تتضمن تدريباً عملياً ومنحة تشجيعية للمتفوقين مع شهادة معتمدة.",
                    qualifications = "خريج إعدادية فنية أو باحث عن عمل لديه رغبة في تعلم المهنة",
                    conditions = "الالتزام بنسبة حضور 90% كحد أدنى",
                    startDate = "2026-10-01",
                    endDate = "2026-10-25",
                    isPublished = true
                )
            )

            // Seed Initial News
            dao.insertNews(
                NewsEntity(
                    id = "news_1",
                    title = "افتتاح مركز استقبال طلبات وشكاوى المواطنين الإلكتروني",
                    date = "2026-09-15",
                    description = "أعلن السيد سامي الفلاحي عن الإطلاق الرسمي للبوابة الرقمية الموحدة لتسهيل التواصل مع الأهالي وسرعة إنجاز معاملاتهم.",
                    content = "ضمن مبادرة تعزيز الشفافية ومد جسور التواصل المباشر مع أهلنا الكرام، تم تدشين المنصة الرقمية التفاعلية لخدمة المواطن، لتكون نافذة موثوقة لاستقبال الشكاوى والطلبات ومتابعتها خطوة بخطوة مع لجان المتابعة الميدانية.",
                    isPublished = true
                )
            )
            dao.insertNews(
                NewsEntity(
                    id = "news_2",
                    title = "متابعة ميدانية لمشاريع تبليط وتأهيل شبكات المياه والكهرباء",
                    date = "2026-09-10",
                    description = "فريق المتابعة يواصل جولاته التفقدية على الأحياء السكنية للوقوف على مستوى الخدمات المقدمة وتذليل العقبات.",
                    content = "أجرى فريق المتابعة زيارات تفقدية لعدد من المشاريع الخدمية في مختلف المناطق، حيث تم التنسيق مع الدوائر البلدية والكهرباء لمعالجة البلاغات الواردة من المواطنين عبر المنصة بأسرع وقت ممكن.",
                    isPublished = true
                )
            )

            // Seed Initial Welcome Notification
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_welcome",
                    targetUserId = "all",
                    title = "مرحباً بكم في منصة سامي الفلاحي لخدمات المواطنين",
                    body = "يسعدنا خدمتكم عبر تقديم طلباتكم وشكاواكم ومتابعتها مباشرة لحظة بلحظة برقم تتبع خاص.",
                    type = "announcement",
                    createdAt = System.currentTimeMillis()
                )
            )

            // Initial Audit Log
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "audit_init",
                    actorEmail = "system@alfalahi.iq",
                    action = "تهيئة النظام وقاعدة البيانات",
                    entityType = "settings",
                    entityId = "system",
                    details = "تم تشغيل منصة سامي الفلاحي وإعداد جداول خدمة المواطن بنجاح"
                )
            )
        }
    }
}
