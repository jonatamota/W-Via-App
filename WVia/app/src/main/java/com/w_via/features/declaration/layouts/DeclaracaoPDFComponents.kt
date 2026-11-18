package com.w_via.features.declaration.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Campo do layout da declaração: rótulo, valor e peso (largura proporcional). */
data class PdfFieldSpec(
    val label: String,
    val value: String,
    val weight: Float
)

/** Cria um campo para ser passado ao [RowPDF]. */
fun field(label: String, value: String, weight: Float): PdfFieldSpec =
    PdfFieldSpec(label = label, value = value, weight = weight)

/**
 * Título de seção do documento, centralizado, entre linhas cheias e sem numeração.
 */
@Composable
fun SectionPDF(title: String, content: @Composable () -> Unit) {
    HorizontalDivider(thickness = 1.dp, color = Color.Black)
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 1.dp)
    )
    HorizontalDivider(thickness = 1.dp, color = Color.Black)
    content()
}

/**
 * Linha de campos sem bordas, com linha de base cinza claro.
 *
 * Os valores são exibidos em caixa alta (os rótulos mantêm o texto digitado).
 *
 * Quando todos os textos couberem em uma única linha, cada campo é renderizado
 * como "Rótulo: VALOR" na mesma linha. Se algum campo quebrar (não couber em uma
 * linha), o campo longo continua "Rótulo: VALOR" com quebra suave, e os demais
 * campos (mesmo os de valores curtos) passam a usar a disposição em coluna
 * (rótulo em cima, valor embaixo). Os pesos das colunas são preservados.
 */
@Composable
fun RowPDF(vararg fields: PdfFieldSpec) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 9.sp)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val spacingPx = with(density) { 4.dp.toPx() }
        val totalWeight = fields.fold(0f) { acc, spec -> acc + spec.weight }

        val cellHorizontalPaddingPx = with(density) { 8.dp.toPx() }
        val fitsList = fields.map { spec ->
            val cellWidth =
                (maxWidthPx - spacingPx * (fields.size - 1)) * spec.weight / totalWeight -
                    cellHorizontalPaddingPx
            textMeasurer.measure(
                text = "${spec.label}: ${spec.value.ifBlank { " " }.uppercase()}",
                style = textStyle,
                constraints = Constraints(maxWidth = cellWidth.toInt().coerceAtLeast(1))
            ).lineCount == 1
        }
        val hasWrapped = fitsList.any { !it }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                fields.forEachIndexed { index, spec ->
                    if (hasWrapped && fitsList[index]) {
                        Column(
                            modifier = Modifier
                                .weight(spec.weight)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = spec.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = spec.value.ifBlank { " " }.uppercase(),
                                fontSize = 9.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(spec.weight)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                                        append(spec.label)
                                        append(": ")
                                    }
                                    append(spec.value.ifBlank { " " }.uppercase())
                                },
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = LineColor)
        }
    }
}

/**
 * Duas colunas lado a lado; cada linha de cada coluna é uma sequência de
 * pares "Rótulo: VALOR" (ex.: Pai e Mãe na filiação). Os valores são
 * exibidos em caixa alta e os rótulos em Medium, como no [RowPDF].
 */
@Composable
fun TwoColumnPDF(
    leftLines: List<List<Pair<String, String>>>,
    rightLines: List<List<Pair<String, String>>>
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            leftLines.forEach { line ->
                Text(text = line.buildFieldLine(), fontSize = 9.sp)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            rightLines.forEach { line ->
                Text(text = line.buildFieldLine(), fontSize = 9.sp)
            }
        }
    }
    HorizontalDivider(thickness = 1.dp, color = LineColor)
}

private fun List<Pair<String, String>>.buildFieldLine(): AnnotatedString =
    buildAnnotatedString {
        forEachIndexed { index, (label, value) ->
            if (index > 0) append("  ")
            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                append(label)
                append(": ")
            }
            append(value.ifBlank { " " }.uppercase())
        }
    }

private val LineColor = Color(0xFFD6D6D6)
