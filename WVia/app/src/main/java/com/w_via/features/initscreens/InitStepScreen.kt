package com.w_via.features.initscreens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.w_via.designsystem.appbuttons.PrimaryButton
import com.w_via.features.authentication.AuthSessionStore
import com.w_via.ui.theme.colorBrand
import com.w_via.features.routes.Routes
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// Adicione estas imagens na pasta res/drawable ou res/mipmap
// R.drawable.ic_welcome, R.drawable.ic_budget, R.drawable.ic_documents

@Composable
fun InitStepScreen(
    navController: NavHostController? = null
) {
    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4
    val scope = rememberCoroutineScope()
    val authSessionStore: AuthSessionStore = koinInject()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Divisão de cor NA TELA: metade de cima colorBrand, metade de baixo tema
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(colorBrand)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Indicador de Steps (Progresso)
        StepIndicator(
            currentStep = currentStep,
            totalSteps = totalSteps
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Conteúdo do Step atual com animação
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { if (targetState > initialState) 200 else -200 }
                ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { if (targetState > initialState) -200 else 200 }
                        ) + fadeOut()
            }
        ) { step ->
            when (step) {
                0 -> StepContent(
                    number = "1",
                    title = "Bem-vindo(a)!",
                    description = "O primeiro aplicativo para Agentes Funerários Autônomos",
                    detail = "Feito para agilizar seus serviços",
                    imageRes = com.w_via.R.drawable.img_agent
                )

                1 -> StepContent(
                    number = "2",
                    title = "Serviços e orçamentos",
                    description = "Faça orçamentos de viagens com facilidade",
                    detail = "Veja quanto gastará com pedágios, custo por km, combustível e outros",
                    imageRes = com.w_via.R.drawable.img_car_fun
                )

                2 -> StepContent(
                    number = "3",
                    title = "Emita seus recibos",
                    description = "Gere, salve e envie Recibos",
                    detail = "Recibos para funerárias direto no app em PDF",
                    imageRes = com.w_via.R.drawable.img_recibo
                )
                3 -> StepContent(
                    number = "3",
                    title = "Emita documentos",
                    description = "Gere e emita Declarações",
                    detail = "Declarações administrativas de óbito direto no app em PDF",
                    imageRes = com.w_via.R.drawable.img_declaration
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botões de navegação
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botão Voltar
            if (currentStep > 0) {
                PrimaryButton(
                    modifier = Modifier.weight(1f),
                    onClick = { currentStep-- },
                    text = "Voltar",
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.width(16.dp))

            PrimaryButton(
                modifier = Modifier
    
                    .weight(1f),
                onClick = {
                    if (currentStep < totalSteps - 1) {
                        currentStep++
                    } else {
                        scope.launch {
                            authSessionStore.setOnboardingCompleted(true)
                            navController?.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                text = if (currentStep == totalSteps - 1) "Concluir" else "Avançar",
            )
        }
    }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (index <= currentStep)
                            Color.White
                        else
                            Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

@Composable
fun StepContent(
    number: String,
    title: String,
    description: String,
    detail: String,
    imageRes: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(2.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column {
                // Box colorBrand que termina junto com a base da imagem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(10f / 13f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Texto
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(8f / 4.5f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = description,
                        fontSize = 18.sp,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = detail,
                        fontSize = 15.sp,
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


// Preview
@Preview(
    name = "Step Screen",
    showBackground = true,
    widthDp = 400,
    heightDp = 800
)
@Composable
fun PreviewStepScreen() {
    MaterialTheme {
        InitStepScreen()
    }
}