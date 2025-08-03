package com.jarvis.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun DSSearchBar(
    modifier: Modifier = Modifier,
    searchText: String,
    onValueChange: (String) -> Unit,
    onTextClean: () -> Unit
) {
    Box {
        DSTextField(
            modifier = modifier,
            text = searchText,
            onValueChange = { onValueChange(it) },
            singleLine = true,
            leadingIcon = {
                DSIcon(
                    modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.xxs),
                    imageVector = Icons.Default.Search,
                    contentDescription = "",
                    tint = Gray
                )
            },
            placeholder = stringResource(R.string.core_design_system_search_placeholder),
            trailingIcon = {
                DSIcon(
                    modifier = Modifier.clickable { onTextClean() },
                    imageVector = Icons.Default.Close,
                    contentDescription = "",
                    tint = Gray
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DSSearchBarPreview() {
    DSJarvisTheme {
        DSSearchBar(
            searchText = "",
            onValueChange = {},
            onTextClean = {}
        )
    }
}