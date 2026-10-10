# Implementações futuras — Android App Cloner

Atualizado: 2026-10-10. Esta lista é separada das tarefas da v0.11 e não autoriza publicação sem testes.

## F01 — Aplicativos clonados com ícones independentes no ecrã inicial

**Estado:** adiado por decisão do proprietário.

Na v0.11, os clones de Free e Pro **não apresentam** o ícone de um APK independente no ecrã inicial. A única funcionalidade de ecrã inicial da v0.11 é, no modo Pro, **criar um atalho pertencente ao aplicativo principal**, que pede ao Clone Hub para abrir o clone.

Estudar numa futura versão:
- Oferecer a possibilidade de atribuir ícone de launcher próprio ao clone APK Pro.
- Informar claramente que ícones independentes podem continuar no ecrã inicial quando o Clone Hub for removido.
- Avaliar o impacto no controlo de instâncias, licenças, histórico, privacidade e ciclo de vida.
- Nunca prometer eliminação automática dos clones ao desinstalar o Clone Hub: o Android não possui dependência de desinstalação em cascata de apps comuns.
- Permitir regressar ao modo apenas-atalhos mediante nova instalação/atualização e consentimento.

## F02 — Plano Pro comercial

- Integrar faturação e recuperação de compra em backend validado.
- Atualmente apenas builds **DEBUG** poderão testar Pro com interruptor QA; builds release não ativam Pro sem entitlement real.
- Definir restauração de compras, transferência entre dispositivos e política de downgrade.
- Implementar testes de entitlement e proteção contra alterações locais no estado de assinatura.

## F03 — Remoção completa e portabilidade

- Investigar se Enterprise Device Owner permite políticas de desinstalação supervisionadas, como produto diferente e explícito.
- Preservar independência dos pacotes no produto comum sem privilégios invasivos.
- Migração/recuperação do catálogo local e detecção assistida de clones criados numa versão anterior; **nunca** remover apps fora da lista cuja origem seja confirmada.
- Explicar que o fluxo guiado de remoção da v0.11 não é executado quando o utilizador desinstala diretamente o Clone Hub nas Definições.

## F04 — Compatibilidade Android 15+, HyperOS e motor de clonagem

- Regredir modo sem categoria LAUNCHER em apps reais, incluindo multiplos launchers, activity-alias e split APK.
- Substituir progressivamente o alvo Android legado `targetSdkVersion 28` com testes funcionais.
- Tratar limitações de apps com Play Integrity, DRM, serviços Google, segurança reforçada.
- Otimizar consumo de RAM e adicionar migração de armazenamento para Android Go.

