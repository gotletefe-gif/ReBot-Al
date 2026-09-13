package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RadioTab
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant

@Composable
fun RadioNavTabBar(
    selectedTab: RadioTab,
    onTabSelected: (RadioTab) -> Unit,
    favoriteCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(4.dp)
            .testTag("radio_navigation_tab_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NavTabItem(
                label = "Tüm Radyolar",
                icon = Icons.Filled.Radio,
                isSelected = selectedTab == RadioTab.STATIONS,
                onClick = { onTabSelected(RadioTab.STATIONS) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_stations")
            )

            NavTabItem(
                label = "Favoriler",
                icon = Icons.Filled.Favorite,
                isSelected = selectedTab == RadioTab.FAVORITES,
                badgeCount = favoriteCount,
                onClick = { onTabSelected(RadioTab.FAVORITES) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_favorites")
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) CrimsonPrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "tab_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "tab_content"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                fontSize = 13.sp
            )

            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Badge(
                    containerColor = if (isSelected) Color.White else CrimsonPrimary,
                    contentColor = if (isSelected) CrimsonPrimary else Color.White,
                    modifier = Modifier.testTag("favorites_badge")
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else "$badgeCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
