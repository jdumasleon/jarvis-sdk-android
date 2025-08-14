package com.jarvis.features.inspector.presentation.ui.rules

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.*
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.navigation.ActionRegistry
import com.jarvis.features.inspector.domain.entity.NetworkRule
import com.jarvis.features.inspector.domain.entity.RuleMode

/**
 * Screen for managing network interception rules
 */
@Composable
fun NetworkRulesScreen(
    actionKey: String,
    modifier: Modifier = Modifier,
    viewModel: NetworkRulesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Register the add rule action
    DisposableEffect(viewModel) {
        ActionRegistry.registerAction(actionKey) {
            viewModel.onEvent(NetworkRulesEvent.CreateNewRule)
        }
        onDispose {
            ActionRegistry.unregisterAction(actionKey)
        }
    }

    NetworkRulesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun NetworkRulesScreen(
    uiState: NetworkRulesUiState,
    onEvent: (NetworkRulesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        ResourceStateContent(
            resourceState = uiState,
            onRetry = { onEvent(NetworkRulesEvent.RefreshRules) },
            onDismiss = { onEvent(NetworkRulesEvent.ClearError) },
            loadingMessage = "Loading network rules...",
            emptyMessage = "No network rules configured",
            emptyActionText = "Create Rule",
            onEmptyAction = { onEvent(NetworkRulesEvent.CreateNewRule) }
        ) { data ->
            NetworkRulesContent(
                data = data,
                onEvent = onEvent
            )
        }
    }
    
    // Rule Editor Dialog
    if (uiState.getDataOrNull()?.showRuleEditor == true && uiState.getDataOrNull()?.selectedRule != null) {
        NetworkRuleEditorDialog(
            rule = uiState.getDataOrNull()!!.selectedRule!!,
            onSave = { rule -> onEvent(NetworkRulesEvent.SaveRule(rule)) },
            onDismiss = { onEvent(NetworkRulesEvent.ShowRuleEditor(false)) }
        )
    }
    
    // Import/Export Dialog
    if (uiState.getDataOrNull()?.showImportExport == true) {
        ImportExportDialog(
            onImport = { json -> onEvent(NetworkRulesEvent.ImportRules(json)) },
            onExport = { onEvent(NetworkRulesEvent.ExportRules) },
            onDismiss = { onEvent(NetworkRulesEvent.ShowImportExport(false)) }
        )
    }
}

@Composable
private fun NetworkRulesContent(
    data: NetworkRulesData,
    onEvent: (NetworkRulesEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var isHeaderVisible by rememberSaveable { mutableStateOf(true) }
    var headerHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    
    // Optimized nested scroll connection with debouncing
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private val threshold = 8f
            private var accumulatedDelta = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && headerHeightPx > 0) {
                    val dy = available.y
                    accumulatedDelta += dy

                    // Only trigger state change when threshold is exceeded
                    when {
                        accumulatedDelta < -threshold && isHeaderVisible -> {
                            isHeaderVisible = false
                            accumulatedDelta = 0f
                        }
                        accumulatedDelta > threshold && !isHeaderVisible -> {
                            isHeaderVisible = true
                            accumulatedDelta = 0f
                        }
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (headerHeightPx > 0) {
                    when {
                        available.y < -200 && isHeaderVisible -> isHeaderVisible = false
                        available.y > 200 && !isHeaderVisible -> isHeaderVisible = true
                    }
                }
                accumulatedDelta = 0f
                return Velocity.Zero
            }
        }
    }

    // Animated progress with optimized spring animation
    val headerProgress by animateFloatAsState(
        targetValue = if (isHeaderVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "headerProgress"
    )

    // Calculate animated offset
    val headerOffsetPx = remember(headerProgress, headerHeightPx) {
        -headerHeightPx * (1f - headerProgress)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Animated Header Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = headerOffsetPx
                    alpha = headerProgress.coerceIn(0f, 1f)
                }
                .onGloballyPositioned { coordinates ->
                    val newHeight = coordinates.size.height
                    if (newHeight != headerHeightPx && newHeight > 0) {
                        headerHeightPx = newHeight
                    }
                }
        ) {
            NetworkRulesHeader(
                rulesCount = data.rules.size,
                enabledRulesCount = data.enabledRulesCount,
                onCreateRule = { onEvent(NetworkRulesEvent.CreateNewRule) },
                onImportExport = { onEvent(NetworkRulesEvent.ShowImportExport(true)) }
            )
        }

        // Content below header with dynamic padding adjustment
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = with(density) {
                        (headerHeightPx * headerProgress).toInt().toDp()
                    }
                ),
            contentPadding = PaddingValues(
                start = DSJarvisTheme.spacing.m,
                end = DSJarvisTheme.spacing.m,
                bottom = DSJarvisTheme.spacing.m
            ),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            // Rules list
            if (data.rules.isNotEmpty()) {
                item {
                    DSText(
                        text = "Rules",
                        style = DSJarvisTheme.typography.heading.heading5,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s)
                    )
                }
                
                items(data.rules) { rule ->
                    NetworkRuleItem(
                        rule = rule,
                        onToggle = { enabled -> 
                            onEvent(NetworkRulesEvent.ToggleRule(rule.id, enabled))
                        },
                        onEdit = { onEvent(NetworkRulesEvent.EditRule(rule)) },
                        onDelete = { onEvent(NetworkRulesEvent.DeleteRule(rule.id)) }
                    )
                }
            }
            
            // Application history section
            if (data.applicationHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DSText(
                            text = "Recent Applications",
                            style = DSJarvisTheme.typography.heading.heading5,
                            fontWeight = FontWeight.Bold
                        )
                        
                        DSButton(
                            text = "Clear",
                            onClick = { onEvent(NetworkRulesEvent.ClearHistory) },
                            style = DSButtonStyle.TEXT,
                            size = DSButtonSize.SMALL
                        )
                    }
                }
                
                items(data.applicationHistory.take(10)) { application ->
                    RuleApplicationItem(application = application)
                }
            }
        }
    }
}

