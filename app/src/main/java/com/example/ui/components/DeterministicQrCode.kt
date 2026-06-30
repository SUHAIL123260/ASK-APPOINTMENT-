package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun DeterministicQrCode(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    qrColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val gridSize = 21 // 21x21 QR Grid
            val cellSize = size.width / gridSize
            
            // Seed a simple LCG pseudo-random generator with text hash code to draw static deterministic data
            val hash = text.hashCode()
            var state = abs(hash)
            fun nextInt(): Int {
                state = (state * 1103515245 + 12345) and 0x7fffffff
                return state
            }

            // Draw Background
            drawRect(color = backgroundColor, size = size)

            // Draw QR modules
            for (row in 0 until gridSize) {
                for (col in 0 until gridSize) {
                    // Skip finder pattern zones (Top-left, Top-right, Bottom-left 7x7 squares)
                    val isTopLeftFinder = row < 7 && col < 7
                    val isTopRightFinder = row < 7 && col >= gridSize - 7
                    val isBottomLeftFinder = row >= gridSize - 7 && col < 7
                    
                    if (isTopLeftFinder || isTopRightFinder || isBottomLeftFinder) {
                        continue
                    }

                    // Draw random data cells
                    val randValue = nextInt() % 100
                    if (randValue < 40) { // ~40% density of dark modules
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f) // overlapping fraction to prevent grid lines
                        )
                    }
                }
            }

            // Function to draw a 7x7 Finder Pattern
            fun drawFinderPattern(startCol: Int, startRow: Int) {
                val ox = startCol * cellSize
                val oy = startRow * cellSize
                
                // Outer Black 7x7
                drawRect(
                    color = qrColor,
                    topLeft = Offset(ox, oy),
                    size = Size(cellSize * 7, cellSize * 7)
                )
                // Inner White 5x5
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset(ox + cellSize, oy + cellSize),
                    size = Size(cellSize * 5, cellSize * 5)
                )
                // Center Black 3x3
                drawRect(
                    color = qrColor,
                    topLeft = Offset(ox + cellSize * 2, oy + cellSize * 2),
                    size = Size(cellSize * 3, cellSize * 3)
                )
            }

            // Draw the 3 standard Finder Patterns
            drawFinderPattern(0, 0) // Top-Left
            drawFinderPattern(gridSize - 7, 0) // Top-Right
            drawFinderPattern(0, gridSize - 7) // Bottom-Left
        }
    }
}
