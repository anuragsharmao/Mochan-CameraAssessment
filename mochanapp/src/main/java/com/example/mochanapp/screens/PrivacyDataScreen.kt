package com.example.mochanapp.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import android.os.Environment
import com.example.mochanapp.utils.UserSessionHelper
import com.example.mochanapp.ui.theme.*
import java.net.ConnectException
import java.net.SocketTimeoutException

// Copy these color definitions from ProfileScreen
private val PrimaryPurpleGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF7C3AED),
        Color(0xFF8B5CF6),
        Color(0xFFA78BFA)
    )
)

private val PurpleLightGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFC084FC),
        Color(0xFFE9D5FF)
    )
)


private val SoftPurpleBg = Color(0xFFF5F3FF)
private val SoftPurpleBorder = Color(0xFFE0E7FF)
/*
private val ColorTextPrimary = Color(0xFF1E1B4B)
private val ColorTextSecondary = Color(0xFF4C4A6E)
private val ColorTextTertiary = Color(0xFF6B6A8A)
private val ColorBorder = Color(0xFFE2E8F0)
private val ColorCardBg = Color.White
private val ColorSuccess = Color(0xFF10B981)
private val ColorError = Color(0xFFEF4444) */

// Data classes for assessments
data class AssessmentItem(
    val id: Int,
    val assessmentType: String,
    val gad7Score: Int?,
    val questionnaireScore: Int?,
    val createdAt: String,
    val videoCount: Int
)

data class DeleteResponse(
    val success: Boolean,
    val message: String,
    val deletedSummary: DeleteSummary? = null
)

data class DeleteSummary(
    val studentId: String? = null,
    val assessmentsDeleted: Int = 0,
    val videosDeleted: Int = 0,
    val filesDeleted: Int = 0
)

