# Plano de execução — Android App Cloner v0.11 (Clone Hub)

Data: 10/10/2026. Base: BFE (GPL-3.0), fork `hatusjc/android-app-cloner`. Este plano documenta decisões aprovadas e gates de verificação.

## Decisões funcionais consolidadas

1. Aplicativo focado em clonagem, não em gestão de ficheiros; aproveitar o motor BFE de leitura, reescrita, assinatura e instalação do APK.
2. Primeiro arranque: fluxo de permissões Android necessárias, incluindo a autorização de instalar aplicativos de origens desconhecidas. Solicitar cada permissão pelo sistema, sem prometer concessão automática. Acesso total a ficheiros não deve bloquear um fluxo de clonagem que utilize armazenamento privado e FileProvider.
3. Ecrã principal: quantidade real de clones instalados, agrupamento por aplicação original, quantidade de instâncias por grupo, ícone e nome; tocar abre modal com **Abrir, Informações, Desinstalar**.
4. Pressão longa seleciona clones, ação **Selecionar todos** e lixeira para solicitar desinstalação em lote, cada uma sob as confirmações impostas pelo Android.
5. **Clonar aplicativo:** pesquisa por nome/pacote dos apps baixados pelo utilizador. Ocultar os aplicativos de sistema, componentes pré-instalados, o clonador e clones já registados.
6. **Free:** máximo de dois clones simultaneamente instalados, independentemente do app de origem; apagar uma instalação liberta um lugar.
7. **Pro:** sem limite imposto pelo aplicativo; permanece sujeito às restrições de armazenamento e do sistema Android. O desbloqueio deve basear-se num entitlement verificável; pode existir modo de teste apenas nas builds DEBUG até existir gateway de pagamento.
8. **Ambos os planos:** clones **sem ícone independente no ecrã inicial do telefone**. Abrir pelo Clone Hub, mesmo que o Android execute a aplicação clonada em uma atividade separada.
9. **Só Pro — atalho opcional no ecrã inicial:** atalho fixado do **Clone Hub** para abrir diretamente aquele clone, por `ShortcutManager.requestPinShortcut` e atividade dispatcher do host. Não é um launcher/instalação independente do clone. O atalho é pertencente ao aplicativo host, pelo que a sua desinstalação remove também os atalhos associados, sujeito ao comportamento do launcher.
10. **Implementação futura (não executar agora):** permitir que clones Pro tenham um ícone independente/launcher próprio, mantendo a opção de apenas atalhos do Clone Hub. Registar e validar primeiro as consequências de perda de histórico ao limpar dados/desinstalar host.
11. **Desinstalar host:** o Android normal **não** remove automaticamente outros pacotes APK independentes ao desinstalar o Clone Hub ou limpar dados dele. É impossibilidade do modelo de instalação normal sem privilégios especiais. Implementar fluxo explícito **Desinstalar todos os clones e depois o Clone Hub**, com passos de confirmação e verificações após cada remoção. Nunca marcar remoções como concluídas antes do PackageManager confirmar; não prometer execução automática após uma desinstalação direta nas Definições do Android.

## Implementação por etapas e precedências

### Etapa A — Fundação v0.11
- Criar catálogo persistente próprio dos clones (fonte, destino, rótulo, classe de lançamento, data, estado) e reconciliação com PackageManager. Não contar APK gerado como instalado.
- Separar a apresentação do motor BFE e construir ecrã inicial Clone Hub com cartão por aplicativo, contagem real e estado vazio.
- Seleção pesquisável só de aplicativos não-sistema e tela de edição do clone; preservar os avisos de incompatibilidade do motor.
- Implementar onboarding de instalação e autorização do Android 15, utilizando `REQUEST_INSTALL_PACKAGES`, `PackageManager.canRequestPackageInstalls`, `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` e `FileProvider`.

### Etapa B — Gestão e políticas
- Integrar compilação `ApkJobService` com pedido de instalação e retorno confirmado, histórico e limite Free com provas de instalação.
- Reescrever o manifest de clones gerados pelo Clone Hub para ocultar `android.intent.category.LAUNCHER`; preservar atividade exportada usada para abrir explicitamente.
- Abrir por `ComponentName`; informações pela tela Android `ACTION_APPLICATION_DETAILS_SETTINGS`.
- Ações individuais/lote para desinstalar pelo instalador oficial `ACTION_DELETE`, sem tentar contornar confirmação.

### Etapa C — Pro / atalhos
- Testar Pro (entitlement de QA restrito a `BuildConfig.DEBUG`), sem publicar desbloqueio permanente inseguro.
- Implementar atalhos fixados para clones Pro através de uma `Activity` exportada do **host**, com validação do pacote solicitado contra o catálogo local e `ShortcutManager.isRequestPinShortcutSupported`.
- Degradação graciosa em launchers Xiaomi que não suportem atalhos fixados.

### Etapa D — Gates independentes e distribuição
- Testes para limite 2, remoção, seleção, nomes únicos, reconciliação e visibilidade do launcher.
- `assembleDebug`, JUnit, `apksigner`, `aapt`, ABI `arm64-v8a`, assinatura e hash em GitHub Actions.
- APK de **teste** via GitHub Releases só após CI verde. Teste físico no Redmi A5 obrigatório para declarar a experiência validada.
- Preservar código base GPL-3.0, sem alterar a versão estável até os gates passarem.

## Regra de segurança

Não criar forma de apagar clones automaticamente depois da desinstalação do host: não há código do host a executar nessa ocasião. Não utilizar root nem permissões de administrador do dispositivo. Nunca apagar aplicativos que não pertençam ao catálogo comprovado.

## Implementações futuras

- Atalho/ícone independente por clone Pro (em vez de um atalho pertencente ao host), apenas depois de reconsiderar cuidadosamente a exigência de desaparecimento ao remover o host.
- Cobrança real/licença Pro robusta; não confundir testes debug com entitlement comercial.
- Compatibilidade de clonagem com apps difíceis, splits dinâmicos, APIs Google e proteções de integridade sem prometer funcionamento universal.
