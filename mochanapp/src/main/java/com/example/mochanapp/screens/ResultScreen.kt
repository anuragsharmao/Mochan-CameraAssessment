package com.example.mochanapp.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import com.example.mochanapp.utils.UploadHelper
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import com.example.mochanapp.utils.ReportDownloadHelper
import java.io.File
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mochanapp.utils.UserSessionHelper
import kotlinx.coroutines.delay
import com.example.mochanapp.utils.NotificationHelper

// ============ DATA CLASSES ============

data class SeverityData(
    val level: String,
    val levelEmoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val gradient: Brush,
    val bgColor: Color,
    val lightBgColor: Color,
    val borderColor: Color,
    val icon: ImageVector,
    val description: String,
    val recommendations: List<RecommendationItem>
)

data class RecommendationItem(
    val icon: String,
    val title: String,
    val description: String,
    val actionColor: Color
)

data class AiPredictionData(
    val score: Int,
    val label: String,
    val confidence: Float,
    val modelVersion: String,
    val frameCount: Int,
    val rawScore: Float
)

enum class SeverityClass {
    MILD, MODERATE, SEVERE
}
enum class AnonymousUploadStatus {
    IDLE, UPLOADING, SUCCESS, ERROR
}

// ============ COLOR DEFINITIONS ============

val GradientBlueCyan = Brush.linearGradient(
    colors = listOf(Color(0xFF60A5FA), Color(0xFF22D3EE))
)

val GradientPurplePink = Brush.linearGradient(
    colors = listOf(Color(0xFFA855F7), Color(0xFFEC4899))
)

val GradientGreenTeal = Brush.linearGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF14B8A6))
)

val GradientOrangeRed = Brush.linearGradient(
    colors = listOf(Color(0xFFF97316), Color(0xFFEF4444))
)

// Severity colors
val MildColor = Color(0xFF10B981)      // Green
val MildSecondary = Color(0xFF34D399)   // Light Green
val MildGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF34D399))
)

val ModerateColor = Color(0xFFF59E0B)   // Orange
val ModerateSecondary = Color(0xFFFBBF24) // Light Orange
val ModerateGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
)

val SevereColor = Color(0xFFEF4444)     // Red
val SevereSecondary = Color(0xFFF87171) // Light Red
val SevereGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFEF4444), Color(0xFFF87171))
)

val MildLightColor = Color(0xFFECFDF5)   // Light Green
val ModerateLightColor = Color(0xFFFFFBEB) // Light Orange
val SevereLightColor = Color(0xFFFFF1F2)   // Light Red

// ============ MAIN SCREEN COMPOSABLE ============

