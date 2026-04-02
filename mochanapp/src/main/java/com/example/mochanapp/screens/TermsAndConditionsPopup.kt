package com.example.mochanapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.TextUnit
import com.example.mochanapp.ui.theme.*



@Composable
fun TermsAndConditionsPopup(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PurpleUltraLight,
                                SurfaceWhite
                            )
                        )
                    )
            ) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PurplePrimary.copy(alpha = 0.15f),
                                    PurpleSecondary.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icon with gradient background
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GradientPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    "Terms & Conditions",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    "Sahay & Mochan App Agreement",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurpleUltraLight)
                                .border(1.dp, PurpleLight.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Introduction
                    TermsSection(
                        title = "Acceptance of Terms",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF10B981)
                    ) {
                        Text(
                            "By downloading, installing, or using the Sahay and Mochan mobile applications (\"Apps\"), you agree to these Terms and Conditions. These AI-based tools are only meant to be used for early screening and wellness monitoring of possible signs of anxiety and depression and are not a substitute for medical diagnosis, treatment, or professional mental health advice.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }

                    // How It Works
                    TermsSection(
                        title = "How the Apps Work",
                        icon = Icons.Default.Info,
                        color = BlueBright
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "The Apps look at facial expressions taken with the device's camera and process non-identifiable data to give users health information.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = PurpleUltraLight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "⚠️ Users must give their informed consent before using the analysis features.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(10.dp),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // User Agreement
                    TermsSection(
                        title = "User Agreement",
                        icon = Icons.Default.Gavel,
                        color = PurplePrimary
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "People should only use the apps if they agree to:",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                            BulletPoint("The collection and processing of limited data that is necessary for the apps to work")
                            BulletPoint("Using the apps only for their intended purpose of early screening and wellness monitoring")
                            BulletPoint("Understanding that these are NOT diagnostic tools")

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Users can stop using the apps at any time.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Medical Disclaimer
                    TermsSection(
                        title = "Important Medical Disclaimer",
                        icon = Icons.Default.Warning,
                        color = OrangeWarm
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "The developers, researchers, and related institutions do not guarantee the accuracy or outcomes of the clinical tests.",
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                border = BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Emergency,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "If you are in distress or have severe symptoms, you should definitely get help from a qualified healthcare professional or a local mental health service.",
                                        fontSize = 13.sp,
                                        color = Color(0xFF991B1B),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Prohibited Uses
                    TermsSection(
                        title = "Prohibited Uses",
                        icon = Icons.Default.Block,
                        color = SevereColor
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "You can't use the Apps for:",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                            BulletPoint("Making decisions in an emergency")
                            BulletPoint("Making a clinical diagnosis")
                            BulletPoint("Discrimination of any kind")
                            BulletPoint("Keeping an eye on people without their permission")
                        }
                    }

                    // Intellectual Property
                    TermsSection(
                        title = "Intellectual Property",
                        icon = Icons.Default.Copyright,
                        color = Color(0xFF8B5CF6)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "The developers own all intellectual property rights to the Apps, algorithms, and content.",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = PurpleUltraLight,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "You can't share, reverse engineer, or use them for business without permission.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    // Updates and Changes
                    TermsSection(
                        title = "Updates & Changes",
                        icon = Icons.Default.Update,
                        color = GreenMint
                    ) {
                        Text(
                            "The developers can change or update the Apps, these terms, and related services at any time. If you keep using the Apps, you agree to the new terms.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }

                    // Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PurpleUltraLight),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PurpleLight.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "📋 Key Points Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", fontSize = 14.sp, color = PurplePrimary, modifier = Modifier.padding(end = 8.dp))
                                Text("Apps are for early screening only, NOT medical diagnosis", fontSize = 13.sp, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", fontSize = 14.sp, color = PurplePrimary, modifier = Modifier.padding(end = 8.dp))
                                Text("Facial expressions analyzed with your consent", fontSize = 13.sp, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", fontSize = 14.sp, color = PurplePrimary, modifier = Modifier.padding(end = 8.dp))
                                Text("Seek professional help for severe symptoms or emergencies", fontSize = 13.sp, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", fontSize = 14.sp, color = PurplePrimary, modifier = Modifier.padding(end = 8.dp))
                                Text("You can stop using the apps at any time", fontSize = 13.sp, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", fontSize = 14.sp, color = PurplePrimary, modifier = Modifier.padding(end = 8.dp))
                                Text("Terms may be updated; continued use means acceptance", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Emergency Resources
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Emergency Resources",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                            Text(
                                "If you're in crisis, please contact:",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                "• National Crisis Hotline: 988\n• Emergency Services: 911\n• Local Mental Health Services",
                                fontSize = 12.sp,
                                color = Color(0xFFB71C1C),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Footer with accept button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PurpleUltraLight
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues()
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
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "I Understand & Accept",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermsSection(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Title
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Content with left padding
        Column(
            modifier = Modifier.padding(start = 32.dp),
            content = content
        )
    }
}

// Reuse BulletPoint from Privacy Policy
@Composable
fun TermsBulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            "•",
            fontSize = 14.sp,
            color = PurplePrimary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// Composable to show a clickable terms link that opens the popup
@Composable
fun TermsLink(
    modifier: Modifier = Modifier,
    text: String = "Terms & Conditions",
    fontSize: TextUnit = 14.sp,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = PurplePrimary,
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}