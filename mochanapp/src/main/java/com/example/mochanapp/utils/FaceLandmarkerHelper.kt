package com.example.mochanapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.abs
import kotlin.math.max

class FaceLandmarkerHelper(
    var minFaceDetectionConfidence: Float = 0.7F,
    var minFaceTrackingConfidence: Float = 0.8F,
    var minFacePresenceConfidence: Float = 0.7F,
    var maxNumFaces: Int = DEFAULT_NUM_FACES,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    val faceLandmarkerHelperListener: LandmarkerListener? = null
) {
    private var faceLandmarker: FaceLandmarker? = null
    private var au17History = 0f
    private val au17SmoothingFactor = 0.3f

    init {
        setupFaceLandmarker()
    }

    fun clearFaceLandmarker() {
        faceLandmarker?.close()
        faceLandmarker = null
        au17History = 0f
    }

    fun isClose(): Boolean {
        return faceLandmarker == null
    }

    fun setupFaceLandmarker() {
        val baseOptionBuilder = BaseOptions.builder()
        when (currentDelegate) {
            DELEGATE_CPU -> baseOptionBuilder.setDelegate(Delegate.CPU)
            DELEGATE_GPU -> baseOptionBuilder.setDelegate(Delegate.GPU)
        }
        baseOptionBuilder.setModelAssetPath(MP_FACE_LANDMARKER_TASK)

        if (runningMode == RunningMode.LIVE_STREAM && faceLandmarkerHelperListener == null) {
            throw IllegalStateException("faceLandmarkerHelperListener must be set when runningMode is LIVE_STREAM.")
        }

        try {
            val baseOptions = baseOptionBuilder.build()
            val optionsBuilder =
                FaceLandmarker.FaceLandmarkerOptions.builder()
                    .setBaseOptions(baseOptions)
                    .setMinFaceDetectionConfidence(minFaceDetectionConfidence)
                    .setMinTrackingConfidence(minFaceTrackingConfidence)
                    .setMinFacePresenceConfidence(minFacePresenceConfidence)
                    .setNumFaces(maxNumFaces)
                    .setOutputFaceBlendshapes(true)
                    .setRunningMode(runningMode)

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }
            val options = optionsBuilder.build()
            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            faceLandmarkerHelperListener?.onError(
                "Face Landmarker failed to initialize. See error logs for details"
            )
            Log.e(TAG, "MediaPipe failed to load the task with error: ${e.message}")
        }
    }

    fun detectLiveStream(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean
    ) {
        if (runningMode != RunningMode.LIVE_STREAM) throw IllegalArgumentException("Attempting to call detectLiveStream while not using RunningMode.LIVE_STREAM")
        val frameTime = SystemClock.uptimeMillis()
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            if (isFrontCamera) postScale(-1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat())
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        detectAsync(mpImage, frameTime)
    }

    @VisibleForTesting
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        faceLandmarker?.detectAsync(mpImage, frameTime)
    }

    fun detectVideoFile(
        videoUri: Uri,
        inferenceIntervalMs: Long
    ): VideoResultBundle? {
        return null
    }

    fun detectImage(image: Bitmap): ResultBundle? {
        if (runningMode != RunningMode.IMAGE) {
            throw IllegalArgumentException("Attempting to call detectImage while not using RunningMode.IMAGE")
        }
        val startTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(image).build()
        faceLandmarker?.detect(mpImage)?.also { landmarkResult ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            return ResultBundle(
                landmarkResult, inferenceTimeMs, image.height, image.width
            )
        }
        faceLandmarkerHelperListener?.onError("Face Landmarker failed to detect.")
        return null
    }

    private fun computeAU17Enhanced(
        faceLandmarks: List<NormalizedLandmark>
    ): Float {
        return try {
            if (faceLandmarks.size < 468) {
                return au17History * (1f - au17SmoothingFactor)
            }

            val chinTipIdx = 152
            val lowerLipIdx = 17
            val jawLineIdx = 18

            val chinTip = faceLandmarks[chinTipIdx]
            val lowerLip = faceLandmarks[lowerLipIdx]
            val jawLine = faceLandmarks[jawLineIdx]

            val primaryDistance = lowerLip.y() - chinTip.y()
            val jawValidation = abs(jawLine.y() - chinTip.y())
            val geometryFactor = if (jawValidation > 0.01f) 1f + (jawValidation * 1.5f) else 1f
            val rawValue = primaryDistance * geometryFactor
            val scaledValue = max(0f, rawValue * 40f)
            val clampedValue = scaledValue.coerceIn(0f, 5f)
            val smoothedValue = clampedValue * (1f - au17SmoothingFactor) + au17History * au17SmoothingFactor
            au17History = smoothedValue
            smoothedValue
        } catch (e: Exception) {
            Log.e(TAG, "Error computing enhanced AU17: ${e.message}")
            au17History * (1f - au17SmoothingFactor)
        }
    }

    private fun returnLivestreamResult(
        result: FaceLandmarkerResult,
        input: MPImage
    ) {
        if (result.faceLandmarks().isNotEmpty()) {
            val finishTimeMs = SystemClock.uptimeMillis()
            val inferenceTime = finishTimeMs - result.timestampMs()

            for (faceLandmarks in result.faceLandmarks()) {
                val au17Value = computeAU17Enhanced(faceLandmarks)
                faceLandmarkerHelperListener?.onAU17(au17Value)
            }

            faceLandmarkerHelperListener?.onResults(
                ResultBundle(result, inferenceTime, input.height, input.width)
            )
        } else {
            faceLandmarkerHelperListener?.onEmpty()
        }
    }

    private fun returnLivestreamError(error: RuntimeException) {
        faceLandmarkerHelperListener?.onError(error.message ?: "An unknown error has occurred")
    }

    companion object {
        const val TAG = "FaceLandmarkerHelper"
        private const val MP_FACE_LANDMARKER_TASK = "face_landmarker.task"
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_FACE_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_FACE_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_FACE_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_FACES = 1
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }

    data class ResultBundle(
        val result: FaceLandmarkerResult,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    data class VideoResultBundle(
        val results: List<FaceLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
        fun onAU17(au17: Float)
        fun onEmpty() {}
    }
}