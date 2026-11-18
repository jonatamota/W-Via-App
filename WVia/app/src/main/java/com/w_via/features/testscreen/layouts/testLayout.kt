package com.w_via.features.testscreen.layouts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import com.w_via.R

/* ---------- Modelo ---------- */

data class LinhaPrescricao(val olho: String)

/** Altura fixa das linhas da grade (tabela LONGE/PERTO). */
private val ALTURA_LINHA_GRID = 24.dp

/** Altura fixa das linhas de cabeçalho da grade. */
private val ALTURA_CABECALHO_GRID = 18.dp

/* ---------- Preview (A5: 420 x 730 dp) ---------- */

@Preview(showBackground = true, widthDp = 420, heightDp = 690)
@Composable
private fun ReceituarioScreenPreview() {
    MaterialTheme {
        ReceituarioScreen()
    }
}

/* ---------- Tela ---------- */

@Composable
fun ReceituarioScreen() {
    Column(
        modifier = Modifier
            .width(420.dp)
            .height(690.dp)
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Cabecalho()
        Spacer(Modifier.height(8.dp))
        CampoSublinhado(rotulo = "Para Sr.(a):", valor = "")

        Spacer(Modifier.height(6.dp))
        DiagramasComTitulo()

        Spacer(Modifier.height(4.dp))
        TabelaPrescricao(
            titulo = "LONGE",
            linhas = listOf(LinhaPrescricao("Olho\nDireito"), LinhaPrescricao("Olho\nEsquerdo"))
        )
        Spacer(Modifier.height(4.dp))
        TabelaPrescricao(
            titulo = "PERTO",
            linhas = listOf(LinhaPrescricao("Olho\nDireito"), LinhaPrescricao("Olho\nEsquerdo"))
        )

        Spacer(Modifier.height(8.dp))
        LinhaChecklist(listOf("Miopia", "Hipermetropia", "Astigmatismo", "Presbiopia"))

        Spacer(Modifier.height(8.dp))
        Text(
            "1- É normal nos primeiros dias sentir tontura, cefaléia, náuseas, ver desníveis no chão ou escada.\n" +
                "2- Para Multifocal ou Bifocal a adaptação poderá levar 07 à 15 dias, podendo ter os mesmo sintomas acima.\n" +
                "3- Um exame de vista é sempre oportuno antes de seu filho começar a estudar.",
            fontWeight = FontWeight.Medium,
            fontSize = 8.sp,
            lineHeight = 11.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(8.dp))
        GradeSelecao(
            colunas = listOf(
                "Multifocal" to "Fotossensível",
                "Bifocal" to "A.R.",
                "V.S." to "Incolor"
            )
        )

        Spacer(Modifier.height(10.dp))
        CampoObservacao(rotulo = "Observação:")

        Spacer(Modifier.height(10.dp))
        Text(
            "O presente documento apresenta avaliação técnica fisiológica de conformidade " +
                "com a CBO N°3223/portaria397-09.10.2002 c.c parecer conselho federal da educação n°45/72.\n" +
                "Alerta: Tratamento de patologias oculares procure um Profissional Médico.",
            fontWeight = FontWeight.Medium,
            fontSize = 8.sp,
            lineHeight = 10.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CampoLinhaInferior(rotulo = "Data", valor = "  /          /   ", modifier = Modifier.weight(1f))
            Spacer(Modifier.width(20.dp))
            CampoLinhaInferior(rotulo = "ASS.", valor = "", modifier = Modifier.weight(1.5f))
        }
    }
}

/* ---------- Componentes ---------- */

@Composable
private fun Cabecalho() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        // Substitua R.drawable.img_optometria pelo drawable real do seu módulo (res/drawable).
        Image(
            painter = painterResource(id = R.drawable.img_optometria),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterStart)
        )
        Text(
            "RECEITUÁRIO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun CampoSublinhado(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(rotulo, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Text(valor, fontSize = 12.sp, modifier = Modifier.weight(1f))
        }
        Divider(color = Color.Black, thickness = 1.dp)
    }
}

/** Campo com o rótulo (Data./ASS.) abaixo da linha, valor escrito sobre a linha. */
@Composable
private fun CampoLinhaInferior(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, fontSize = 12.sp)
        Divider(color = Color.Black, thickness = 1.dp)
        Spacer(Modifier.height(2.dp))
        Text(rotulo, fontSize = 11.sp, color = Color.DarkGray)
    }
}

/** Observação: primeira linha logo à frente do rótulo, segunda linha embaixo. */
@Composable
private fun CampoObservacao(rotulo: String) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(rotulo, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.Black)
            )
        }
        Spacer(Modifier.height(20.dp))
        Divider(color = Color.Black, thickness = 1.dp)
    }
}

