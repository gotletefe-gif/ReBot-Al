package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.LiveGreen

@Composable
fun ThemeSelectionDialog(
    currentMode: AppThemeMode,
    dynamicColor: Boolean,
    onSelectMode: (AppThemeMode) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CrimsonPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Tema ve Görünüm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sistem modu ve dinamik renk ayarları",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: Automatic System Default
                ThemeModeItem(
                    title = "Sistem Teması (Otomatik)",
                    subtitle = "Cihazın açık/koyu mod ayarını anında algılar",
                    activeHint = if (isSystemDark) "Şu an: Koyu Mod aktif" else "Şu an: Açık Mod aktif",
                    icon = Icons.Filled.BrightnessAuto,
                    isSelected = currentMode == AppThemeMode.SYSTEM,
                    onClick = { onSelectMode(AppThemeMode.SYSTEM) },
                    testTag = "theme_option_system"
                )

                // Option 2: Light Mode
                ThemeModeItem(
                    title = "Açık Mod",
                    subtitle = "Ferah, yüksek kontrastlı aydınlık görünüm",
                    icon = Icons.Filled.LightMode,
                    isSelected = currentMode == AppThemeMode.LIGHT,
                    onClick = { onSelectMode(AppThemeMode.LIGHT) },
                    testTag = "theme_option_light"
                )

                // Option 3: Dark Mode
                ThemeModeItem(
                    title = "Koyu Mod",
                    subtitle = "Göz yormayan şık obsidian gece teması",
                    icon = Icons.Filled.DarkMode,
                    isSelected = currentMode == AppThemeMode.DARK,
                    onClick = { onSelectMode(AppThemeMode.DARK) },
                    testTag = "theme_option_dark"
                )

                // Optional Material You Dynamic Colors (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onToggleDynamicColor(!dynamicColor) }
                            .testTag("dynamic_color_toggle_row")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ColorLens,
                                    contentDescription = null,
                                    tint = if (dynamicColor) CrimsonPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Dinamik Renkler (Material You)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Duvar kağıdınızdan tonları otomatik türetir",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = dynamicColor,
                                onCheckedChange = onToggleDynamicColor,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CrimsonPrimary
                                ),
                                modifier = Modifier.testTag("dynamic_color_switch")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("theme_dialog_confirm_button")
            ) {
                Text("Tamam", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.testTag("theme_selection_dialog")
    )
}

@Composable
private fun ThemeModeItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    activeHint: String? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        label = "theme_item_border"
    )
    val containerBg by animateColorAsState(
        targetValue = if (isSelected) CrimsonPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        label = "theme_item_bg"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerBg,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CrimsonPrimary
                            ) {
                                Text(
                                    text = "SEÇİLİ",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (activeHint != null && isSelected) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(LiveGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeHint,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LiveGreen
                            )
                        }
                    }
                }
            }

            // Radio check circle indicator
            Surface(
                shape = CircleShape,
                color = if (isSelected) CrimsonPrimary else Color.Transparent,
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) CrimsonPrimary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(22.dp)
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Seçildi",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