@Composable
fun ResultScreen(
    navController: NavController,
    score: Int // AI score (0-24)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val session = UserSessionHelper.getUserData(context)
    val savedEmail = session.email
    val userName = session.name
    val anonymousId = session.anonymousId
    val userAge = session.age
    val userGender = session.gender
    val registrationId = session.registrationId

    // Retrieve AI prediction data from assessment_prefs
    val prefs = context.getSharedPreferences("assessment_prefs", Context.MODE_PRIVATE)
    val aiScore = remember { prefs.getInt("ai_prediction_score", score) }
    val aiLabel = remember { prefs.getString("ai_prediction_label", "") ?: "" }
    val aiConfidence = remember { prefs.getFloat("ai_prediction_confidence", 0f) }
    val aiModelVersion = remember { prefs.getString("ai_model_version", "") ?: "" }
    val aiFrameCount = remember { prefs.getInt("ai_frame_count", 0) }
    val aiRawScore = remember { prefs.getFloat("ai_raw_score", 0f) }

    // Create AI data
    val aiData = AiPredictionData(
        score = aiScore,
        label = aiLabel,
        confidence = aiConfidence,
        modelVersion = aiModelVersion,
        frameCount = aiFrameCount,
        rawScore = aiRawScore
    )

    // Get severity class for AI
    val aiSeverityClass = when {
        aiData.score <= 9 -> SeverityClass.MILD
        aiData.score <= 14 -> SeverityClass.MODERATE
        else -> SeverityClass.SEVERE
    }

    val aiSeverityData = getSeverityDataFromClass(aiSeverityClass)

    // State for upload
    var uploadStatus by remember { mutableStateOf<UploadStatus?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadAttempted by remember { mutableStateOf(false) }

    // State for download
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadedFilePath by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    Log.d("RESULT_SCREEN", "Retrieved email from user_prefs: $savedEmail")
    Log.d("RESULT_SCREEN", "Retrieved registration ID from user_prefs: $registrationId")

    // Check notification permission for Android 13+
    val hasNotificationPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Notification permission denied. You won't receive download updates.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Show permission dialog if needed
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Enable Notifications") },
            text = {
                Text(
                    "MochanApp needs notification permission to show you download progress " +
                            "and completion status for your assessment reports."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // Auto-upload when screen opens
    LaunchedEffect(Unit) {
        if (!uploadAttempted && anonymousId.isNotBlank()) {
            uploadAttempted = true
            isUploading = true
            uploadStatus = UploadStatus.UPLOADING

            try {
                UploadHelper.uploadAssessment(
                    context = context,
                    coroutineScope = coroutineScope,
                    anonymousId = anonymousId,
                    age = userAge,
                    aiRawScore = aiRawScore,
                    email = savedEmail,
                    registrationId = registrationId,
                    onProgress = { progress, message ->
                        // Update progress
                    },
                    onSuccess = { message ->
                        isUploading = false
                        uploadStatus = UploadStatus.SUCCESS(message)
                        Toast.makeText(context, "Data uploaded successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        isUploading = false
                        uploadStatus = UploadStatus.ERROR(error)
                    }
                )
            } catch (e: Exception) {
                isUploading = false
                uploadStatus = UploadStatus.ERROR(e.message ?: "Unknown error")
                Log.e("UPLOAD", "Exception during upload: ${e.message}", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Header
            item { AssessmentHeader(navController, aiSeverityData.primaryColor) }

            // 2. Upload Status Card (if upload failed)
            if (uploadStatus is UploadStatus.ERROR) {
                item {
                    UploadErrorCard(
                        errorMessage = (uploadStatus as UploadStatus.ERROR).message,
                        onRetry = {
                            isUploading = true
                            uploadStatus = UploadStatus.UPLOADING

                            coroutineScope.launch {
                                try {
                                    UploadHelper.uploadAssessment(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        anonymousId = anonymousId,
                                        age = userAge,
                                        aiRawScore = aiRawScore,
                                        email = savedEmail,
                                        registrationId = registrationId,
                                        onProgress = { progress, message -> },
                                        onSuccess = { message ->
                                            isUploading = false
                                            uploadStatus = UploadStatus.SUCCESS(message)
                                            Toast.makeText(context, "Data uploaded successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            isUploading = false
                                            uploadStatus = UploadStatus.ERROR(error)
                                        }
                                    )
                                } catch (e: Exception) {
                                    isUploading = false
                                    uploadStatus = UploadStatus.ERROR(e.message ?: "Unknown error")
                                }
                            }
                        },
                        isUploading = isUploading
                    )
                }
            }

            // 3. User Info Card (from the first version)
            item {
                UserInfoCard(
                    userName = userName,
                    userAge = userAge,
                    userGender = userGender,
                    anonymousId = anonymousId,
                    registrationId = registrationId,
                    date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
                )
            }

            // 4. AI Severity Card - Enhanced design
            item {
                EnhancedAiSeverityCard(
                    severityData = aiSeverityData,
                    aiData = aiData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            // 5. Enhanced Recommendations based on AI severity
            item {
                EnhancedRecommendationsCard(
                    severityData = aiSeverityData,
                    recommendations = aiSeverityData.recommendations
                )
            }

            // 6. Download Report Button (from first version)
            item {
                DownloadReportButton(
                    userName = userName,
                    userAge = userAge,
                    userGender = userGender,
                    anonymousId = anonymousId,
                    registrationId = registrationId,
                    aiData = aiData,
                    aiSeverity = aiSeverityData,
                    isDownloading = isDownloading,
                    hasNotificationPermission = hasNotificationPermission,
                    onShowPermissionDialog = { showPermissionDialog = true },
                    onDownloadStart = { isDownloading = true },
                    onDownloadProgress = { progress -> downloadProgress = progress },
                    onDownloadComplete = { success, filePath ->
                        isDownloading = false
                        if (success) {
                            downloadedFilePath = filePath
                            Toast.makeText(
                                context,
                                "PDF Report downloaded successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // 7. Show "View Report" button if a file has been downloaded
            if (downloadedFilePath != null && !isDownloading) {
                item {
                    ViewReportButton(
                        filePath = downloadedFilePath!!,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }

            // 8. SETU Promotional Card
            item { EnhancedSetuPromoCard(navController) }

            // 9. Wellness Tools Card
            item { EnhancedWellnessToolsCard(navController) }
        }
    }
}

// ============ USER INFO CARD (from first version) ============

@Composable
fun UserInfoCard(
    userName: String,
    userAge: Int,
    userGender: String,
    anonymousId: String,
    registrationId: String,
    date: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientPurplePink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Patient Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        date,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User details in a grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChipEnhanced(
                        label = "Name",
                        value = userName,
                        icon = Icons.Default.Person,
                        color = Color(0xFF8B5CF6)
                    )
                    InfoChipEnhanced(
                        label = "Age",
                        value = "$userAge years",
                        icon = Icons.Default.Numbers,
                        color = Color(0xFFEC4899)
                    )
                }

                // Right column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChipEnhanced(
                        label = "Gender",
                        value = userGender,
                        icon = when (userGender.lowercase(Locale.getDefault())) {
                            "male" -> Icons.Default.Male
                            "female" -> Icons.Default.Female
                            else -> Icons.Default.Transgender
                        },
                        color = Color(0xFF3B82F6)
                    )
                    InfoChipEnhanced(
                        label = "ID",
                        value = anonymousId.take(8) + "...",
                        icon = Icons.Default.Fingerprint,
                        color = Color(0xFF10B981)
                    )
                }
            }

            // Registration ID
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF3F4F6)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Registration ID: $registrationId",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChipEnhanced(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    fontSize = 10.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 12.sp
                )
                Text(
                    value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    maxLines = 1
                )
            }
        }
    }
}

// ============ UPLOAD STATUS ============

sealed class UploadStatus {
    object UPLOADING : UploadStatus()
    data class SUCCESS(val message: String) : UploadStatus()
    data class ERROR(val message: String) : UploadStatus()
}

// ============ UPLOAD ERROR CARD ============

@Composable
fun UploadErrorCard(
    errorMessage: String,
    onRetry: () -> Unit,
    isUploading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFECACA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF1F2))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = SevereColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upload Failed",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SevereColor
                )

                Text(
                    text = errorMessage.take(50) + if (errorMessage.length > 50) "..." else "",
                    fontSize = 13.sp,
                    color = Color(0xFF991B1B),
                    maxLines = 1
                )
            }

            Button(
                onClick = onRetry,
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SevereColor,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Retry", fontSize = 14.sp)
                }
            }
        }
    }
}

