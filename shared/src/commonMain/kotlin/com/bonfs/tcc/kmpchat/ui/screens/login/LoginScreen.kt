package com.bonfs.tcc.kmpchat.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bonfs.tcc.kmpchat.ui.theme.ErrorRed
import com.bonfs.tcc.kmpchat.ui.theme.KmpChatTheme
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBackground
import com.bonfs.tcc.kmpchat.ui.theme.MidnightBorder
import com.bonfs.tcc.kmpchat.ui.theme.MidnightE2EE
import com.bonfs.tcc.kmpchat.ui.theme.MidnightPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightSecondary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightSurface
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextSecondary

@Composable
fun LoginScreen(
    state: LoginUiState,
    onIntent: (LoginUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        LoginCardContent(state = state, onIntent = onIntent)
    }
}

@Composable
private fun LoginCardContent(
    state: LoginUiState,
    onIntent: (LoginUiIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MidnightSurface, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LoginHeader()
        Spacer(modifier = Modifier.height(24.dp))
        LoginForm(state = state, onIntent = onIntent)
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            LoginErrorBanner(state.errorMessage)
        }
        Spacer(modifier = Modifier.height(20.dp))
        LoginSubmitButton(state = state, onIntent = onIntent)
    }
}

@Composable
private fun LoginHeader() {
    Text(text = "🔒", fontSize = 42.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "KmpChat",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MidnightTextPrimary
    )
    Text(
        text = "Comunicação Segura E2EE",
        style = MaterialTheme.typography.bodyMedium,
        color = MidnightE2EE
    )
}

@Composable
private fun LoginForm(
    state: LoginUiState,
    onIntent: (LoginUiIntent) -> Unit
) {
    HomeserverField(
        value = state.homeserver,
        onValueChange = { onIntent(LoginUiIntent.HomeserverChanged(it)) }
    )
    Spacer(modifier = Modifier.height(12.dp))
    UsernameField(
        value = state.username,
        onValueChange = { onIntent(LoginUiIntent.UsernameChanged(it)) }
    )
    Spacer(modifier = Modifier.height(12.dp))
    PasswordField(
        value = state.password,
        onValueChange = { onIntent(LoginUiIntent.PasswordChanged(it)) }
    )
}

@Composable
private fun HomeserverField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Homeserver Matrix") },
        singleLine = true,
        colors = defaultFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Usuário") },
        singleLine = true,
        colors = defaultFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Senha") },
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Text(
                    text = if (isPasswordVisible) "Ocultar" else "Ver",
                    color = MidnightSecondary,
                    fontSize = 12.sp
                )
            }
        },
        colors = defaultFieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LoginErrorBanner(message: String) {
    Text(
        text = message,
        color = ErrorRed,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LoginSubmitButton(
    state: LoginUiState,
    onIntent: (LoginUiIntent) -> Unit
) {
    Button(
        onClick = { onIntent(LoginUiIntent.SubmitLogin) },
        enabled = !state.isLoading && state.username.isNotBlank() && state.password.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = MidnightPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = "Entrar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun defaultFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MidnightTextPrimary,
    unfocusedTextColor = MidnightTextPrimary,
    focusedBorderColor = MidnightPrimary,
    unfocusedBorderColor = MidnightBorder,
    focusedLabelColor = MidnightPrimary,
    unfocusedLabelColor = MidnightTextSecondary
)

@Preview
@Composable
private fun LoginScreenPreview() {
    KmpChatTheme {
        LoginScreen(
            state = LoginUiState(
                homeserver = "https://matrix.org",
                username = "alice",
                password = "secretpassword",
                isLoading = false,
                errorMessage = null
            ),
            onIntent = {}
        )
    }
}