@Composable
private fun NetworkRulesHeader(
    rulesCount: Int,
    enabledRulesCount: Int,
    onCreateRule: () -> Unit,
    onImportExport: () -> Unit
) {
    DSCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DSJarvisTheme.spacing.m),
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    DSText(
                        text = "Network Rules",
                        style = DSJarvisTheme.typography.heading.heading4,
                        fontWeight = FontWeight.Bold
                    )
                    
                    DSText(
                        text = "$enabledRulesCount of $rulesCount rules enabled",
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    DSButton(
                        text = "Import/Export",
                        onClick = onImportExport,
                        style = DSButtonStyle.OUTLINE,
                        size = DSButtonSize.SMALL,
                        leadingIcon = Icons.Default.ImportExport
                    )
                    
                    DSButton(
                        text = "Create Rule",
                        onClick = onCreateRule,
                        style = DSButtonStyle.PRIMARY,
                        size = DSButtonSize.SMALL,
                        leadingIcon = Icons.Default.Add
                    )
                }
            }
            
            if (enabledRulesCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = DSJarvisTheme.colors.success.success20,
                            shape = DSJarvisTheme.shapes.s
                        )
                        .padding(DSJarvisTheme.spacing.s),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    DSIcon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Active",
                        tint = DSJarvisTheme.colors.success.success80
                    )
                    
                    DSText(
                        text = "Network interception is active",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.success.success80,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkRuleItem(
    rule: NetworkRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    DSCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = DSJarvisTheme.elevations.level1
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            // Header with name, status, and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    DSText(
                        text = rule.name,
                        style = DSJarvisTheme.typography.heading.heading5,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    DSTag(
                        tag = rule.mode.name,
                        style = when (rule.mode) {
                            RuleMode.INSPECT -> DSTagStyle.Info
                            RuleMode.MOCK -> DSTagStyle.Neutral
                        }
                    )
                    
                    if (rule.isEnabled) {
                        DSTag(
                            tag = "ENABLED",
                            style = DSTagStyle.Success
                        )
                    }
                }
                
                Row {
                    DSThreeDotsMenu(
                        items = listOf(
                            DSDropdownMenuItem(
                                text = if (rule.isEnabled) "Disable" else "Enable",
                                icon = if (rule.isEnabled) Icons.Default.ToggleOff else Icons.Default.ToggleOn,
                                onClick = { onToggle(!rule.isEnabled) }
                            ),
                            DSDropdownMenuItem(
                                text = "Edit",
                                icon = Icons.Default.Edit,
                                onClick = onEdit
                            ),
                            DSDropdownMenuItem(
                                text = "Delete",
                                icon = Icons.Default.Delete,
                                onClick = onDelete
                            )
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            
            // Origin details
            Column(
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
            ) {
                rule.origin.hostUrl?.let { host ->
                    RuleDetailItem("Host", host)
                }
                
                rule.origin.method?.let { method ->
                    RuleDetailItem("Method", method)
                }
                
                rule.origin.path?.let { path ->
                    RuleDetailItem("Path", path)
                }
                
                if (rule.origin.protocols.isNotEmpty()) {
                    RuleDetailItem("Protocols", rule.origin.protocols.joinToString(", "))
                }
            }
        }
    }
}

@Composable
private fun RuleDetailItem(
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        DSText(
            text = "$label:",
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral60,
            fontWeight = FontWeight.Medium
        )
        
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
    }
}

@Composable
private fun RuleApplicationItem(
    application: com.jarvis.features.inspector.domain.entity.RuleApplicationResult
) {
    DSCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = DSJarvisTheme.elevations.none
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSJarvisTheme.spacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                DSText(
                    text = application.ruleName,
                    style = DSJarvisTheme.typography.body.medium,
                    fontWeight = FontWeight.Medium
                )
                
                DSText(
                    text = application.modificationsApplied.joinToString(" • "),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
            
            DSTag(
                tag = application.mode.name,
                style = when (application.mode) {
                    RuleMode.INSPECT -> DSTagStyle.Info
                    RuleMode.MOCK -> DSTagStyle.Warning
                }
            )
        }
    }
}

// Placeholder dialogs - these would be implemented as separate components
@Composable
private fun NetworkRuleEditorDialog(
    rule: NetworkRule,
    onSave: (NetworkRule) -> Unit,
    onDismiss: () -> Unit
) {
    // Placeholder for rule editor dialog
    // This would contain forms for editing all rule properties
    DSText(text = "Rule Editor Dialog - To be implemented")
}

@Composable
private fun ImportExportDialog(
    onImport: (String) -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    // Placeholder for import/export dialog
    // This would contain text fields and file choosers
    DSText(text = "Import/Export Dialog - To be implemented")
}

@Preview(showBackground = true, name = "Network Rules Screen - With Rules")
@Composable
private fun NetworkRulesScreenPreview() {
    DSJarvisTheme {
        NetworkRulesScreen(
            uiState = com.jarvis.core.presentation.state.ResourceState.Success(
                NetworkRulesData.mockRulesData()
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Network Rules Screen - Empty")
@Composable
private fun NetworkRulesScreenEmptyPreview() {
    DSJarvisTheme {
        NetworkRulesScreen(
            uiState = com.jarvis.core.presentation.state.ResourceState.Success(
                NetworkRulesData.empty()
            ),
            onEvent = {}
        )
    }
}