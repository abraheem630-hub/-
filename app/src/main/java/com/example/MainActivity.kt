package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.components.PortalBottomNavigationBar
import com.example.ui.components.PortalTopAppBar
import com.example.ui.screens.*
import com.example.ui.screens.admin.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PortalViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PortalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Force RTL (Right-to-Left) layout for official Iraqi Arabic language experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PortalApp(viewModel = viewModel, onFinish = { finish() })
                }
            }
        }
    }
}

@Composable
fun PortalApp(
    viewModel: PortalViewModel,
    onFinish: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Toast / SnackBar messages
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearMessage()
            }
        }
    }

    // Hardware Back Button Handler
    BackHandler {
        val handled = viewModel.goBack()
        if (!handled) {
            onFinish()
        }
    }

    val isAtHomeScreen = currentScreen is Screen.Home
    val isAdminScreen = currentScreen is Screen.AdminDashboard ||
            currentScreen is Screen.AdminRequests ||
            currentScreen is Screen.AdminComplaints ||
            currentScreen is Screen.AdminReports ||
            currentScreen is Screen.AdminCitizens ||
            currentScreen is Screen.AdminJobs ||
            currentScreen is Screen.AdminSocialServices ||
            currentScreen is Screen.AdminNews ||
            currentScreen is Screen.AdminNotifications ||
            currentScreen is Screen.AdminMessages ||
            currentScreen is Screen.AdminAuditLogs ||
            currentScreen is Screen.AdminSettings ||
            currentScreen is Screen.AdminLogin

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PortalTopAppBar(
                title = currentScreen.title,
                canNavigateBack = !isAtHomeScreen,
                onNavigateBack = { viewModel.goBack() },
                isAdminLoggedIn = isAdmin,
                onAdminClick = {
                    if (isAdmin) {
                        viewModel.navigateTo(Screen.AdminDashboard)
                    } else {
                        viewModel.navigateTo(Screen.AdminLogin)
                    }
                },
                unreadCount = unreadCount,
                onNotificationsClick = {
                    viewModel.navigateTo(Screen.CitizenProfile)
                }
            )
        },
        bottomBar = {
            if (!isAdminScreen) {
                PortalBottomNavigationBar(
                    currentScreen = currentScreen.title,
                    onHomeClick = { viewModel.navigateTo(Screen.Home) },
                    onRequestsClick = { viewModel.navigateTo(Screen.TrackRequest()) },
                    onNotificationsClick = { viewModel.navigateTo(Screen.CitizenProfile) },
                    onProfileClick = { viewModel.navigateTo(Screen.CitizenProfile) },
                    unreadCount = unreadCount
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> HomeScreen(viewModel = viewModel)
                is Screen.SubmitRequest -> SubmitRequestScreen(viewModel = viewModel)
                is Screen.TrackRequest -> TrackRequestScreen(viewModel = viewModel, initialNumber = screen.initialNumber)
                is Screen.SubmitComplaint -> SubmitComplaintScreen(viewModel = viewModel)
                is Screen.ReportProblem -> ReportProblemScreen(viewModel = viewModel)
                is Screen.CitizenProfile -> CitizenProfileScreen(viewModel = viewModel)
                is Screen.JobsAndTraining -> JobsAndTrainingScreen(viewModel = viewModel)
                is Screen.SocialServices -> SocialServicesScreen(viewModel = viewModel)
                is Screen.NewsAndAnnouncements -> NewsAndAnnouncementsScreen(viewModel = viewModel)
                is Screen.Messages -> MessagesScreen(viewModel = viewModel)
                is Screen.AboutApp -> AboutAppScreen(viewModel = viewModel)

                // Admin Screens
                is Screen.AdminLogin -> AdminLoginScreen(viewModel = viewModel)
                is Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                is Screen.AdminRequests -> AdminRequestsScreen(viewModel = viewModel)
                is Screen.AdminComplaints -> AdminComplaintsScreen(viewModel = viewModel)
                is Screen.AdminReports -> AdminReportsScreen(viewModel = viewModel)
                is Screen.AdminCitizens -> AdminCitizensScreen(viewModel = viewModel)
                is Screen.AdminJobs -> AdminJobsScreen(viewModel = viewModel)
                is Screen.AdminSocialServices -> AdminSocialServicesScreen(viewModel = viewModel)
                is Screen.AdminNews -> AdminNewsScreen(viewModel = viewModel)
                is Screen.AdminNotifications -> AdminNotificationsScreen(viewModel = viewModel)
                is Screen.AdminMessages -> AdminMessagesScreen(viewModel = viewModel)
                is Screen.AdminAuditLogs -> AdminAuditLogsScreen(viewModel = viewModel)
                is Screen.AdminSettings -> AdminSettingsScreen(viewModel = viewModel)
            }
        }
    }
}
