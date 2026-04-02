package com.example.mochanapp.viewmodels

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mochanapp.utils.FaceLandmarkerHelper
import com.example.mochanapp.utils.TFLiteModelHelper
import com.example.mochanapp.utils.VideoRecorderHelper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.*
import android.os.Environment

// Data class for frame data
data class FrameData(
    val frameNumber: Int,
    val timestamp: Float,
    val aus: Map<String, Float>,
    val gazeX: Float,
    val gazeY: Float
) {
    fun toMap(): Map<String, Float> {
        val map = mutableMapOf<String, Float>()
        map["frame"] = frameNumber.toFloat()
        map["timestamp"] = timestamp
        map.putAll(aus)
        map["gaze_x"] = gazeX
        map["gaze_y"] = gazeY
        return map
    }
}

class CameraViewModel : ViewModel() {

    companion object {
        private const val TAG = "CameraViewModel"
        private const val FRAME_INTERVAL_MS = 100L // 10 FPS
        private const val MIN_FRAMES_FOR_ANALYSIS = 50

        // These are the 18 AUs your app uses
        private val keyAUs = listOf(
            "AU01_r", "AU02_r", "AU04_r", "AU05_r", "AU06_r", "AU07_r", "AU09_r", "AU10_r",
            "AU12_r", "AU14_r", "AU15_r", "AU17_r", "AU20_r", "AU23_r", "AU25_r", "AU26_r",
            "AU28_r", "AU45_r"
        )

        private val auBiasCorrections = mapOf(
            "AU06_r" to -0.20f,
            "AU07_r" to 0.30f
        )
    }

    // State flows for UI
    private val _faceDetected = MutableStateFlow(false)
    val faceDetected: StateFlow<Boolean> = _faceDetected.asStateFlow()

    private val _frameCount = MutableStateFlow(0)
    val frameCount: StateFlow<Int> = _frameCount.asStateFlow()

    private val _frameData = MutableStateFlow<List<Map<String, Float>>>(emptyList())
    val frameData: StateFlow<List<Map<String, Float>>> = _frameData.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _au17Value = MutableStateFlow(0f)
    val au17Value: StateFlow<Float> = _au17Value.asStateFlow()

    private val _cameraError = MutableStateFlow<String?>(null)
    val cameraError: StateFlow<String?> = _cameraError.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Track if video capture is ready
    private val _isVideoCaptureReady = MutableStateFlow(false)
    val isVideoCaptureReady: StateFlow<Boolean> = _isVideoCaptureReady.asStateFlow()

    // Helpers
    private lateinit var faceHelper: FaceLandmarkerHelper
    private lateinit var videoRecorderHelper: VideoRecorderHelper
    private lateinit var tfliteHelper: TFLiteModelHelper
    private val bgExecutor = Executors.newSingleThreadExecutor()

    // Recording state
    private var startTime = 0L
    private var lastFrameTime = 0L
    private var anonymousId = ""

    // AU history for smoothing
    private val auHistory = mutableMapOf<String, Float>()
    private val smoothingFactor = 0.25f

    // File paths for upload
    private var lastVideoFilePath: String? = null
    private var lastAuCsvPath: String? = null

    // Video capture property
    private var _videoCapture: VideoCapture<Recorder>? = null

    // Flag to track if we're in the middle of a recording session
    private var isRecordingSessionActive = false

    // FIXED: Get VideoCapture instance with null safety
    fun getVideoCapture(): VideoCapture<Recorder>? {
        return if (::videoRecorderHelper.isInitialized) {
            if (_videoCapture == null) {
                _videoCapture = videoRecorderHelper.initializeVideoCapture()
                _isVideoCaptureReady.value = true
                Log.d(TAG, "VideoCapture initialized successfully")
            }
            _videoCapture
        } else {
            Log.e(TAG, "VideoRecorderHelper not initialized yet")
            null
        }
    }

    // ========== Getter for video path ==========
    fun getLastVideoPath(): String? {
        return lastVideoFilePath
    }

    // ========== Getter for AU CSV path ==========
    fun getLastAuCsvPath(): String? {
        return lastAuCsvPath
    }

    // ========== Check if ready to record ==========
    fun canStartRecording(): Boolean {
        return ::videoRecorderHelper.isInitialized && _videoCapture != null && !_isRecording.value
    }

