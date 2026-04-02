package com.example.mochanapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============ MATERIAL DESIGN BASE COLORS ============
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ============ PRIMARY THEME COLORS ============
// Purple Family
val PurplePrimary = Color(0xFF8B5CF6)      // Medium purple - Main brand color
val PurpleSecondary = Color(0xFFA78BFA)    // Light purple
val PurpleDark = Color(0xFF7C3AED)         // Dark purple
val PurpleLight = Color(0xFFC4B5FD)        // Very light purple
val PurpleUltraLight = Color(0xFFF5F3FF)   // Almost white purple

// Pink/Magenta Family
val PinkPrimary = Color(0xFFEC4899)        // Hot pink
val PinkLight = Color(0xFFF472B6)          // Light pink
val PinkUltraLight = Color(0xFFFFF0F6)     // Soft pink background
val MagentaBright = Color(0xFFE843C4)      // Bright magenta

// Orange/Peach Family
val OrangeRed = Color(0xFFEF4444)
val OrangePrimary = Color(0xFFF59E0B)      // Amber/Orange
val OrangeLight = Color(0xFFFBBF24)        // Light orange
val OrangeWarm = Color(0xFFF97316)         // Warm orange
val OrangePeach = Color(0xFFF6A97A)        // Peach - Added from GroundingScreen
val OrangeCoral = Color(0xFFFF8A5C)         // Coral

// Coral Family
val CoralStart = Color(0xFFFF385C)          // Bright pinkish-red
val CoralMid = Color(0xFFFF5E3A)            // Orange-red
val CoralEnd = Color(0xFFFF9345)            // Golden orange
val CoralBright = Color(0xFFFF8A5C)         // Coral

// Yellow Family
val YellowBright = Color(0xFFF2C94C)        // Bright yellow - Added from GroundingScreen
val YellowLight = Color(0xFFFACC15)         // Light yellow

// Blue Family
val BluePrimary = Color(0xFF3B82F6)         // Medium blue
val BlueBright = Color(0xFF60A5FA)           // Bright blue
val BlueLight = Color(0xFF93C5FD)            // Light blue
val BlueCyan = Color(0xFF22D3EE)             // Cyan blue
val BlueSoft = Color(0xFFDBEAFE)             // Very light blue
val BlueSky = Color(0xFF38BDF8)              // Sky blue - Added from BreathingScreen

// Green Family
val GreenPrimary = Color(0xFF10B981)         // Mint green
val GreenLight = Color(0xFF34D399)           // Light mint
val GreenSoft = Color(0xFFD1FAE5)            // Very light mint
val GreenTeal = Color(0xFF14B8A6)             // Teal
val GreenMint = Color(0xFF10B981)
val GreenPastel = Color(0xFF4ADE80)           // Pastel green - Added from MoodTrackerScreen
val GreenForest = Color(0xFF15803D)           // Forest green - Added for success states

// Red/Error Family
val RedPrimary = Color(0xFFEF4444)            // Error red
val RedLight = Color(0xFFF87171)               // Light red
val RedUltraLight = Color(0xFFFEE2E2)          // Very light red
val RedPink = Color(0xFFFF5252)                 // Pinkish red
val RedError = Color(0xFFB45309)                // Error brown - Added for warnings

// ============ TEXT COLORS ============
val TextPrimary = Color(0xFF1F2937)            // Dark gray - Primary text
val TextSecondary = Color(0xFF6B7280)          // Medium gray - Secondary text
val TextTertiary = Color(0xFF9CA3AF)            // Light gray - Tertiary text
val TextMuted = Color(0xFF94A3B8)                // Muted gray
val TextDark = Color(0xFF1D2335)                 // Very dark - Headers
val TextSoft = Color(0xFF4B5563)                  // Soft dark
val TextLight = Color(0xFF6B7280)                 // Light text - Added from MoodTrackerScreen
val TextGray = Color.Gray                          // Gray text - Added for headers

// ============ BACKGROUND & SURFACE COLORS ============
val SurfaceWhite = Color(0xFFFFFFFF)             // Pure white
val SurfaceOffWhite = Color(0xFFF9FAFB)          // Off white
val SurfaceCream = Color(0xFFFFFDF9)              // Cream white
val SurfaceSoftGray = Color(0xFFF3F4F6)           // Soft gray
val SurfaceLightGray = Color(0xFFE5E7EB)          // Light gray
val SurfaceWarmWhite = Color(0xFFFFFAF0)          // Warm white - Added from JournalScreen
val SurfaceLavenderCream = Color(0xFFF5E6FF)      // Lavender cream - Added from JournalScreen
val SurfaceCreamWhite = Color(0xFFF8F8FA)         // Cream white - Added from JournalScreen
val SurfaceVeryLightCyan = Color(0xFFE0F2FE)      // Very light cyan - Added from MoodTrackerScreen
val SurfaceCyanTint = Color(0xFFF0F9FF)           // Cyan tint - Added from MoodTrackerScreen
val SurfaceLightGreen = Color(0xFFF0FDF4)         // Light green - Added for success backgrounds

