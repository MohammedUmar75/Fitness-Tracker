package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    onEmailAuth: (email: String, password: String, isSignUp: Boolean, name: String?) -> Unit,
    onGoogleAuth: (email: String, name: String) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var isSignUp by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showGoogleAccountChooser by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("user@gmail.com") }
    var customGoogleName by remember { mutableStateOf("User Name") }

    val colorScheme = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(28.dp)),
            color = colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(colorScheme.primary, colorScheme.tertiary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = "Fitness Tracker",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Welcome Back",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = if (isSignUp) "Sign up to start tracking your fitness goals" else "Sign in to access your workout and health data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Toggle Tab (Sign In / Sign Up)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = {
                                isSignUp = false
                                errorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isSignUp) colorScheme.primary else Color.Transparent,
                                contentColor = if (!isSignUp) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = null,
                            modifier = Modifier.weight(1f).testTag("tab_sign_in")
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isSignUp = true
                                errorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUp) colorScheme.primary else Color.Transparent,
                                contentColor = if (isSignUp) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = null,
                            modifier = Modifier.weight(1f).testTag("tab_sign_up")
                        ) {
                            Text("Sign Up", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (errorMessage != null) {
                    Surface(
                        color = colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Sign Up Name Field
                if (isSignUp) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            errorMessage = null
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Alex Johnson") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Email Field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("user@example.com") },
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Submit Button
                Button(
                    onClick = {
                        val email = emailInput.trim()
                        val password = passwordInput.trim()

                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }
                        if (isSignUp && nameInput.isBlank()) {
                            errorMessage = "Please enter your full name."
                            return@Button
                        }

                        onEmailAuth(email, password, isSignUp, nameInput.ifBlank { null })
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // OR Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = colorScheme.outlineVariant)
                    Text(
                        text = " OR ",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = colorScheme.outlineVariant)
                }

                // Google Direct Sign-In Button
                OutlinedButton(
                    onClick = {
                        showGoogleAccountChooser = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_signin_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
    }

    // Google Account Picker Dialog
    if (showGoogleAccountChooser) {
        Dialog(
            onDismissRequest = { showGoogleAccountChooser = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp)),
                color = colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("G", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Choose an account to continue to Fitness Tracker",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )

                    // Quick Select Current User Google Account
                    Surface(
                        onClick = {
                            showGoogleAccountChooser = false
                            onGoogleAuth(customGoogleEmail, customGoogleName)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.5.dp, colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().testTag("select_google_account_default")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = customGoogleName.take(1).uppercase(),
                                    color = colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customGoogleName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = customGoogleEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = colorScheme.primary)
                        }
                    }

                    // Custom Google Account Input
                    var editAccountMode by remember { mutableStateOf(false) }
                    if (!editAccountMode) {
                        TextButton(
                            onClick = { editAccountMode = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Use another Google account")
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customGoogleName,
                                onValueChange = { customGoogleName = it },
                                label = { Text("Google Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = customGoogleEmail,
                                onValueChange = { customGoogleEmail = it },
                                label = { Text("Google Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    showGoogleAccountChooser = false
                                    onGoogleAuth(customGoogleEmail, customGoogleName)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue as $customGoogleName")
                            }
                        }
                    }

                    TextButton(
                        onClick = { showGoogleAccountChooser = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
