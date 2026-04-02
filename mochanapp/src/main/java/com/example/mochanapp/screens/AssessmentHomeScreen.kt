package com.example.mochanapp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mochanapp.ui.theme.*
val DarkText = Color(0xFF1D2335)
val SecondaryText = Color(0xFF6B7280)

class AssessmentHomeScreen {

    @Composable
    fun AssessmentHomeContent(navController: NavController) {
        val context = LocalContext.current

        // Check login status directly from SharedPreferences
        val isLoggedIn = remember {
            val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
            prefs.getBoolean("is_logged_in", false)
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // -------- HEADER WITH CREAMY TRANSPARENCY --------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFDF9).copy(alpha = 0.4f))
                        .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pulse Icon with Gradient
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFF8A80), Color(0xFFFFB74D))
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = "Pulse",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Mental Health Check",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1D2335),
                                lineHeight = 24.sp
                            )
                            Text(
                                text = "Track your mental wellness journey",
                                fontSize = 14.sp,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }
                }

                // -------- CONTENT BODY --------
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- DECORATED PHQ-9 ASSESSMENT CARD ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color(0x20000000)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFFF0F5), Color(0xFFFFF7ED))
                                    )
                                )
                                .clip(RoundedCornerShape(24.dp))
                        ) {
                            // 1. Background Decoration (Pink Circle)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 30.dp, y = (-30).dp)
                                    .size(160.dp)
                                    .background(Color(0xFFFECDD3).copy(alpha = 0.4f), CircleShape)
                            )

                            // 2. Main Content
                            Column(modifier = Modifier.padding(20.dp)) {
                                // Icon and Title Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Milky Icon Surface
                                    Surface(
                                        modifier = Modifier.size(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White.copy(alpha = 0.6f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.MonitorHeart,
                                                contentDescription = "PHQ-9 Icon",
                                                tint = Color(0xFFFF5252),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = "AI Mental Health Analysis",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkText
                                        )
                                        Text(
                                            text = "Powered by Advanced AI",
                                            fontSize = 14.sp,
                                            color = SecondaryText
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Description
                                Text(
                                    text = "60-second facial analysis using AI to assess your mental well-being. Get instant insights about your emotional state.",
                                    fontSize = 14.sp,
                                    color = SecondaryText,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Features Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    FeatureItem(
                                        icon = Icons.Default.Timer,
                                        text = "60 Sec",
                                        color = Color(0xFFFF5252)
                                    )
                                    FeatureItem(
                                        icon = Icons.Default.Security,
                                        text = "Private",
                                        color = Color(0xFFFF5252)
                                    )
                                    FeatureItem(
                                        icon = Icons.Default.Analytics,
                                        text = "AI-Powered",
                                        color = Color(0xFFFF5252)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // UPDATED BUTTON - Check login status first
                                Button(
                                    onClick = {
                                        if (isLoggedIn) {
                                            // If logged in, go directly to terms and conditions
                                            navController.navigate("consent")
                                        } else {
                                            // If not logged in, go to profile screen for login/signup
                                            navController.navigate("profile")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFFFF385C),
                                                        Color(0xFFFF5E3A),
                                                        Color(0xFFFF9345)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isLoggedIn) "Start Assessment" else "Login to Continue",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = if (isLoggedIn) Icons.Default.AutoAwesome else Icons.Default.Login,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- ABOUT SECTION CARD (Milky White) ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 24.dp,
                            bottomEnd = 4.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "About AI Analysis",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Our AI analyzes facial expressions and action units (AUs) using advanced machine learning to provide insights into your mental health. The analysis takes 60 seconds and is completely private and secure.",
                                fontSize = 14.sp,
                                color = SecondaryText,
                                lineHeight = 20.sp
                            )
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun FeatureItem(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        text: String,
        color: Color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}