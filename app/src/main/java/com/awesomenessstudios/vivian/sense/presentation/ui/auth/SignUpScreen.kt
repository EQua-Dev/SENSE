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
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel()
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
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Join our community today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Sign Up Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomTextField(
                    value = state.displayName,
                    onValueChange = viewModel::updateDisplayName,
                    label = "Display Name",
                    imeAction = ImeAction.Next,
                    isError = state.error != null
                )

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
                    imeAction = ImeAction.Next,
                    isError = state.error != null
                )

                CustomTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    label = "Confirm Password",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = { viewModel.signUp() },
                    isError = state.error != null,
                    errorMessage = state.error
                )

                LoadingButton(
                    onClick = { viewModel.signUp() },
                    isLoading = state.isLoading,
                    text = "Sign Up",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Navigate to Sign In
        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onNavigateToSignIn
            ) {
                Text(
                    text = "Sign In",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}