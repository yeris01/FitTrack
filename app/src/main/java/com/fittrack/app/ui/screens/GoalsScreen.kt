package com.fittrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittrack.app.ui.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalViewModel) {
    val progress by viewModel.weekProgress.collectAsState()
    val purpleThemeColor = Color(0xFF6B4DFF)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            buildAnnotatedString {
                                append("Objetivo ")
                                withStyle(style = SpanStyle(color = purpleThemeColor)) { append("semanal") }
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                        Text("Pequeños hábitos, grandes resultados", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Icon(
                        Icons.Default.TrackChanges, 
                        contentDescription = null, 
                        tint = purpleThemeColor, 
                        modifier = Modifier.padding(end = 16.dp).size(40.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TARJETA DE SELECCIÓN ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = purpleThemeColor.copy(alpha = 0.1f)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = purpleThemeColor, modifier = Modifier.padding(10.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.width(2.dp).height(30.dp).background(purpleThemeColor))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "¿Cuántos días por semana quieres entrenar?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(2, 3, 4, 5, 6).forEach { n ->
                            val isSelected = progress.target == n
                            DaySelectorItem(n, isSelected, purpleThemeColor) {
                                viewModel.setTarget(n)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- ILUSTRACIÓN CENTRAL ---
            Icon(
                Icons.Default.FitnessCenter, 
                contentDescription = null, 
                tint = Color(0xFF2D3142), 
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Tu constancia",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF2D3142)
            )
            Text(
                text = "hace la diferencia",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = purpleThemeColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Elige la cantidad de días que quieres entrenar por semana y da el primer paso hacia tus objetivos.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun DaySelectorItem(number: Int, isSelected: Boolean, themeColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isSelected) themeColor else Color.White)
                .border(1.dp, if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.5f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Días", fontSize = 11.sp, color = if (isSelected) themeColor else Color.Gray)
    }
}