// ============ ENHANCED AI SEVERITY CARD ============

@Composable
fun EnhancedAiSeverityCard(
    severityData: SeverityData,
    aiData: AiPredictionData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Top gradient section with integrated title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                severityData.primaryColor.copy(alpha = 0.15f),
                                severityData.secondaryColor.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                // Decorative circles
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = severityData.primaryColor.copy(alpha = 0.1f),
                        radius = 70.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(
                            size.width - 30.dp.toPx(),
                            15.dp.toPx()
                        )
                    )
                    drawCircle(
                        color = severityData.secondaryColor.copy(alpha = 0.1f),
                        radius = 50.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(
                            size.width - 15.dp.toPx(),
                            60.dp.toPx()
                        )
                    )
                }

                // Title text inside the card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        "AI Mental Health Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            "Your Depression Level: ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(severityData.lightBgColor)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    severityData.levelEmoji,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    severityData.level,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = severityData.primaryColor
                                )
                            }
                        }
                    }
                }
            }

            // Main content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT Circle with progress
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background circle
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color(0xFFE2E8F0),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }

                    // Progress arc
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawArc(
                            color = severityData.primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (aiData.score.toFloat() / 24f),
                            useCenter = false,
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // Center content
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            severityData.level,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = severityData.primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // RIGHT Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // AI Icon with gradient
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GradientBlueCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Face,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Confidence badge
                        Surface(
                            color = severityData.primaryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                "${(aiData.confidence * 100).toInt()}% confidence",
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 3.dp
                                ),
                                fontSize = 11.sp,
                                color = severityData.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        severityData.description,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF475569),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Frame count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "Frames analyzed: ${aiData.frameCount}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

