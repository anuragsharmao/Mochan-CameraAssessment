package com.example.mochanapp.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.security.MessageDigest
import java.util.Locale
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.example.mochanapp.utils.UserSessionHelper
import androidx.compose.runtime.saveable.rememberSaveable

// ============ THEME COLORS ============

// Main theme gradient
val GradientLateNight = Brush.linearGradient(
    colors = listOf(
        Color(0xFFB34766), // Muted, darker pink/rose
        Color(0xFF3B1E6D), // Deep mid-purple
        Color(0xFF1A0B2E)  // Dark purple/blue
    )
)

// Supporting gradients for variety
val GradientSunsetRose = Brush.linearGradient(
    colors = listOf(Color(0xFFFF6B6B), Color(0xFFB34766))
)

val GradientPurpleDream = Brush.linearGradient(
    colors = listOf(Color(0xFF9F7AEA), Color(0xFF3B1E6D))
)

val GradientTealOcean = Brush.linearGradient(
    colors = listOf(Color(0xFF38B2AC), Color(0xFF1A0B2E))
)

val GradientGold = Brush.linearGradient(
    colors = listOf(Color(0xFFFBBF24), Color(0xFFB34766))
)

// Solid colors from theme
val ThemePink = Color(0xFFB34766)
val ThemePurple = Color(0xFF3B1E6D)
val ThemeDarkPurple = Color(0xFF1A0B2E)
val ThemeLightPink = Color(0xFFFEE2E2)
val ThemeLightPurple = Color(0xFFEDE9FE)

// UI Colors - professional palette
val ColorTextPrimary = Color(0xFF1F2937)
val ColorTextSecondary = Color(0xFF6B7280)
val ColorTextTertiary = Color(0xFF9CA3AF)
val ColorBorder = Color(0xFFE5E7EB)
val ColorCardBg = Color.White
val ColorSuccess = Color(0xFF10B981)
val ColorError = Color(0xFFEF4444)
val InputBgColor = Color(0xFFF9FAFB)

// ============ VALIDATION FUNCTIONS ============

fun validateRegistrationId(regId: String): String? {
    return when {
        regId.isBlank() -> "Registration ID is required"
        regId.contains(" ") -> "Registration ID cannot contain spaces"
        else -> null
    }
}

fun validatePassword(password: String): String? {
    return when {
        password.isBlank() -> "Password is required"
        password.length < 6 -> "Password must be at least 6 characters"
        password.contains(" ") -> "Password cannot contain spaces"
        else -> null
    }
}

fun validateName(name: String): String? {
    return when {
        name.isBlank() -> "Name is required"
        name.length < 2 -> "Name must be at least 2 characters"
        else -> null
    }
}

fun validateEmail(email: String): String? {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return when {
        email.isBlank() -> "Email is required"
        email.contains(" ") -> "Email cannot contain spaces"
        !emailRegex.matches(email) -> "Enter a valid email address"
        else -> null
    }
}

fun validateAge(age: String): String? {
    val ageInt = age.toIntOrNull()
    return when {
        age.isBlank() -> "Age is required"
        ageInt == null -> "Please enter a valid number"
        ageInt < 18 -> "You must be 18 or older"
        ageInt > 120 -> "Please enter a valid age"
        else -> null
    }
}

fun validateConfirmPassword(password: String, confirmPassword: String): String? {
    return when {
        confirmPassword.isBlank() -> "Please confirm your password"
        password != confirmPassword -> "Passwords don't match"
        else -> null
    }
}

// Data classes
data class UserRegister(
    val registration_id: String,
    val password: String,
    val name: String,
    val gender: String,
    val email: String,
    val age: Int
)

data class UserLogin(
    val registration_id: String,
    val password: String
)

data class UserProfile(
    val name: String,
    val gender: String,
    val email: String,
    val age: Int,
    val registration_id: String,
    val anonymousId: String = ""
)

enum class AuthMode { LOGIN, SIGNUP }

