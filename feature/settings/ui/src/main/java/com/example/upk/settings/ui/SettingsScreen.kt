package com.example.upk.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upk.settings.domain.models.ThemeMode

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .selectableGroup(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleLarge,
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            ThemeMode.entries.forEach { option ->
                val label = when (option) {
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.SYSTEM -> "System"
                }

                val enabled = !uiState.isUpdatingTheme

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.themeMode == option,
                            enabled = enabled,
                            onClick = {
                                onIntent(
                                    SettingsIntent.ThemeModeSelected(option),
                                )
                            },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = uiState.themeMode == option,
                        onClick = null,
                        enabled = enabled,
                    )

                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )

            TextButton(
                onClick = {
                    onIntent(
                        SettingsIntent.ErrorMessageDismissed,
                    )
                },
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Preview(showBackground = true, name = "Loaded - Dark Mode")
@Composable
private fun SettingsContentLoadedPreview() {
    MaterialTheme {
        Surface {
            SettingsContent(
                uiState = SettingsUiState(
                    themeMode = ThemeMode.DARK,
                    isLoading = false,
                ),
                onIntent = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun SettingsContentLoadingPreview() {
    MaterialTheme {
        Surface {
            SettingsContent(
                uiState = SettingsUiState(
                    isLoading = true,
                ),
                onIntent = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun SettingsContentErrorPreview() {
    MaterialTheme {
        Surface {
            SettingsContent(
                uiState = SettingsUiState(
                    themeMode = ThemeMode.SYSTEM,
                    isLoading = false,
                    errorMessage = "Unable to update appearance settings.",
                ),
                onIntent = {},
            )
        }
    }
}