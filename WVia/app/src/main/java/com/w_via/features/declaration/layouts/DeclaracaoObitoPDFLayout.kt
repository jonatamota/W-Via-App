package com.w_via.features.declaration.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w_via.features.company.data.EmpresaEntity
import com.w_via.features.datamycnpj.LogoStorage
import com.w_via.features.declaration.CasamentoCartorioInfo
import com.w_via.features.declaration.DeclaracaoObito
import com.w_via.features.declaration.Declarante
import com.w_via.features.declaration.DadosObito
import com.w_via.features.declaration.DadosFalecido
import com.w_via.features.declaration.EleitorInfo
import com.w_via.features.declaration.EstadoCivil
import com.w_via.features.declaration.Filiacao
import com.w_via.features.declaration.Filho
import com.w_via.features.declaration.Sexo
import com.w_via.features.receipt.composeToPdf.PdfAsyncImage

/**
 * Número exibido na primeira declaração de óbito. O id interno do banco começa
 * em 1, portanto o número exibido é o id somado a este deslocamento.
 */
const val DECLARACAO_NUMERO_INICIAL: Long = 101L

/** Converte o id interno da declaração no número exibido no documento. */
fun numeroExibido(declaracaoId: Long): Long =
    declaracaoId + (DECLARACAO_NUMERO_INICIAL - 1L)

/**
 * Monta "Cidade/UF" da funerária a partir dos dados da empresa. Se a cidade/UF
 * não estiverem salvos separadamente (dados antigos), extrai do fim do endereço
 * completo.
 */
fun cidadeEstadoDe(empresa: EmpresaEntity?): String {
    if (empresa == null) return ""
    val cidade = empresa.cidade.trim()
    val uf = empresa.uf.trim()
    if (cidade.isNotBlank() || uf.isNotBlank()) {
        return if (cidade.isNotBlank() && uf.isNotBlank()) "$cidade/$uf" else cidade
    }
    val endereco = empresa.endereco
    if (endereco.isBlank()) return ""
    val antesCep = endereco.substringBefore(" - CEP:").trim()
    return antesCep.substringAfterLast(" - ", antesCep)
}

/** Data de hoje por extenso em pt-BR, ex.: "11 de agosto de 2026". */
fun dataAtualPorExtenso(): String {
    val hoje = java.time.LocalDate.now()
    val meses = listOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    )
    return "${hoje.dayOfMonth} de ${meses[hoje.monthValue - 1]} de ${hoje.year}"
}

/**
 * Layout da Declaração de Óbito em formato padrão (sem cantos arredondados),
 * com campos sem borda e disposição "Rótulo: valor" na mesma linha (regra por
 * linha). Renderizado via composeToPdf (PdfGenerator) como o layout do recibo.
 *
 * O cabeçalho traz a logo da funerária, o título e o número da declaração
 * (índice do banco). O conteúdo é envolvido em [A4Fit] para nunca ser cortado
 * numa folha A4.
 */
