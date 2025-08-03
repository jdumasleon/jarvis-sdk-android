package com.jarvis.demo.presentation.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSCircularProgressIndicator
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSTextField
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.demo.R

@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferencesList.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DSJarvisTheme.spacing.l)
    ) {
        // Header
        DSText(
            text = stringResource(R.string.preferences_description),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60,
            modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.m)
        )
        
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            // Preferences List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                items(preferences) { preference ->
                    PreferenceItem(
                        preference = preference,
                        onValueChanged = { newValue ->
                            viewModel.updatePreference(
                                preference.key,
                                newValue,
                                preference.type
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSCircularProgressIndicator(
            modifier = Modifier.size(DSJarvisTheme.dimensions.xxxl),
            color = DSJarvisTheme.colors.primary.primary40
        )
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))
        
        DSText(
            text = stringResource(R.string.loading),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

@Composable
private fun PreferenceItem(
    preference: PreferenceItem,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editableValue by remember(preference.value) { mutableStateOf(preference.value) }
    var isEditing by remember { mutableStateOf(false) }
    
    DSCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            // Key and Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = preference.key,
                        style = DSJarvisTheme.typography.body.medium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    DSText(
                        text = preference.type.name.lowercase(),
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral40
                    )
                }
                
                // Type indicator
                TypeIndicator(type = preference.type)
            }
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            
            // Value editor based on type
            when (preference.type) {
                PreferenceType.BOOLEAN -> {
                    Switch(
                        checked = preference.value.toBoolean(),
                        onCheckedChange = { checked ->
                            onValueChanged(checked.toString())
                        }
                    )
                }
                
                PreferenceType.STRING, PreferenceType.NUMBER -> {
                    if (isEditing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSTextField(
                                text = editableValue,
                                onValueChange = { editableValue = it },
                                modifier = Modifier.weight(1f),
                                placeholder = "Enter value..."
                            )
                            
                            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.s))
                            
                            // Save button (simplified - could use actual buttons)
                            Box(
                                modifier = Modifier
                                    .background(
                                        DSJarvisTheme.colors.primary.primary40,
                                        DSJarvisTheme.shapes.xs
                                    )
                                    .padding(DSJarvisTheme.spacing.s)
                            ) {
                                DSText(
                                    text = "Save",
                                    style = DSJarvisTheme.typography.body.small,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSText(
                                text = preference.value,
                                style = DSJarvisTheme.typography.body.medium,
                                color = DSJarvisTheme.colors.neutral.neutral60,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Edit button (simplified)
                            Box(
                                modifier = Modifier
                                    .background(
                                        DSJarvisTheme.colors.neutral.neutral20,
                                        DSJarvisTheme.shapes.xs
                                    )
                                    .padding(DSJarvisTheme.spacing.s)
                            ) {
                                DSText(
                                    text = "Edit",
                                    style = DSJarvisTheme.typography.body.small,
                                    color = DSJarvisTheme.colors.neutral.neutral60
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeIndicator(
    type: PreferenceType,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (type) {
        PreferenceType.STRING -> Pair(Color(0xFF6366F1), "STR")
        PreferenceType.BOOLEAN -> Pair(Color(0xFF10B981), "BOOL")
        PreferenceType.NUMBER -> Pair(Color(0xFFF59E0B), "NUM")
    }
    
    Box(
        modifier = modifier
            .background(color, DSJarvisTheme.shapes.xs)
            .padding(horizontal = DSJarvisTheme.dimensions.s, vertical = DSJarvisTheme.dimensions.xs)
    ) {
        DSText(
            text = text,
            style = DSJarvisTheme.typography.body.small,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}