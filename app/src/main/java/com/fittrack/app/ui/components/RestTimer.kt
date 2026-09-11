package com.fittrack.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RestTimerBar(
    secondsLeft: Int,
    isRunning: Boolean,
    onStart: (Int) -> Unit,
    onCancel: () -> Unit
) {
    val orangeColor = Color(0xFFFF6D00)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isRunning) orangeColor.copy(alpha = 0.1f) else Color.White,
        border = if (isRunning) androidx.compose.foundation.BorderStroke(1.dp, orangeColor) else null,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (isRunning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, tint = orangeColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Descanso: ${secondsLeft}s",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = orangeColor
                        )
                    }
                    TextButton(onClick = onCancel) {
                        Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Barra de progreso sutil (simulada ya que no tenemos el tiempo total aquí fácilmente)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp).clip(CircleShape),
                    color = orangeColor,
                    trackColor = Color.White
                )
            } else {
                Text("¿Cuánto quieres descansar?", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(30, 60, 90).forEach { s ->
                        OutlinedButton(
                            onClick = { onStart(s) },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, orangeColor.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Text("${s}s", color = orangeColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
