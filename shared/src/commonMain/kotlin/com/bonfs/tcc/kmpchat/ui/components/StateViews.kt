package com.bonfs.tcc.kmpchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.bonfs.tcc.kmpchat.ui.theme.KmpChatTheme
import com.bonfs.tcc.kmpchat.ui.theme.MidnightPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextPrimary
import com.bonfs.tcc.kmpchat.ui.theme.MidnightTextSecondary

@Composable
fun LoadingView(
    message: String = "Carregando...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MidnightPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MidnightTextSecondary
        )
    }
}

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚠️", fontSize = 42.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Ocorreu um erro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MidnightTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MidnightTextSecondary,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(onClick = onRetry) {
                Text(text = "Tentar novamente")
            }
        }
    }
}

@Composable
fun EmptyView(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "💬", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MidnightTextPrimary,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MidnightTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun LoadingViewPreview() {
    KmpChatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LoadingView(message = "Sincronizando com o servidor Matrix...")
        }
    }
}

@Preview
@Composable
private fun ErrorViewPreview() {
    KmpChatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            ErrorView(
                message = "Não foi possível conectar ao servidor. Verifique sua conexão.",
                onRetry = {}
            )
        }
    }
}

@Preview
@Composable
private fun EmptyViewPreview() {
    KmpChatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            EmptyView(
                title = "Nenhuma conversa encontrada",
                subtitle = "Inicie um novo chat seguro usando o Matrix ID de um contato."
            )
        }
    }
}
