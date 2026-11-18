package com.w_via.features.receipt.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.w_via.features.receipt.Recibo
import com.w_via.features.utils.PhoneFormatter
import com.w_via.features.utils.valorPorExtenso
import java.util.Locale

@Composable
fun ReciboPDFLayout(recibo: Recibo) {
    val modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(30.dp, 2.dp, 30.dp, 30.dp)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White)
            ) {
                if (recibo.logoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(recibo.logoUri)
                            .allowHardware(false)
                            .build(),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Inside
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Recibo",
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "N° ${recibo.numero.ifBlank { "0001" }}",
                    color = Color.Red,
                    fontSize = 14.sp,
                )
            }
        }

        // Linha com endereço (até 2 linhas) e CNPJ centralizados horizontalmente
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val enderecoAgente = recibo.endereco_agente.ifBlank { "Recibo de Pagamento" }
                val fontSizeEndereco = when {
                    enderecoAgente.length <= 120 -> 12f
                    enderecoAgente.length <= 160 -> 10f
                    else -> 9f
                }
                Text(
                    text = enderecoAgente,
                    fontSize = fontSizeEndereco.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)
                )
                TextSimple(
                    text = recibo.cnpj_agente.ifBlank { "N CNPJ" },
                    fontSize = 12,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Primeiro Grid - 7 linhas
        GridPrimeiroPDF(
            nomeContratante = recibo.nomeContratante,
            rg_ie = recibo.rg_ie,
            cpf_cnpj = recibo.cpf_cnpj,
            endereco_contratante = recibo.endereco_contratante,
            tel = recibo.tel,
            nome_falecido = recibo.nome_falecido,
            estado_civil_falecido = recibo.estado_civil_falecido,
            idade_falecido = recibo.idade_falecido,
            sexo_falecido = recibo.sexo_falecido,
            naturalidade_falecido = recibo.naturalidade_falecido,
            uf_falecido = recibo.uf_falecido,
            rg_falecido = recibo.rg_falecido,
            cpf_falecido = recibo.cpf_falecido,
            filiacao_falecido = recibo.filiacao_falecido,
            pai_falecido = recibo.pai_falecido,
            mae_falecido = recibo.mae_falecido,
            local_funeral = recibo.local_funeral,
            hora_saida_funeral = recibo.hora_saida_funeral,
            dia_funeral = recibo.dia_funeral,
            cemiterio_sepultamento = recibo.cemiterio_sepultamento
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Segundo Grid - 9 linhas
        GridSegundoPDF(
            quantidade = recibo.quantidade,
            descricao = recibo.descricao,
            unidade = recibo.unidade,
            valor = recibo.valor,
        )

        CampoValorTotalPDF(
            total_pago = recibo.total_pago
        )

        CampoValorPorExtensoPDF(
            valor_por_extenso = recibo.valor_por_extenso.ifBlank {
                valorPorExtenso(recibo.total_pago.toDoubleOrNull() ?: 0.0)
            }
        )

        // Terceiro Grid - 1 linha
        GridTerceiroPDF(
            total_pago = recibo.total_pago,
            ass_contratante = recibo.ass_contratante1,
            ass_contratante2 = recibo.ass_contratante2,
            ass_contratante3 = recibo.ass_contratante3,
            n_cheque = recibo.n_cheque,
            valor_cheque = recibo.valor_cheque,
            banco = recibo.banco,
            agencia = recibo.agencia,
            rg_contratante = recibo.rg_contratante,
            emitente_cheque = recibo.emitente_cheque,
            data_pagamento = recibo.data_pagamento,
            identificadorUnico = recibo.uuid
        )
    }
}

@Composable
fun TextSimple(
    text: String = "Texto",
    fontSize: Int = 9,
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    Text(
        text,
        textAlign = textAlign,
        fontSize = fontSize.sp,
        lineHeight = 6.sp,
        modifier = modifier
    )
}