    // ========== Reset recording state only (for validation dialog) ==========
    fun resetRecordingState() {
        Log.d(TAG, "Resetting recording state only")
        _frameData.value = emptyList()
        _frameCount.value = 0
        startTime = System.currentTimeMillis()
        lastFrameTime = startTime
        auHistory.clear()
    }

    // ========== Resume recording after being stopped ==========
    fun resumeRecording() {
        if (!_isRecording.value) {
            Log.d(TAG, "Resuming recording for user: $anonymousId")

            // Reset frame data but keep the video recording going
            _frameData.value = emptyList()
            _frameCount.value = 0
            startTime = System.currentTimeMillis()
            lastFrameTime = startTime
            auHistory.clear()

            // Actually start the video recording again
            if (::videoRecorderHelper.isInitialized) {
                videoRecorderHelper.startRecording(anonymousId)
            }

            _isRecording.value = true
            isRecordingSessionActive = true
            Log.d(TAG, "Recording resumed successfully")
        } else {
            Log.d(TAG, "Already recording, ignoring resume request")
        }
    }

    // ========== Initialize in correct order ==========
    fun initialize(context: Context, anonymousId: String) {
        this.anonymousId = anonymousId

        Log.d(TAG, "Starting CameraViewModel initialization for user: $anonymousId")

        // Step 1: Initialize VideoRecorderHelper FIRST
        try {
            videoRecorderHelper = VideoRecorderHelper(context, object : VideoRecorderHelper.VideoRecordingListener {
                override fun onRecordingStarted(filePath: String) {
                    Log.d(TAG, "Recording started: $filePath")
                    lastVideoFilePath = filePath

                    // Save video path to SharedPreferences
                    try {
                        val prefs = context.getSharedPreferences("file_paths", Context.MODE_PRIVATE)
                        prefs.edit().putString("video_path", filePath).apply()
                        Log.d(TAG, "Video path saved to SharedPreferences: $filePath")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving video path to SharedPreferences: ${e.message}")
                    }
                }

                override fun onRecordingStopped(filePath: String, durationMs: Long) {
                    Log.d(TAG, "Recording stopped: $filePath, duration: ${durationMs}ms")
                    isRecordingSessionActive = false
                }

                override fun onRecordingError(error: String) {
                    Log.e(TAG, "Recording error: $error")
                    _cameraError.value = error
                    isRecordingSessionActive = false
                }
            })

            // Step 2: Initialize VideoCapture immediately
            _videoCapture = videoRecorderHelper.initializeVideoCapture()
            _isVideoCaptureReady.value = true
            Log.d(TAG, "VideoRecorderHelper and VideoCapture initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize VideoRecorderHelper: ${e.message}")
            _cameraError.value = "Video recording initialization failed"
            return
        }

        // Step 3: Initialize TFLiteModelHelper
        try {
            tfliteHelper = TFLiteModelHelper(context)
            Log.d(TAG, "TFLiteModelHelper initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TFLiteModelHelper: ${e.message}")
            // Continue anyway, fallback will be used
        }

        // Step 4: Initialize FaceLandmarker on background thread
        bgExecutor.execute {
            try {
                faceHelper = FaceLandmarkerHelper(
                    context = context,
                    runningMode = RunningMode.LIVE_STREAM,
                    minFaceDetectionConfidence = 0.5f,
                    minFaceTrackingConfidence = 0.5f,
                    minFacePresenceConfidence = 0.5f,
                    maxNumFaces = 1,
                    currentDelegate = FaceLandmarkerHelper.DELEGATE_CPU,
                    faceLandmarkerHelperListener = object : FaceLandmarkerHelper.LandmarkerListener {
                        override fun onError(error: String, errorCode: Int) {
                            Log.e(TAG, "FaceLandmarker error: $error")
                            _cameraError.value = error
                        }

                        override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
                            processFaceResults(resultBundle)
                        }

                        override fun onAU17(au17: Float) {
                            _au17Value.value = au17
                        }

                        override fun onEmpty() {
                            _faceDetected.value = false
                        }
                    }
                )
                _isInitialized.value = true
                Log.d(TAG, "FaceLandmarkerHelper initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize FaceLandmarker: ${e.message}")
                _cameraError.value = "Face detection initialization failed"
            }
        }
    }

    fun processFrame(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        if (::faceHelper.isInitialized) {
            faceHelper.detectLiveStream(imageProxy, isFrontCamera)
        } else {
            imageProxy.close()
        }
    }

    private fun processFaceResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
        val currentTime = System.currentTimeMillis()
        val result = resultBundle.result

        if (result.faceLandmarks().isNotEmpty()) {
            _faceDetected.value = true
            val landmarks = result.faceLandmarks()[0]

            if (_isRecording.value && (currentTime - lastFrameTime) >= FRAME_INTERVAL_MS) {
                if (landmarks.size >= 468) {
                    // Calculate AUs and gaze
                    val auData = calculateAUs(landmarks)
                    val gazeData = calculateGaze(landmarks)

                    val frameNumber = _frameCount.value + 1
                    val preciseTimestamp = (currentTime - startTime) / 1000.0f

                    val frameEntry = mutableMapOf<String, Float>()
                    frameEntry["frame"] = frameNumber.toFloat()
                    frameEntry["timestamp"] = preciseTimestamp
                    frameEntry.putAll(auData)
                    frameEntry.putAll(gazeData)

                    // Update state
                    _frameCount.value = frameNumber
                    _frameData.value = _frameData.value + frameEntry
                    lastFrameTime = currentTime

                    // Log every 10th frame for debugging
                    if (frameNumber % 10 == 0) {
                        val nonZeroAUs = auData.values.count { it > 0.1f }
                        Log.d(TAG, "Frame $frameNumber: $nonZeroAUs/${auData.size} active AUs, AU12=${auData["AU12_r"]}, AU04=${auData["AU04_r"]}")
                    }
                }
            }
        } else {
            _faceDetected.value = false
        }
    }

    // ========== AU CALCULATION FUNCTIONS ==========

    private fun calculateAUs(landmarks: List<NormalizedLandmark>): Map<String, Float> {
        val aus = mutableMapOf<String, Float>()
        try {
            if (landmarks.size < 468) {
                Log.w(TAG, "Insufficient landmarks: ${landmarks.size}")
                return keyAUs.associateWith { 0.0f }
            }

            val faceWidth = abs(landmarks[454].x() - landmarks[234].x())
            val faceHeight = abs(landmarks[10].y() - landmarks[152].y())
            val normFactor = sqrt(faceWidth * faceHeight).coerceAtLeast(0.01f)

            // AU01 - Inner Brow Raiser
            val innerBrowPoints = listOf(21, 22, 23, 24, 25, 251, 252, 253, 254, 255)
            val browRaise = abs(averageY(landmarks, innerBrowPoints) - 0.392f)
            aus["AU01_r"] = scaleAndClamp(browRaise * 9.5f)

            // AU02 - Outer Brow Raiser
            val outerBrowPoints = listOf(46, 53, 70, 276, 283, 300)
            aus["AU02_r"] = scaleAndClamp(min(3.0f, abs(averageY(landmarks, outerBrowPoints) - 0.355f) * 8.0f))

            // AU04 - Brow Lowerer
            val browLowererPoints = listOf(55, 65, 70, 285, 295, 300)
            val browLower = (0.42f - averageY(landmarks, browLowererPoints)) / normFactor
            aus["AU04_r"] = scaleAndClamp(browLower * 280f)

            // AU05 - Upper Lid Raiser
            val eyeHeight = eyeOpeningHeightImproved(landmarks)
            aus["AU05_r"] = scaleAndClamp(max(0f, (eyeHeight - 0.0145f) * 85f))

            // AU06 - Cheek Raiser
            val cheekPoints = listOf(116, 117, 345, 346)
            val cheekRaise = abs(0.540f - averageY(landmarks, cheekPoints)) / normFactor
            aus["AU06_r"] = scaleAndClamp(cheekRaise * 120f)

            // AU07 - Lid Tightener
            aus["AU07_r"] = scaleAndClamp(max(0f, (0.0245f - eyeHeight) * 105f))

            // AU09 - Nose Wrinkler
            aus["AU09_r"] = scaleAndClamp(min(2.5f, abs(0.468f - averageY(landmarks, listOf(48,64,278,294))) * 6.2f))

            // AU10 - Upper Lip Raiser
            val upperLipPoints = listOf(0, 11, 12, 13, 14, 15, 16, 17, 18)
            val lipToNose = abs(0.58f - averageY(landmarks, upperLipPoints)) / normFactor
            aus["AU10_r"] = scaleAndClamp(lipToNose * 160f)

            // AU12 - Lip Corner Puller (smiling)
            val leftCorner = landmarks[61]
            val rightCorner = landmarks[291]
            val lipCenter = landmarks[17]
            val cornerElevation = -(((leftCorner.y() + rightCorner.y()) / 2) - lipCenter.y())
            aus["AU12_r"] = scaleAndClamp(max(0f, cornerElevation * 28f))

            // AU14 - Dimpler
            val lipWidth = abs(leftCorner.x() - rightCorner.x())
            aus["AU14_r"] = scaleAndClamp((lipWidth / normFactor) * 700f)

            // AU15 - Lip Corner Depressor (frowning)
            val cornerDrop = (lipCenter.y() - (leftCorner.y() + rightCorner.y()) / 2)
            aus["AU15_r"] = scaleAndClamp(max(0f, cornerDrop * 28f))

            // AU17 - Chin Raiser
            aus["AU17_r"] = scaleAndClamp(max(0f, (lipCenter.y() - landmarks[152].y()) * 40f))

            // AU20 - Lip stretcher
            aus["AU20_r"] = scaleAndClamp((lipWidth / normFactor) * 1400f)

            // AU23 - Lip Tightener
            val lipSeparation = abs(landmarks[13].y() - landmarks[14].y())
            aus["AU23_r"] = scaleAndClamp(max(0f, (0.0248f - lipSeparation) * 82f))

            // AU25 - Lips part
            aus["AU25_r"] = scaleAndClamp(max(0f, (lipSeparation - 0.0145f) * 65f))

            // AU26 - Jaw Drop
            aus["AU26_r"] = scaleAndClamp(max(0f, ((landmarks[152].y() - landmarks[1].y()) - 0.242f) * 25f))

            // AU28 - Lip Suck
            val innerLipSeparation = abs(landmarks[12].y() - landmarks[15].y())
            aus["AU28_r"] = scaleAndClamp(max(0f, (0.0115f - innerLipSeparation) * 155f))

            // AU45 - Blink
            val blinkThreshold = 0.0195f
            aus["AU45_r"] = scaleAndClamp(max(0f, (blinkThreshold - eyeHeight) * 175f))

            // Apply bias corrections
            aus.keys.forEach { auKey ->
                val correction = auBiasCorrections[auKey] ?: 0f
                aus[auKey] = scaleAndClamp((aus[auKey] ?: 0f) + correction)
            }

            // Apply temporal smoothing
            applySmoothingToAUs(aus)

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating optimized AUs: ${e.message}")
            return keyAUs.associateWith { 0.0f }
        }
        return aus
    }

    private fun calculateGaze(landmarks: List<NormalizedLandmark>): Map<String, Float> {
        val leftEyeCenter = landmarks[159]
        val rightEyeCenter = landmarks[386]
        val gazeX = ((leftEyeCenter.x() + rightEyeCenter.x()) / 2 - 0.5f) * 10
        val gazeY = ((leftEyeCenter.y() + rightEyeCenter.y()) / 2 - 0.5f) * 10
        return mapOf("gaze_x" to gazeX, "gaze_y" to gazeY)
    }

    private fun eyeOpeningHeightImproved(landmarks: List<NormalizedLandmark>): Float {
        return try {
            val leftEyeTop = listOf(159, 158, 157)
            val leftEyeBottom = listOf(145, 144, 143)
            val rightEyeTop = listOf(386, 385, 384)
            val rightEyeBottom = listOf(374, 373, 372)
            val leftEyeHeight = abs(averageY(landmarks, leftEyeTop) - averageY(landmarks, leftEyeBottom))
            val rightEyeHeight = abs(averageY(landmarks, rightEyeTop) - averageY(landmarks, rightEyeBottom))
            (leftEyeHeight + rightEyeHeight) / 2f
        } catch (e: Exception) {
            Log.w(TAG, "Error calculating eye opening: ${e.message}")
            val leftEyeTop = landmarks[159]
            val leftEyeBottom = landmarks[145]
            val rightEyeTop = landmarks[386]
            val rightEyeBottom = landmarks[374]
            (abs(leftEyeTop.y() - leftEyeBottom.y()) + abs(rightEyeTop.y() - rightEyeBottom.y())) / 2f
        }
    }

    private fun averageY(landmarks: List<NormalizedLandmark>, points: List<Int>): Float {
        return try {
            var sum = 0f
            var validPoints = 0
            for (p in points) {
                if (p < landmarks.size) {
                    sum += landmarks[p].y()
                    validPoints++
                }
            }
            if (validPoints == 0) {
                Log.w(TAG, "No valid points for averaging")
                return 0.5f
            }
            sum / validPoints
        } catch (e: Exception) {
            Log.w(TAG, "Error in averageY: ${e.message}")
            0.5f
        }
    }

    private fun scaleAndClamp(value: Float): Float {
        return value.coerceIn(0f, 5f)
    }

    private fun applySmoothingToAUs(aus: MutableMap<String, Float>) {
        aus.keys.forEach { auKey ->
            val currentValue = aus[auKey] ?: 0f
            val historicalValue = auHistory[auKey] ?: currentValue
            val smoothedValue = currentValue * (1f - smoothingFactor) + historicalValue * smoothingFactor
            aus[auKey] = smoothedValue
            auHistory[auKey] = smoothedValue
        }
    }

    // ========== PUBLIC FUNCTIONS ==========

    fun startRecording() {
        if (!::videoRecorderHelper.isInitialized) {
            Log.e(TAG, "Cannot start recording: VideoRecorderHelper not initialized")
            _cameraError.value = "Video recorder not initialized"
            return
        }

        // If already recording, don't start again
        if (_isRecording.value) {
            Log.d(TAG, "Already recording, ignoring start request")
            return
        }

        Log.d(TAG, "Starting recording for user: $anonymousId")

        // Reset all recording state
        _isRecording.value = true
        _frameData.value = emptyList()
        _frameCount.value = 0
        startTime = System.currentTimeMillis()
        lastFrameTime = startTime
        auHistory.clear()
        isRecordingSessionActive = true

        // Start video recording
        videoRecorderHelper.startRecording(anonymousId)
    }

    fun stopRecording() {
        if (!_isRecording.value) {
            Log.d(TAG, "Not recording, ignoring stop request")
            return
        }

        Log.d(TAG, "Stopping recording. Total frames: ${_frameCount.value}")

        _isRecording.value = false

        if (::videoRecorderHelper.isInitialized) {
            videoRecorderHelper.stopRecording()
        }

        // Don't clear frame data here - we need it for validation
        // _frameData.value = emptyList()  // KEEP THIS COMMENTED
    }

    fun validateFacialData(): Boolean {
        if (_frameData.value.size < MIN_FRAMES_FOR_ANALYSIS) {
            Log.w(TAG, "Insufficient frames: ${_frameData.value.size} < $MIN_FRAMES_FOR_ANALYSIS")
            return false
        }
        val avgAUValues = calculateAverageAUValuesForValidation()
        val hasNonZeroAUs = avgAUValues.values.any { it > 0.1f }
        if (!hasNonZeroAUs) {
            Log.w(TAG, "All AU values are near zero - likely no face detected")
            return false
        }
        Log.d(TAG, "Facial data validation passed: ${_frameData.value.size} frames, meaningful AU data")
        return true
    }

    private fun calculateAverageAUValuesForValidation(): Map<String, Float> {
        val auAverages = mutableMapOf<String, Float>()
        if (_frameData.value.isEmpty()) {
            return auAverages
        }
        keyAUs.forEach { auKey ->
            val auValues = _frameData.value.mapNotNull { it[auKey] }
            auAverages[auKey] = if (auValues.isNotEmpty()) auValues.average().toFloat() else 0.0f
        }
        return auAverages
    }

    /**
     * Get depression prediction from AI model
     */
    fun getPrediction(): TFLiteModelHelper.PredictionResult? {
        Log.d(TAG, "========== GETTING PREDICTION ==========")
        Log.d(TAG, "Frame data size: ${_frameData.value.size}")

        if (_frameData.value.isEmpty()) {
            Log.e(TAG, "No frame data available for prediction")
            return null
        }

        // Log sample frame data to verify
        val firstFrame = _frameData.value.firstOrNull()
        firstFrame?.let {
            Log.d(TAG, "Sample frame keys: ${it.keys.joinToString()}")
            // Log key AUs for depression
            Log.d(TAG, "AU12_r (smiling): ${it["AU12_r"]}")
            Log.d(TAG, "AU04_r (frowning): ${it["AU04_r"]}")
            Log.d(TAG, "AU15_r (lip corner depressor): ${it["AU15_r"]}")
        }

        val startTime = System.currentTimeMillis()
        val result = if (::tfliteHelper.isInitialized) {
            tfliteHelper.predictDepression(_frameData.value)
        } else {
            Log.e(TAG, "TFLiteHelper not initialized, cannot get prediction")
            null
        }
        val inferenceTime = System.currentTimeMillis() - startTime

        Log.d(TAG, "Prediction result: ${result != null}")
        result?.let {
            Log.d(TAG, "AI Score: ${it.score}/24")
            Log.d(TAG, "AI Label: ${it.prediction}")
            Log.d(TAG, "Confidence: ${it.confidence}")
            Log.d(TAG, "Model version: ${it.modelVersion}")
            Log.d(TAG, "Inference time: ${inferenceTime}ms")
        } ?: run {
            Log.e(TAG, "Prediction returned null")
        }

        Log.d(TAG, "=========================================")
        return result
    }

    /**
     * Check if AI model is loaded
     */
    fun isModelLoaded(): Boolean {
        return if (::tfliteHelper.isInitialized) {
            tfliteHelper.isModelLoaded()
        } else {
            false
        }
    }

    /**
     * Get model info for debugging
     */
    fun getModelInfo(): String {
        return if (::tfliteHelper.isInitialized) {
            tfliteHelper.getModelInfo()
        } else {
            "TFLiteHelper not initialized"
        }
    }

    // ========== UPDATED: saveCSVWithAUData now returns path and saves to SharedPreferences ==========
    fun saveCSVWithAUData(context: Context): String? {
        if (_frameData.value.isEmpty()) {
            Log.w(TAG, "No frame data to save")
            return null
        }

        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appFolder = File(downloadsDir, "MochanApp")
            val userFolder = File(appFolder, anonymousId)
            if (!userFolder.exists()) userFolder.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "${anonymousId}_depression_AU_${timestamp}.csv"
            val csvFile = File(userFolder, fileName)

            val headers = listOf("frame", "timestamp") + keyAUs + listOf("gaze_x", "gaze_y")
            val csvContent = StringBuilder()
            csvContent.append(headers.joinToString(",")).append("\n")

            for (frame in _frameData.value) {
                val row = headers.map { key ->
                    when (key) {
                        "frame" -> frame["frame"]?.toInt()?.toString() ?: "0"
                        "timestamp" -> String.format("%.3f", frame["timestamp"] ?: 0f)
                        else -> String.format("%.3f", frame[key] ?: 0.0f)
                    }
                }
                csvContent.append(row.joinToString(",")).append("\n")
            }

            csvFile.writeText(csvContent.toString())
            lastAuCsvPath = csvFile.absolutePath
            Log.d(TAG, "AU CSV saved: ${csvFile.absolutePath} (${_frameData.value.size} frames)")

            // Save AU CSV path to SharedPreferences
            try {
                val prefs = context.getSharedPreferences("file_paths", Context.MODE_PRIVATE)
                prefs.edit().putString("au_csv_path", csvFile.absolutePath).apply()
                Log.d(TAG, "AU CSV path saved to SharedPreferences: ${csvFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving AU CSV path to SharedPreferences: ${e.message}")
            }

            return csvFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error saving AU CSV: ${e.message}")
            return null
        }
    }

    fun reset() {
        _isRecording.value = false
        _frameData.value = emptyList()
        _frameCount.value = 0
        _faceDetected.value = false
        _au17Value.value = 0f
        _cameraError.value = null
        auHistory.clear()
        isRecordingSessionActive = false
        Log.d(TAG, "ViewModel reset")
    }

    override fun onCleared() {
        super.onCleared()
        try {
            if (::faceHelper.isInitialized) {
                faceHelper.clearFaceLandmarker()
            }
            if (::videoRecorderHelper.isInitialized) {
                videoRecorderHelper.release()
            }
            if (::tfliteHelper.isInitialized) {
                tfliteHelper.close()
            }
            bgExecutor.shutdown()
            Log.d(TAG, "ViewModel cleaned up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up: ${e.message}")
        }
    }
}