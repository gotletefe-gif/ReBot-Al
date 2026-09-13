package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface

@Composable
fun StationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchedCount: Int,
    modifier: Modifier = Modifier,
    placeholderText: String = "İstasyon adı veya frekans ara... (Örn: Kral, TRT, Power)"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("station_search_bar")
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = placeholderText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Ara",
                    tint = if (query.isNotEmpty()) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CrimsonPrimary.copy(alpha = 0.18f),
                            border = BorderStroke(0.5.dp, CrimsonPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "$matchedCount sonuç",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CrimsonPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Aramayı Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrimsonPrimary,
                unfocusedBorderColor = if (query.isNotEmpty()) CrimsonPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = CrimsonPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_field")
        )
    }
}
