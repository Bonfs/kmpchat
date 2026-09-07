# KmpChat — Cliente Matrix Multiplataforma Seguro

KmpChat é uma Prova de Conceito (PoC) de um cliente de mensagens instantâneas descentralizado e seguro baseado no protocolo **Matrix**, construído com **Kotlin Multiplatform (KMP)**, **Compose Multiplatform** e **Trixnity SDK**.

---

## Features

### 1. Autenticação Segura & Gestão de Sessão
* **Login com Credenciais:** Conexão a qualquer servidor Matrix federado (e.g. `https://matrix.org`) utilizando identificador de usuário e senha.
* **Restauração Automática de Sessão:** Detecção e inicialização instantânea da sessão ativa salva no armazenamento local criptografado ao reabrir o aplicativo.
* **Logout & Sanitização Local (Wipe):** Encerramento seguro da sessão com revogação de chaves no servidor e destruição completa dos bancos de dados locais e chaves do cofre.

### 2. Criptografia Ponta a Ponta (E2EE) com Vodozemac
* **Criptografia Nativa:** Integração com o driver criptográfico Rust **Vodozemac** para gerenciamento de chaves e sessões Olm/Megolm.
* **Indicadores Criptográficos:** Identificação visual explícita com selo `🔒 E2EE` na barra de títulos da sala e nos balões de mensagens cifradas.

### 3. Gerenciamento de Chaves em Cofres Nativos de Hardware
* **Android:** Geração e armazenamento seguro da chave de 256 bits via `AndroidKeyStore` respaldado pelo ambiente de execução confiável (TEE / StrongBox).
* **iOS:** Armazenamento seguro da chave no `Keychain Services` com acessibilidade `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`.
* **Desktop JVM:** Cofre de chaves isolado com limpeza explícita de arrays na memória após a utilização.

### 4. Armazenamento Local Cifrado (SQLCipher Native Driver)
* **Proteção em Repouso:** Banco de dados de salas e eventos protegido integralmente em repouso por chave AES-256 aplicada via `PRAGMA key` no driver nativo do Room.
* **Recuperação Automática de Integridade:** Rotina preventiva que detecta corrupção ou incompatibilidade de chaves/dispositivos legados e reconfigura o armazenamento de forma resiliente.

### 5. Lista de Conversas & Gestão de Convites
* **Listagem Reativa de Salas:** Observação em tempo real do estado de sincronização e fluxo de conversas ativas.
* **Central de Convites:** Seção destacada para aceitar (`join`) ou recusar/ignorar (`leave`) convites recebidos para novas salas.
* **Criação de Conversas Diretas (DMs):** Modal para iniciar novas conversas diretas individuais a partir do Matrix ID do destinatário (`@usuario:servidor`).

### 6. Timeline do Chat & Envio de Mensagens
* **Linha do Tempo em Tempo Real:** Renderização cronológica de mensagens com paginação e auto-scroll ao receber novas mensagens.
* **Balões Direcionais:** Balões de conversa distintos para mensagens enviadas (Midnight Cobalt) e recebidas (Midnight Surface) com timestamps formatados.
* **Envio Criptografado:** Despacho de mensagens de texto simples criptografadas de ponta a ponta.

### 7. Design System Midnight Cobalt & Layout Adaptativo
* **Identidade Visual *Midnight Cobalt*:** Paleta escura de alto contraste (`MidnightBackground #0B0F19`, `MidnightSurface #151D2E`, `MidnightPrimary #3B82F6`, `MidnightE2EE #22D3EE`).
* **Layout Adaptativo Multiplataforma:**
  * **Mobile (< 720dp):** Stack Navigation tradicional entre telas de Login, Conversas e Chat.
  * **Desktop / Telas Largas (>= 720dp):** Master-Detail (Split-View) com listagem de conversas fixa à esquerda (340dp) e painel principal de leitura/envio à direita.

---

## Arquitetura & Tecnologias

* **Linguagem & Plataforma:** Kotlin Multiplatform 2.4.10, Compose Multiplatform 1.11.1
* **Matrix SDK:** Trixnity BOM 5.8.0 (`trixnity-client`, `trixnity-client-cryptodriver-vodozemac`, `trixnity-client-repository-room`)
* **Injeção de Dependências:** Koin 4.0.2 (`koin-core`, `koin-compose`)
* **Navegação:** Navigation Compose 2.9.0
* **Logging:** Kermit 2.0.5
* **Padrão de Apresentação:** Model-View-Intent (MVI) com `StateFlow` e `SharedFlow`

---

## Como Executar

### Android
```bash
./gradlew :androidApp:assembleDebug
```
O APK gerado estará em `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

### Desktop (JVM)
```bash
./gradlew :desktopApp:run
```

### iOS
Abra o diretório `iosApp` no Xcode e execute no simulador ou dispositivo físico:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

---

## Execução dos Testes

Para executar toda a suíte de testes unitários e de integração:

```bash
# Testes JVM compartilhados (Use Cases, ViewModels, Criptografia, Repositórios)
./gradlew :shared:jvmTest

# Testes Android Host
./gradlew :shared:testAndroidHostTest
```