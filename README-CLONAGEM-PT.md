# Clonagem de apps no Android — edição comunitária de teste (BFE)

Este repositório é um **fork** de [The412Banner/BFE](https://github.com/The412Banner/BFE), mantido sob **GPL-3.0**. O software permanece aberto e gratuito; **não existe licença premium para desbloquear**. Não há associação ou endosso do projeto original ao fork.

## Funções existentes

- Clonar um APK guardado no telefone, atribuindo nome de pacote diferente para instalar uma segunda cópia.
- Escolher uma aplicação instalada para tentar cloná-la; o projeto inclui combinação de instalações divididas (split APKs).
- Alterar rótulo, versão, ícone e outros parâmetros na operação de clonagem, conforme a interface original.
- Assinar o clone com uma chave gerada dentro do aplicativo ou importada pelo utilizador; instalar após confirmação do Android.
- Outras funções herdadas do BFE: gestão de ficheiros, compressão, extração e conversão de vídeo.

**Limites importantes:** apps bancários, autenticação forte, Google Play Integrity, DRM, assinatura/certificado verificados pelo servidor, Firebase/Google Services e plugins fornecidos sob demanda **podem não funcionar depois da clonagem**. Não existe forma garantida de contornar essas limitações. Não use um APK de origem desconhecida.

## Redmi A5 (25028RN03Y), Android 15 Go

- A variante Android compilada é **arm64-v8a**, minSdk 26 (Android 8 ou posterior).
- O clonador **não depende do Work Profile**, que pode estar indisponível no Android Go.
- O projeto herdado foi compilado com **targetSdk 28**, que ainda permite instalação no Android 15, mas implica compatibilidade legada e precisa de futura atualização controlada.
- **Teste físico no Redmi A5 ainda pendente**. Uma compilação assinada e verificada não equivale a teste de execução, segurança em produção ou garantia de que cada app funcionará.
- O aplicativo solicita acesso amplo a ficheiros para as funções herdadas de arquivo. Autorize-o apenas se tiver confiança no código e na origem da compilação.

## Descarregar e instalar

1. Abra [Releases](https://github.com/hatusjc/android-app-cloner/releases) e escolha a versão mais recente deste fork, quando publicada.
2. Baixe o APK **arm64** para o telefone. Confira o nome e o SHA-256 apresentados nas notas da versão.
3. Permita temporariamente a instalação a partir do navegador/gestor de ficheiros e confirme o instalador do Android.
4. No BFE, selecione **Clone installed app…** ou num APK escolha **Clone APK…**, defina um novo nome de pacote e use **Clone & Install**.
5. Instale o clone após a confirmação do Android. Mantenha o APK original e dados importantes intactos até confirmar que o clone funciona.
6. Desative a permissão para instalar apps desconhecidos quando não for necessária.

## Assinatura e transparência

- O ID da aplicação desta edição é `com.hatusjc.androidappcloner` para não colidir com o BFE original.
- APKs de distribuição são assinados com certificado **privado**, guardado como segredo de GitHub Actions. A chave pública de teste do upstream **não** assina esta edição.
- A workflow `.github/workflows/build.yml` executa build debug, testes unitários, build release, validação de assinaturas APK v2/v3, verificação de package/minSdk/ABI e cálculo SHA-256.
- Os arquivos de evidência do build ficam nos artefatos da execução de GitHub Actions.
- A chave privada de **assinar o aplicativo BFE** é diferente da chave de **assinar clones**, criada por cada instalação do BFE. Evite perder os dados/chave usados para assinar clones já instalados.
- As releases públicas são destinadas a testes, não a garantir compatibilidade com apps financeiros/sensíveis.

Consulte [AUDITORIA_ANDROID_15_GO.md](AUDITORIA_ANDROID_15_GO.md) para limites e testes necessários.
