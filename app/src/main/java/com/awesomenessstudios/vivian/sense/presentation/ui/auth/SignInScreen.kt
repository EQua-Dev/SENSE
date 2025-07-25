package com.awesomenessstudios.vivian.sense.presentation.ui.auth


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.awesomenessstudios.vivian.sense.presentation.ui.components.CustomTextField
import com.awesomenessstudios.vivian.sense.presentation.ui.components.LoadingButton
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.SignInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Sign In Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomTextField(
                    value = state.email,
                    onValueChange = viewModel::updateEmail,
                    label = "Email",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    isError = state.error != null
                )

                CustomTextField(
                    value = state.password,
                    onValueChange = viewModel::updatePassword,
                    label = "Password",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = { viewModel.signIn() },
                    isError = state.error != null,
                    errorMessage = state.error
                )

                LoadingButton(
                    onClick = { viewModel.signIn() },
                    isLoading = state.isLoading,
                    text = "Sign In",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Navigate to Sign Up
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onNavigateToSignUp
            ) {
                Text(
                    text = "Sign Up",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