// ============ DOWNLOAD REPORT BUTTON (from first version) ============

@Composable
fun DownloadReportButton(
    userName: String,
    userAge: Int,
    userGender: String,
    anonymousId: String,
    registrationId: String,
    aiData: AiPredictionData,
    aiSeverity: SeverityData,
    isDownloading: Boolean,
    hasNotificationPermission: Boolean,
    onShowPermissionDialog: () -> Unit,
    onDownloadStart: () -> Unit,
    onDownloadProgress: (Int) -> Unit,
    onDownloadComplete: (Boolean, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloadProgress by remember { mutableStateOf(0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientPurplePink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "Download PDF Report",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        "Complete AI assessment report",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick summary of AI assessment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFFF3F4F6),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "AI Analysis Result",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            aiSeverity.levelEmoji,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${aiSeverity.level} (${aiData.score}/24)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = aiSeverity.primaryColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Confidence",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        "${(aiData.confidence * 100).toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = aiSeverity.primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Download a professionally formatted PDF containing your personal information, AI analysis results, and personalized recommendations.",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                lineHeight = 18.sp
            )

            // Show notification permission warning if not granted on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Enable notifications to receive download updates",
                            fontSize = 12.sp,
                            color = Color(0xFF856404),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onShowPermissionDialog,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF856404)
                            )
                        ) {
                            Text("Enable", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show progress bar when downloading
            if (isDownloading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    // Progress bar
                    LinearProgressIndicator(
                        progress = downloadProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF8B5CF6),
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress text with dynamic message
                    val progressMessage = when {
                        downloadProgress < 10 -> "Initializing..."
                        downloadProgress < 30 -> "Creating document..."
                        downloadProgress < 50 -> "Adding patient info..."
                        downloadProgress < 60 -> "Adding AI results..."
                        downloadProgress < 70 -> "Adding analysis details..."
                        downloadProgress < 80 -> "Adding recommendations..."
                        downloadProgress < 90 -> "Saving to storage..."
                        downloadProgress < 100 -> "Finalizing..."
                        else -> "Complete!"
                    }

                    Text(
                        text = "$progressMessage $downloadProgress%",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = {
                    NotificationHelper.createNotificationChannel(context)
                    onDownloadStart()
                    coroutineScope.launch {
                        try {
                            val filePath = ReportDownloadHelper.generateReport(
                                context = context,
                                userName = userName,
                                userAge = userAge,
                                userGender = userGender,
                                anonymousId = anonymousId,
                                registrationId = registrationId,
                                aiData = aiData,
                                onProgress = { progress ->
                                    downloadProgress = progress
                                    onDownloadProgress(progress)
                                }
                            )

                            if (filePath != null) {
                                // Small delay to show 100% progress
                                delay(500)
                            }

                            onDownloadComplete(filePath != null, filePath)

                            // Reset progress after completion
                            if (filePath != null) {
                                downloadProgress = 0
                            }
                        } catch (e: Exception) {
                            Log.e("DownloadReport", "Error: ${e.message}")
                            onDownloadComplete(false, null)
                            downloadProgress = 0
                        }
                    }
                },
                enabled = !isDownloading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (!isDownloading)
                                GradientPurplePink
                            else
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF9CA3AF),
                                        Color(0xFF6B7280)
                                    )
                                ),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDownloading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when {
                                    downloadProgress < 30 -> "Preparing..."
                                    downloadProgress < 60 -> "Generating..."
                                    downloadProgress < 90 -> "Saving..."
                                    else -> "Finalizing..."
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Download PDF Report",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "PDF will be saved to Downloads/MochanApp/Reports/",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============ VIEW REPORT BUTTON ============

@Composable
fun ViewReportButton(
    filePath: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Report Ready",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        "Tap to view your PDF",
                        fontSize = 12.sp,
                        color = Color(0xFF047857)
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        val file = File(filePath)
                        if (!file.exists()) {
                            Toast.makeText(
                                context,
                                "File not found. Please download again.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                        } else {
                            Uri.fromFile(file)
                        }

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        }

                        // Verify that there's an app to handle PDF intents
                        val packageManager = context.packageManager
                        if (intent.resolveActivity(packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context,
                                "No PDF viewer found. Please install a PDF reader app.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e("ViewReport", "Error opening PDF: ${e.message}")
                        Toast.makeText(
                            context,
                            "Could not open PDF: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View", color = Color.White)
            }
        }
    }
}

// ============ ENHANCED RECOMMENDATIONS CARD ============

@Composable
fun EnhancedRecommendationsCard(
    severityData: SeverityData,
    recommendations: List<RecommendationItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with severity color
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(severityData.lightBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = severityData.primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "${severityData.levelEmoji} ${severityData.level} — Recommended Actions",
                        fontWeight = FontWeight.Bold,
                        color = severityData.primaryColor,
                        fontSize = 18.sp
                    )
                    Text(
                        "Personalized recommendations based on your results",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recommendations list
            recommendations.forEachIndexed { index, rec ->
                EnhancedRecommendationItem(
                    recommendation = rec,
                    isLast = index == recommendations.size - 1
                )
            }
        }
    }
}