@Composable
fun CampoTextoVertical(
    label: String = "Texto",
    conteudo: String = "Texto2",
    modifier: Modifier = Modifier,
    conteudoMaxLines: Int = Int.MAX_VALUE,
    conteudoAutoReduzir: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, Color.Black)
            .padding(vertical = 1.dp)
            .padding(start = 2.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextSimple(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 8
        )
        Spacer(modifier = Modifier.height(2.dp))
        val fontSize = if (conteudoAutoReduzir) {
            fontConteudo(conteudo, conteudoMaxLines)
        } else {
            10
        }
        Text(
            text = conteudo,
            modifier = Modifier.padding(start = 4.dp),
            fontSize = fontSize.sp,
            lineHeight = fontSize.sp,
            maxLines = conteudoMaxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Reduz o tamanho da fonte do conteúdo conforme o texto e o limite de linhas,
 * mantendo o texto legível e dentro da célula (sem estourar o grid).
 */
private fun fontConteudo(texto: String, maxLines: Int): Int {
    val linhas = maxOf(1, maxLines)
    return when {
        texto.length <= 45 -> 10
        texto.length <= 60 * linhas -> 8
        else -> 7
    }
}

@Composable
fun CampoValorTotalPDF(
    total_pago: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.weight(1.5f))
        Row(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "VALOR TOTAL R$",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(34.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatarMoeda(total_pago),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CampoValorPorExtensoPDF(
    valor_por_extenso: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color.Black)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "VALOR POR EXTENSO: ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 4.dp)
        )
        val fontSizePorExtenso = when {
            valor_por_extenso.length <= 70 -> 11
            valor_por_extenso.length <= 140 -> 10
            else -> 9
        }
        Text(
            text = valor_por_extenso.uppercase(),
            fontSize = fontSizePorExtenso.sp,
            lineHeight = fontSizePorExtenso.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GridPrimeiroPDF(
    nomeContratante: String,
    rg_ie: String,
    cpf_cnpj: String,
    endereco_contratante: String,
    tel: String,
    nome_falecido: String,
    estado_civil_falecido: String,
    idade_falecido: String,
    sexo_falecido: String,
    naturalidade_falecido: String,
    uf_falecido: String,
    rg_falecido: String,
    cpf_falecido: String,
    filiacao_falecido: String,
    pai_falecido: String,
    mae_falecido: String,
    local_funeral: String,
    hora_saida_funeral: String,
    dia_funeral: String,
    cemiterio_sepultamento: String
) {
    val cornerBorder = 8.dp
    val spacing = 26.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Linha 1: 3 partes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .clip(RoundedCornerShape(topStart = cornerBorder, topEnd = cornerBorder))
                .border(
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(topStart = cornerBorder, topEnd = cornerBorder)
                ),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            CampoTextoVertical(
                label = "Nome do contratante",
                conteudo = nomeContratante,
                modifier = Modifier.weight(2f),
                conteudoMaxLines = 2,
                conteudoAutoReduzir = true
            )
            CampoTextoVertical(
                label = "RG/IE",
                conteudo = rg_ie,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "CPF/CNPJ",
                conteudo = cpf_cnpj,
                modifier = Modifier.weight(1f)
            )
        }

        // Linha 2: 2 partes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .border(1.dp, Color.Black),
            horizontalArrangement = Arrangement.spacedBy(0.5.dp)
        ) {
            CampoTextoVertical(
                label = "Endereço do Contratante",
                conteudo = endereco_contratante,
                modifier = Modifier.weight(3f),
                conteudoMaxLines = 2,
                conteudoAutoReduzir = true
            )
            CampoTextoVertical(
                label = "Tel",
                conteudo = PhoneFormatter.format(tel),
                modifier = Modifier.weight(1f)
            )
        }

        // Linha 3: 4 partes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .border(1.dp, Color.Black),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CampoTextoVertical(
                label = "Nome do falecido",
                conteudo = nome_falecido,
                modifier = Modifier.weight(3f),
                conteudoMaxLines = 2,
                conteudoAutoReduzir = true
            )
            CampoTextoVertical(
                label = "Estado Civil",
                conteudo = estado_civil_falecido,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "Idade",
                conteudo = idade_falecido,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "Sexo",
                conteudo = sexo_falecido,
                modifier = Modifier.weight(1f)
            )
        }

        // Linha 4: 4 partes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .border(1.dp, Color.Black),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CampoTextoVertical(
                label = "Natural de",
                conteudo = naturalidade_falecido,
                modifier = Modifier.weight(1.7f)
            )
            CampoTextoVertical(
                label = "Estado",
                conteudo = uf_falecido,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "RG",
                conteudo = rg_falecido,
                modifier = Modifier.weight(1.2f)
            )
            CampoTextoVertical(
                label = "CPF",
                conteudo = cpf_falecido,
                modifier = Modifier.weight(1.2f)
            )
        }

        // Linha 5: Filiação
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .border(1.dp, Color.Black),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filiacao = listOfNotNull(
                pai_falecido.takeIf { it.isNotBlank() },
                mae_falecido.takeIf { it.isNotBlank() }
            ).joinToString(", ")
            CampoTextoVertical(
                label = "Filiação",
                conteudo = filiacao.ifBlank { filiacao_falecido },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Linha 6: Local de Saída do Funeral
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .border(1.dp, Color.Black),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CampoTextoVertical(
                label = "Local de Saída do Funeral",
                conteudo = local_funeral,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Linha 7: 3 partes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .clip(RoundedCornerShape(bottomStart = cornerBorder, bottomEnd = cornerBorder))
                .border(
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(bottomStart = cornerBorder, bottomEnd = cornerBorder)
                ),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CampoTextoVertical(
                label = "Hora de Saída",
                conteudo = hora_saida_funeral,
                modifier = Modifier.weight(2f)
            )
            CampoTextoVertical(
                label = "Dia",
                conteudo = dia_funeral,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "Sepultamento será no cemitério",
                conteudo = cemiterio_sepultamento,
                modifier = Modifier.weight(3f)
            )
        }
    }
}