// Glass/Milky backgrounds
val GlassWhite = Color.White.copy(alpha = 0.4f)
val GlassWhiteHeavy = Color.White.copy(alpha = 0.75f)
val GlassWhiteLight = Color.White.copy(alpha = 0.3f)
val GlassWhiteUltraLight = Color.White.copy(alpha = 0.1f)
val GlassWhiteTransparent = Color.White.copy(alpha = 0.95f) // Added for cards

// ============ BORDER COLORS ============
val BorderLight = Color(0xFFE5E7EB)              // Light gray border
val BorderSoft = Color(0xFFE2E8F0)                // Soft gray border
val BorderPurpleSoft = Color(0xFFE0E7FF)           // Soft purple border
val BorderOrange = Color(0xFFFF9800).copy(alpha = 0.3f) // Orange border - Added from MoodTrackerScreen
val BorderCyan = Color(0xFF34D399)                  // Cyan border - Added from MoodTrackerScreen

// ============ STATUS COLORS ============
val Success = Color(0xFF10B981)                   // Green - Success
val SuccessDark = Color(0xFF15803D)                // Dark green - Added for success text
val Error = Color(0xFFEF4444)                      // Red - Error
val Warning = Color(0xFFF59E0B)                     // Orange - Warning
val Info = Color(0xFF3B82F6)                         // Blue - Info
val WarningDark = Color(0xFFB45309)                  // Dark orange - Added for warnings

// ============ MOOD TRACKER SPECIFIC COLORS ============
val MoodCyan = Color(0xFF34D399)                     // Cyan - Primary mood color
val MoodCyanLight = Color(0xFF99F6E4)                 // Light cyan - Gradient partner
val MoodBlue = Color(0xFF3B82F6)                       // Blue - Energy level
val MoodOrange = Color(0xFFFFB74D)                     // Orange - Streak background
val MoodOrangeDark = Color(0xFFE65100)                  // Dark orange - Streak text
val MoodGreenSuccess = Color(0xFF15803D)                // Green - Success text
val MoodGrayLight = Color.LightGray                      // Light gray - Disabled button

// ============ JOURNAL SPECIFIC COLORS ============
val JournalPurple = Color(0xFFAB47BC)                    // Purple - Journal icon gradient start
val JournalPurpleDark = Color(0xFF7E57C2)                 // Purple - Journal icon gradient end
val JournalTagPurple = Color(0xFFA855F7)                  // Purple - Tag text
val JournalTagBg = Color(0xFFF3E8FF)                       // Purple - Tag background
val JournalTagBorder = Color(0xFFE5E7EB)                   // Gray - Tag border
val JournalTagSelectedBg = Color(0xFFFDF4FF)               // Light purple - Selected tag background
val JournalTextDark = Color(0xFF1D2335)                     // Dark - Journal text
val JournalTextSoft = Color(0xFF4B5563)                     // Soft - Journal text
val JournalTextLight = Color(0xFF9CA3AF)                     // Light - Journal text
val JournalTextGray = Color(0xFF6B7280)                      // Gray - Journal text
val JournalPlaceholderGray = Color(0xFFBDBDBD)               // Placeholder - Journal input
val JournalBorderGray = Color(0xFFE5E7EB)                    // Border - Journal input
val JournalErrorRed = Color.Red                               // Red - Error text
val JournalWarningBg = Color(0xFFFFF3E0)                      // Light orange - Warning background

// ============ GROUNDING SPECIFIC COLORS ============
val GroundingYellow = Color(0xFFF2C94C)                      // Yellow - Grounding gradient start
val GroundingPeach = Color(0xFFF6A97A)                        // Peach - Grounding gradient
val GroundingPink = Color(0xFFF27AA5)                         // Pink - Grounding gradient
val GroundingMagenta = Color(0xFFE843C4)                       // Magenta - Grounding gradient end
val GroundingBlue = Color(0xFF60A5FA)                          // Blue - See sense
val GroundingOrange = Color(0xFFFB923C)                        // Orange - Touch sense
val GroundingPurple = Color(0xFFA855F7)                        // Purple - Hear sense
val GroundingGreen = Color(0xFF4ADE80)                         // Green - Smell sense
val GroundingRed = Color(0xFFF97316)                           // Red - Taste sense
val GroundingSuccess = Color(0xFFF2C94C)                       // Yellow - Success overlay