enum class DeleteMode { SINGLE, ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDataScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State
    var isLoading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var assessments by remember { mutableStateOf<List<AssessmentItem>>(emptyList()) }
    var selectedAssessment by remember { mutableStateOf<AssessmentItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(DeleteMode.SINGLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var deleteConfirmationText by remember { mutableStateOf("") }

    // Get current user - use the function from ProfileScreen
    val currentUser = loadSavedUser(context)
    val isLoggedIn = currentUser != null

    // Function to load assessments
    fun loadAssessments() {
        if (!isLoggedIn) return

        coroutineScope.launch {
            isLoading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    fetchAssessments(currentUser!!.registration_id)
                }
                assessments = result
                Log.d("PrivacyScreen", "Loaded ${result.size} assessments")
            } catch (e: Exception) {
                Log.e("PrivacyScreen", "Error loading assessments", e)
                errorMessage = "Failed to load assessments: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Load assessments when screen is first shown AND when it gains focus
    LaunchedEffect(Unit) {
        loadAssessments()
    }

    // Also reload when the screen comes back into focus (user navigates back)
    DisposableEffect(Unit) {
        onDispose {
            // You could add a callback here if needed
        }
    }

    // Add a side effect to reload when returning to this screen
    LaunchedEffect(navController.currentBackStackEntry?.destination?.route) {
        // This will trigger whenever the destination changes
        if (navController.currentBackStackEntry?.destination?.route == "privacy_data") {
            loadAssessments()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            item {
                PrivacyHeader(
                    onBack = { navController.popBackStack() },
                    isLoggedIn = isLoggedIn,
                    onRefresh = { loadAssessments() }
                )
            }

            if (!isLoggedIn) {
                item {
                    NotLoggedInContent(
                        onLoginClick = {
                            navController.navigate("profile") {
                                popUpTo("privacy_data") { inclusive = true }
                            }
                        }
                    )
                }
            } else {
                // Data Management Options
                item {
                    DataManagementCard(
                        onDeleteAllClick = {
                            deleteMode = DeleteMode.ALL
                            showDeleteAllDialog = true
                        },
                        modifier = Modifier.padding(20.dp)
                    )
                }

                // Error/Success Messages
                if (errorMessage != null) {
                    item {
                        StatusMessage(
                            message = errorMessage!!,
                            isError = true,
                            onDismiss = { errorMessage = null }
                        )
                    }
                }

                if (successMessage != null) {
                    item {
                        StatusMessage(
                            message = successMessage!!,
                            isError = false,
                            onDismiss = { successMessage = null }
                        )
                    }
                }

                // History Header with Refresh
                item {
                    HistoryHeader(
                        totalCount = assessments.size,
                        onRefresh = { loadAssessments() },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // Loading indicator
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF7C3AED),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                // Assessments List
                if (assessments.isEmpty() && !isLoading) {
                    item {
                        EmptyHistoryContent()
                    }
                } else {
                    items(assessments) { assessment ->
                        AssessmentHistoryItem(
                            assessment = assessment,
                            onDeleteClick = {
                                selectedAssessment = assessment
                                deleteMode = DeleteMode.SINGLE
                                showDeleteDialog = true
                            },
                            modifier = Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }

                // Bottom padding
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        // Loading overlay for delete operations
        if (isDeleting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorCardBg),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF7C3AED),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Deleting...",
                            color = ColorTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog && selectedAssessment != null) {
            DeleteConfirmationDialog(
                assessment = selectedAssessment!!,
                onConfirm = {
                    coroutineScope.launch {
                        isDeleting = true
                        showDeleteDialog = false

                        val result = withContext(Dispatchers.IO) {
                            deleteSingleAssessment(context, selectedAssessment!!.id)
                        }

                        if (result.success) {
                            successMessage = "Assessment deleted successfully"
                            // Reload assessments
                            val newList = withContext(Dispatchers.IO) {
                                fetchAssessments(currentUser!!.registration_id)
                            }
                            assessments = newList
                        } else {
                            errorMessage = result.message
                        }

                        isDeleting = false
                        selectedAssessment = null
                    }
                },
                onDismiss = {
                    showDeleteDialog = false
                    selectedAssessment = null
                }
            )
        }

        // Delete All Confirmation Dialog
        if (showDeleteAllDialog) {
            DeleteAllConfirmationDialog(
                assessmentCount = assessments.size,
                confirmationText = deleteConfirmationText,
                onConfirmationTextChange = { deleteConfirmationText = it },
                onConfirm = {
                    coroutineScope.launch {
                        isDeleting = true
                        showDeleteAllDialog = false

                        val result = withContext(Dispatchers.IO) {
                            deleteAllUserData(context, currentUser!!.registration_id)
                        }

                        if (result.success) {
                            successMessage = "All your data has been deleted"
                            assessments = emptyList()

                            // Logout user after data deletion
                            logoutUser(context)

                            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()

                            // Small delay to show success message
                            delay(1500)
                            navController.popBackStack()
                        } else {
                            errorMessage = result.message
                            isDeleting = false
                        }

                        deleteConfirmationText = ""
                    }
                },
                onDismiss = {
                    showDeleteAllDialog = false
                    deleteConfirmationText = ""
                }
            )
        }
    }
}

@Composable
fun PrivacyHeader(
    onBack: () -> Unit,
    isLoggedIn: Boolean,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.9f))
            .border(width = 0.5.dp, color = SoftPurpleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftPurpleBg)
                    .border(1.dp, SoftPurpleBorder, RoundedCornerShape(12.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Privacy & Data",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTextPrimary
                )
                Text(
                    if (isLoggedIn) "Manage your data and history"
                    else "Sign in to view your data",
                    fontSize = 13.sp,
                    color = ColorTextSecondary
                )
            }

            // Refresh button (only show when logged in)
            if (isLoggedIn) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftPurpleBg)
                        .border(1.dp, SoftPurpleBorder, RoundedCornerShape(12.dp))
                        .clickable { onRefresh() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            // Shield icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryPurpleGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NotLoggedInContent(
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lock icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SoftPurpleBg)
                .border(2.dp, SoftPurpleBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Not Signed In",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Please sign in to view and manage your assessment history and privacy settings",
            fontSize = 14.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
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
                        brush = PrimaryPurpleGradient,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Go to Login",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun DataManagementCard(
    onDeleteAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg),
        border = BorderStroke(1.dp, SoftPurpleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Data Management",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Delete all data option
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDeleteAllClick() },
                color = ColorError.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, ColorError.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Warning icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorError.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ColorError,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Delete All Data",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorError
                        )
                        Text(
                            "Permanently delete your account and all assessments",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )
                    }

                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = ColorError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Info text
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = SoftPurpleBg
            ) {
                Text(
                    "⚠️ Deleting data is permanent and cannot be undone",
                    fontSize = 12.sp,
                    color = ColorTextSecondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun HistoryHeader(
    totalCount: Int,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Assessment History",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary
        )

        Spacer(modifier = Modifier.weight(1f))

        // Refresh button
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SoftPurpleBg)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Count badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SoftPurpleBg,
            border = BorderStroke(1.dp, SoftPurpleBorder)
        ) {
            Text(
                "$totalCount ${if (totalCount == 1) "item" else "items"}",
                fontSize = 12.sp,
                color = Color(0xFF7C3AED),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyHistoryContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Empty state icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SoftPurpleBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF7C3AED).copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "No Assessments Yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Your assessment history will appear here",
            fontSize = 14.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AssessmentHistoryItem(
    assessment: AssessmentItem,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg),
        border = BorderStroke(1.dp, SoftPurpleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryPurpleGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Assessment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                // Type and scores
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Assessment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date and video count
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ColorTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        formatDate(assessment.createdAt),
                        fontSize = 12.sp,
                        color = ColorTextTertiary
                    )

                    if (assessment.videoCount > 0) {
                        Spacer(modifier = Modifier.width(12.dp))

                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            tint = ColorTextTertiary,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            "${assessment.videoCount} video${if (assessment.videoCount > 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = ColorTextTertiary
                        )
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftPurpleBg)
                    .border(1.dp, SoftPurpleBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ColorError,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    assessment: AssessmentItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = ColorCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = ColorError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Delete Assessment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            }
        },
        text = {
            Column {
                Text(
                    "Are you sure you want to delete this assessment?",
                    fontSize = 14.sp,
                    color = ColorTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Assessment summary
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SoftPurpleBg,
                    border = BorderStroke(1.dp, SoftPurpleBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            assessment.assessmentType,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary
                        )

                        Text(
                            "Date: ${formatDate(assessment.createdAt)}",
                            fontSize = 12.sp,
                            color = ColorTextSecondary
                        )

                        if (assessment.videoCount > 0) {
                            Text(
                                "Videos: ${assessment.videoCount}",
                                fontSize = 12.sp,
                                color = ColorTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "⚠️ This action cannot be undone",
                    fontSize = 12.sp,
                    color = ColorError,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorError
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ColorTextSecondary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteAllConfirmationDialog(
    assessmentCount: Int,
    confirmationText: String,
    onConfirmationTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isConfirmed = confirmationText.equals("DELETE", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = ColorCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = ColorError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Delete All Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            }
        },
        text = {
            Column {
                Text(
                    "This will permanently delete:",
                    fontSize = 14.sp,
                    color = ColorTextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List of items to delete
                listOf(
                    "Your user account",
                    "$assessmentCount assessment records",
                    "All video recordings",
                    "All assessment data (CSV files)"
                ).forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = ColorError,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            item,
                            fontSize = 13.sp,
                            color = ColorTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Warning
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ColorError.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ColorError.copy(alpha = 0.3f))
                ) {
                    Text(
                        "⚠️ This action is IRREVERSIBLE. All your data will be permanently lost.",
                        fontSize = 12.sp,
                        color = ColorError,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Extra confirmation for safety
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = onConfirmationTextChange,
                    label = { Text("Type DELETE to confirm") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = confirmationText.isNotBlank() && !isConfirmed,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorError,
                        unfocusedBorderColor = ColorBorder,
                        cursorColor = ColorError,
                        focusedLabelColor = ColorError
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontWeight = if (confirmationText.isNotBlank() && !isConfirmed)
                            FontWeight.Bold else FontWeight.Normal
                    )
                )

                if (confirmationText.isNotBlank() && !isConfirmed) {
                    Text(
                        "Please type 'DELETE' exactly to confirm",
                        fontSize = 12.sp,
                        color = ColorError,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConfirmed) ColorError else ColorError.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete Everything", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ColorTextSecondary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StatusMessage(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isError)
            ColorError.copy(alpha = 0.1f)
        else
            ColorSuccess.copy(alpha = 0.1f),
        border = BorderStroke(
            1.dp,
            if (isError)
                ColorError.copy(alpha = 0.3f)
            else
                ColorSuccess.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) ColorError else ColorSuccess,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                message,
                modifier = Modifier.weight(1f),
                color = if (isError) ColorError else ColorSuccess,
                fontSize = 13.sp
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = if (isError) ColorError else ColorSuccess,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ==================== API FUNCTIONS ====================

suspend fun fetchAssessments(registrationId: String): List<AssessmentItem> {
    return try {
        Log.d("PrivacyScreen", "Fetching assessments for: $registrationId")
        val url = URL("http://203.110.243.202:8000/api/student/${registrationId}/assessments")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        Log.d("PrivacyScreen", "Response code: $responseCode")

        val response = if (responseCode == 200) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            Log.e("PrivacyScreen", "Error response: $error")
            ""
        }

        connection.disconnect()

        if (responseCode == 200) {
            val json = JSONObject(response)
            Log.d("PrivacyScreen", "Response JSON: $json")

            val assessmentsArray = json.getJSONArray("assessments")
            val count = assessmentsArray.length()
            Log.d("PrivacyScreen", "Found $count assessments")

            List(count) { index ->
                val item = assessmentsArray.getJSONObject(index)

                // Map backend fields to your app's expected fields
                val id = item.getInt("id")

                // Since backend doesn't have assessment_type, create a default
                val assessmentType = "Mental Health Assessment"

                // Get assessment_score as Double, convert to Int? for display
                val assessmentScore = if (item.has("assessment_score") && !item.isNull("assessment_score")) {
                    item.getDouble("assessment_score").toInt()
                } else null

                // Map gad7_score from backend (currently null in your data)
                val gad7Score = if (item.has("gad7_score") && !item.isNull("gad7_score")) {
                    item.getInt("gad7_score")
                } else null

                // Map phq_score to questionnaire_score
                val questionnaireScore = if (item.has("phq_score") && !item.isNull("phq_score")) {
                    item.getInt("phq_score")
                } else null

                val createdAt = item.getString("created_at")
                val videoCount = item.getInt("video_count")

                Log.d("PrivacyScreen", "Parsed assessment $id: type=$assessmentType, score=$assessmentScore, phq=$questionnaireScore")

                AssessmentItem(
                    id = id,
                    assessmentType = assessmentType,
                    gad7Score = gad7Score,
                    questionnaireScore = questionnaireScore,
                    createdAt = createdAt,
                    videoCount = videoCount
                )
            }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("PrivacyScreen", "Fetch assessments error", e)
        emptyList()
    }
}

suspend fun deleteSingleAssessment(
    context: Context,
    assessmentId: Int
): DeleteResponse {
    return try {
        Log.d("PrivacyScreen", "Deleting assessment: $assessmentId")
        val url = URL("http://203.110.243.202" + ":8000/api/assessment/${assessmentId}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        val response = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        connection.disconnect()

        if (responseCode in 200..299) {
            Log.d("PrivacyScreen", "Delete successful")
            DeleteResponse(
                success = true,
                message = "Assessment deleted successfully"
            )
        } else {
            Log.e("PrivacyScreen", "Delete failed with code: $responseCode")
            DeleteResponse(
                success = false,
                message = "Failed to delete assessment (Code: $responseCode)"
            )
        }
    } catch (e: Exception) {
        Log.e("PrivacyScreen", "Delete assessment error", e)
        DeleteResponse(
            success = false,
            message = "Connection failed: ${e.message}"
        )
    }
}

suspend fun deleteAllUserData(
    context: Context,
    registrationId: String
): DeleteResponse {
    return try {
        Log.d("PrivacyScreen", "Deleting all data for: $registrationId")
        val url = URL("http://203.110.243.202:8000/api/student/delete-all/${registrationId}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        val response = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        connection.disconnect()

        return when (responseCode) {
            in 200..299 -> {
                Log.d("PrivacyScreen", "Delete all successful: $response")
                // Parse response to get details
                val json = try {
                    JSONObject(response)
                } catch (e: Exception) {
                    JSONObject()
                }

                val summary = if (json.has("deleted_summary")) {
                    val summaryJson = json.getJSONObject("deleted_summary")
                    DeleteSummary(
                        studentId = summaryJson.optString("student_id", registrationId),
                        assessmentsDeleted = summaryJson.optInt("assessments_deleted", 0),
                        videosDeleted = summaryJson.optInt("videos_deleted", 0),
                        filesDeleted = summaryJson.optInt("files_deleted", 0)
                    )
                } else {
                    DeleteSummary()
                }

                // Also delete local files
                deleteLocalUserFiles(context, registrationId)

                DeleteResponse(
                    success = true,
                    message = json.optString("message", "All data deleted successfully"),
                    deletedSummary = summary
                )
            }
            404 -> {
                Log.e("PrivacyScreen", "User not found")
                DeleteResponse(
                    success = false,
                    message = "User not found. You may have been already deleted."
                )
            }
            401 -> {
                Log.e("PrivacyScreen", "Unauthorized")
                DeleteResponse(
                    success = false,
                    message = "Unauthorized. Please login again."
                )
            }
            else -> {
                val errorMsg = try {
                    JSONObject(response).optString("detail", "Failed to delete data (Code: $responseCode)")
                } catch (e: Exception) {
                    "Server error (code: $responseCode)"
                }
                Log.e("PrivacyScreen", errorMsg)
                DeleteResponse(success = false, message = errorMsg)
            }
        }
    } catch (e: ConnectException) {
        Log.e("PrivacyScreen", "Connection failed", e)
        DeleteResponse(success = false, message = "Cannot connect to server. Check your internet connection.")
    } catch (e: SocketTimeoutException) {
        Log.e("PrivacyScreen", "Timeout", e)
        DeleteResponse(success = false, message = "Connection timeout. Server is not responding.")
    } catch (e: Exception) {
        Log.e("PrivacyScreen", "Delete all error", e)
        DeleteResponse(success = false, message = "Connection failed: ${e.message}")
    }
}

fun deleteLocalUserFiles(context: Context, registrationId: String) {
    try {
        // Delete app-specific files in Downloads
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appFolder = File(downloadsDir, "MochanApp")

        if (appFolder.exists()) {
            // Find and delete user's folder (by scanning for folders with their anonymous ID)
            val userSession = UserSessionHelper.getUserData(context)
            if (userSession.isLoggedIn && userSession.anonymousId.isNotBlank()) {
                val userFolder = File(appFolder, userSession.anonymousId)
                if (userFolder.exists()) {
                    userFolder.deleteRecursively()
                    Log.d("PrivacyScreen", "Deleted local user folder: ${userFolder.absolutePath}")
                }
            }
        }

        // Clear all preferences - use the function from ProfileScreen
        logoutUser(context)

    } catch (e: Exception) {
        Log.e("PrivacyScreen", "Error deleting local files", e)
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val date = inputFormat.parse(dateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}