// ============ MAIN PROFILE SCREEN ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Form states
    var registrationId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // Touched states
    var registrationIdTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var ageTouched by remember { mutableStateOf(false) }
    var genderTouched by remember { mutableStateOf(false) }

    var formSubmitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var isLoggedIn by remember { mutableStateOf(false) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    var showTermsPopup by remember { mutableStateOf(false) }
    var showPrivacyPopup by remember { mutableStateOf(false) }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadSavedUser(context)?.let { profile ->
            userProfile = profile
            isLoggedIn = true
            Log.d("ProfileScreen", "Loaded saved user - Reg ID: ${profile.registration_id}")
        }
    }

    LaunchedEffect(authMode) {
        registrationIdTouched = false
        passwordTouched = false
        confirmPasswordTouched = false
        nameTouched = false
        emailTouched = false
        ageTouched = false
        genderTouched = false
        formSubmitted = false

        registrationId = ""
        password = ""
        confirmPassword = ""
        name = ""
        email = ""
        age = ""
        gender = ""

        showPassword = false
        showConfirmPassword = false
    }

    // Validation
    val registrationIdError = remember(registrationId) { validateRegistrationId(registrationId) }
    val passwordError = remember(password) { validatePassword(password) }
    val nameError = remember(name) { validateName(name) }
    val emailErrorText = remember(email) { validateEmail(email) }
    val ageErrorText = remember(age) { validateAge(age) }
    val confirmPasswordError = remember(password, confirmPassword) {
        validateConfirmPassword(password, confirmPassword)
    }

    val showRegistrationIdError = (registrationIdTouched || formSubmitted) && registrationIdError != null
    val showPasswordError = (passwordTouched || formSubmitted) && passwordError != null
    val showConfirmPasswordError = (confirmPasswordTouched || formSubmitted) && confirmPasswordError != null
    val showNameError = (nameTouched || formSubmitted) && nameError != null
    val showEmailError = (emailTouched || formSubmitted) && emailErrorText != null
    val showAgeError = (ageTouched || formSubmitted) && ageErrorText != null
    val showGenderError = (genderTouched || formSubmitted) && gender.isBlank()

    val isLoginValid = remember(registrationId, password, registrationIdError, passwordError) {
        registrationId.isNotBlank() && password.isNotBlank() &&
                registrationIdError == null && passwordError == null
    }

    val isSignupValid = remember(
        registrationId, password, confirmPassword, name, email, age, gender,
        registrationIdError, passwordError, nameError, emailErrorText, ageErrorText, confirmPasswordError
    ) {
        registrationId.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                name.isNotBlank() &&
                email.isNotBlank() &&
                age.isNotBlank() &&
                gender.isNotBlank() &&
                registrationIdError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                nameError == null &&
                emailErrorText == null &&
                ageErrorText == null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Background decoration (kept minimal)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = ThemePink.copy(alpha = 0.03f),
                radius = 300.dp.toPx(),
                center = Offset(size.width - 100.dp.toPx(), -50.dp.toPx())
            )
            drawCircle(
                color = ThemePurple.copy(alpha = 0.03f),
                radius = 250.dp.toPx(),
                center = Offset(-50.dp.toPx(), size.height - 200.dp.toPx())
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header (kept exactly the same)
            item {
                ProfileHeader(
                    authMode = authMode,
                    isLoggedIn = isLoggedIn,
                    onSwitchMode = {
                        authMode = if (authMode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN
                    }
                )
            }

            if (isLoggedIn && userProfile != null) {
                item {
                    LoggedInContent(
                        profile = userProfile!!,
                        navController = navController,
                        context = context,
                        onLogoutClick = { showLogoutDialog = true },
                        onShowTermsPopup = { showTermsPopup = true },
                        onShowPrivacyPopup = { showPrivacyPopup = true }
                    )
                }
            } else {
                item {
                    AuthCard(
                        authMode = authMode,
                        registrationId = registrationId,
                        onRegistrationIdChange = { registrationId = it },
                        onRegistrationIdTouched = { registrationIdTouched = true },
                        registrationIdError = if (showRegistrationIdError) registrationIdError else null,
                        password = password,
                        onPasswordChange = { password = it },
                        onPasswordTouched = { passwordTouched = true },
                        passwordError = if (showPasswordError) passwordError else null,
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        onConfirmPasswordTouched = { confirmPasswordTouched = true },
                        confirmPasswordError = if (showConfirmPasswordError) confirmPasswordError else null,
                        name = name,
                        onNameChange = { name = it },
                        onNameTouched = { nameTouched = true },
                        nameError = if (showNameError) nameError else null,
                        email = email,
                        onEmailChange = { email = it },
                        onEmailTouched = { emailTouched = true },
                        emailError = if (showEmailError) emailErrorText else null,
                        age = age,
                        onAgeChange = { age = it },
                        onAgeTouched = { ageTouched = true },
                        ageError = if (showAgeError) ageErrorText else null,
                        gender = gender,
                        onGenderChange = { gender = it },
                        onGenderTouched = { genderTouched = true },
                        genderError = if (showGenderError) "Please select a gender" else null,
                        showPassword = showPassword,
                        onShowPasswordChange = { showPassword = it },
                        showConfirmPassword = showConfirmPassword,
                        onShowConfirmPasswordChange = { showConfirmPassword = it },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        successMessage = successMessage,
                        isFormValid = if (authMode == AuthMode.LOGIN) isLoginValid else isSignupValid,
                        onSubmit = {
                            formSubmitted = true

                            registrationIdTouched = true
                            passwordTouched = true
                            if (authMode == AuthMode.SIGNUP) {
                                confirmPasswordTouched = true
                                nameTouched = true
                                emailTouched = true
                                ageTouched = true
                                genderTouched = true
                            }

                            val isValid = if (authMode == AuthMode.LOGIN) {
                                isLoginValid
                            } else {
                                isSignupValid
                            }

                            if (!isValid) {
                                return@AuthCard
                            }

                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                if (authMode == AuthMode.LOGIN) {
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            performLogin(context, registrationId, password)
                                        } catch (e: Exception) {
                                            Pair(null, "Connection failed")
                                        }
                                    }

                                    val (profile, error) = result
                                    if (profile != null) {
                                        val updatedProfile = if (profile.anonymousId.isEmpty()) {
                                            profile.copy(anonymousId = generateAnonymousId(profile.name, profile.registration_id))
                                        } else {
                                            profile
                                        }

                                        saveUser(context, updatedProfile)
                                        userProfile = updatedProfile
                                        isLoggedIn = true

                                        Toast.makeText(context, "Welcome back, ${updatedProfile.name}!", Toast.LENGTH_SHORT).show()

                                        registrationId = ""
                                        password = ""
                                        formSubmitted = false
                                    } else {
                                        errorMessage = error ?: "Login failed"
                                    }
                                } else { // SIGNUP
                                    val result = withContext(Dispatchers.IO) {
                                        try {
                                            performSignup(registrationId, password, name, gender, email, age.toIntOrNull() ?: 0)
                                        } catch (e: Exception) {
                                            Pair(null, "Connection failed")
                                        }
                                    }

                                    val (profile, error) = result
                                    if (profile != null) {
                                        val anonymousId = generateAnonymousId(name, registrationId)
                                        val newUserProfile = UserProfile(
                                            name = name,
                                            gender = gender,
                                            email = email,
                                            age = age.toIntOrNull() ?: 0,
                                            registration_id = registrationId,
                                            anonymousId = anonymousId
                                        )

                                        saveRegistrationDetailsCSV(context, newUserProfile)
                                        saveUser(context, newUserProfile)

                                        userProfile = newUserProfile
                                        isLoggedIn = true

                                        Toast.makeText(context, "Welcome, ${newUserProfile.name}!", Toast.LENGTH_SHORT).show()

                                        registrationId = ""
                                        password = ""
                                        confirmPassword = ""
                                        name = ""
                                        email = ""
                                        age = ""
                                        gender = ""
                                        formSubmitted = false

                                        val cameFromAssessment = navController.previousBackStackEntry?.destination?.route == "check"
                                        if (cameFromAssessment) {
                                            navController.popBackStack()
                                        }
                                    } else {
                                        errorMessage = error ?: "Registration failed"
                                    }
                                }
                                isLoading = false
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // Dialogs
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    logoutUser(context)
                    isLoggedIn = false
                    userProfile = null
                    showLogoutDialog = false
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        if (showTermsPopup) {
            TermsAndConditionsPopup(
                onDismiss = { showTermsPopup = false }
            )
        }

        if (showPrivacyPopup) {
            PrivacyPolicyPopup(
                onDismiss = { showPrivacyPopup = false }
            )
        }
    }
}

// ==================== BACKGROUND NETWORK FUNCTIONS ====================

suspend fun performLogin(
    context: Context,
    registrationId: String,
    password: String
): Pair<UserProfile?, String?> {
    Log.d("LOGIN_DEBUG", "=== Starting Login ===")
    try {
        val url = URL("http://203.110.243.202:8000/login-user")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val requestBody = JSONObject().apply {
            put("registration_id", registrationId)
            put("password", password)
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val responseCode = connection.responseCode
        val response = if (responseCode == 200) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        connection.disconnect()

        if (responseCode == 200) {
            try {
                val json = JSONObject(response)
                val name = json.getString("name")
                val gender = json.getString("gender")
                val email = json.getString("email")
                val age = json.getInt("age")

                val serverAnonymousId = if (json.has("anonymous_id")) {
                    json.getString("anonymous_id")
                } else {
                    ""
                }

                val profile = UserProfile(
                    name = name,
                    gender = gender,
                    email = email,
                    age = age,
                    registration_id = registrationId,
                    anonymousId = serverAnonymousId
                )

                return Pair(profile, null)

            } catch (e: Exception) {
                return Pair(null, "Server error")
            }
        } else if (responseCode == 401) {
            return Pair(null, "Invalid registration ID or password")
        } else {
            return Pair(null, "Login failed. Please try again.")
        }
    } catch (e: java.net.ConnectException) {
        return Pair(null, "Cannot connect to server")
    } catch (e: java.net.SocketTimeoutException) {
        return Pair(null, "Connection timeout")
    } catch (e: Exception) {
        return Pair(null, "Connection failed")
    }
}

suspend fun performSignup(
    registrationId: String,
    password: String,
    name: String,
    gender: String,
    email: String,
    age: Int
): Pair<UserProfile?, String?> {
    try {
        val url = URL("http://203.110.243.202:8000/register-user")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val requestBody = JSONObject().apply {
            put("registration_id", registrationId)
            put("password", password)
            put("name", name)
            put("gender", gender)
            put("email", email)
            put("age", age)
            put("roll_no", registrationId)
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val responseCode = connection.responseCode
        val response = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        connection.disconnect()

        if (responseCode in 200..299) {
            val profile = UserProfile(
                name = name,
                gender = gender,
                email = email,
                age = age,
                registration_id = registrationId,
                anonymousId = ""
            )
            return Pair(profile, null)
        } else if (responseCode == 400) {
            if (response.contains("already exists", ignoreCase = true)) {
                return Pair(null, "Registration ID already exists")
            } else {
                return Pair(null, "Registration failed")
            }
        } else {
            return Pair(null, "Registration failed. Please try again.")
        }

    } catch (e: java.net.ConnectException) {
        return Pair(null, "Cannot connect to server")
    } catch (e: java.net.SocketTimeoutException) {
        return Pair(null, "Connection timeout")
    } catch (e: Exception) {
        return Pair(null, "Connection failed")
    }
}

// ==================== LOGOUT CONFIRMATION DIALOG ====================

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    "Logout",
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
            }
        },
        text = {
            Text(
                "Are you sure you want to logout?",
                color = ColorTextSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ColorError
                )
            ) {
                Text("Yes, Logout", fontWeight = FontWeight.Bold)
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
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

// ==================== UI COMPONENTS ====================

@Composable
fun ProfileHeader(
    authMode: AuthMode,
    isLoggedIn: Boolean,
    onSwitchMode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with theme gradient
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = if (isLoggedIn) GradientPurpleDream else GradientSunsetRose,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLoggedIn) Icons.Default.Person else Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isLoggedIn) "My Profile" else "Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextPrimary
                )
                Text(
                    text = if (isLoggedIn)
                        "Manage your account"
                    else if (authMode == AuthMode.LOGIN)
                        "Sign in to continue"
                    else
                        "Create new account",
                    fontSize = 14.sp,
                    color = ColorTextSecondary
                )
            }

            if (!isLoggedIn) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .clickable { onSwitchMode() },
                    color = ThemePink.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (authMode == AuthMode.LOGIN) "Sign Up" else "Login",
                            color = ThemePink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (authMode == AuthMode.LOGIN)
                                Icons.Default.PersonAdd
                            else
                                Icons.Default.Login,
                            contentDescription = null,
                            tint = ThemePink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoggedInContent(
    profile: UserProfile,
    navController: NavController,
    context: Context,
    onLogoutClick: () -> Unit,
    onShowTermsPopup: () -> Unit,
    onShowPrivacyPopup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // === PROFILE CARD - REDESIGNED ===
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ColorCardBg)
        ) {
            Column {
                // Profile header with avatar and logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with initial and gradient
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                brush = GradientLateNight,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .shadow(8.dp, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            profile.name.take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // User Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            profile.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            profile.email,
                            fontSize = 13.sp,
                            color = ColorTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Logout Button
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onLogoutClick() },
                        color = ColorError.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = ColorError,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    color = ColorBorder.copy(alpha = 0.5f)
                )

                // Profile Details in a more compact, elegant layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Age
                    ProfileStatItem(
                        icon = Icons.Default.Cake,
                        value = "${profile.age}",
                        label = "Age",
                        gradient = GradientSunsetRose
                    )

                    // Gender
                    ProfileStatItem(
                        icon = when (profile.gender.lowercase(Locale.getDefault())) {
                            "male" -> Icons.Default.Male
                            "female" -> Icons.Default.Female
                            else -> Icons.Default.Transgender
                        },
                        value = profile.gender,
                        label = "Gender",
                        gradient = GradientPurpleDream
                    )

                    // Reg ID (shortened)
                    ProfileStatItem(
                        icon = Icons.Filled.ConfirmationNumber,
                        value = profile.registration_id.take(6) + "...",
                        label = "Reg ID",
                        gradient = GradientTealOcean
                    )
                }
            }
        }

        // === QUICK ACTIONS SECTION ===
        Text(
            "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
        )

        // Data Settings Card - Full width
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { navController.navigate("privacy_data") }
                )
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ColorCardBg),
            border = BorderStroke(1.dp, ColorBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(brush = GradientPurpleDream, shape = RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Data Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Text(
                        "Manage your privacy and assessment data",
                        fontSize = 13.sp,
                        color = ColorTextSecondary
                    )
                }

                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = ThemePink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // History Card - Full width
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Navigate to history */ }
                )
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ColorCardBg),
            border = BorderStroke(1.dp, ColorBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(brush = GradientTealOcean, shape = RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Assessment History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Text(
                        "View your past assessments",
                        fontSize = 13.sp,
                        color = ColorTextSecondary
                    )
                }

                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = ThemePink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // === LEGAL SECTION ===
        Text(
            "Legal",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Terms Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowTermsPopup() }
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ColorCardBg),
                border = BorderStroke(1.dp, ColorBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(brush = GradientSunsetRose, shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Terms & Conditions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Read terms",
                        fontSize = 12.sp,
                        color = ColorTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Privacy Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowPrivacyPopup() }
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ColorCardBg),
                border = BorderStroke(1.dp, ColorBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(brush = GradientGold, shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Privacy Policy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Read policy",
                        fontSize = 12.sp,
                        color = ColorTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Help & Support Card
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Navigate to help */ }
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ColorCardBg),
            border = BorderStroke(1.dp, ColorBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color = ThemePink.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ThemePink,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Help & Support",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextPrimary
                    )
                    Text(
                        "Get assistance with the app",
                        fontSize = 13.sp,
                        color = ColorTextSecondary
                    )
                }

                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = ThemePink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush = gradient, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary
        )
        Text(
            label,
            fontSize = 11.sp,
            color = ColorTextSecondary
        )
    }
}