@Composable
fun EnhancedRecommendationItem(
    recommendation: RecommendationItem,
    isLast: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(recommendation.actionColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    recommendation.icon,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recommendation.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    recommendation.description,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (!isLast) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 56.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ============ HEADER ============

@Composable
fun AssessmentHeader(
    navController: NavController,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 10.dp)
    ) {
        TextButton(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Dashboard", fontSize = 14.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(GradientBlueCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "AI Analysis Results",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Facial Expression Analysis",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

// ============ SETU PROMO CARD ============

@Composable
fun EnhancedSetuPromoCard(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientPurplePink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "Professional Support",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        "SETU Counseling Services",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "If you're facing persistent difficulties, our SETU counseling services provide confidential help with trained professionals.",
                fontSize = 14.sp,
                color = Color(0xFF4B5563),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Features grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeaturePill("One-on-one counseling", Color(0xFFA855F7), Modifier.weight(1f))
                FeaturePill("Trained professionals", Color(0xFFEC4899), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeaturePill("Personalized strategies", Color(0xFFA855F7), Modifier.weight(1f))
                FeaturePill("Follow-up support", Color(0xFFEC4899), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://setu.iitkgp.ac.in/")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Could not open link: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            GradientPurplePink,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Connect with SETU Counselors",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
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

@Composable
fun FeaturePill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============ WELLNESS TOOLS CARD ============

@Composable
fun EnhancedWellnessToolsCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientGreenTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "Wellness Tools",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        "Support your mental wellness journey",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tools grid
            val tools = listOf(
                "🧘 Guided meditation" to "Stress reduction",
                "📝 Mood tracking" to "Daily check-ins",
                "🌿 Breathing exercises" to "Anxiety relief",
                "😴 Sleep improvement" to "Better rest"
            )

            tools.chunked(2).forEach { rowTools ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTools.forEach { tool ->
                        ToolItem(
                            title = tool.first,
                            subtitle = tool.second,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate("wellness") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            GradientGreenTeal,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Visit Wellness Section",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
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

@Composable
fun ToolItem(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

// ============ HELPER FUNCTIONS ============

fun getSeverityDataFromClass(severityClass: SeverityClass): SeverityData {
    return when (severityClass) {
        SeverityClass.MILD -> SeverityData(
            level = "Mild",
            levelEmoji = "🟢",
            primaryColor = MildColor,
            secondaryColor = MildSecondary,
            gradient = MildGradient,
            bgColor = MildLightColor,
            lightBgColor = MildLightColor,
            borderColor = Color(0xFF34D399),
            icon = Icons.Default.Face,
            description = "AI analysis indicates mild symptoms. Continue with self-care practices and monitor your wellbeing.",
            recommendations = listOf(
                RecommendationItem(
                    icon = "🧘",
                    title = "Practice daily mindfulness",
                    description = "Start with just 5-10 minutes of meditation or deep breathing exercises each day to reduce stress.",
                    actionColor = MildColor
                ),
                RecommendationItem(
                    icon = "🏃",
                    title = "Maintain regular exercise",
                    description = "Gentle activities like walking, yoga, or stretching can significantly improve your mood and energy levels.",
                    actionColor = MildColor
                ),
                RecommendationItem(
                    icon = "👪",
                    title = "Connect with others",
                    description = "Regular social connections, even brief check-ins with friends or family, help maintain emotional balance.",
                    actionColor = MildColor
                ),
                RecommendationItem(
                    icon = "📓",
                    title = "Keep a mood journal",
                    description = "Track your daily emotions, triggers, and coping strategies to better understand your patterns.",
                    actionColor = MildColor
                )
            )
        )

        SeverityClass.MODERATE -> SeverityData(
            level = "Moderate",
            levelEmoji = "🟡",
            primaryColor = ModerateColor,
            secondaryColor = ModerateSecondary,
            gradient = ModerateGradient,
            bgColor = ModerateLightColor,
            lightBgColor = ModerateLightColor,
            borderColor = Color(0xFFFBBF24),
            icon = Icons.Default.Face,
            description = "AI analysis indicates moderate symptoms. Professional guidance and active self-care are recommended.",
            recommendations = listOf(
                RecommendationItem(
                    icon = "🏥",
                    title = "Visit a counseling center",
                    description = "Consider scheduling a session at a nearby counseling center to talk through what you're experiencing.",
                    actionColor = ModerateColor
                ),
                RecommendationItem(
                    icon = "💬",
                    title = "Speak with a mental health professional",
                    description = "A licensed psychologist or therapist can help you understand and manage these symptoms early.",
                    actionColor = ModerateColor
                ),
                RecommendationItem(
                    icon = "📋",
                    title = "Create a simple daily routine",
                    description = "Maintain regular sleep, meals, and light activity to support emotional stability and reduce anxiety.",
                    actionColor = ModerateColor
                ),
                RecommendationItem(
                    icon = "👥",
                    title = "Talk to someone you trust",
                    description = "Share how you're feeling with a close friend or family member who can offer support.",
                    actionColor = ModerateColor
                )
            )
        )

        SeverityClass.SEVERE -> SeverityData(
            level = "Severe",
            levelEmoji = "🔴",
            primaryColor = SevereColor,
            secondaryColor = SevereSecondary,
            gradient = SevereGradient,
            bgColor = SevereLightColor,
            lightBgColor = SevereLightColor,
            borderColor = Color(0xFFFB7185),
            icon = Icons.Default.Face,
            description = "AI analysis indicates severe symptoms. Immediate professional support is strongly advised.",
            recommendations = listOf(
                RecommendationItem(
                    icon = "🏥",
                    title = "Visit a counseling or psychiatric center urgently",
                    description = "Seek immediate in-person support from a counseling center, hospital, or mental health clinic.",
                    actionColor = SevereColor
                ),
                RecommendationItem(
                    icon = "📞",
                    title = "Contact emergency or crisis support",
                    description = "Call a 24/7 mental health helpline (988) or local emergency number if you feel unsafe.",
                    actionColor = SevereColor
                ),
                RecommendationItem(
                    icon = "👤",
                    title = "Do not stay alone",
                    description = "Stay connected with someone you trust and let them know you need immediate support.",
                    actionColor = SevereColor
                ),
                RecommendationItem(
                    icon = "⚕️",
                    title = "Seek urgent professional care",
                    description = "Schedule an immediate consultation with a psychiatrist or clinical psychologist for proper assessment.",
                    actionColor = SevereColor
                )
            )
        )
    }
}