package com.jarvis.features.inspector.presentation.ui.breakpoints

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jarvis.core.designsystem.component.*
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.inspector.domain.entity.*

/**
 * Comprehensive rule editor dialog for creating and editing network interception rules
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkRuleEditorDialog(
    rule: NetworkRule,
    onSave: (NetworkRule) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedRule by remember { mutableStateOf(rule) }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("Basic", "Origin", "Request", "Response")
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
            DSCard(
                modifier = modifier.fillMaxSize(),
                shape = DSJarvisTheme.shapes.l,
                elevation = DSJarvisTheme.elevations.level5
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DSJarvisTheme.spacing.m),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DSText(
                            text = if (rule.id.isEmpty()) "Create Network Rule" else "Edit Network Rule",
                            style = DSJarvisTheme.typography.heading.large,
                            fontWeight = FontWeight.Bold
                        )
                        
                        DSIconButton(
                            onClick = onDismiss,
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                    
                    // Tabs
                    DSTabBar(
                        selectedTabIndex = selectedTab,
                        tabCount = tabs.size,
                        onTabSelected = { selectedTab = it }
                    ) { index, selected ->
                        Box (
                            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
                        ) {
                            DSText(
                                text = tabs[index],
                                style = DSJarvisTheme.typography.body.medium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) {
                                    DSJarvisTheme.colors.primary.primary100
                                } else {
                                    DSJarvisTheme.colors.neutral.neutral100
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))
                    
                    // Tab Content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = DSJarvisTheme.spacing.m)
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (selectedTab) {
                            0 -> BasicRuleEditor(
                                rule = editedRule,
                                onRuleChange = { editedRule = it }
                            )
                            1 -> OriginRuleEditor(
                                origin = editedRule.origin,
                                onOriginChange = { editedRule = editedRule.copy(origin = it) }
                            )
                            2 -> RequestModificationEditor(
                                modifications = editedRule.requestModifications,
                                onModificationsChange = { editedRule = editedRule.copy(requestModifications = it) }
                            )
                            3 -> ResponseModificationEditor(
                                modifications = editedRule.responseModifications,
                                onModificationsChange = { editedRule = editedRule.copy(responseModifications = it) }
                            )
                        }
                    }
                    
                    // Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DSJarvisTheme.spacing.m),
                        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m, Alignment.End)
                    ) {
                        DSButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            style = DSButtonStyle.OUTLINE
                        )
                        
                        DSButton(
                            text = "Save Rule",
                            onClick = { onSave(editedRule) },
                            style = DSButtonStyle.PRIMARY,
                            disabled = editedRule.name.isNotBlank()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicRuleEditor(
    rule: NetworkRule,
    onRuleChange: (NetworkRule) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        // Rule Name
        DSTextField(
            text = rule.name,
            onValueChange = { onRuleChange(rule.copy(name = it)) },
            title = "Rule Name",
            placeholder = "Enter a descriptive name for this rule",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Enabled Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                DSText(
                    text = "Enable Rule",
                    style = DSJarvisTheme.typography.body.medium,
                    fontWeight = FontWeight.Medium
                )
                DSText(
                    text = "Enable this rule to start intercepting matching requests",
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
            
            DSSwitch(
                checked = rule.isEnabled,
                onCheckedChange = { onRuleChange(rule.copy(isEnabled = it)) }
            )
        }
        
        // Rule Mode
        Column {
            DSText(
                text = "Interception Mode",
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                RuleModeOption(
                    mode = RuleMode.INSPECT,
                    selectedMode = rule.mode,
                    onModeSelected = { onRuleChange(rule.copy(mode = it)) }
                )
                
                RuleModeOption(
                    mode = RuleMode.MOCK,
                    selectedMode = rule.mode,
                    onModeSelected = { onRuleChange(rule.copy(mode = it)) }
                )
            }
        }
    }
}

@Composable
private fun RuleModeOption(
    mode: RuleMode,
    selectedMode: RuleMode,
    onModeSelected: (RuleMode) -> Unit
) {
    val isSelected = mode == selectedMode
    
    DSCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onModeSelected(mode) }
            )
            .then(
                if (isSelected) {
                    Modifier.background(
                        DSJarvisTheme.colors.primary.primary100,
                        DSJarvisTheme.shapes.s
                    )
                } else Modifier
            ),
        elevation = if (isSelected) DSJarvisTheme.elevations.level2 else DSJarvisTheme.elevations.level1,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSJarvisTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onModeSelected(mode) }
            )
            
            Spacer(modifier = Modifier.width(DSJarvisTheme.spacing.m))
            
            Column {
                DSText(
                    text = when (mode) {
                        RuleMode.INSPECT -> "Inspect Mode"
                        RuleMode.MOCK -> "Mock Mode"
                    },
                    style = DSJarvisTheme.typography.body.medium,
                    fontWeight = FontWeight.Medium
                )
                
                DSText(
                    text = when (mode) {
                        RuleMode.INSPECT -> "Intercepts requests, applies modifications, then continues with actual network call"
                        RuleMode.MOCK -> "Intercepts requests and responds with mock data without making actual network call"
                    },
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

@Composable
private fun OriginRuleEditor(
    origin: RuleOrigin,
    onOriginChange: (RuleOrigin) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Configure which requests this rule should match",
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
        
        // Protocols
        DSMultiSelectChipGroup(
            options = listOf("http", "https", "ws", "wss"),
            selectedOptions = origin.protocols,
            onSelectionChange = { onOriginChange(origin.copy(protocols = it)) },
            label = "Protocols",
            placeholder = "Select protocols (leave empty for all)"
        )
        
        // Host URL
        DSTextField(
            text = origin.hostUrl ?: "",
            onValueChange = { value ->
                onOriginChange(origin.copy(hostUrl = value.ifBlank { null }))
            },
            title = "Host URL",
            placeholder = "example.com, *.api.com, localhost",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Port
        DSTextField(
            text = origin.port?.toString() ?: "",
            onValueChange = { value ->
                val port = value.toIntOrNull()
                onOriginChange(origin.copy(port = port))
            },
            title = "Port",
            placeholder = "8080, 443, 80 (leave empty for all)",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Path
        DSTextField(
            text = origin.path ?: "",
            onValueChange = { value ->
                onOriginChange(origin.copy(path = value.ifBlank { null }))
            },
            title = "Path",
            placeholder = "/api/users, /api/*, /auth/**",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Query
        DSTextField(
            text = origin.query ?: "",
            onValueChange = { value ->
                onOriginChange(origin.copy(query = value.ifBlank { null }))
            },
            title = "Query Parameters",
            placeholder = "key=value, debug=*",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Method
        DSMultiSelectChipGroup(
            options = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"),
            selectedOptions = origin.method?.let { listOf(it) } ?: emptyList(),
            onSelectionChange = { selected ->
                onOriginChange(origin.copy(method = selected.firstOrNull()))
            },
            label = "HTTP Method",
            placeholder = "Select method (leave empty for all)",
            singleSelect = true
        )
    }
}

@Composable
private fun RequestModificationEditor(
    modifications: RequestModifications?,
    onModificationsChange: (RequestModifications?) -> Unit
) {
    val mods = modifications ?: RequestModifications()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Modify outgoing requests (optional)",
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
        
        // Headers
        HeadersEditor(
            addHeaders = mods.addHeaders,
            modifyHeaders = mods.modifyHeaders,
            removeHeaders = mods.removeHeaders,
            onAddHeadersChange = { headers ->
                onModificationsChange(mods.copy(addHeaders = headers))
            },
            onModifyHeadersChange = { headers ->
                onModificationsChange(mods.copy(modifyHeaders = headers))
            },
            onRemoveHeadersChange = { headers ->
                onModificationsChange(mods.copy(removeHeaders = headers))
            }
        )
        
        // Body Modification
        DSTextField(
            text = mods.modifyBody ?: "",
            onValueChange = { value ->
                onModificationsChange(mods.copy(modifyBody = value.ifBlank { null }))
            },
            title = "Modify Request Body",
            placeholder = "New request body (JSON, XML, etc.)",
            modifier = Modifier.fillMaxWidth()
        )
        
        // URL Modification  
        DSTextField(
            text = mods.modifyUrl ?: "",
            onValueChange = { value ->
                onModificationsChange(mods.copy(modifyUrl = value.ifBlank { null }))
            },
            title = "Modify URL",
            placeholder = "https://new-endpoint.com/api",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Method Modification
        DSTextField(
            text = mods.modifyMethod ?: "",
            onValueChange = { value ->
                onModificationsChange(mods.copy(modifyMethod = value.ifBlank { null }))
            },
            title = "Modify HTTP Method",
            placeholder = "GET, POST, PUT, etc.",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ResponseModificationEditor(
    modifications: ResponseModifications?,
    onModificationsChange: (ResponseModifications?) -> Unit
) {
    val mods = modifications ?: ResponseModifications()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Modify responses or create mock responses",
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
        
        // Status Code
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            DSTextField(
                text = mods.statusCode?.toString() ?: "",
                onValueChange = { value ->
                    val statusCode = value.toIntOrNull()
                    onModificationsChange(mods.copy(statusCode = statusCode))
                },
                title = "Status Code",
                placeholder = "200, 404, 500",
                modifier = Modifier.weight(1f)
            )
            
            DSTextField(
                text = mods.statusMessage ?: "",
                onValueChange = { value ->
                    onModificationsChange(mods.copy(statusMessage = value.ifBlank { null }))
                },
                title = "Status Message",
                placeholder = "OK, Not Found, Error",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Headers
        HeadersEditor(
            addHeaders = mods.addHeaders,
            modifyHeaders = mods.modifyHeaders,
            removeHeaders = mods.removeHeaders,
            onAddHeadersChange = { headers ->
                onModificationsChange(mods.copy(addHeaders = headers))
            },
            onModifyHeadersChange = { headers ->
                onModificationsChange(mods.copy(modifyHeaders = headers))
            },
            onRemoveHeadersChange = { headers ->
                onModificationsChange(mods.copy(removeHeaders = headers))
            }
        )
        
        // Body Modification
        DSTextField(
            text = mods.modifyBody ?: "",
            onValueChange = { value ->
                onModificationsChange(mods.copy(modifyBody = value.ifBlank { null }))
            },
            title = "Response Body",
            placeholder = "Response body content (JSON, XML, HTML, etc.)",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Delay
        DSTextField(
            text = if (mods.delay > 0) mods.delay.toString() else "",
            onValueChange = { value ->
                val delay = value.toLongOrNull() ?: 0L
                onModificationsChange(mods.copy(delay = delay))
            },
            title = "Response Delay (ms)",
            placeholder = "1000, 2000 (artificial delay)",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HeadersEditor(
    addHeaders: Map<String, String>,
    modifyHeaders: Map<String, String>,
    removeHeaders: List<String>,
    onAddHeadersChange: (Map<String, String>) -> Unit,
    onModifyHeadersChange: (Map<String, String>) -> Unit,
    onRemoveHeadersChange: (List<String>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        DSText(
            text = "Headers",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium
        )
        
        // This is a simplified headers editor
        // In a real implementation, you'd have dynamic add/remove functionality
        DSTextField(
            text = addHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" },
            onValueChange = { value ->
                val headers = parseHeadersString(value)
                onAddHeadersChange(headers)
            },
            title = "Add Headers",
            placeholder = "Content-Type: application/json\nAuthorization: Bearer token",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun parseHeadersString(headersString: String): Map<String, String> {
    if (headersString.isBlank()) return emptyMap()
    
    return headersString.lines()
        .mapNotNull { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                parts[0].trim() to parts[1].trim()
            } else null
        }
        .toMap()
}

// Placeholder for multi-select chip group - would be implemented as a design system component
@Composable
private fun DSMultiSelectChipGroup(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleSelect: Boolean = false
) {
    Column(modifier = modifier) {
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
        
        // Simplified chip implementation
        DSText(
            text = if (selectedOptions.isEmpty()) placeholder else selectedOptions.joinToString(", "),
            style = DSJarvisTheme.typography.body.small,
            color = if (selectedOptions.isEmpty()) DSJarvisTheme.colors.neutral.neutral60 else DSJarvisTheme.colors.neutral.neutral100
        )
    }
}

/**
 * Standalone previews for each tab of NetworkRuleEditorDialog
 * so you can iterate on UI quickly without navigating tabs.
 */

