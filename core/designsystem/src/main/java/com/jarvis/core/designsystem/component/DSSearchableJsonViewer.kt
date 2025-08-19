package com.jarvis.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * DSJarvis Searchable JSON Viewer Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSSearchableJsonViewerDialog(
    jsonContent: String,
    modifier: Modifier = Modifier,
    title: String = "JSON Viewer",
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val formattedJson = remember(jsonContent) { formatJson(jsonContent) }
    
    val highlightBackgroundColor = DSJarvisTheme.colors.warning.warning100
    val highlightTextColor = DSJarvisTheme.colors.neutral.neutral100
    
    val highlightedJson by remember(formattedJson, searchQuery, highlightBackgroundColor, highlightTextColor) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                AnnotatedString(formattedJson)
            } else {
                highlightSearchMatches(
                    formattedJson, 
                    searchQuery,
                    highlightBackgroundColor,
                    highlightTextColor
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f))
                .fillMaxSize()
                .padding(DSJarvisTheme.spacing.m)
        ) {
            DSCard(
                modifier = modifier.fillMaxSize(),
                shape = DSJarvisTheme.shapes.l,
                elevation = DSJarvisTheme.elevations.level1
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = DSJarvisTheme.spacing.s)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSText(
                        text = title,
                        style = DSJarvisTheme.typography.body.large,
                        fontWeight = FontWeight.Thin,
                        color = DSJarvisTheme.colors.extra.black
                    )

                    DSIconButton(
                        onClick = onDismiss,
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DSJarvisTheme.colors.extra.black
                    )
                }

                Column (
                    modifier = modifier.padding(DSJarvisTheme.spacing.s),
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ){
                    // Search Bar
                    DSSearchBar(
                        searchText = searchQuery,
                        onValueChange = { searchQuery = it },
                        onTextClean = { searchQuery = "" },
                        placeholder = "Search in JSON content...",
                    )

                    // JSON content with search highlighting
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = DSJarvisTheme.colors.extra.background,
                                    shape = DSJarvisTheme.shapes.s
                                )
                                .padding(DSJarvisTheme.dimensions.m)
                        ) {
                            DSText(
                                text = highlightedJson,
                                style = DSJarvisTheme.typography.body.small.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = DSJarvisTheme.colors.neutral.neutral100,
                                modifier = Modifier
                                    .background(
                                        color = DSJarvisTheme.colors.extra.background,
                                        shape = DSJarvisTheme.shapes.s
                                    )
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState()),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact JSON Viewer for inline display
 */
@Composable
fun DSSearchableJsonViewer(
    jsonContent: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 10,
    onSearchRequested: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    val formattedJson = remember(jsonContent) { formatJson(jsonContent) }
    
    val highlightBackgroundColor = DSJarvisTheme.colors.warning.warning60
    val highlightTextColor = DSJarvisTheme.colors.neutral.neutral20
    
    val highlightedJson by remember(formattedJson, searchQuery, highlightBackgroundColor, highlightTextColor) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                AnnotatedString(formattedJson)
            } else {
                highlightSearchMatches(
                    formattedJson, 
                    searchQuery,
                    highlightBackgroundColor,
                    highlightTextColor
                )
            }
        }
    }

    Column(modifier = modifier) {
        // Search bar (if search is enabled)
        if (onSearchRequested != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Bar
                DSSearchBar(
                    searchText = searchQuery,
                    onValueChange = { searchQuery = it },
                    onTextClean = { searchQuery = "" },
                    placeholder = "Search in JSON content...",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m))
        }
        
        // JSON content
        SelectionContainer {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DSJarvisTheme.colors.extra.background,
                        shape = DSJarvisTheme.shapes.s
                    )
                    .padding(DSJarvisTheme.dimensions.m)
            ) {
                DSText(
                    text = highlightedJson,
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
                        .horizontalScroll(rememberScrollState()),
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

private fun highlightSearchMatches(
    text: String, 
    searchQuery: String,
    highlightBackgroundColor: Color,
    highlightTextColor: Color
): AnnotatedString {
    if (searchQuery.isBlank()) return AnnotatedString(text)
    
    return buildAnnotatedString {
        var currentIndex = 0
        val pattern = Pattern.compile(Pattern.quote(searchQuery), Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        
        while (matcher.find()) {
            // Add text before the match
            if (matcher.start() > currentIndex) {
                append(text.substring(currentIndex, matcher.start()))
            }
            
            // Add highlighted match
            withStyle(
                style = SpanStyle(
                    background = highlightBackgroundColor,
                    color = highlightTextColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(matcher.start(), matcher.end()))
            }
            
            currentIndex = matcher.end()
        }
        
        // Add remaining text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

@Preview(showBackground = true, name = "Compact JSON Viewer")
@Composable
private fun DSSearchableJsonViewerPreview() {
    DSJarvisTheme {
        DSSearchableJsonViewer(
            jsonContent = """{"name": "John Doe", "age": 30, "city": "New York", "skills": ["JavaScript", "React", "Node.js"]}""",
            onSearchRequested = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "JSON Viewer Dialog")
@Composable
private fun DSSearchableJsonViewerDialogPreview() {
    DSJarvisTheme {
        DSSearchableJsonViewerDialog(
            jsonContent = """{"name": "John Doe", "age": 30, "city": "New York", "skills": ["JavaScript", "React", "Node.js"], "address": {"street": "123 Main St", "zipCode": "10001"}}""",
            title = "Response Body",
            onDismiss = { }
        )
    }
}