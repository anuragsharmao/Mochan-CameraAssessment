package com.example.mochanapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.mochanapp.ui.theme.*


// Gradient for grounding (matches wellness aesthetic)
val GroundingGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF2C94C), // soft yellow
        Color(0xFFF6A97A), // soft peach
        Color(0xFFF27AA5), // soft pink
        Color(0xFFE843C4)  // soft magenta
    )
)

val SenseGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF22D3EE))), // See - blue
    Brush.linearGradient(listOf(Color(0xFFFB923C), Color(0xFFF472B6))), // Touch - orange-pink
    Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFFEC4899))), // Hear - purple-pink
    Brush.linearGradient(listOf(Color(0xFF4ADE80), Color(0xFF10B981))), // Smell - green
    Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEF4444)))  // Taste - orange-red
)

val SenseIcons = listOf(
    Icons.Default.Visibility,
    Icons.Default.TouchApp,
    Icons.Default.VolumeUp,
    Icons.Outlined.Spa,
    Icons.Default.Restaurant
)

val SenseLabels = listOf("See", "Touch", "Hear", "Smell", "Taste")
val SenseDescriptions = listOf(
    "5 things you can see",
    "4 things you can feel",
    "3 things you can hear",
    "2 things you can smell",
    "1 thing you can taste"
)
val SensePrompts = listOf(
    "What do you see around you?",
    "What can you physically feel?",
    "What sounds do you hear?",
    "What scents are in the air?",
    "What can you taste right now?"
)

// Accent colors from gradients
val SenseAccentColors = listOf(
    Color(0xFF3B82F6), // blue
    Color(0xFFF97316), // orange
    Color(0xFFA855F7), // purple
    Color(0xFF10B981), // green
    Color(0xFFEF4444)  // red
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundingScreen(onBack: () -> Unit) {
    val senseInputs = remember {
        mutableStateMapOf<Int, List<String>>().apply {
            (0..4).forEach { index ->
                this[index] = List(5 - index) { "" }
            }
        }
    }

    var showSuccessMessage by remember { mutableStateOf(false) }
    var completedSenses by remember { mutableStateOf(0) }
    var expandedSense by remember { mutableStateOf<Int?>(null) }

    val isFullyComplete = (0..4).all { senseIndex ->
        senseInputs[senseIndex]?.all { it.isNotBlank() } == true
    }

    // Update completed senses when inputs change
    LaunchedEffect(senseInputs) {
        completedSenses = (0..4).count { senseIndex ->
            senseInputs[senseIndex]?.all { it.isNotBlank() } == true
        }
    }

    // Auto-dismiss success message
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(2000)
            showSuccessMessage = false
        }
    }

    fun updateSenseItem(senseIndex: Int, itemIndex: Int, value: String) {
        val currentList = senseInputs[senseIndex] ?: return
        val newList = currentList.toMutableList()
        newList[itemIndex] = value
        senseInputs[senseIndex] = newList
    }

    fun resetAllInputs() {
        (0..4).forEach { index ->
            senseInputs[index] = List(5 - index) { "" }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Glass Header (exactly like other wellness screens)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GlassWhite,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GroundingGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Spa,
                            contentDescription = "Grounding",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Title and subtitle
                    Column {
                        Text(
                            text = "5-4-3-2-1 Grounding",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Anchor yourself in the present",
                            fontSize = 14.sp,
                            color = TextSoft,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Progress Bar (below header)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSoft
                    )
                    Text(
                        text = "$completedSenses/5 senses",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TextLight.copy(alpha = 0.2f))
                ) {
                    // Simple progress fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(completedSenses / 5f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(GroundingGradient)
                    )
                }
            }

            // Main Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card (subtle like other wellness screens)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Subtle icon - FIXED: Using a solid color instead of trying to set alpha on Brush
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF2C94C).copy(alpha = 0.2f)), // Fixed: Use Color with alpha, not Brush
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFF2C94C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Notice 5 things you see, 4 you can touch, 3 you hear, 2 you smell, and 1 you taste.",
                                fontSize = 14.sp,
                                color = TextSoft,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sense Cards (clean white cards like other wellness screens)
                items(5) { senseIndex ->
                    val items = senseInputs[senseIndex] ?: List(5 - senseIndex) { "" }
                    val isExpanded = expandedSense == senseIndex || items.any { it.isNotBlank() }
                    val isComplete = items.all { it.isNotBlank() }
                    val gradient = SenseGradients[senseIndex]
                    val accentColor = SenseAccentColors[senseIndex]

                    GroundingSenseCard(
                        label = SenseLabels[senseIndex],
                        description = SenseDescriptions[senseIndex],
                        items = items,
                        gradient = gradient,
                        accentColor = accentColor,
                        icon = SenseIcons[senseIndex],
                        isExpanded = isExpanded,
                        isComplete = isComplete,
                        onExpandToggle = {
                            expandedSense = if (expandedSense == senseIndex) null else senseIndex
                        },
                        onItemChange = { itemIndex, value ->
                            updateSenseItem(senseIndex, itemIndex, value)
                        }
                    )
                }

                // Complete Button (matching other wellness screens)
                item {
                    Button(
                        onClick = {
                            showSuccessMessage = true
                            resetAllInputs()
                        },
                        enabled = isFullyComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFullyComplete) Color(0xFF34D399) else TextLight.copy(alpha = 0.3f),
                            disabledContainerColor = TextLight.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFullyComplete) "Complete Exercise" else "Complete all senses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Success Overlay (using a different name to avoid conflict)
        if (showSuccessMessage) {
            GroundingSuccessOverlay()
        }
    }
}

