# W-Via

Site institucional e app Android do **W-Via**, voltado para agentes funerários autônomos no
Brasil. O app gera a **Declaração de Óbito** em PDF por formulário multi-etapa, além de
recibos, roteirização e gestão do perfil da empresa.

## Conteúdo do repositório

```
.
├── index.html        # site (GitHub Pages)
├── policy.html       # política de privacidade
├── termos-de-uso.html
├── images/           # capturas de tela usadas no site
├── WVia/             # projeto Android
└── w_via_app_icons/  # ícones e assets
```

## Site (GitHub Pages)

O site é servido a partir da raiz do branch `main`:

**Settings → Pages → Deploy from a branch → `main` / `/ (root)`**

URL: https://jonatamota.github.io/W-Via-App/

## App Android

Stack: Kotlin, Jetpack Compose, MVVM, Koin, Supabase e Room.

Requisitos: JDK 17, Android Studio e Android SDK (minSdk 27 / targetSdk 37).

### Configuração

```bash
cd WVia
cp local.properties.template local.properties
```

Preencha `local.properties` com SDK, chaves de mapas (HERE/GraphHopper), Supabase e dados
do keystore de release. Esse arquivo é ignorado pelo Git.

O SQL do banco (tabelas e RLS) está em `WVia/supabase/`.

### Build

```bash
cd WVia
./gradlew assembleDebug
```

> Nunca versione `local.properties`, keystores ou service accounts.
