package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface

@Composable
fun AddStationDialog(
    onAddStation: (name: String, streamUrl: String, frequency: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var streamUrl by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Pop") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Radio,
                    contentDescription = null,
                    tint = CrimsonPrimary
                )
                Text(
                    text = "Özel Radyo Ekle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kendi yerel veya internet radyo akış URL'inizi (MP3/AAC/M3U8) ekleyin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Radyo Adı (Örn: Kent FM)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_station_name")
                )

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it; errorMessage = null },
                    label = { Text("Yayın Bağlantısı (URL)") },
                    placeholder = { Text("http://.../stream.mp3", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = ObsidianCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_stream_url")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frekans") },
                        placeholder = { Text("98.0 FM", color = Color(0xFF64748B)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_frequency")
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = ObsidianCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_category")
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Lütfen radyo adını girin."
                    } else if (streamUrl.isBlank() || (!streamUrl.startsWith("http://") && !streamUrl.startsWith("https://"))) {
                        errorMessage = "Geçerli bir yayın URL'si (http:// veya https://) girin."
                    } else {
                        onAddStation(name, streamUrl, frequency, category)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("button_confirm_add_station")
            ) {
                Text("Ekle ve Çal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("button_cancel_add_station")
            ) {
                Text("Vazgeç", color = Color(0xFF94A3B8))
            }
        }
    )
}