// ============ BREATHING SPECIFIC COLORS ============
val BreathingBlue = Color(0xFF38BDF8)                          // Blue - Breathing gradient
val BreathingCyan = Color(0xFF22D3EE)                          // Cyan - Breathing gradient
val BreathingTextDark = Color(0xFF1F2937)                       // Dark - Breathing text
val BreathingAccentBlue = Color(0xFF0EA5E9)                     // Blue - Cycle count
val BreathingInstructionBlue = Color(0xFF2563EB)                // Blue - Instruction text
val BreathingInstructionPurple = Color(0xFF9333EA)              // Purple - Instruction text
val BreathingInstructionTeal = Color(0xFF0891B2)                // Teal - Instruction text
val BreathingBgBlue = Color(0xFFDBEAFE)                         // Light blue - Instruction background
val BreathingBgPurple = Color(0xFFF3E8FF)                       // Light purple - Instruction background
val BreathingBgTeal = Color(0xFFCFFAFE)                         // Light teal - Instruction background
val BreathingTextGray = Color(0xFF374151)                        // Gray - Instruction text

// ============ SOUND SCREEN SPECIFIC COLORS ============
val SoundOrange = Color(0xFFFB923C)                             // Orange - Volume slider
val SoundOrangeDark = Color(0xFFEA580C)                         // Dark orange - Volume percentage
val SoundButtonBg = Color(0xFFF3F4F6)                           // Light gray - Button background

// ============ SEVERITY INDICATOR COLORS ============
val MildColor = Color(0xFF10B981)                  // Green
val MildSecondary = Color(0xFF34D399)               // Light Green
val ModerateColor = Color(0xFFF59E0B)                // Orange
val ModerateSecondary = Color(0xFFFBBF24)             // Light Orange
val SevereColor = Color(0xFFEF4444)                   // Red
val SevereSecondary = Color(0xFFF87171)                // Light Red

val MildLightColor = Color(0xFFECFDF5)                // Light Green Background
val ModerateLightColor = Color(0xFFFFFBEB)             // Light Orange Background
val SevereLightColor = Color(0xFFFFF1F2)                // Light Red Background

// ============ GRADIENTS ============

// Purple Gradients
val GradientPurple = Brush.linearGradient(
    colors = listOf(PurplePrimary, PurpleSecondary)
)

val GradientPurpleDark = Brush.linearGradient(
    colors = listOf(PurpleDark, PurplePrimary)
)

val GradientPurpleLight = Brush.linearGradient(
    colors = listOf(PurpleSecondary, PurpleLight)
)

val GradientPurplePink = Brush.linearGradient(
    colors = listOf(PurplePrimary, PinkPrimary)
)

// Pink/Orange Gradients
val GradientPinkOrange = Brush.linearGradient(
    colors = listOf(PinkPrimary, OrangePrimary)
)

val GradientOrangeRed = Brush.linearGradient(
    colors = listOf(OrangeWarm, RedPrimary)
)

val GradientCoralSunset = Brush.linearGradient(
    colors = listOf(CoralBright, OrangeWarm, OrangeLight)
)

// Blue Gradients
val GradientBlueCyan = Brush.linearGradient(
    colors = listOf(BlueBright, BlueCyan)
)

val GradientBlueLight = Brush.linearGradient(
    colors = listOf(BlueLight, BlueCyan)
)

val GradientSkyBlue = Brush.linearGradient(
    colors = listOf(BreathingBlue, BreathingCyan)
)

// Green Gradients
val GradientGreenTeal = Brush.linearGradient(
    colors = listOf(GreenPrimary, GreenTeal)
)

val GradientGreenLight = Brush.linearGradient(
    colors = listOf(GreenLight, GreenSoft)
)

val GradientMint = Brush.linearGradient(
    colors = listOf(MoodCyan, MoodCyanLight)
)

// Severity Gradients
val GradientMild = Brush.linearGradient(
    colors = listOf(MildColor, MildSecondary)
)

val GradientCoral = Brush.linearGradient(
    colors = listOf(CoralStart, CoralMid, CoralEnd)
)

val GradientModerate = Brush.linearGradient(
    colors = listOf(ModerateColor, ModerateSecondary)
)

val GradientSevere = Brush.linearGradient(
    colors = listOf(SevereColor, SevereSecondary)
)

// Multi-color Gradients
val GradientVibrant = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF385C),  // Bright pinkish-red
        Color(0xFFFF5E3A),  // Red-orange
        Color(0xFFFF9345)   // Golden orange
    )
)

