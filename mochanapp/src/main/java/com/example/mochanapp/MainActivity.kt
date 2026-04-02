package com.example.mochanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.mochanapp.screens.AssessmentHomeScreen
import com.example.mochanapp.screens.CameraAssessmentScreen
import com.example.mochanapp.screens.BreathingScreen
import com.example.mochanapp.screens.DashboardScreen
import com.example.mochanapp.screens.JournalScreen
import com.example.mochanapp.screens.MoodTrackerScreen
import com.example.mochanapp.screens.ProfileScreen
import com.example.mochanapp.screens.ResultScreen
import com.example.mochanapp.screens.SoundScreen
import com.example.mochanapp.screens.GroundingScreen  // Adjust package if needed
import com.example.mochanapp.screens.ConsentScreen
import com.example.mochanapp.screens.WellnessScreen
import com.example.mochanapp.ui.theme.MOCHANAPPTheme
import com.example.mochanapp.utils.AnonymousIdMigrationHelper
import com.example.mochanapp.screens.PrivacyDataScreen
import com.example.mochanapp.utils.NotificationHelper
import android.Manifest
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnonymousIdMigrationHelper.ensureConsistentAnonymousId(this)

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // You might want to request this at an appropriate time
                // Or show a dialog explaining why you need it
            }
        }

        setContent {
            MOCHANAPPTheme {
                val navController = rememberNavController()

                AppBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            AppFooter(navController)
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("dashboard") {
                                DashboardScreen(navController)
                            }
                            composable("profile") {
                                ProfileScreen(
                                    navController = navController,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("check") {
                                AssessmentHomeScreen().AssessmentHomeContent(navController)
                            }
                            composable("wellness") {
                                WellnessScreen(navController)
                            }
                            composable("journal") {
                                JournalScreen().JournalContent(navController = navController)
                            }
                            composable("assessment") {
                                CameraAssessmentScreen(navController = navController)
                            }
                            composable("consent") {
                                ConsentScreen(
                                    navController = navController,
                                    onAccept = {
                                        navController.navigate("assessment") {
                                            popUpTo("Consent") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(
                                route = "result/{score}",
                                arguments = listOf(navArgument("score") { type = NavType.IntType })
                            ) { backStackEntry ->
                                val score = backStackEntry.arguments?.getInt("score") ?: 0
                                ResultScreen(
                                    navController = navController,
                                    score = score
                                )
                            }
                            composable("grounding") {  // ← ADD THIS
                                GroundingScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("breathing") {
                                BreathingScreen(onBack = { navController.popBackStack() })
                            }
                            composable("sounds") {
                                SoundScreen(onBack = { navController.popBackStack() })
                            }
                            composable("mood-tracker") {
                                MoodTrackerScreen(onBack = { navController.popBackStack() })
                            }
                            composable("privacy_data") {
                                PrivacyDataScreen(
                                    navController = navController,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppFooter(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == "dashboard") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") },
            selected = currentRoute == "dashboard",
            onClick = {
                if (currentRoute != "dashboard") {
                    navController.navigate("dashboard") {
                        popUpTo(0)
                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == "check") Icons.Filled.MonitorHeart else Icons.Outlined.MonitorHeart,
                    contentDescription = "Check"
                )
            },
            label = { Text("Check") },
            selected = currentRoute == "check",
            onClick = {
                if (currentRoute != "check") {
                    navController.navigate("check") {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == "wellness") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Wellness"
                )
            },
            label = { Text("Wellness") },
            selected = currentRoute == "wellness",
            onClick = {
                if (currentRoute != "wellness") {
                    navController.navigate("wellness") {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFF4CC),
                                Color(0xFFFFD1DF),
                                Color(0xFFDDE4FF),
                                Color(0xFFFFD1DF),
                                Color(0xFFFFE4C4)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )


                }
        )

        // Refined glow overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height * 0.15f),
                            radius = size.width * 0.7f
                        )
                    )
                }
        )

        content()
    }
}