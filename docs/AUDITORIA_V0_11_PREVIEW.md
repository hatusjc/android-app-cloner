# Relatório técnico — Clone Hub v0.11.0-alpha.1 Preview

Data: 2026-10-10. Auditoria de build e revisão funcional dirigida. **Este relatório não equivale a auditoria independente nem a teste físico**; o desenvolvimento foi realizado na branch de funcionalidade e a revisão do código é do mesmo implementador.

## Identidade imutável do APK de testes

- Repositório: `hatusjc/android-app-cloner`.
- Branch de desenvolvimento: `feature/clone-hub-v0.11-planning`; PR draft #5.
- Commit exato construído e distribuído: `e071a7c7d552aeea762985043f2b1f021e692114`.
- GitHub Actions: [run 38042907467](https://github.com/hatusjc/android-app-cloner/actions/runs/38042907467) — **SUCCESS**.
- APK: [Clone-Hub-Preview-v0.11.0-alpha.1-ARM64.apk](https://github.com/hatusjc/android-app-cloner/releases/download/v0.11.0-alpha.1-preview/Clone-Hub-Preview-v0.11.0-alpha.1-ARM64.apk).
- Tamanho: 35.215.944 bytes.
- SHA-256: `7dd1e131b44b482e013d93a1ceb95d33ccb4fe98abf1a04241ef2b8d55996485`.
- ID Preview distinto: `com.hatusjc.androidappcloner.preview`.
- Copiado para o PC autorizado em `/home/kirian/Downloads/Clone-Hub-Preview-v0.11.0-alpha.1-ARM64.apk`; arquivo validado contra hash do CI.

## Gates automáticos PASS

- [x] Android SDK, Gradle, build debug Android arm64-v8a.
- [x] JUnit: regressões da base + regras de até 2 clones Free, Pro QA ilimitado, exclusão de system apps, substituição de categorias de launcher.
- [x] APK debug validado pelo `apksigner`, assinatura v2.
- [x] Pacote `.preview` e `minSdk 26` confirmados via `aapt`.
- [x] Bibliotecas arm64-v8a incluídas.
- [x] A permissão de gestão total de arquivos (`MANAGE_EXTERNAL_STORAGE`) não aparece no relatório gerado; retirados do manifesto serviços do antigo explorador e acesso amplo pelo FileProvider.
- [x] Artefatos com relatórios e SHA-256 no GitHub Release.
- [x] Versão v0.10 existente no GitHub não foi alterada nem substituída.

## Funcionalidades implementadas no código, ainda com verificação física pendente

- [ ] Wizard inicial de permissões necessárias; solicitações oficiais Android (autorizar instalação desconhecida; notificações opcionais).
- [ ] Ecrã inicial de clones agrupados por aplicativo de origem; contagens só após registro de instalação.
- [ ] Pesquisa só de apps do utilizador, excluindo system/updated-system.
- [ ] Clone editor + BFE ARSCLib/apksig com remoção de `LAUNCHER` e `LEANBACK_LAUNCHER`, sem remover a atividade Android usada em abertura explícita.
- [ ] Instalação confirmada pelo PackageManager; cancelar não consome slot.
- [ ] Abrir/Info/Desinstalar; pressionar e segurar, selecionar todos, remoção em lote com confirmação por clone.
- [ ] Free 2 instalações; Pro DEBUG sem limite lógico; atalhos Pro pertencentes ao host com ícone do clone.
- [ ] Fluxo de remoção guiada (clones antes do host) bloqueia desinstalação do host se restarem clones.

## Riscos abertos e decisões

| Identificador | Gravidade | Estado |
|---|---|---|
| QA-01 | P2 | Falta execução física Redmi A5/Android 15 Go; em especial abertura explícita de atividade quando o clone não tem launcher e comportamento dos atalhos HyperOS. |
| QA-02 | P2 | Se o utilizador desinstalar o host diretamente pelas Definições, clones APK independentes não são desinstalados pelo Android. O fluxo guiado é a alternativa sem root. |
| QA-03 | P2 | O certificado de build DEBUG é temporário do runner. Atualizações da edição Preview podem necessitar de desinstalação. |
| QA-04 | P2 | Pro comercial exige entitlement backend confiável. A v0.11 contém apenas comutador Pro **DEBUG de QA**. |
| QA-05 | P2 | `targetSdkVersion 28` legado e compatibilidade com alguns split APKs/apps que validam assinatura/DRM/Play Integrity exigem testes posteriores. |
| QA-06 | P3 | Catálogo persistente em SharedPreferences do host: limpar seus dados perde histórico local; APKs independentes permanecem, exigindo fluxo de recuperação futuro. |

**Veredito:** **GO para APK de teste técnico** (compilação e integridade automatizadas aprovadas). **NO-GO para declarar a v0.11 homologada para o Xiaomi ou para produção comercial** sem QA física, testes de remoção e assinatura privada persistente. Nenhuma alteração na main autorizada por este relatório.

Issue de execução física: https://github.com/hatusjc/android-app-cloner/issues/6
