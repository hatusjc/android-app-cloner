# Clone Hub v0.11.0-alpha.1 — guia de teste no Redmi A5

**Tipo:** pré-visualização DEBUG Android ARM64, não produção. A v0.10 aprovada permanece intacta; a edição DEBUG usa o identificador `com.hatusjc.androidappcloner.preview` para poder ser instalada em paralelo. A assinatura debug dos GitHub Actions não é persistente entre builds: novas versões de teste podem exigir remover o preview anterior.

## Objetivos desta versão

- Novo ecrã inicial focado em clones em vez do explorador.
- Primeira abertura apresenta autorização para instalar APKs desconhecidos via Android; notificações são opcionais. Não se solicita acesso geral aos ficheiros.
- Pesquisa mostra aplicações descarregadas e lançáveis pelo utilizador, sem apps de sistema.
- Editor de clone usa o motor BFE; esconde as categorias de launcher em ambos os planos. O clone instala como APK separado mas deve ser aberto pelo Clone Hub.
- Catálogo persistido localmente com agrupamento e contagem de instalações confirmadas.
- Pressão longa, selecionar todos, desinstalar em lote, informações do Android e abrir diretamente a atividade do clone.
- Free com dois clones ativos; modo Pro **para QA apenas em builds DEBUG** permite clones ilimitados por política da aplicação.
- Pro QA disponibiliza pedido de atalho fixado no launcher; o atalho pertence ao **Clone Hub**, com ícone do clone e dispatcher do host. Ao desinstalar o host, os atalhos pertencentes ao pacote desaparecem do launcher; **os pacotes clonados permanecem no Android**.
- Menu para remoção guiada de todos os clones e só então do aplicativo pai, com as confirmações obrigatórias do Android.

## Testes manuais necessários antes de declarar GO

1. Instalar a pré-release `*.apk` ARM64 do GitHub, com v0.10 ainda no telefone.
2. No primeiro arranque, autorizar em Definições **Instalar apps desconhecidas** para Clone Hub Preview. Testar recusa e reabertura sem conceder acesso desnecessário.
3. Pesquisar uma aplicação simples descarregada (sem dados sensíveis), verificar que não aparecem apps de sistema.
4. Criar o clone e confirmar a instalação no ecrã Android; verificar que a contagem só aumenta depois de instalado.
5. Confirmar que nenhum novo ícone independente surgiu no ecrã inicial; tocar no grupo do Clone Hub e utilizar **Abrir**.
6. Criar segunda cópia, verificar nomes e pacotes distintos; tentar terceira em Free e confirmar o bloqueio.
7. Pressionar e segurar, **Selecionar todos**, usar lixeira e verificar cada pedido de desinstalação. Cancelar um pedido e confirmar que o pacote não desaparece da contagem.
8. Ativar **Pro para testes**; criar terceiro clone. Solicitar atalho no launcher e verificar que inicia o clone. Desativar Pro de teste e verificar que o atalho não permite acesso Pro.
9. Remover um clone nas definições do Android, voltar ao Clone Hub e confirmar a reconciliação.
10. Testar **Desinstalar todos os clones e o Clone Hub** apenas num ambiente que possa ser apagado; confirmar que a desinstalação do host só é oferecida depois da remoção real de todos os clones.
11. Testar clonar aplicativos com APKs divididos, reiniciar app e telefone, verificar estabilidade no Android 15 Go.

## Limitações explicitamente conhecidas

- Não é possível apagar clones em cascata quando o Android remove diretamente o Clone Hub. Isto é uma restrição de isolamento de aplicativos Android comuns, não algo a contornar.
- Apps com assinatura/attestation, DRM, componentes remotos, Play Integrity, bancos e alguns split APKs podem não abrir após clonagem.
- Android pode exibir o clone no menu de **Definições > Aplicativos**, mesmo sem ícone no ecrã inicial. Isso é esperado.
- O modo Pro real, com pagamento e recuperação de compras, exige entitlement remoto e fica para etapa futura.
- O armazenamento local do catálogo desaparece ao limpar dados/desinstalar o host, enquanto os APKs clonados independentes podem permanecer. Por isso **não desinstalar o host diretamente sem antes usar a remoção guiada**.
- Os atalhos dependem de suporte do launcher e confirmação do utilizador; não são colocados silenciosamente.

## Auditoria de build
O GitHub Actions verifica compilação, JUnit, assinatura APK v2, `minSdk`, identificador `com.hatusjc.androidappcloner.preview`, ABI ARM64 e SHA-256. Nenhum desses testes substitui a execução física.

## Pendências futuras
[docs/IMPLEMENTACOES_FUTURAS.md](IMPLEMENTACOES_FUTURAS.md) contém os ícones independentes dos clones Pro, licença comercial e melhorias de compatibilidade.