@Composable
fun GridSegundoPDF(
    quantidade: String,
    descricao: String,
    unidade: String,
    valor: String,
) {
    val cornerBorder = 8.dp
    val spacing = 24.dp

    // Descrição extensa: reduz a fonte para caber em até 2 linhas; se ainda
    // exceder, ocupa as linhas de descrição seguintes (sem divisórias internas).
    // Descrições médias não ficam com espaço sobrando (ocupam só o necessário).
    val descLen = descricao.length
    val charsPorLinha = 58
    val (descricaoFontSize, linhasDescricao) = if (descLen <= 45) {
        10f to 1
    } else {
        8f to minOf(6, maxOf(1, (descLen + charsPorLinha - 1) / charsPorLinha))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Linha título: apenas rótulos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .clip(RoundedCornerShape(topStart = cornerBorder, topEnd = cornerBorder))
                .border(
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(topStart = cornerBorder, topEnd = cornerBorder)
                ),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            TextoGridCabecalho("Quantidade", Modifier.weight(1f))
            TextoGridCabecalho("Descrição", Modifier.weight(3.5f))
            TextoGridCabecalho("Unidade", Modifier.weight(1f))
            TextoGridCabecalho("Valor", Modifier.weight(1f))
        }

        // Linha de dados: quantidade, descrição (ocupa as linhas necessárias), unidade e valor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing * linhasDescricao)
                .border(1.dp, Color.Black),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ColunaCelula(
                texto = quantidade,
                modifier = Modifier.weight(1f)
            )
            Column(
                modifier = Modifier
                    .weight(3.5f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Black)
                    .padding(vertical = 1.dp)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = descricao,
                    fontSize = descricaoFontSize.sp,
                    lineHeight = (descricaoFontSize * 1.2f).sp,
                    maxLines = linhasDescricao,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            ColunaCelula(
                texto = unidade,
                modifier = Modifier.weight(1f)
            )
            ColunaCelula(
                texto = formatarMoeda(valor),
                modifier = Modifier.weight(1f)
            )
        }

        // Linhas vazias fixas (a descrição ocupa o espaço das que precisar)
        RowDescription(text1 = "", text2 = "", text3 = "", text4 = "")
        RowDescription(text1 = "", text2 = "", text3 = "", text4 = "")
        RowDescription(text1 = "", text2 = "", text3 = "", text4 = "")
        RowDescription(text1 = "", text2 = "", text3 = "", text4 = "")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .clip(RoundedCornerShape(bottomStart = cornerBorder, bottomEnd = cornerBorder))
                .border(
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(bottomStart = cornerBorder, bottomEnd = cornerBorder)
                ),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Black)
                    .padding(vertical = 1.dp)
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextSimple(
                    text = "",
                    modifier = Modifier.padding(start = 2.dp),
                    fontSize = 10
                )
            }

            Column(
                modifier = Modifier
                    .weight(3.5f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Black)
                    .padding(vertical = 1.dp)
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextSimple(
                    text = "",
                    modifier = Modifier.padding(start = 2.dp),
                    fontSize = 10
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Black)
                    .padding(vertical = 1.dp)
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextSimple(
                    text = "",
                    modifier = Modifier.padding(start = 2.dp),
                    fontSize = 10
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, Color.Black)
                    .padding(vertical = 1.dp)
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextSimple(
                    text = "",
                    modifier = Modifier.padding(start = 2.dp),
                    fontSize = 10
                )
            }
        }
    }
}

