import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressStepper(
    currentStep: Int, // 1, 2 ou 3
    modifier: Modifier = Modifier,
    circleSize: Dp = 38.dp,
    strokeWidth: Dp = 3.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    textColor: Color = MaterialTheme.colorScheme.primary,
    showConnectingLine: Boolean = true,
    connectingLineColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    showCompletedFill: Boolean = true,
    completedFillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
) {
    val totalSteps = 3

    Row(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep
            val isFuture = stepNumber > currentStep

            // Animação para o passo ativo
            val pulse by animateFloatAsState(
                targetValue = if (isActive) 1.1f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            // Círculo do passo
            Box(
                modifier = Modifier
                    .size(circleSize * if (isActive) pulse else 1f)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isCompleted && showCompletedFill -> completedFillColor
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        width = strokeWidth,
                        color = when {
                            isActive -> activeColor
                            isCompleted -> activeColor
                            else -> inactiveColor
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Número dentro do círculo
                Text(
                    text = stepNumber.toString(),
                    color = when {
                        isActive -> textColor
                        isCompleted -> textColor // ou completedTextColor se quiser diferente
                        else -> textColor.copy(alpha = 0.5f)
                    },
                    fontSize = 16.sp,
                    fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Medium
                )
            }

            // Linha conectando (exceto após o último círculo)
            if (index < totalSteps - 1 && showConnectingLine) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            color = if (stepNumber < currentStep)
                                activeColor.copy(alpha = 0.5f)
                            else
                                connectingLineColor
                        )
                )
            }
        }
    }
}