val GradientSunset = Brush.linearGradient(
    colors = listOf(
        OrangeWarm,
        PinkPrimary,
        PurplePrimary
    )
)

val GradientRainbow = Brush.linearGradient(
    colors = listOf(
        YellowBright,
        OrangePeach,
        PinkPrimary,
        MagentaBright
    )
)

val GradientLateNight = Brush.linearGradient(
    colors = listOf(
        Color(0xFFB34766), // Muted pink/rose
        Color(0xFF3B1E6D), // Deep mid-purple
        Color(0xFF1A0B2E)  // Very dark purple
    )
)

val GradientTeenParty = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF007A), // Vibrant hot pink
        Color(0xFF380088)  // Deep dark purple
    )
)

// Grounding Gradient
val GradientGrounding = Brush.linearGradient(
    colors = listOf(
        GroundingYellow,
        GroundingPeach,
        GroundingPink,
        GroundingMagenta
    )
)

// Sense Gradients
val GradientSenseSee = Brush.linearGradient(
    colors = listOf(GroundingBlue, BlueCyan)
)

val GradientSenseTouch = Brush.linearGradient(
    colors = listOf(GroundingOrange, PinkPrimary)
)

val GradientSenseHear = Brush.linearGradient(
    colors = listOf(GroundingPurple, MagentaBright)
)

val GradientSenseSmell = Brush.linearGradient(
    colors = listOf(GroundingGreen, GreenTeal)
)

val GradientSenseTaste = Brush.linearGradient(
    colors = listOf(GroundingRed, OrangeRed)
)

// ============ BACKGROUND GRADIENTS ============
val GradientCreamyWhite = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFF4CC),
        Color(0xFFFFD1DF),
        Color(0xFFDDE4FF),
        Color(0xFFFFD1DF),
        Color(0xFFFFE4C4)
    )
)

val GradientCyanWhite = Brush.linearGradient(
    colors = listOf(
        SurfaceWhite,
        SurfaceVeryLightCyan,
        SurfaceCyanTint
    )
)

val GradientCream = Brush.linearGradient(
    colors = listOf(
        SurfaceWhite,
        SurfaceCreamWhite
    )
)

val GradientLavenderCream = Brush.linearGradient(
    colors = listOf(
        SurfaceWhite,
        SurfaceLavenderCream
    )
)

val GradientWarmWhite = Brush.linearGradient(
    colors = listOf(
        SurfaceWhite,
        SurfaceWarmWhite
    )
)

// ============ CARD BACKGROUND GRADIENTS ============
val GradientMentalHealth = Brush.linearGradient(
    colors = listOf(Color(0xFFFFF0F5), Color(0xFFFFF7ED))
)

val GradientWellness = Brush.verticalGradient(
    colors = listOf(
        SurfaceWhite,
        SurfaceCyanTint,
        SurfaceVeryLightCyan
    )
)

val GradientDailyCheckin = Brush.linearGradient(
    colors = listOf(Color(0xFFFDFCFE), Color(0xFFF3E5F5))
)

// ============ BUTTON GRADIENTS ============
val GradientButtonPrimary = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF385C),
        Color(0xFFFF5E3A),
        Color(0xFFFF9345)
    )
)

val GradientButtonSecondary = Brush.linearGradient(
    colors = listOf(
        PurplePrimary,
        PinkPrimary
    )
)

val GradientButtonOrangePink = Brush.linearGradient(
    colors = listOf(SoundOrange, PinkPrimary)
)

val GradientButtonPurplePink = Brush.linearGradient(
    colors = listOf(GroundingPurple, MagentaBright)
)

// ============ FUNCTION TO GET SEVERITY COLORS ============
fun getSeverityColors(score: Int): SeverityColors {
    return when {
        score <= 9 -> SeverityColors(
            primary = MildColor,
            secondary = MildSecondary,
            light = MildLightColor,
            gradient = GradientMild,
            level = "Mild",
            emoji = "🟢"
        )
        score <= 14 -> SeverityColors(
            primary = ModerateColor,
            secondary = ModerateSecondary,
            light = ModerateLightColor,
            gradient = GradientModerate,
            level = "Moderate",
            emoji = "🟡"
        )
        else -> SeverityColors(
            primary = SevereColor,
            secondary = SevereSecondary,
            light = SevereLightColor,
            gradient = GradientSevere,
            level = "Severe",
            emoji = "🔴"
        )
    }
}

data class SeverityColors(
    val primary: Color,
    val secondary: Color,
    val light: Color,
    val gradient: Brush,
    val level: String,
    val emoji: String
)