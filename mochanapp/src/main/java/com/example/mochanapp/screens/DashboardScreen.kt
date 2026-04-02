package com.example.mochanapp.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mochanapp.R
import androidx.compose.foundation.BorderStroke

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current

    // Check login status
    val isLoggedIn = remember {
        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.getBoolean("is_logged_in", false)
    }

    // Get user name if logged in
    val userName = remember {
        if (isLoggedIn) {
            val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
            prefs.getString("user_name", "User") ?: "User"
        } else {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // -------- HEADER --------
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            color = Color(0xFFFFFDF9).copy(alpha = 0.75f),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ---- LEFT LOGO ----
                Image(
                    painter = painterResource(id = R.drawable.mochan_logo),
                    contentDescription = "Mochan Logo",
                    modifier = Modifier
                        .height(70.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(16.dp))

                // ---- TEXT SECTION ----
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Mochan",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(bottom = 0.dp)
                    )

                    Text(
                        text = if (isLoggedIn) "Welcome, $userName" else "Your mental wellness partner",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4B5563),
                        modifier = Modifier.padding(top = 0.dp)
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // -------- CARD 1: MENTAL HEALTH CHECK --------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isLoggedIn) {
                            navController.navigate("check")
                        } else {
                            navController.navigate("profile")
                        }
                    }
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x20000000)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(colors = listOf(Color(0xFFFFF0F5), Color(0xFFFFF7ED)))
                        )
                ) {
                    // Background Decoration
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                            .size(160.dp)
                            .background(Color(0xFFFECDD3).copy(alpha = 0.4f), CircleShape)
                    )

                    // Main Content Column
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
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

                            // Tag
                            Surface(
                                color = Color.White.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "PHQ-9",
                                    color = Color(0xFFBE185D),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "Mental Health Check",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = if (isLoggedIn)
                                "Take a gentle assessment to understand how you're feeling"
                            else
                                "Login to access mental health assessment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563),
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (isLoggedIn) {
                                    navController.navigate("consent")
                                } else {
                                    navController.navigate("profile")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        if (isLoggedIn) "Start Assessment" else "Login to Start",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        if (isLoggedIn) Icons.Default.AutoAwesome else Icons.Default.Login,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Takes 2–3 minutes • Private & secure",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // -------- CARD 2: WELLNESS HUB --------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isLoggedIn) {
                            navController.navigate("wellness")
                        } else {
                            navController.navigate("profile")
                        }
                    }
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0x10000000)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFF0F9FF),
                                    Color(0xFFE0F2FE)
                                )
                            )
                        )
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // --- BACKGROUND DECORATIVE HEARTS ---
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 20.dp, y = 20.dp)
                            .size(140.dp),
                        tint = Color(0xFF29B6F6).copy(alpha = 0.05f)
                    )

                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 20.dp, end = 40.dp)
                            .size(32.dp),
                        tint = Color(0xFF26C6DA).copy(alpha = 0.2f)
                    )

                    // --- MAIN CONTENT ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF29B6F6), Color(0xFF26C6DA))
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Heart",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Wellness Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = if (isLoggedIn) "Breathing, sounds & more" else "Login to access wellness tools",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isLoggedIn) "Explore tools" else "Login to explore",
                                color = Color(0xFF0288D1),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                if (isLoggedIn) Icons.Default.AutoAwesome else Icons.Default.Login,
                                contentDescription = null,
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // -------- CARD 3: DAILY CHECK-INS --------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x20000000)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFDFCFE), Color(0xFFF3E5F5))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFAB47BC), Color(0xFF7E57C2))
                                        ),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = "Chart",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Daily Check-ins",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "Track your mood and thoughts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isLoggedIn) {
                                        navController.navigate("mood-tracker")
                                    } else {
                                        navController.navigate("profile")
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(2.dp, Color(0xFF9C27B0)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFF9C27B0)
                                )
                            ) {
                                Text("Log Mood", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isLoggedIn) {
                                        navController.navigate("journal")
                                    } else {
                                        navController.navigate("profile")
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(2.dp, Color(0xFFE91E63)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFFE91E63)
                                )
                            ) {
                                Text("Journal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // -------- CARD 4: MOTIVATIONAL QUOTE --------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x20000000)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "\"Take care of your mind, it's the only place you have to live.\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF374151),
                        lineHeight = 24.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "— Your journey to wellness starts here",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}