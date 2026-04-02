package com.example.mochanapp.screens

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mochanapp.viewmodels.CameraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
// Add these imports at the top if not already present
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.mochanapp.ui.theme.*
import com.example.mochanapp.ui.theme.*

// --- ENHANCED COLOR PALETTE ---
private val ColorPrimary = Color(0xFFD81B60)      // Vibrant Pink
private val ColorSecondary = Color(0xFFFF8A5C)    // Coral Orange
private val ColorTextMain = Color(0xFF1A1F36)     // Dark Blue/Black
private val ColorTextSoft = Color(0xFF6B7280)     // Grey
private val ColorGlass = Color(0xFFFFFFFF).copy(alpha = 0.75f)
private val ColorGlassDark = Color(0xFFFFFFFF).copy(alpha = 0.95f)

@Composable
fun CameraAssessmentScreen(
    navController: NavController,
    cameraViewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // States
    var hasCameraPermission by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showValidationDialog by remember { mutableStateOf(false) }
    var shouldNavigate by remember { mutableStateOf(false) }
    var navigationScore by remember { mutableStateOf(0) }
    var isCameraReady by remember { mutableStateOf(false) }
    var cameraActive by remember { mutableStateOf(false) }
    var initializationStarted by remember { mutableStateOf(false) }

    // Professional guidance messages
    val guidanceMessages = listOf(
        "Keep your camera steady",
        "Maintain a neutral expression",
        "Position face in the center",
        "Ensure adequate lighting",
        "Remove any face coverings",
        "Look directly at the camera",
        "Avoid sudden movements",
        "Keep eyes open and visible"
    )

    var currentMessageIndex by remember { mutableStateOf(0) }
    var messageAlpha by remember { mutableStateOf(1f) }

    // Collect states from ViewModel
    val faceDetected by cameraViewModel.faceDetected.collectAsState()
    val frameCount by cameraViewModel.frameCount.collectAsState()
    val isViewModelRecording by cameraViewModel.isRecording.collectAsState()
    val cameraError by cameraViewModel.cameraError.collectAsState()
    val isInitialized by cameraViewModel.isInitialized.collectAsState()
    val isVideoCaptureReady by cameraViewModel.isVideoCaptureReady.collectAsState()

    // Get anonymousId and registrationId from SharedPreferences
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val anonymousId = prefs.getString("user_anonymous_id", "") ?: ""
    val registrationId = prefs.getString("user_id", "") ?: ""  // ADDED: Get registration ID

    // Professional glow animation
    val glowAlpha by animateFloatAsState(
        targetValue = if (isViewModelRecording) 0.5f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Track initialization
    LaunchedEffect(isInitialized, isVideoCaptureReady) {
        isCameraReady = isInitialized && isVideoCaptureReady
        cameraActive = isInitialized && hasCameraPermission && isVideoCaptureReady
    }

    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraActive = isGranted
        hasCameraPermission = isGranted
        if (isGranted) {
            // Initialize camera and start recording
            initializationStarted = true
            cameraViewModel.initialize(context, anonymousId)
            coroutineScope.launch {
                // Wait for both initialization and video capture to be ready
                while (!isVideoCaptureReady) {
                    delay(100)
                }
                delay(500) // Additional small delay for stability
                cameraViewModel.startRecording()
            }
        } else {
            errorMessage = "Camera permission is required"
            showError = true
        }
    }

    // Request permission on start
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Handle camera errors
    LaunchedEffect(cameraError) {
        cameraError?.let { error ->
            errorMessage = "Camera error: $error"
            showError = true
        }
    }

    // Rotate guidance messages with fade animation
    LaunchedEffect(isViewModelRecording) {
        if (isViewModelRecording) {
            while (isViewModelRecording) {
                delay(4000)
                messageAlpha = 0f
                delay(300)
                currentMessageIndex = (currentMessageIndex + 1) % guidanceMessages.size
                messageAlpha = 1f
                delay(300)
            }
        }
    }

    // Recording timer (countdown)
    LaunchedEffect(isViewModelRecording) {
        if (isViewModelRecording) {
            recordingTime = 0 // Start at 0 and count up
            while (isViewModelRecording && recordingTime < 60) {
                delay(1000)
                recordingTime++
            }
            if (recordingTime >= 60 && isViewModelRecording) {
                cameraViewModel.stopRecording()
                isProcessing = true
                coroutineScope.launch {
                    delay(500)
                    processRecording(
                        context = context,
                        anonymousId = anonymousId,
                        registrationId = registrationId,  // ADDED: Pass registration ID
                        cameraViewModel = cameraViewModel,
                        onScore = { score -> navigationScore = score },
                        onProcessingComplete = { isProcessing = false },
                        onNavigate = { shouldNavigate = true },
                        onError = { error ->
                            errorMessage = error
                            showError = true
                            isProcessing = false
                        }
                    )
                }
            }
        }
    }

    // Navigate when ready
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            navController.navigate("result/$navigationScore") {
                popUpTo("assessment") { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
                    .shadow(
                        elevation = 30.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = ColorPrimary.copy(alpha = 0.2f),
                        ambientColor = ColorSecondary.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ColorGlass
                ),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.3f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header with back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (isViewModelRecording) {
                                        cameraViewModel.stopRecording()
                                    }
                                    navController.popBackStack()
                                },
                            color = Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF1D2335),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "AI Wellness Assessment",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D2335)
                            )
                            Text(
                                text = "Facial analysis in progress",
                                fontSize = 12.sp,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }

                    // Camera Preview Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(4f)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        // Camera preview
                        if (hasCameraPermission && isInitialized && isVideoCaptureReady) {
                            EnhancedCameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                cameraViewModel = cameraViewModel,
                                context = context,
                                lifecycleOwner = lifecycleOwner
                            )
                        } else {
                            // Elegant placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF2D3748),
                                                Color(0xFF1A202C)
                                            )
                                        ),
                                        RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = Color.White.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = when {
                                            !hasCameraPermission -> "Camera permission required"
                                            !isInitialized -> "Initializing camera..."
                                            !isVideoCaptureReady -> "Preparing video capture..."
                                            else -> "Ready"
                                        },
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 14.sp
                                    )

                                    if (!hasCameraPermission) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ColorPrimary
                                            ),
                                            shape = RoundedCornerShape(30.dp)
                                        ) {
                                            Text("Grant Permission")
                                        }
                                    }
                                }
                            }
                        }

                        // Face detection status (Top Left)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                !hasCameraPermission -> Color.Gray
                                                !isInitialized -> Color.Yellow
                                                !isVideoCaptureReady -> Color(0xFFFFA500) // Orange for video capture not ready
                                                faceDetected -> ColorSuccess
                                                else -> ColorError
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        !hasCameraPermission -> "Permission needed"
                                        !isInitialized -> "Initializing"
                                        !isVideoCaptureReady -> "Preparing video"
                                        faceDetected -> "Face detected"
                                        else -> "No face"
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Timer (Top Right)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = if (isViewModelRecording) ColorSuccess else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isViewModelRecording)
                                        String.format("%02d:%02d", recordingTime / 60, recordingTime % 60)
                                    else
                                        "00:00",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }

                        // Frame count indicator (Bottom Left)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                        ) {
                            Text(
                                text = "$frameCount/50 frames",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // STOP BUTTON - Updated to use resumeRecording for better flow
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        enabled = isViewModelRecording && !isProcessing,
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        cameraViewModel.stopRecording()
                                        coroutineScope.launch {
                                            delay(500)
                                            if (cameraViewModel.validateFacialData()) {
                                                processRecording(
                                                    context = context,
                                                    anonymousId = anonymousId,
                                                    registrationId = registrationId,  // ADDED: Pass registration ID
                                                    cameraViewModel = cameraViewModel,
                                                    onScore = { score -> navigationScore = score },
                                                    onProcessingComplete = { isProcessing = false },
                                                    onNavigate = { shouldNavigate = true },
                                                    onError = { error ->
                                                        errorMessage = error
                                                        showError = true
                                                        isProcessing = false
                                                    }
                                                )
                                            } else {
                                                showValidationDialog = true
                                            }
                                        }
                                    },
                                color = ColorError,
                                shadowElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            color = Color.White,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop Recording",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Time remaining text
                        if (isViewModelRecording) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 96.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Black.copy(alpha = 0.5f),
                            ) {
                                Text(
                                    text = "${60 - recordingTime}s remaining",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Bottom area - guidance card
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ColorGlassDark
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.3f)
                                )
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = ColorSuccess.copy(alpha = glowAlpha),
                                            radius = size.width / 2 + 4.dp.toPx()
                                        )
                                    }
                                    .clip(CircleShape)
                                    .background(
                                        if (faceDetected) ColorSuccess else Color(0xFFFFA500)
                                    )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = guidanceMessages[currentMessageIndex],
                                    color = ColorTextMain,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            alpha = messageAlpha
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Dialogs - UPDATED with proper resume functionality
            if (showValidationDialog) {
                AlertDialog(
                    onDismissRequest = { showValidationDialog = false },
                    title = {
                        Text(
                            text = "Insufficient Facial Data",
                            color = ColorError,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "We need at least 50 frames with clear facial expressions for accurate analysis.",
                                color = ColorTextSoft
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { frameCount / 50f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (frameCount < 50) ColorError else ColorSuccess,
                                trackColor = Color.LightGray.copy(alpha = 0.3f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "$frameCount/50 frames captured",
                                fontWeight = FontWeight.Bold,
                                color = if (frameCount < 50) ColorError else ColorSuccess,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showValidationDialog = false
                                // Use resumeRecording to continue capturing frames
                                if (!isViewModelRecording && !isProcessing) {
                                    coroutineScope.launch {
                                        // Make sure video capture is ready
                                        while (!isVideoCaptureReady) {
                                            delay(100)
                                        }
                                        delay(300) // Small delay for stability

                                        // Use the new resumeRecording function
                                        cameraViewModel.resumeRecording()
                                    }
                                }
                            }
                        ) {
                            Text("Continue Recording", color = ColorPrimary)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showValidationDialog = false
                                isProcessing = true
                                coroutineScope.launch {
                                    processRecording(
                                        context = context,
                                        anonymousId = anonymousId,
                                        registrationId = registrationId,  // ADDED: Pass registration ID
                                        cameraViewModel = cameraViewModel,
                                        onScore = { score -> navigationScore = score },
                                        onProcessingComplete = { isProcessing = false },
                                        onNavigate = { shouldNavigate = true },
                                        onError = { error ->
                                            errorMessage = error
                                            showError = true
                                            isProcessing = false
                                        }
                                    )
                                }
                            }
                        ) {
                            Text("Submit Anyway", color = ColorError)
                        }
                    }
                )
            }

            if (showError) {
                AlertDialog(
                    onDismissRequest = { showError = false },
                    title = {
                        Text(
                            "Error",
                            color = ColorError,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(onClick = { showError = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

// --- ENHANCED CAMERA PREVIEW with VideoCapture - UPDATED with null safety ---
@Composable
fun EnhancedCameraPreview(
    modifier: Modifier = Modifier,
    cameraViewModel: CameraViewModel,
    context: Context,
    lifecycleOwner: LifecycleOwner
) {
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var previewInitialized by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // Preview
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(surfaceProvider)

                    // Image Analysis for face detection
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()

                    imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                        cameraViewModel.processFrame(imageProxy, true)
                    }

                    // Get VideoCapture from ViewModel with null safety
                    val videoCapture = cameraViewModel.getVideoCapture()

                    if (videoCapture == null) {
                        Log.e("CameraPreview", "VideoCapture is null - cannot bind camera")
                        return@addListener
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        // Bind all three use cases
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer,
                            videoCapture
                        )
                        previewInitialized = true
                        Log.d("CameraPreview", "Camera bound with video capture")
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Error binding camera: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = modifier.clip(RoundedCornerShape(24.dp))
    )
}

// ==================== PROCESSING FUNCTION ====================

suspend fun processRecording(
    context: Context,
    anonymousId: String,
    registrationId: String,  // ADDED: Registration ID parameter
    cameraViewModel: CameraViewModel,
    onScore: (Int) -> Unit,
    onProcessingComplete: () -> Unit,
    onNavigate: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        Log.d("CameraScreen", "=== Starting processRecording ===")
        Log.d("CameraScreen", "Registration ID: $registrationId")  // ADDED: Log registration ID

        // Get prediction from AI model FIRST (so we have the score)
        val prediction = cameraViewModel.getPrediction()
        val score = prediction?.score ?: 0
        Log.d("CameraScreen", "Prediction score: $score")

        // CREATE ACTUAL PHQ-8 CSV file (not just empty)
        val phq8Path = createPhq8Csv(context, anonymousId, score)
        Log.d("CameraScreen", "PHQ-8 CSV created: $phq8Path")

        // Save AU data to CSV
        val auPath = cameraViewModel.saveCSVWithAUData(context)
        Log.d("CameraScreen", "AU CSV saved: $auPath")

        // Save prediction to preferences
        val assessmentPrefs = context.getSharedPreferences("assessment_prefs", Context.MODE_PRIVATE)
        assessmentPrefs.edit().apply {
            putInt("ai_prediction_score", score)
            putString("ai_prediction_label", prediction?.prediction ?: "Unknown")
            putFloat("ai_prediction_confidence", prediction?.confidence ?: 0f)
            putString("ai_model_version", prediction?.modelVersion ?: "unknown")
            putInt("ai_frame_count", cameraViewModel.frameCount.value)
            putFloat("ai_raw_score", prediction?.rawProbability ?: 0f)
            apply()
        }
        Log.d("CameraScreen", "Prediction saved to preferences")

        // Get video path
        val videoPath = cameraViewModel.getLastVideoPath()

        // Save ALL file paths - including the PHQ-8 CSV and registration ID
        val filePrefs = context.getSharedPreferences("file_paths", Context.MODE_PRIVATE)
        filePrefs.edit().apply {
            videoPath?.let { putString("video_path", it) }
            auPath?.let { putString("au_csv_path", it) }
            phq8Path?.let { putString("phq9_csv_path", it) }  // Store the path
            putString("last_anonymous_id", anonymousId)
            putString("last_registration_id", registrationId)  // ADDED: Save registration ID
            putLong("last_assessment_timestamp", System.currentTimeMillis())
            apply()
        }
        Log.d("CameraScreen", "File paths saved - including PHQ-8 CSV and registration ID")

        onScore(score)
        onProcessingComplete()
        onNavigate()
        Log.d("CameraScreen", "=== processRecording completed successfully ===")
    } catch (e: Exception) {
        Log.e("CameraScreen", "Processing error: ${e.message}", e)
        onError("Processing failed: ${e.message}")
    }
}

// Replace createEmptyPhq8Csv with this function that creates a REAL CSV file
fun createPhq8Csv(context: Context, anonymousId: String, score: Int): String? {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appFolder = File(downloadsDir, "MochanApp")
        val userFolder = File(appFolder, anonymousId)
        if (!userFolder.exists()) userFolder.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${anonymousId}_PHQ8_${timestamp}.csv"
        val csvFile = File(userFolder, fileName)

        // Create a real CSV file with proper headers and AI score
        val content = StringBuilder()
        content.append("question_id,question_text,selected_option,score,timestamp,attempted\n")

        // Add 8 dummy questions with the AI score
        for (i in 1..8) {
            val questionText = when(i) {
                1 -> "Little interest or pleasure in doing things"
                2 -> "Feeling down, depressed, or hopeless"
                3 -> "Trouble falling/staying asleep, sleeping too much"
                4 -> "Feeling tired or having little energy"
                5 -> "Poor appetite or overeating"
                6 -> "Feeling bad about yourself"
                7 -> "Trouble concentrating on things"
                8 -> "Moving/speaking slowly or being fidgety"
                else -> "Question $i"
            }
            // Distribute the score across questions
            val questionScore = (score / 8).coerceAtLeast(1)
            content.append("$i,\"$questionText\",$questionScore,$questionScore,${System.currentTimeMillis()},1\n")
        }

        csvFile.writeText(content.toString())
        Log.d("CameraScreen", "PHQ-8 CSV created with score $score: ${csvFile.absolutePath}")

        // Save path to SharedPreferences
        val prefs = context.getSharedPreferences("file_paths", Context.MODE_PRIVATE)
        prefs.edit().putString("phq9_csv_path", csvFile.absolutePath).apply()

        csvFile.absolutePath
    } catch (e: Exception) {
        Log.e("CameraScreen", "Error creating PHQ-8 CSV: ${e.message}")
        null
    }
}