@Composable
fun AuthCard(
    authMode: AuthMode,
    registrationId: String,
    onRegistrationIdChange: (String) -> Unit,
    onRegistrationIdTouched: () -> Unit,
    registrationIdError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    onPasswordTouched: () -> Unit,
    passwordError: String?,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onConfirmPasswordTouched: () -> Unit,
    confirmPasswordError: String?,
    name: String,
    onNameChange: (String) -> Unit,
    onNameTouched: () -> Unit,
    nameError: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    onEmailTouched: () -> Unit,
    emailError: String?,
    age: String,
    onAgeChange: (String) -> Unit,
    onAgeTouched: () -> Unit,
    ageError: String?,
    gender: String,
    onGenderChange: (String) -> Unit,
    onGenderTouched: () -> Unit,
    genderError: String?,
    showPassword: Boolean,
    onShowPasswordChange: (Boolean) -> Unit,
    showConfirmPassword: Boolean,
    onShowConfirmPasswordChange: (Boolean) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    isFormValid: Boolean,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title with gradient
            Column {
                Text(
                    if (authMode == AuthMode.LOGIN) "Welcome Back!" else "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .background(brush = GradientLateNight, shape = RoundedCornerShape(2.dp))
                )
            }

            // Messages
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ColorError.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ColorError.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ColorError, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage, color = ColorError, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (successMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ColorSuccess.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ColorSuccess.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMessage, color = ColorSuccess, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Form Fields
            AuthTextFieldProfile(
                value = registrationId,
                onValueChange = onRegistrationIdChange,
                onTouched = onRegistrationIdTouched,
                label = "Registration ID",
                icon = Icons.Outlined.ConfirmationNumber,
                enabled = !isLoading,
                isError = registrationIdError != null,
                errorText = registrationIdError,
                gradient = GradientPurpleDream
            )

            AuthTextFieldProfile(
                value = password,
                onValueChange = onPasswordChange,
                onTouched = onPasswordTouched,
                label = "Password",
                icon = Icons.Outlined.Lock,
                isPassword = true,
                showPassword = showPassword,
                onShowPasswordChange = onShowPasswordChange,
                enabled = !isLoading,
                isError = passwordError != null,
                errorText = passwordError,
                gradient = GradientSunsetRose
            )

            if (authMode == AuthMode.SIGNUP) {
                AuthTextFieldProfile(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    onTouched = onConfirmPasswordTouched,
                    label = "Confirm Password",
                    icon = Icons.Outlined.Lock,
                    isPassword = true,
                    showPassword = showConfirmPassword,
                    onShowPasswordChange = onShowConfirmPasswordChange,
                    enabled = !isLoading,
                    isError = confirmPasswordError != null,
                    errorText = confirmPasswordError,
                    gradient = GradientTealOcean
                )

                AuthTextFieldProfile(
                    value = name,
                    onValueChange = onNameChange,
                    onTouched = onNameTouched,
                    label = "Full Name",
                    icon = Icons.Outlined.Person,
                    enabled = !isLoading,
                    isError = nameError != null,
                    errorText = nameError,
                    gradient = GradientGold
                )

                AuthTextFieldProfile(
                    value = email,
                    onValueChange = onEmailChange,
                    onTouched = onEmailTouched,
                    label = "Email",
                    icon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                    enabled = !isLoading,
                    isError = emailError != null,
                    errorText = emailError,
                    gradient = GradientPurpleDream
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AuthTextFieldProfile(
                            value = age,
                            onValueChange = onAgeChange,
                            onTouched = onAgeTouched,
                            label = "Age",
                            icon = Icons.Default.Numbers,
                            keyboardType = KeyboardType.Number,
                            enabled = !isLoading,
                            isError = ageError != null,
                            errorText = ageError,
                            gradient = GradientSunsetRose
                        )
                    }

                    Box(modifier = Modifier.weight(1.3f)) {
                        GenderDropdownProfile(
                            selectedOption = gender,
                            onOptionSelected = {
                                onGenderChange(it)
                                onGenderTouched()
                            },
                            label = "Gender",
                            enabled = !isLoading,
                            isError = genderError != null,
                            errorText = genderError,
                            gradient = GradientTealOcean
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Submit Button
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isFormValid && !isLoading) GradientLateNight else Brush.horizontalGradient(listOf(ColorBorder, ColorBorder)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = Color.White
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (authMode == AuthMode.LOGIN) "Sign In" else "Create Account",
                                color = if (isFormValid) Color.White else ColorTextTertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = if (isFormValid) Color.White else ColorTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (authMode == AuthMode.SIGNUP) {
                Text(
                    "By creating an account, you agree to our Terms and Privacy Policy",
                    fontSize = 12.sp,
                    color = ColorTextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                )
            }
        }
    }
}

@Composable
fun AuthTextFieldProfile(
    value: String,
    onValueChange: (String) -> Unit,
    onTouched: () -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onShowPasswordChange: ((Boolean) -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    gradient: Brush = GradientPurpleDream
) {
    var isFocused by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (!focusState.isFocused) {
                        onTouched()
                    }
                },
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(label, color = ColorTextTertiary) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = if (value.isNotBlank() && !isError)
                                gradient
                            else
                                Brush.linearGradient(listOf(ColorTextTertiary.copy(alpha = 0.1f), ColorTextTertiary.copy(alpha = 0.1f))),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (value.isNotBlank() && !isError) Color.White else ColorTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon = if (isPassword && onShowPasswordChange != null) {
                {
                    IconButton(onClick = { onShowPasswordChange(!showPassword) }) {
                        Icon(
                            if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (isFocused) ThemePink else ColorTextTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            enabled = enabled,
            isError = isError,
            textStyle = LocalTextStyle.current.copy(
                color = ColorTextPrimary,
                fontSize = 16.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ThemePink,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = InputBgColor,
                errorBorderColor = ColorError,
                errorContainerColor = ColorError.copy(alpha = 0.05f),
                cursorColor = ThemePink,
                errorCursorColor = ColorError,
            )
        )

        if (errorText != null) {
            Text(
                errorText,
                fontSize = 12.sp,
                color = ColorError,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun GenderDropdownProfile(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    gradient: Brush = GradientPurpleDream
) {
    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val options = listOf("Male", "Female", "Other", "Prefer not to say")

    val genderIcon = when (selectedOption.lowercase(Locale.getDefault())) {
        "male" -> Icons.Default.Male
        "female" -> Icons.Default.Female
        "other" -> Icons.Default.Transgender
        "prefer not to say" -> Icons.Default.Help
        else -> Icons.Outlined.Person
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    color = when {
                        isError -> ColorError.copy(alpha = 0.05f)
                        expanded || selectedOption.isNotEmpty() -> Color.White
                        else -> InputBgColor
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> ColorError
                        expanded -> ThemePink
                        else -> Color.Transparent
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(enabled = enabled) {
                    expanded = true
                    isFocused = true
                }
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = if (selectedOption.isNotEmpty() && !isError)
                                gradient
                            else
                                Brush.linearGradient(listOf(ColorTextTertiary.copy(alpha = 0.1f), ColorTextTertiary.copy(alpha = 0.1f))),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        genderIcon,
                        contentDescription = null,
                        tint = if (selectedOption.isNotEmpty() && !isError) Color.White else ColorTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = if (selectedOption.isEmpty()) label else selectedOption,
                    color = if (selectedOption.isEmpty()) ColorTextTertiary else
                        if (isError) ColorError else ColorTextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (expanded || isFocused) ThemePink else ColorTextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (errorText != null) {
            Text(
                errorText,
                fontSize = 12.sp,
                color = ColorError,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(color = ColorCardBg, shape = RoundedCornerShape(12.dp))
                .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = if (option == selectedOption) ThemePink else ColorTextPrimary,
                            fontWeight = if (option == selectedOption) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    leadingIcon = {
                        val optionIcon = when (option.lowercase(Locale.getDefault())) {
                            "male" -> Icons.Default.Male
                            "female" -> Icons.Default.Female
                            "other" -> Icons.Default.Transgender
                            else -> Icons.Default.Help
                        }
                        Icon(
                            optionIcon,
                            contentDescription = null,
                            tint = if (option == selectedOption) ThemePink else ColorTextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

// ==================== UTILITY FUNCTIONS ====================

fun generateAnonymousId(name: String, rollNo: String): String {
    return try {
        val input = "${name.lowercase(Locale.getDefault()).trim()}_${rollNo.lowercase(Locale.getDefault()).trim()}"
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(input.toByteArray())
        val hexString = hash.joinToString("") { "%02x".format(it) }
        "STU_${hexString.take(8).uppercase(Locale.getDefault())}"
    } catch (e: Exception) {
        "STU_${System.currentTimeMillis().toString().takeLast(8)}"
    }
}

fun saveRegistrationDetailsCSV(context: Context, userInfo: UserProfile) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appFolder = File(downloadsDir, "MochanApp")
        val userFolder = File(appFolder, userInfo.anonymousId)

        if (!userFolder.exists()) userFolder.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${userInfo.anonymousId}_registration_${timestamp}.csv"
        val csvFile = File(userFolder, fileName)

        val csvContent = StringBuilder()
        csvContent.append("anonymous_id,user_name,age,gender,roll_number,email\n")

        val emailToSave = userInfo.email.takeIf { it.isNotBlank() } ?: "NA"

        csvContent.append(
            "\"${userInfo.anonymousId}\"," +
                    "\"${userInfo.name}\"," +
                    "${userInfo.age}," +
                    "\"${userInfo.gender}\"," +
                    "\"${userInfo.registration_id}\"," +
                    "\"${emailToSave}\"\n"
        )

        csvFile.writeText(csvContent.toString())
        Log.d("ProfileScreen", "Registration CSV saved: ${csvFile.absolutePath}")

        val prefs = context.getSharedPreferences("file_paths", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("registration_csv", csvFile.absolutePath)
            putString("user_email", userInfo.email)
            apply()
        }

    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error saving registration CSV: ${e.message}")
    }
}

fun saveUser(context: Context, profile: UserProfile) {
    UserSessionHelper.saveUserData(
        context,
        UserSessionHelper.UserData(
            name = profile.name,
            gender = profile.gender,
            email = profile.email,
            age = profile.age,
            registrationId = profile.registration_id,
            anonymousId = profile.anonymousId,
            isLoggedIn = true
        )
    )

    val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    userPrefs.edit().apply {
        putString("user_name", profile.name)
        putString("user_gender", profile.gender)
        putString("user_email", profile.email)
        putInt("user_age", profile.age)
        putString("registration_id", profile.registration_id)
        putString("anonymous_id", profile.anonymousId)
        apply()
    }

    Log.d("ProfileScreen", "✅ Saved user via UserSessionHelper - Reg ID: ${profile.registration_id}, Anon ID: ${profile.anonymousId}")
}

fun loadSavedUser(context: Context): UserProfile? {
    val session = UserSessionHelper.getUserData(context)
    return if (session.isLoggedIn) {
        UserProfile(
            name = session.name,
            gender = session.gender,
            email = session.email,
            age = session.age,
            registration_id = session.registrationId,
            anonymousId = session.anonymousId
        )
    } else {
        null
    }
}

fun logoutUser(context: Context) {
    Log.d("ProfileScreen", "Logging out user")

    val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    sessionPrefs.edit().clear().apply()

    val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    userPrefs.edit().clear().apply()

    Log.d("ProfileScreen", "User logged out, preferences cleared")
}