package com.jarvis.features.inspector.presentation.ui.components

import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSDropdownMenuItem
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSSearchableJsonViewerDialog
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSThreeDotsMenu
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSIconButton
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Enhanced body viewer component with support for:
 * - JSON formatting and syntax highlighting
 * - Image display
 * - Text with download functionality
 * - Copy to clipboard
 */
@Composable
fun EnhancedBodyViewer(
    title: String,
    body: String?,
    contentType: String?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showSearchDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    Column {
        DSText(
            text = title,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Thin,
            color = DSJarvisTheme.colors.neutral.neutral100,
            modifier = Modifier.padding(
                start = DSJarvisTheme.spacing.m,
                bottom = DSJarvisTheme.spacing.s
            )
        )

        DSCard(
            modifier = modifier.fillMaxWidth(),
            shape = DSJarvisTheme.shapes.s,
            elevation = DSJarvisTheme.elevations.level1
        ) {
            Column {
                // Header with expand/collapse and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (!body.isNullOrBlank()) {
                        Row {
                            if (isJsonContent(contentType)) {
                                DSIconButton(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search JSON",
                                    onClick = {
                                        showSearchDialog = true
                                    },
                                    tint = DSJarvisTheme.colors.primary.primary60
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            DSIconButton(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy to clipboard",
                                onClick = {
                                    coroutineScope.launch {
                                        clipboard.setClipEntry(
                                            ClipEntry(
                                                ClipData.newPlainText(
                                                    "Body content",
                                                    body
                                                )
                                            )
                                        )
                                        Toast.makeText(
                                            context,
                                            "Copied to clipboard",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                tint = DSJarvisTheme.colors.primary.primary60
                            )

                            // Expand/collapse button
                            IconButton(
                                onClick = { isExpanded = !isExpanded }
                            ) {
                                DSIcon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                                )
                            }
                        }
                    }
                }

                if (isExpanded) {
                    if (body.isNullOrBlank()) {
                        DSText(
                            text = "No content",
                            style = DSJarvisTheme.typography.body.medium,
                            color = DSJarvisTheme.colors.neutral.neutral60,
                            modifier = Modifier.padding(DSJarvisTheme.spacing.m)

                        )
                    } else {
                        when {
                            isJsonContent(contentType) -> JsonViewer(contentType, body)
                            isImageContent(contentType) -> ImageViewer(body, context)
                            else -> TextViewer(contentType, body)
                        }
                    }
                }
            }
        }

        // Search dialog for JSON content
        if (showSearchDialog && !body.isNullOrBlank() && isJsonContent(contentType)) {
            DSSearchableJsonViewerDialog(
                jsonContent = body,
                title = title,
                onDismiss = { showSearchDialog = false }
            )
        }
    }
}

@Composable
private fun JsonViewer(
    contentType: String?,
    jsonString: String
) {
    val formattedJson = remember(jsonString) { formatJson(jsonString) }

    SelectionContainer {
        Box {
            DSText(
                text = formattedJson,
                style = DSJarvisTheme.typography.body.small.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = DSJarvisTheme.colors.neutral.neutral100,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DSJarvisTheme.colors.extra.background,
                        shape = DSJarvisTheme.shapes.s
                    )
                    .padding(DSJarvisTheme.dimensions.m)
                    .horizontalScroll(rememberScrollState())
            )

            DSText(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(DSJarvisTheme.spacing.s),
                text = getContentTypeLabel(contentType),
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.primary.primary60
            )
        }
    }
}

@Composable
private fun ImageViewer(imageData: String, context: Context) {
    // For now, we assume the body contains a URL or base64 data
    // In a real implementation, you might need to handle different image formats
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(DSJarvisTheme.dimensions.xxxxxxl),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageData.startsWith("http") -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageData)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Response image",
                    modifier = Modifier.fillMaxSize()
                )
            }
            imageData.startsWith("data:image") -> {
                // Handle base64 images
                DSText(
                    text = "Base64 Image\n(${imageData.length} characters)",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
            }
            else -> {
                DSText(
                    text = "Image Content\n(${imageData.length} characters)\nPreview not available",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
            }
        }
    }
}

@Composable
private fun TextViewer(
    contentType: String?,
    text: String
) {
    SelectionContainer {
        Box {
            DSText(
                text = text,
                style = DSJarvisTheme.typography.body.small.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = DSJarvisTheme.colors.neutral.neutral100,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DSJarvisTheme.colors.extra.background,
                        shape = DSJarvisTheme.shapes.s
                    )
                    .padding(DSJarvisTheme.dimensions.m)
                    .horizontalScroll(rememberScrollState()),
                maxLines = 50,
                overflow = TextOverflow.Ellipsis
            )

            DSText(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(DSJarvisTheme.spacing.s),
                text = getContentTypeLabel(contentType),
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.primary.primary60
            )
        }
    }
}

private fun formatJson(jsonString: String): String {
    return try {
        when {
            jsonString.trim().startsWith("{") -> {
                JSONObject(jsonString).toString(2)
            }
            jsonString.trim().startsWith("[") -> {
                JSONArray(jsonString).toString(2)
            }
            else -> jsonString
        }
    } catch (e: JSONException) {
        jsonString // Return original if not valid JSON
    }
}

private fun isJsonContent(contentType: String?): Boolean {
    return contentType?.contains("application/json") == true ||
           contentType?.contains("application/vnd.api+json") == true ||
           contentType?.contains("text/json") == true
}

private fun isImageContent(contentType: String?): Boolean {
    return contentType?.startsWith("image/") == true
}

private fun getContentTypeLabel(contentType: String?): String {
    return when {
        contentType == null -> "Unknown"
        isJsonContent(contentType) -> "JSON"
        isImageContent(contentType) -> "Image"
        contentType.startsWith("text/") -> "Text"
        contentType.startsWith("application/xml") -> "XML"
        contentType.startsWith("text/html") -> "HTML"
        else -> contentType.substringAfter("/").uppercase()
    }
}

@Preview(showBackground = true, name = "JSON Content")
@Composable
private fun EnhancedBodyViewerJsonPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnhancedBodyViewer(
                title = "User Data",
                body = """{"name":"John Doe","age":30,"email":"john@example.com"}""",
                contentType = "application/json"
            )
        }
    }
}

@Preview(showBackground = true, name = "Text Content")
@Composable
private fun EnhancedBodyViewerTextPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnhancedBodyViewer(
                title = "Log Output",
                body = "This is a plain text log entry.\nLine 2 of the log.\nLine 3 of the log.",
                contentType = "text/plain"
            )
        }
    }
}

@Preview(showBackground = true, name = "Image Content (URL)")
@Composable
private fun EnhancedBodyViewerImagePreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnhancedBodyViewer(
                title = "Profile Picture",
                body = "https://developer.android.com/images/brand/Android_Robot.png",
                contentType = "image/png"
            )
        }
    }
}

@Preview(showBackground = true, name = "No Content")
@Composable
private fun EnhancedBodyViewerEmptyPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EnhancedBodyViewer(
                title = "Empty Body",
                body = null,
                contentType = null
            )
        }
    }
}