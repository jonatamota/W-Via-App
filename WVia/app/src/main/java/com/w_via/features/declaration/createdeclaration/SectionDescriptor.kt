package com.w_via.features.declaration.createdeclaration

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.w_via.R
import com.w_via.features.routes.Routes

/**
 * Registro único que liga uma seção da Declaração de Óbito à sua rota, ícone
 * e tela de conteúdo. O grid e a navegação derivam tudo deste registro —
 * adicionar uma seção passa a exigir alteração em apenas um lugar.
 *
 * Critério de nomenclatura: as rotas de seção usam o prefixo consistente
 * `DECLARATION_` (mesma família de `DECLARATION`/`DECLARATION_GRID`).
 */
data class SectionDescriptor(
    val section: DeclarationSection,
    val route: String,
    val iconRes: Int,
    val content: @Composable (NavController, DeclarationDraftViewModel) -> Unit
)

/** Descritores de todas as seções, na ordem exibida no grid. */
val declarationSectionDescriptors: List<SectionDescriptor> = DeclarationSection.entries.map { section ->
    SectionDescriptor(
        section = section,
        route = when (section) {
            DeclarationSection.FALECIDO -> Routes.DECLARATION_FALECIDO
            DeclarationSection.OBITO -> Routes.DECLARATION_OBITO
            DeclarationSection.FILIACAO -> Routes.DECLARATION_FILIACAO
            DeclarationSection.CONJUGE_FILHOS -> Routes.DECLARATION_CONJUGE_FILHOS
            DeclarationSection.DECLARANTE -> Routes.DECLARATION_DECLARANTE
            DeclarationSection.OBS -> Routes.DECLARATION_OBS
        },
        iconRes = R.drawable.ic_doc_svgrepo_com,
        content = when (section) {
            DeclarationSection.FALECIDO -> { navController, draft ->
                DadosFalecidoSectionScreen(navController, draft)
            }
            DeclarationSection.OBITO -> { navController, draft ->
                DadosObitoSectionScreen(navController, draft)
            }
            DeclarationSection.FILIACAO -> { navController, draft ->
                FiliacaoSectionScreen(navController, draft)
            }
            DeclarationSection.CONJUGE_FILHOS -> { navController, draft ->
                ConjugeFilhosSectionScreen(navController, draft)
            }
            DeclarationSection.DECLARANTE -> { navController, draft ->
                DeclaranteSectionScreen(navController, draft)
            }
            DeclarationSection.OBS -> { navController, draft ->
                ObsSectionScreen(navController, draft)
            }
        }
    )
}
