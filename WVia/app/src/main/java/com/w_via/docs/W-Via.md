# W-Via

## O que é

W-Via é um app independente (spin-off, não sub-marca do SetDrive que é outro projeto) voltado para **agentes funerários autônomos** no Brasil. Reaproveita a infraestrutura de roteirização e recibos já construída no SetDrive, e adiciona um domínio próprio: geração estruturada de PDF para **Declaração de Óbito**, com formulário multi-etapa e lógica condicional de campos gated por idade e sexo biológico.

---

## Tom

- **Sóbrio, mas não frio.** O público lida com luto e burocracia sensível o tempo todo; o app deve parecer confiável e respeitoso, sem soar clínico ou burocrático demais.
- **Direto e sem fricção.** Agentes funerários trabalham sob pressão de tempo (janelas legais para declaração de óbito, translado). Cada tela deve reduzir passos, não adicionar cerimônia.
- **Sem personagem/mascote.** Diferente do SetDrive (que tem a Hanna como voz de marca), o W-Via não usa persona de IA ou comunicação "humanizada" — o contexto não pede isso, e seria deslocado.
- **Paleta como extensão do tom:** preto, branco e dourado-amarelado. Remete a solenidade e institucionalidade (contraste com o tom vibrante/pop que produtos de consumo costumam usar), sem parecer luxuoso ou institucional-frio (tipo cartório).

---

## Orientações de design

- **Fundo de mapa persistente.** O mapa é elemento estrutural da UI, não uma tela isolada — funciona como camada de fundo constante durante o fluxo de translado/roteirização, com overlays de conteúdo por cima.
- **Overlays de status/navegação por composable, por tela.** Cada tela pode customizar cor de status bar e nav bar via composable dedicado, em vez de configuração global — necessário porque o app alterna entre contextos visuais diferentes (mapa full-bleed vs. formulários).
- **Formulários como fluxo de etapas (steps), não formulário único.** A Declaração de Óbito é longa e tem ramificações condicionais; quebrar em steps com navegação controlada (`BackHandler` gerenciando pilha) evita sobrecarga cognitiva e permite validação incremental.
- **Campos condicionais são a regra, não exceção.** Sexo biológico e idade alteram quais campos aparecem (ex: exigências específicas em óbito fetal/infantil). O design de formulário parte do princípio de que a estrutura do formulário muda dinamicamente.
- **Paleta aplicada com moderação.** Preto e branco dominam superfícies; dourado-amarelado é usado como accent (CTAs, destaques, marca), não como cor de fundo extensa.
- **Tema do app** As cores, tipografia, border radios e outros elementos do tema do app são e estão definidos na pasta ui.theme desse projeto

---

## Arquitetura

- **Stack:** Kotlin, Jetpack Compose, MVVM.
- **DI: Koin**, adotado no lugar de wiring manual de ViewModel — decisão específica para compatibilidade futura com uma eventual migração para Compose Multiplatform (Koin tem melhor suporte multiplataforma que alternativas como Hilt).
- **Backend: Supabase** — Auth, Row Level Security (RLS) e Storage.
- **Decisões de arquitetura documentadas via ADRs**, formalizando escolhas como a adoção do Koin.
- **Reaproveitamento de infraestrutura do SetDrive:** roteirização e módulo de geração de recibos são compartilhados/portados, evitando duplicar lógica já validada em produção.

---

## Abstrações

- **ViewModel com camada de conversão de domínio**: o ViewModel do formulário de Declaração de Óbito não expõe o modelo de domínio bruto para a UI — há uma etapa de conversão que traduz estado de domínio em estado de apresentação, mantendo a lógica condicional (idade/sexo → campos visíveis) fora da camada de Compose.
- **Composables de overlay para status/nav bar**: abstração reaproveitável que encapsula a lógica de styling de sistema por tela, evitada de ser espalhada em cada Activity/Screen individualmente.
- **Navegação por steps com gerenciamento de pilha via `BackHandler`**: abstrai a navegação linear-com-ramificações do formulário sem acoplar a lógica de "voltar" a cada tela individualmente.

---

## Principais Responsabilidades por camada

| Camada                                          | Responsabilidade                                                                                                                         |
|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| **UI (Compose)**                                | Renderizar steps do formulário, aplicar overlays de status/nav bar, exibir mapa persistente                                              |
| **ViewModel**                                   | Orquestrar estado dos steps, aplicar lógica condicional de campos, converter domínio → apresentação                                      |
| **Domínio**                                     | Regras de negócio da Declaração de Óbito (campos obrigatórios por idade/sexo)                                                          |
| **Dados (Supabase)**                            | Persistência via Auth/RLS/Storage, isolamento de dados por agente/funerária                                                              |
| **Infraestrutura compartilhada (via SetDrive)** | Roteirização e geração de recibos                                                                                                        |
| **Geração de PDF**                              | Conversão do estado final do formulário em documento estruturado de Declaração de Óbito, em conformidade com o modelo oficial brasileiro |
| **Rotas e navegação**                           | As rotas são definidas em constantes na pasta features.routes                                                                            |

---

*Documento vivo — atualizar conforme decisões de arquitetura (ADRs) e definição de produto evoluírem.*