@Composable
fun GroundingSenseCard(
    label: String,
    description: String,
    items: List<String>,
    gradient: Brush,
    accentColor: Color,
    icon: ImageVector,
    isExpanded: Boolean,
    isComplete: Boolean,
    onExpandToggle: () -> Unit,
    onItemChange: (Int, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isExpanded) 4.dp else 2.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and description
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = TextSoft
                    )
                }

                // Status indicator
                if (isComplete) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Text(
                        text = "${items.count { it.isNotBlank() }}/${items.size}",
                        color = TextLight,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Expandable content
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    items.forEachIndexed { index, value ->
                        GroundingInputField(
                            value = value,
                            onValueChange = { onItemChange(index, it) },
                            prompt = if (index == 0) SensePrompts[items.size - 1] else "",
                            index = index + 1,
                            total = items.size,
                            accentColor = accentColor,
                            isLast = index == items.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroundingInputField(
    value: String,
    onValueChange: (String) -> Unit,
    prompt: String,
    index: Int,
    total: Int,
    accentColor: Color,
    isLast: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!isLast) 8.dp else 0.dp)
    ) {
        // Number indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (index == 1 && prompt.isNotEmpty()) {
                Text(
                    text = prompt,
                    fontSize = 12.sp,
                    color = TextSoft
                )
            }
        }

        // Input field (matching OutlinedTextField style from other screens)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = "e.g., ${getExampleForIndex(index, total)}",
                    fontSize = 13.sp,
                    color = TextLight
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = TextLight.copy(alpha = 0.3f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextSoft,
                cursorColor = accentColor
            ),
            singleLine = true,
            interactionSource = interactionSource
        )
    }
}

fun getExampleForIndex(index: Int, total: Int): String {
    return when (total) {
        5 -> listOf("blue sky", "green plant", "wooden table", "white wall", "phone screen")[index - 1]
        4 -> listOf("soft fabric", "warm mug", "cool breeze", "ground beneath")[index - 1]
        3 -> listOf("birds singing", "fan humming", "distant traffic")[index - 1]
        2 -> listOf("coffee aroma", "fresh air")[index - 1]
        else -> "mint taste"
    }
}

@Composable
fun GroundingSuccessOverlay() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xAAF2C94C), Color.Transparent),
                        radius = 500f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFF2C94C)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Grounded! 🌟",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "You're present and aware",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}