package com.example.mochanapp.utils

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.Log
import com.example.mochanapp.R
import com.example.mochanapp.screens.AiPredictionData
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportDownloadHelper{
    private const val TAG = "ReportDownloadHelper"

    fun generateReport(
        context: Context,
        userName: String,
        userAge: Int,
        userGender: String,
        anonymousId: String,
        registrationId: String,
        aiData: AiPredictionData?,  // Removed phq9Score and phq9Severity
        onProgress: (Int) -> Unit = {}
    ): String? {
        return try {
            // Show started notification only (simplified)
            NotificationHelper.showDownloadStarted(context)
            onProgress(10)

            // Create directory in Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appFolder = File(downloadsDir, "MochanApp")
            val reportsFolder = File(appFolder, "Reports")

            if (!reportsFolder.exists()) {
                reportsFolder.mkdirs()
            }

            onProgress(20)

            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "${anonymousId}_screening_${timestamp}.pdf"
            val pdfFile = File(reportsFolder, fileName)

            onProgress(30)

            // Create PDF document
            val document = android.graphics.pdf.PdfDocument()

            // Create a page
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            onProgress(40)

            // Load Mochan logo
            val logoBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.mochan_logo)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading logo: ${e.message}")
                null
            }

            // ============ HEADER SECTION ============
            val headerYPosition = 50f

            logoBitmap?.let {
                // Scale logo to appropriate size
                val scaledLogo = Bitmap.createScaledBitmap(it, 70, 70, false)

                // Larger text size for title
                val titlePaint = Paint().apply {
                    color = Color.parseColor("#1F2937")
                    textSize = 28f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }

                // Measure text width to calculate proper centering
                val textWidth = titlePaint.measureText("MOCHAN MENTAL HEALTH SCREENING")
                val logoWidth = 70f
                val spacing = 15f // Space between logo and text

                // Total width of logo + spacing + text
                val totalElementWidth = logoWidth + spacing + textWidth

                // Page usable width (from 50 to 545 = 495)
                val pageWidth = 495f
                val leftMargin = 50f

                // Calculate starting X position to center everything
                val startX = leftMargin + (pageWidth - totalElementWidth) / 2

                // Draw logo at calculated position
                canvas.drawBitmap(scaledLogo, startX, headerYPosition - 35f, null)

                // Draw text next to logo
                canvas.drawText("MOCHAN MENTAL HEALTH SCREENING", startX + logoWidth + spacing, headerYPosition, titlePaint)

                Log.d(TAG, "Logo positioned at: $startX, Text at: ${startX + logoWidth + spacing}, Text width: $textWidth")
            } ?: run {
                // If logo not available, just draw centered text
                val titlePaint = Paint().apply {
                    color = Color.parseColor("#1F2937")
                    textSize = 28f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("MOCHAN MENTAL HEALTH SCREENING", 297.5f, headerYPosition, titlePaint)
            }

            var yPosition = headerYPosition + 40f

            onProgress(50)

            // Draw line
            val linePaint = Paint().apply {
                color = Color.parseColor("#E5E7EB")
                strokeWidth = 2f
            }
            canvas.drawLine(50f, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            // Date and Disclaimer
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())
            canvas.drawText("Report Generated: ${dateFormat.format(Date())}", 50f, yPosition,
                Paint().apply {
                    color = Color.parseColor("#4B5563")
                    textSize = 10f
                }
            )
            yPosition += 20f

            // IMPORTANT DISCLAIMER - Screening tool notice
            val disclaimerPaint = Paint().apply {
                color = Color.parseColor("#DC2626") // Red color for emphasis
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("IMPORTANT: This is a screening tool only, NOT a diagnostic instrument.", 50f, yPosition, disclaimerPaint)
            yPosition += 15f
            canvas.drawText("Results should be discussed with a qualified mental health professional.", 50f, yPosition,
                Paint().apply {
                    color = Color.parseColor("#4B5563")
                    textSize = 10f
                }
            )
            yPosition += 30f

            onProgress(60)

            // Patient Information Section
            val headerPaint = Paint().apply {
                color = Color.parseColor("#4F46E5")
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("PATIENT INFORMATION", 50f, yPosition, headerPaint)
            yPosition += 25f

            // Draw info box
            val infoBoxPaint = Paint().apply {
                color = Color.parseColor("#F9FAFB")
                style = Paint.Style.FILL
            }
            canvas.drawRect(50f, yPosition - 15f, 545f, yPosition + 85f, infoBoxPaint)

            val textPaint = Paint().apply {
                color = Color.parseColor("#4B5563")
                textSize = 11f
            }
            val boldTextPaint = Paint().apply {
                color = Color.parseColor("#1F2937")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            // Patient details
            canvas.drawText("Name: $userName", 70f, yPosition + 5f, boldTextPaint)
            canvas.drawText("Age: $userAge years", 70f, yPosition + 25f, textPaint)
            canvas.drawText("Gender: $userGender", 70f, yPosition + 45f, textPaint)
            canvas.drawText("Anonymous ID: $anonymousId", 320f, yPosition + 5f, textPaint)
            canvas.drawText("Registration: $registrationId", 320f, yPosition + 25f, textPaint)

            yPosition += 100f

            onProgress(70)

            // AI Screening Results Section
            canvas.drawText("AI SCREENING RESULTS", 50f, yPosition, headerPaint)
            yPosition += 30f

            // AI Results (if available)
            if (aiData != null) {
                val aiBoxPaint = Paint().apply {
                    color = Color.parseColor("#F5F3FF")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(50f, yPosition - 10f, 545f, yPosition + 120f, aiBoxPaint)

                val sectionPaint = Paint().apply {
                    color = Color.parseColor("#374151")
                    textSize = 14f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }

                canvas.drawText("🤖 AI FACIAL EXPRESSION ANALYSIS", 70f, yPosition + 5f, sectionPaint)

                // Determine severity level from score (0-24 scale)
                val severityLevel = when {
                    aiData.score <= 9 -> "Mild"
                    aiData.score <= 14 -> "Moderate"
                    else -> "Severe"
                }

                val severityColor = when {
                    aiData.score <= 9 -> Color.parseColor("#10B981") // Green
                    aiData.score <= 14 -> Color.parseColor("#F59E0B") // Orange
                    else -> Color.parseColor("#EF4444") // Red
                }

                val severityEmoji = when {
                    aiData.score <= 9 -> "🟢"
                    aiData.score <= 14 -> "🟡"
                    else -> "🔴"
                }

                // Display severity level with emoji
                val severityPaint = Paint().apply {
                    color = severityColor
                    textSize = 13f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("$severityEmoji Severity Indicator: $severityLevel", 70f, yPosition + 30f, severityPaint)

                canvas.drawText("Confidence: ${(aiData.confidence * 100).toInt()}%", 70f, yPosition + 70f, textPaint)
                canvas.drawText("Frames Analyzed: ${aiData.frameCount}", 70f, yPosition + 90f, textPaint)
                canvas.drawText("Model Version: ${aiData.modelVersion}", 70f, yPosition + 110f, textPaint)

                yPosition += 140f

                // Add disclaimer about AI analysis
                val aiDisclaimerPaint = Paint().apply {
                    color = Color.parseColor("#6B7280")
                    textSize = 10f
                }
                canvas.drawText("Note: AI analysis is based on facial expressions and should be", 70f, yPosition - 15f, aiDisclaimerPaint)
                canvas.drawText("considered alongside professional clinical assessment.", 70f, yPosition, aiDisclaimerPaint)
                yPosition += 30f
            } else {
                // No AI data available
                canvas.drawText("No AI analysis data available", 70f, yPosition + 20f, textPaint)
                yPosition += 50f
            }

            onProgress(80)

            // Professional Help Recommendations Section
            canvas.drawText("PROFESSIONAL SUPPORT RECOMMENDATIONS", 50f, yPosition, headerPaint)
            yPosition += 30f

            // Calculate height needed for recommendations
            val recBoxHeight = 160f

            // Draw recommendations box
            val recBoxPaint = Paint().apply {
                color = Color.parseColor("#FFF7ED")
                style = Paint.Style.FILL
            }
            canvas.drawRect(50f, yPosition - 10f, 545f, yPosition + recBoxHeight, recBoxPaint)

            var recYPos = yPosition + 5f

            // National Crisis Helpline (instead of SETU)
            val crisisBoldPaint = Paint().apply {
                color = Color.parseColor("#1F2937")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            canvas.drawText("📞 NATIONAL CRISIS HELPLINE", 70f, recYPos, crisisBoldPaint)
            recYPos += 20f

            val crisisTextPaint = Paint().apply {
                color = Color.parseColor("#DC2626")
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("988", 70f, recYPos, crisisTextPaint)
            recYPos += 20f

            canvas.drawText("24/7, confidential, free support", 70f, recYPos,
                Paint().apply {
                    color = Color.parseColor("#4B5563")
                    textSize = 11f
                }
            )
            recYPos += 25f

            // Counseling Center Recommendation
            canvas.drawText("🏥 COUNSELING CENTERS", 70f, recYPos, crisisBoldPaint)
            recYPos += 20f
            canvas.drawText("• Visit your nearest counseling or mental health center", 70f, recYPos,
                Paint().apply {
                    color = Color.parseColor("#4B5563")
                    textSize = 11f
                }
            )
            recYPos += 18f
            canvas.drawText("• Contact your university/college counseling services", 70f, recYPos,
                Paint().apply {
                    color = Color.parseColor("#4B5563")
                    textSize = 11f
                }
            )
            recYPos += 18f
            canvas.drawText("• Schedule an appointment with a mental health professional", 70f, recYPos,
                Paint().apply {
                    color = Color.parseColor("#4B5563")
                    textSize = 11f
                }
            )

            yPosition = recYPos + 30f

            onProgress(85)

            // Immediate Help Section (if severe)
            if (aiData != null && aiData.score > 14) {
                val severeBoxPaint = Paint().apply {
                    color = Color.parseColor("#FEF2F2")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(50f, yPosition - 10f, 545f, yPosition + 70f, severeBoxPaint)

                val severeHeaderPaint = Paint().apply {
                    color = Color.parseColor("#DC2626")
                    textSize = 14f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("⚠️ IMMEDIATE HELP AVAILABLE", 70f, yPosition + 5f, severeHeaderPaint)

                canvas.drawText("• Crisis Helpline: 988 (24/7)", 70f, yPosition + 30f,
                    Paint().apply {
                        color = Color.parseColor("#1F2937")
                        textSize = 11f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                )
                canvas.drawText("• Emergency Services: 911", 70f, yPosition + 50f,
                    Paint().apply {
                        color = Color.parseColor("#1F2937")
                        textSize = 11f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                )
                canvas.drawText("• Go to nearest hospital emergency room", 70f, yPosition + 70f,
                    Paint().apply {
                        color = Color.parseColor("#1F2937")
                        textSize = 11f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                )

                yPosition += 100f
            } else {
                // General resources
                canvas.drawText("CRISIS RESOURCES", 50f, yPosition,
                    Paint().apply {
                        color = Color.parseColor("#1F2937")
                        textSize = 14f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                )
                yPosition += 25f

                val crisisBoxPaint = Paint().apply {
                    color = Color.parseColor("#FEF2F2")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(50f, yPosition - 10f, 545f, yPosition + 80f, crisisBoxPaint)

                canvas.drawText("📞 National Crisis Helpline: 988", 70f, yPosition + 5f,
                    Paint().apply {
                        color = Color.parseColor("#1F2937")
                        textSize = 11f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                )
                canvas.drawText("📞 Emergency Services: 911", 70f, yPosition + 25f,
                    Paint().apply {
                        color = Color.parseColor("#1F2937")
                        textSize = 11f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                )
                canvas.drawText("👥 Talk to a trusted friend or family member", 70f, yPosition + 45f,
                    Paint().apply {
                        color = Color.parseColor("#4B5563")
                        textSize = 10f
                    }
                )
                canvas.drawText("🏥 Visit your nearest counseling center", 70f, yPosition + 65f,
                    Paint().apply {
                        color = Color.parseColor("#4B5563")
                        textSize = 10f
                    }
                )

                yPosition += 100f
            }

            onProgress(90)

            // Footer with disclaimer
            val footerPaint = Paint().apply {
                color = Color.parseColor("#9CA3AF")
                textSize = 8f
                textAlign = Paint.Align.CENTER
            }

            // Add multiple lines for disclaimer
            canvas.drawText("IMPORTANT DISCLAIMER:", 297.5f, yPosition,
                Paint().apply {
                    color = Color.parseColor("#6B7280")
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
            )
            yPosition += 12f

            canvas.drawText("This report is based on an AI screening tool and is NOT a medical diagnosis.", 297.5f, yPosition, footerPaint)
            yPosition += 10f
            canvas.drawText("It should not replace professional clinical assessment or advice.", 297.5f, yPosition, footerPaint)
            yPosition += 10f
            canvas.drawText("Always consult with qualified mental health professionals for proper evaluation.", 297.5f, yPosition, footerPaint)
            yPosition += 12f

            canvas.drawText("© Mochan - Supporting Your Mental Wellness Journey", 297.5f, yPosition, footerPaint)

            // Finish page
            document.finishPage(page)

            onProgress(95)

            // Write to file
            val fileOutputStream = FileOutputStream(pdfFile)
            document.writeTo(fileOutputStream)
            document.close()
            fileOutputStream.close()

            onProgress(100)

            Log.d(TAG, "PDF Report saved: ${pdfFile.absolutePath}")

            // Show success notification
            NotificationHelper.showDownloadSuccess(context, pdfFile.absolutePath)

            pdfFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF report: ${e.message}")
            e.printStackTrace()

            // Show error notification
            NotificationHelper.showDownloadError(context, e.message ?: "Unknown error occurred")

            null
        }
    }
}