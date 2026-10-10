# Auditoria delimitada — BFE Android App Cloner (10/10/2026)

## Identidade
- Base: `The412Banner/BFE` v0.10, GPL-3.0; fork `hatusjc/android-app-cloner`.
- Dispositivo de destino: Xiaomi Redmi A5, modelo `25028RN03Y`, Android 15 Go, ABI arm64.
- Escopo: integridade do fork, instalabilidade e geração de APK; fluxo de clonagem apenas em análise estática até haver execução em dispositivo.

## Achados iniciais e remediação

| ID | Gravidade | Constatação | Remediação |
|---|---|---|---|
| AC-01 | P1 | A base assinava releases com a chave `keystore/bfe-test.jks`, senha/alias públicos (`bfetest`). Qualquer terceiro podia imitar atualizações legítimas. | Removida a chave da HEAD e a configuração de assinatura pública. Novo PKCS#12 exclusivo, transmitido a GitHub Actions por segredos criptografados. Chave histórica ainda é acessível na história original, mas **não assina este fork**. |
| AC-02 | P2 | O BFE original e esta edição usariam o mesmo `applicationId` e certificados diferentes, causando falhas de atualização/colisões. | Novo ID `com.hatusjc.androidappcloner`, mantendo fontes e atribuição originais. |
| AC-03 | P2 | `targetSdkVersion 28` é antigo; fluxo de acesso amplo a arquivos exige modernização cuidadosa antes de elevar o alvo para Android 15. | Registrado como dívida técnica. Não alterar às cegas: atualização pode quebrar clonagem, instalador ou extração. |
| AC-04 | P2 | O registro do projeto original reconhece falta de testes reais da interface de clonagem nas versões recentes. | Testes unitários + assinatura/verificações automatizadas na nuvem e checklist de execução física abaixo. Nenhuma alegação de teste real do Redmi A5 até a execução. |
| AC-05 | P2 | Aplicativos autenticados por assinatura, atestação Play Integrity, DRM, Firebase e splits sob demanda podem rejeitar clonagem. | Limitações publicadas no guia português; nunca prometer clonagem universal. |
| AC-06 | P2 | O aplicativo herdado solicita gestão total de arquivos (`MANAGE_EXTERNAL_STORAGE`), com impacto em privacidade. | Permissão sinalizada claramente ao testador; modernização/redução de permissões requer regressão do gestor de arquivos. |

## Resultados verificados em 10/10/2026

- **PASS:** build debug + cinco testes JUnit de identificadores/etapas, assinatura APK v2, manifesto correto e bibliotecas ARM64; [execução de CI 38040109351](https://github.com/hatusjc/android-app-cloner/actions/runs/38040109351).
- **PASS:** [pré-lançamento de testes v0.10-hatus.1-test](https://github.com/hatusjc/android-app-cloner/releases/tag/v0.10-hatus.1-test) aponta ao commit testado `b86a4ef343492462d2bb74ded0559e9dbb720759`.
- **PASS:** APK de 35.138.500 bytes, SHA-256 `14e60e2d88f8e3608b0c398f94b7219814e8c99783bfc5ee0d6d1685d90eaf7c`, descarregado e conferido no PC autorizado em `/home/kirian/Downloads/Android-App-Cloner-Redmi-A5-ARM64-v0.10-hatus1-test.apk`. Relatórios na própria Release.
- **PENDENTE:** certificado de assinatura **privado e persistente** nos segredos GitHub; esta pré-release usa certificado debug efémero do runner e **não deve ser tratada como release de produção**.
- **PENDENTE:** validação física do Redmi A5 e dos fluxos reais de clonagem (ver abaixo).

## Verificações técnicas exigidas

- [x] Workflow GitHub Actions conclui **build debug e testes unitários reais**.
- [x] Assinatura APK de teste verificada por `apksigner` (v2).
- [ ] Build de **distribuição de produção** assinado com certificado privado persistente; v2 e v3 verificados por `apksigner`.
- [x] `aapt` comprova ID `com.hatusjc.androidappcloner` e `minSdk 26`.
- [x] APK contém bibliotecas `arm64-v8a`.
- [x] Evidência SHA-256 anexa ao workflow; pré-release aponta para commit exato.
- [x] APK guardado no PC autorizado e publicado na aba Releases do GitHub.

**Teste manual que só pode ser marcado com o telefone ligado/operante:**
- [ ] Redmi A5 aceita instalar o APK e inicia a aplicação sem crash.
- [ ] Gestão de permissões é compatível com a ROM do Redmi A5.
- [ ] Clonagem de um aplicativo de teste simples sem contas/dados sensíveis conclui.
- [ ] Original e clone abrem ao mesmo tempo, com dados independentes.
- [ ] Fechar, reabrir, atualizar e desinstalar clone não danifica app original.
- [ ] Testes de múltiplos APK/split APK (quando aplicável), sem incompatibilidade de assinaturas.
- [ ] Confirmar desempenho, consumo de RAM e comportamento de background em Android Go.

## Critério de veredito

**GO para disponibilizar APK de testes** quando CI, verificações criptográficas, metadados e integridade do arquivo estiverem verdes. **NO-GO para alegar suporte plenamente homologado ao Redmi A5 ou clonagem universal** até passar o checklist manual em aparelho real. Não misturar aprovação de build com aprovação de produto.

## Licença e isolamento

Preservar GPL-3.0, código-fonte integral, aviso de origem e alterações. Não remover a atribuição BFE original. Acesso premium em outros aplicativos não é ativado pela clonagem; esta ferramenta é 100% gratuita.