/** Célula de cabeçalho do grid de itens (apenas o rótulo, centralizado). */
@Composable
private fun TextoGridCabecalho(texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, Color.Black)
            .padding(vertical = 1.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextSimple(text = texto, fontSize = 8, textAlign = TextAlign.Center)
    }
}

/** Célula de valor do grid de itens, com o texto centralizado. */
@Composable
private fun ColunaCelula(texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, Color.Black)
            .padding(vertical = 1.dp)
            .padding(start = 2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextSimple(
            text = texto,
            modifier = Modifier.padding(start = 2.dp),
            fontSize = 10,
            textAlign = TextAlign.Center
        )
    }
}

/** Formata um valor monetário em pt-BR (ex.: "1234.5" -> "1.234,50"). */
private fun formatarMoeda(texto: String): String {
    val valor = texto.toDoubleOrNull() ?: return texto
    return String.format(Locale("pt", "BR"), "%,.2f", valor)
}


@Composable
fun GridTerceiroPDF(
    total_pago: String,
    ass_contratante: String,
    ass_contratante2: String,
    ass_contratante3: String,
    n_cheque: String,
    valor_cheque: String,
    banco: String,
    agencia: String,
    rg_contratante: String,
    emitente_cheque: String,
    data_pagamento: String,
    identificadorUnico: String
) {
    val spacing = 30.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Linha 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .border(1.dp, Color.Black),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CampoTextoVertical(
                label = "Assinatura do Contratante",
                conteudo = ass_contratante,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "Assinatura do Contratante",
                conteudo = ass_contratante2,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "Assinatura do Contratante",
                conteudo = ass_contratante3,
                modifier = Modifier.weight(1f)
            )
        }

        // Linha 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing)
                .border(1.dp, Color.Black),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CampoTextoVertical(
                label = "Cheque N",
                conteudo = n_cheque,
                modifier = Modifier.weight(1.2f)
            )
            CampoTextoVertical(
                label = "Valor",
                conteudo = valor_cheque.takeIf {
                    it.trim().isNotEmpty() && it.toDoubleOrNull() != 0.0
                } ?: "",
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "Banco",
                conteudo = banco,
                modifier = Modifier.weight(0.7f)
            )
            CampoTextoVertical(
                label = "Agência",
                conteudo = agencia,
                modifier = Modifier.weight(0.6f)
            )
            CampoTextoVertical(
                label = "RG",
                conteudo = rg_contratante,
                modifier = Modifier.weight(2f)
            )
            CampoTextoVertical(
                label = "Emitente",
                conteudo = emitente_cheque,
                modifier = Modifier.weight(1f)
            )
            CampoTextoVertical(
                label = "Fone",
                conteudo = "",
                modifier = Modifier.weight(1.5f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .border(1.dp, Color.Black),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Data:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = data_pagamento,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = "Id:${identificadorUnico}",
                    maxLines = 2,
                    color = Color.Gray,
                    fontSize = 10.sp,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                )

                Text(
                    text = "Assinatura do responsável",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RowDescription(text1: String, text2: String, text3: String, text4: String) {
    val spacing = 24.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing)
            .border(1.dp, Color.Black),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, Color.Black)
                .padding(vertical = 1.dp)
                .padding(start = 2.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextSimple(
                text = text1,
                modifier = Modifier.padding(start = 2.dp),
                fontSize = 10
            )
        }

        Column(
            modifier = Modifier
                .weight(3.5f)
                .fillMaxHeight()
                .border(1.dp, Color.Black)
                .padding(vertical = 1.dp)
                .padding(start = 2.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextSimple(
                text = text2,
                modifier = Modifier.padding(start = 2.dp),
                fontSize = 10
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, Color.Black)
                .padding(vertical = 1.dp)
                .padding(start = 2.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextSimple(
                text = text3,
                modifier = Modifier.padding(start = 2.dp),
                fontSize = 10
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, Color.Black)
                .padding(vertical = 1.dp)
                .padding(start = 2.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextSimple(
                text = text4,
                modifier = Modifier.padding(start = 2.dp),
                fontSize = 10
            )
        }
    }
}

@Preview
@Composable
fun ReciboPDFLayoutPreview() {
    ReciboPDFLayout(
        recibo = Recibo()
    )
}