// ——— BASIC TAB ———
@Preview(showBackground = true, name = "Basic Tab • Inspect")
@Composable
private fun BasicRuleEditorPreviewInspect() {
    DSJarvisTheme {
        BasicRuleEditor(
            rule = NetworkRule(
                id = "",
                name = "Inspect Requests",
                isEnabled = true,
                mode = RuleMode.INSPECT,
                origin = RuleOrigin(protocols = listOf("https"), hostUrl = "api.example.com"),
                requestModifications = RequestModifications(),
                responseModifications = ResponseModifications()
            ),
            onRuleChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Basic Tab • Mock")
@Composable
private fun BasicRuleEditorPreviewMock() {
    DSJarvisTheme {
        BasicRuleEditor(
            rule = NetworkRule(
                id = "",
                name = "Mock Login",
                isEnabled = false,
                mode = RuleMode.MOCK,
                origin = RuleOrigin(protocols = listOf("https"), hostUrl = "auth.example.com"),
                requestModifications = RequestModifications(),
                responseModifications = ResponseModifications()
            ),
            onRuleChange = {}
        )
    }
}

// ——— ORIGIN TAB ———
@Preview(showBackground = true, name = "Origin Tab • Typical API Filter")
@Composable
private fun OriginRuleEditorPreview() {
    DSJarvisTheme {
        OriginRuleEditor(
            origin = RuleOrigin(
                protocols = listOf("https"),
                hostUrl = "api.example.com",
                port = 443,
                path = "/v1/users/*",
                query = "debug=*",
                method = "GET"
            ),
            onOriginChange = {}
        )
    }
}

// ——— REQUEST TAB ———
@Preview(showBackground = true, name = "Request Tab • Headers & Body")
@Composable
private fun RequestModificationEditorPreview() {
    DSJarvisTheme {
        RequestModificationEditor(
            modifications = RequestModifications(
                addHeaders = mapOf("X-Debug" to "true", "Authorization" to "Bearer <token>") ,
                modifyHeaders = mapOf("Content-Type" to "application/json"),
                removeHeaders = listOf("If-None-Match"),
                modifyBody = "{\n  \"force\": true\n}",
                modifyUrl = "https://staging.example.com/v1/users",
                modifyMethod = "POST"
            ),
            onModificationsChange = {}
        )
    }
}

// ——— RESPONSE TAB ———
@Preview(showBackground = true, name = "Response Tab • Mock 200")
@Composable
private fun ResponseModificationEditorPreviewOk() {
    DSJarvisTheme {
        ResponseModificationEditor(
            modifications = ResponseModifications(
                statusCode = 200,
                statusMessage = "OK",
                addHeaders = mapOf("Cache-Control" to "no-store"),
                modifyHeaders = emptyMap(),
                removeHeaders = listOf("Set-Cookie"),
                modifyBody = "{\n  \"id\": 1,\n  \"name\": \"Ada\"\n}",
                delay = 250
            ),
            onModificationsChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Response Tab • Error 429 with Delay")
@Composable
private fun ResponseModificationEditorPreviewError() {
    DSJarvisTheme {
        ResponseModificationEditor(
            modifications = ResponseModifications(
                statusCode = 429,
                statusMessage = "Too Many Requests",
                addHeaders = mapOf("Retry-After" to "3"),
                modifyHeaders = emptyMap(),
                removeHeaders = emptyList(),
                modifyBody = "{\n  \"error\": \"rate_limited\"\n}",
                delay = 1500
            ),
            onModificationsChange = {}
        )
    }
}

// ——— FULL DIALOG (per-tab) ———
// If you prefer full-dialog previews with a given tab selected,
// you can preview the whole dialog by passing a composed rule and no-op callbacks.
@Preview(showBackground = true, name = "Dialog • Basic Tab")
@Composable
private fun NetworkRuleEditorDialogPreviewBasicTab() {
    DSJarvisTheme {
        NetworkRuleEditorDialog(
            rule = NetworkRule(
                id = "",
                name = "API Mock Rule",
                isEnabled = true,
                mode = RuleMode.MOCK,
                origin = RuleOrigin(protocols = listOf("https"), hostUrl = "api.example.com"),
                requestModifications = RequestModifications(),
                responseModifications = ResponseModifications()
            ),
            onSave = {},
            onDismiss = {}
        )
    }
}