@Composable
fun DeclaracaoObitoPDFLayout(declaracao: DeclaracaoObito, numero: Long) {
    A4Fit {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(20.dp, 10.dp, 20.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            DeclaracaoHeader(
                numero = numero,
                cidadeEstado = declaracao.cidadeEstado,
                dataEmissao = declaracao.dataEmissaoPorExtenso
            )

            SectionPDF(title = declaracao.falecido.nome.uppercase()) {
                RowPDF(
                    field("CPF", declaracao.falecido.cpf, 1.2f),
                    field("RG", declaracao.falecido.rg, 1f),
                    field("Cor", declaracao.falecido.cor, 1f),
                    field("Sexo", declaracao.falecido.sexo.name.lowercase().replaceFirstChar(Char::uppercase), 1f),
                    field("Idade", declaracao.falecido.idade.toString(), 1f)
                )
                RowPDF(
                    field("Nascimento", declaracao.falecido.dataNascimento, 1.2f),
                    field("Natural de", declaracao.falecido.natural, 1.2f),
                    field("UF", declaracao.falecido.ufNatural, 0.5f),
                    field("Estado civil", declaracao.falecido.estadoCivil?.name?.lowercase()?.replaceFirstChar(Char::uppercase) ?: "", 1.2f),
                    field("Profissão", declaracao.falecido.profissao, 1.4f)
                )
                RowPDF(
                    field("Endereço", declaracao.falecido.endereco, 2f),
                    field("Cidade", declaracao.falecido.cidade, 1.1f),
                    field("UF", declaracao.falecido.uf, 0.5f),
                    field("CEP", declaracao.falecido.cep, 0.9f)
                )
                RowPDF(
                    field("Cartório", declaracao.falecido.cartorio, 1.8f),
                    field("Livro", declaracao.falecido.livro, 0.8f),
                    field("Folha", declaracao.falecido.folha, 0.8f),
                    field("N°", declaracao.falecido.numero, 0.8f)
                )
                RowPDF(
                    field("Eleitor", simNao(declaracao.falecido.eleitor), 0.8f),
                    field("Cidade (título)", declaracao.falecido.eleitorInfo?.cidade ?: "", 1.2f),
                    field("UF", declaracao.falecido.eleitorInfo?.uf ?: "", 0.6f),
                    field("Zona", declaracao.falecido.eleitorInfo?.zona ?: "", 1f),
                    field("Seção", declaracao.falecido.eleitorInfo?.secao ?: "", 1f),
                    field("N° do título", declaracao.falecido.eleitorInfo?.numeroTitulo ?: "", 1f)
                )
                RowPDF(
                    field("INSS", declaracao.falecido.inss ?: "", 1f),
                    field("N° benefício", declaracao.falecido.numeroBeneficio ?: "", 1f),
                    field("Deixa bens", simNao(declaracao.falecido.deixaBens), 1f),
                    field("Testamento", simNao(declaracao.falecido.testamento), 1f)
                )
            }

            SectionPDF(title = "CÔNJUGE") {
                RowPDF(
                    field("Foi casado em cartório", simNao(declaracao.falecido.casamentoEmCartorio), 1f),
                    field("Nome do cônjuge", declaracao.falecido.nomeConjuge ?: "", 2f)
                )
                declaracao.falecido.casamentoCartorioInfo?.let { info ->
                    RowPDF(
                        field("Livro", info.livro, 1f),
                        field("Folha", info.folha, 1f),
                        field("N°", info.numero, 1f)
                    )
                }
            }

            SectionPDF(title = "DADOS DO ÓBITO") {
                RowPDF(
                    field("Local do óbito", declaracao.obito.tipoLocal, 1f),
                    field("Nome do local", declaracao.obito.nomeLocal, 1.6f),
                    field("Cidade", declaracao.obito.cidade, 1.2f),
                    field("UF", declaracao.obito.uf, 0.6f)
                )
                RowPDF(
                    field("Data do óbito", declaracao.obito.dataObito, 1f),
                    field("Hora do falecimento", declaracao.obito.horaFalecimento, 1f),
                    field("N° atestado", declaracao.obito.numeroAtestadoObito, 1f)
                )
                RowPDF(field("Causa da morte", declaracao.obito.causaMorte, 1f))
                RowPDF(
                    field("Cemitério", declaracao.obito.cemiterio, 1.4f),
                    field("Hora do sepultamento", declaracao.obito.horaSepultamento, 1f)
                )
                RowPDF(
                    field("Local do velório", declaracao.obito.localVelorio, 1.4f),
                    field("Hora do velório", declaracao.obito.horaVelorio, 1f)
                )
                RowPDF(
                    field("Endereço", declaracao.obito.endereco, 1.8f),
                    field("N°", declaracao.obito.numero, 0.6f),
                    field("Bairro", declaracao.obito.bairro, 1f)
                )
                RowPDF(
                    field("Cidade", declaracao.obito.cidadeVelorio, 1f),
                    field("CEP", declaracao.obito.cep, 0.8f),
                    field("UF", declaracao.obito.uf2, 0.6f)
                )
                RowPDF(
                    field("Médico", declaracao.obito.medico1, 2f),
                    field("CRM", declaracao.obito.crm1, 1f),
                    field("UF", declaracao.obito.ufMedico1, 0.6f)
                )
                declaracao.obito.medico2?.takeIf { it.isNotBlank() }?.let { medico2 ->
                    RowPDF(
                        field("Médico", medico2, 2f),
                        field("CRM", declaracao.obito.crm2 ?: "", 1f),
                        field("UF", declaracao.obito.ufMedico2 ?: "", 0.6f)
                    )
                }
            }

            SectionPDF(title = "FILIAÇÃO") {
                TwoColumnPDF(
                    leftLines = listOf(
                        listOf("Pai" to (declaracao.falecido.filiacao.nomePai ?: "")),
                        listOf(
                            "Natural de" to (declaracao.falecido.filiacao.naturalPai ?: ""),
                            "UF" to (declaracao.falecido.filiacao.ufNaturalPai ?: "")
                        ),
                        listOf("Profissão" to (declaracao.falecido.filiacao.profissaoPai ?: "")),
                        listOf("Estado civil" to (declaracao.falecido.filiacao.estadoCivilPai ?: ""))
                    ),
                    rightLines = listOf(
                        listOf("Mãe" to (declaracao.falecido.filiacao.nomeMae ?: "")),
                        listOf(
                            "Natural de" to (declaracao.falecido.filiacao.naturalMae ?: ""),
                            "UF" to (declaracao.falecido.filiacao.ufNaturalMae ?: "")
                        ),
                        listOf("Profissão" to (declaracao.falecido.filiacao.profissaoMae ?: "")),
                        listOf("Estado civil" to (declaracao.falecido.filiacao.estadoCivilMae ?: ""))
                    )
                )
            }

            if (declaracao.filhos.isNotEmpty()) {
                SectionPDF(title = "FILHOS") {
                    RowPDF(field("Filhos", declaracao.filhos.joinToString(", ") { it.nome }, 1f))
                }
            }

            SectionPDF(title = "DADOS DO DECLARANTE") {
                RowPDF(
                    field("Nome", declaracao.declarante.nomeDeclarante, 2f),
                    field("Grau de parentesco", declaracao.declarante.grauParentesco, 1f),
                    field("Profissão", declaracao.declarante.profissao, 1f)
                )
                RowPDF(
                    field("RG", declaracao.declarante.rg, 1f),
                    field("CPF", declaracao.declarante.cpf, 1f),
                    field("Telefone", declaracao.declarante.celular, 1f),
                    field("E-mail", declaracao.declarante.email, 1.2f)
                )
                RowPDF(
                    field("Endereço", declaracao.declarante.endereco, 1.8f),
                    field("N°", declaracao.declarante.numero, 0.4f),
                    field("Bairro", declaracao.declarante.bairro, 1f)
                )
                RowPDF(
                    field("Município", declaracao.declarante.municipio, 1.2f),
                    field("CEP", declaracao.declarante.cep, 1f),
                    field("UF", declaracao.declarante.uf, 0.6f)
                )
                if (declaracao.declarante.enderecoCartorio.isNotBlank() ||
                    declaracao.declarante.ufCartorio.isNotBlank() ||
                    declaracao.declarante.cidadeCartorio.isNotBlank()
                ) {
                    RowPDF(
                        field("Cartório", declaracao.declarante.cartorio, 1.5f),
                        field("Telefone", declaracao.declarante.telefone, 1f)
                    )
                    RowPDF(
                        field("Endereço do cartório", declaracao.declarante.enderecoCartorio, 1.8f),
                        field("Cidade", declaracao.declarante.cidadeCartorio, 1.2f),
                        field("UF", declaracao.declarante.ufCartorio, 0.6f)
                    )
                    if (declaracao.declarante.emailCartorio.isNotBlank() ||
                        declaracao.declarante.oficialCartorio.isNotBlank()
                    ) {
                        RowPDF(
                            field("E-mail do cartório", declaracao.declarante.emailCartorio, 1f),
                            field("Oficial", declaracao.declarante.oficialCartorio, 1f)
                        )
                    }
                }
            }

            SectionPDF(title = "OBSERVAÇÕES") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .border(1.dp, Color.White)
                        .padding(4.dp)
                ) {
                    Text(
                        text = declaracao.observacoes,
                        fontSize = 9.sp,
                        lineHeight = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "A presente declaração é válida para fins de sepultamento e remoção do corpo, inclusive para além dos limites do município, conforme a legislação vigente e as exigências dos órgãos competentes. Destina-se exclusivamente ao registro do óbito perante o Cartório de Registro Civil competente.\nDeclaro que li integralmente, conferi, estou de acordo com as informações nela contidas e assumo a responsabilidade pelos dados fornecidos.".uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SignatureField("ASSINATURA DO DECLARANTE", Modifier.weight(1f))
                SignatureField("AGENTE FUNERÁRIO", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (declaracao.enderecoFuneraria.isNotBlank()) {
                Text(
                    text = declaracao.enderecoFuneraria.uppercase(),
                    fontSize = 7.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = declaracao.telefoneFuneraria.uppercase(),
                fontSize = 7.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Cabeçalho de 70dp com a logo da funerária, o título e o número da declaração. */
@Composable
private fun DeclaracaoHeader(
    numero: Long,
    cidadeEstado: String = "",
    dataEmissao: String = ""
) {
    val context = LocalContext.current
    val logoUri = remember { LogoStorage.load(context) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
        ) {
            if (logoUri != null) {
                PdfAsyncImage(
                    model = logoUri,
                    contentDescription = "Logo da funerária",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DECLARAÇÃO DE ÓBITO",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    val subtitulo = listOf(cidadeEstado, dataEmissao)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    if (subtitulo.isNotBlank()) {
                        Text(
                            text = subtitulo.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text(
                    text = "N° $numero",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }
        }
        Spacer(modifier = Modifier.width(150.dp))
    }
    HorizontalDivider(thickness = 1.dp, color = Color.Black)
}

/** Campo de assinatura: espaço em branco, linha e rótulo em medium abaixo. */
@Composable
private fun SignatureField(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(22.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.Black)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 7.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

private fun simNao(valor: Boolean?): String = when (valor) {
    true -> "Sim"
    false -> "Não"
    null -> ""
}

/**
 * Garante que o conteúdo caiba na folha A4: mede a altura intrínseca e, se ela
 * exceder o espaço disponível, aplica uma escala proporcional uniforme.
 */
@Composable
private fun A4Fit(content: @Composable () -> Unit) {
    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val placeable = subcompose("content", content)
            .first()
            .measure(
                Constraints(
                    maxWidth = constraints.maxWidth,
                    maxHeight = Constraints.Infinity
                )
            )
        val maxHeight = constraints.maxHeight
        val scale = if (maxHeight != Constraints.Infinity && placeable.height > maxHeight) {
            maxHeight / placeable.height.toFloat()
        } else {
            1f
        }
        layout(
            width = constraints.maxWidth,
            height = (placeable.height * scale).toInt()
        ) {
            placeable.placeWithLayer(0, 0) {
                this.scaleX = scale
                this.scaleY = scale
            }
        }
    }
}

@Preview
@Composable
fun DeclaracaoObitoPDFLayoutPreview() {
    DeclaracaoObitoPDFLayout(
        numero = DECLARACAO_NUMERO_INICIAL,
        declaracao = DeclaracaoObito(
            falecido = DadosFalecido(
                nome = "João da Silva",
                cpf = "123.456.789-00",
                rg = "12.345.678-9",
                dataNascimento = "01/01/1940",
                idade = 85,
                sexo = Sexo.MASCULINO,
                cor = "Branca",
                profissao = "Aposentado",
                natural = "São Paulo",
                ufNatural = "SP",
                cartorio = "",
                livro = "",
                folha = "",
                numero = "",
                endereco = "Rua Exemplo, 100",
                cep = "01000-000",
                cidade = "São Paulo",
                uf = "SP",
                estadoCivil = EstadoCivil.CASADO,
                nomeConjuge = "Maria da Silva",
                casamentoEmCartorio = true,
                casamentoCartorioInfo = CasamentoCartorioInfo(
                    dia = "",
                    livro = "123",
                    folha = "45",
                    numero = "678"
                ),
                deixaBens = true,
                testamento = false,
                eleitor = true,
                eleitorInfo = EleitorInfo(
                    cidade = "São Paulo",
                    uf = "SP",
                    zona = "1",
                    secao = "3",
                    numeroTitulo = "12345678"
                ),
                filiacao = Filiacao(
                    nomePai = "José da Silva",
                    naturalPai = "Salvador",
                    ufNaturalPai = "BA",
                    estadoCivilPai = null,
                    profissaoPai = null,
                    nomeMae = "Ana da Silva",
                    naturalMae = "São Paulo",
                    ufNaturalMae = "SP",
                    estadoCivilMae = null,
                    profissaoMae = null
                )
            ),
            obito = DadosObito(
                tipoLocal = "Hospital",
                nomeLocal = "Hospital Central",
                cidade = "São Paulo",
                uf = "SP",
                dataObito = "03/08/2026",
                horaFalecimento = "14:30",
                numeroAtestadoObito = "00123",
                causaMorte = "Parada cardiorrespiratória",
                cemiterio = "Cemitério da Paz",
                horaSepultamento = "16:00",
                localVelorio = "",
                horaVelorio = "",
                endereco = "",
                numero = "",
                bairro = "",
                cidadeVelorio = "",
                cep = "",
                uf2 = "",
                medico1 = "Dr. Roberto Alves",
                crm1 = "CRM 12345",
                ufMedico1 = "SP"
            ),
            filhos = listOf(Filho("Pedro da Silva")),
            observacoes = "Informações passadas pela declarante.",
            declarante = Declarante(
                cartorio = "",
                telefone = "",
                endereco = "",
                nomeDeclarante = "Maria da Silva",
                grauParentesco = "Cônjuge",
                profissao = "Do lar",
                rg = "",
                cpf = "987.654.321-00",
                residencia = "",
                bairro = "",
                municipio = "São Paulo",
                cep = "",
                uf = "SP",
                celular = "(11) 99999-9999",
                email = ""
            ),
            cidadeEstado = "São Paulo/SP",
            dataEmissaoPorExtenso = "11 de agosto de 2026",
            enderecoFuneraria = "Rua das Flores, 123 - Centro - São Paulo/SP - CEP: 01000-000",
            telefoneFuneraria = "(11) 3333-3333 - Atendimento 24h"
        )
    )
}