/** Título "Prescrição de Óculos" sobreposto, quase entre os arcos e um pouco acima deles. */
@Composable
private fun DiagramasComTitulo() {
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DiagramaEixo(rotulo = "OD")
            Spacer(modifier = Modifier.width(50.dp))
            DiagramaEixo(rotulo = "OE")
        }
        Text(
            "Prescrição de Óculos",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * Diagrama do eixo (semicírculo 0–180°) com numeração pequena de 10 em 10,
 * começando em 0 à direita até 180 à esquerda.
 */
@Composable
private fun DiagramaEixo(rotulo: String) {
    val density = LocalDensity.current
    val labelSizePx = with(density) { 6.5.sp.toPx() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(160.dp, 62.dp)) {
            desenharArco(labelSizePx)
        }
        Text(rotulo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

private fun DrawScope.desenharArco(labelSizePx: Float) {
    val cx = size.width / 2
    val cy = size.height - 4f
    val r = size.width / 2 * 0.78f
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = labelSizePx
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    for (deg in 0..180 step 10) {
        val rad = Math.toRadians(deg.toDouble())
        val x = cx - r * cos(rad).toFloat()
        val y = cy - r * sin(rad).toFloat()
        drawLine(Color.Gray, Offset(cx, cy), Offset(x, y), strokeWidth = 1f)

        val labelR = r + labelSizePx * 0.85f
        val lx = cx - labelR * cos(rad).toFloat()
        val ly = cy - labelR * sin(rad).toFloat() + labelSizePx * 0.3f
        drawContext.canvas.nativeCanvas.drawText((180 - deg).toString(), lx, ly, paint)
    }
    drawLine(Color.Black, Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1.5f)
    drawLine(Color.Black, Offset(cx, cy), Offset(cx, cy - r), strokeWidth = 1.5f)
}

/** Tabela impressa: cabeçalho com texto, células de valor apenas com o retângulo (preenchidas à mão). */
@Composable
private fun TabelaPrescricao(titulo: String, linhas: List<LinhaPrescricao>) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF3A3A3A))
        ) {
            CelulaCabecalho(titulo, 1.2f)
            CelulaCabecalho("Esférico", 1f)
            CelulaCabecalho("Cilíndrico", 1f)
            CelulaCabecalho("Eixo", 0.8f)
            CelulaCabecalho("AV", 0.8f)
        }
        linhas.forEach { linha ->
            Row(Modifier.fillMaxWidth()) {
                CelulaRotulo(linha.olho, 1.2f)
                CelulaVazia(1f)
                CelulaVazia(1f)
                CelulaVazia(0.8f)
                CelulaVazia(0.8f)
            }
        }
    }
}

@Composable
private fun RowScope.CelulaCabecalho(texto: String, peso: Float) {
    Box(
        modifier = Modifier
            .weight(peso)
            .height(ALTURA_CABECALHO_GRID)
            .border(0.5.dp, Color.Black)
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = Color.White,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/** Rótulo fixo de linha (ex.: "Olho Direito") — impresso, não preenchido à mão. Mesma altura das células vazias. */
@Composable
private fun RowScope.CelulaRotulo(texto: String, peso: Float) {
    Box(
        modifier = Modifier
            .weight(peso)
            .height(ALTURA_LINHA_GRID)
            .border(0.5.dp, Color.Black)
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/** Célula em branco: apenas o retângulo da grade, sem texto, para preenchimento à mão. Mesma altura do rótulo. */
@Composable
private fun RowScope.CelulaVazia(peso: Float) {
    Box(
        modifier = Modifier
            .weight(peso)
            .height(ALTURA_LINHA_GRID)
            .border(0.5.dp, Color.Black)
    )
}

/**
 * Caixa de seleção customizada (sem alvo de toque mínimo do Material),
 * dimensionada praticamente na altura do texto ao lado.
 */
@Composable
private fun CaixaSelecao(marcado: Boolean, tamanho: Dp) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .border(1.dp, Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (marcado) {
            Box(
                Modifier
                    .fillMaxSize(0.55f)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
private fun LinhaChecklist(itens: List<String>, marcado: Set<String> = emptySet()) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        itens.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                CaixaSelecao(marcado = item in marcado, tamanho = 10.dp)
                Spacer(Modifier.width(4.dp))
                Text(item, fontSize = 12.sp)
            }
        }
    }
}

/**
 * Grade de seleção em colunas, alinhada com SpaceAround: cada coluna tem dois
 * itens empilhados, um imediatamente abaixo do outro. Nenhuma opção pré-marcada
 * (layout impresso, marcação feita à mão).
 */
@Composable
private fun GradeSelecao(colunas: List<Pair<String, String>>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Row(Modifier.weight(0.25f)) {}
        colunas.forEach { (superior, inferior) ->
            Column(Modifier.weight(1f)) {
                ItemSelecao(superior)
                ItemSelecao(inferior)
            }
        }
    }
}

@Composable
private fun ItemSelecao(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CaixaSelecao(marcado = false, tamanho = 11.dp)
        Spacer(Modifier.width(4.dp))
        Text(texto, fontSize = 12.sp)
    }
}
