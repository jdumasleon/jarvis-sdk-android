package com.jarvis.features.inspector.presentation.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jarvis.features.inspector.domain.entity.NetworkRequest
import com.jarvis.features.inspector.domain.entity.NetworkResponse
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.s,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with expand/collapse and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSText(
                        text = title,
                        style = DSJarvisTheme.typography.heading.heading4,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!body.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        DSText(
                            text = getContentTypeLabel(contentType),
                            style = DSJarvisTheme.typography.body.small,
                            color = DSJarvisTheme.colors.primary.primary100
                        )
                    }
                }
                
                if (!body.isNullOrBlank()) {
                    Row {
                        // Copy button
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(body))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            DSIcon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy to clipboard"
                            )
                        }
                        
                        // Download button
                        IconButton(
                            onClick = {
                                downloadContent(context, body, contentType, title)
                            }
                        ) {
                            DSIcon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download content"
                            )
                        }
                        
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
                Spacer(modifier = Modifier.height(12.dp))
                
                if (body.isNullOrBlank()) {
                    DSText(
                        text = "No content",
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                } else {
                    when {
                        isJsonContent(contentType) -> JsonViewer(body)
                        isImageContent(contentType) -> ImageViewer(body, context)
                        else -> TextViewer(body)
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonViewer(jsonString: String) {
    val formattedJson = remember(jsonString) { formatJson(jsonString) }
    
    SelectionContainer {
        DSText(
            text = formattedJson,
            style = DSJarvisTheme.typography.body.small.copy(
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DSJarvisTheme.colors.neutral.neutral20,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun ImageViewer(imageData: String, context: Context) {
    // For now, we assume the body contains a URL or base64 data
    // In a real implementation, you might need to handle different image formats
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
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
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
            else -> {
                DSText(
                    text = "Image Content\n(${imageData.length} characters)\nPreview not available",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

@Composable
private fun TextViewer(text: String) {
    SelectionContainer {
        DSText(
            text = text,
            style = DSJarvisTheme.typography.body.small.copy(
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DSJarvisTheme.colors.neutral.neutral20,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState()),
            maxLines = 50,
            overflow = TextOverflow.Ellipsis
        )
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

private fun downloadContent(
    context: Context,
    content: String,
    contentType: String?,
    title: String
) {
    try {
        val fileName = generateFileName(title, contentType)
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileOutputStream(file).use { output ->
            output.write(content.toByteArray())
        }
        
        // Create intent to share/open the file
        val uri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, contentType ?: "text/plain")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "File saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to download: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun generateFileName(title: String, contentType: String?): String {
    val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
    val extension = when {
        isJsonContent(contentType) -> "json"
        isImageContent(contentType) -> contentType?.substringAfter("/") ?: "img"
        contentType?.contains("xml") == true -> "xml"
        contentType?.contains("html") == true -> "html"
        else -> "txt"
    }
    return "${sanitizedTitle}.${extension}"
}