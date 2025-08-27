package com.jarvis.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    placeholder: String = stringResource(R.string.core_design_system_search_placeholder),
    onTextClean: () -> Unit
) {
    // ✅ PERFORMANCE: Memoize icons and callbacks
    val leadingIcon = remember {
        @Composable {
            DSIcon(
                modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.xxs),
                imageVector = Icons.Default.Search,
                contentDescription = "",
                tint = Gray
            )
        }
    }
    
    val trailingIcon = remember(searchText.isNotEmpty()) {
        if (searchText.isNotEmpty()) {
            @Composable {
                DSIcon(
                    modifier = Modifier.clickable { onTextClean() },
                    imageVector = Icons.Default.Close,
                    contentDescription = "",
                    tint = Gray
                )
            }
        } else null
    }

    Box {
        DSTextField(
            modifier = modifier,
            text = searchText,
            onValueChange = onValueChange,
            singleLine = true,
            leadingIcon = leadingIcon,
            placeholder = placeholder,
            trailingIcon = trailingIcon
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