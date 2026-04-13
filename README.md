# Jogo da Forca Multijogador — Sistemas Distribuídos

Trabalho Prático 1 da Unidade Curricular de Sistemas Distribuídos do 3º ano de Engenharia Informática, UBI.

## Descrição

Implementação de um **Jogo da Forca multijogador síncrono**, onde 2 a 4 jogadores participam em simultâneo para descobrir uma palavra oculta comum. A aplicação segue uma arquitetura **cliente-servidor** com comunicação via **TCP/IP**.

Este projeto utiliza a biblioteca externa `myinputs` para a leitura de dados no terminal do cliente.

## Estrutura do Repositório

```
.
├── src/
│   ├── server/
│   │   ├── HangmanServer.java      # Servidor principal (ponto de entrada)
│   │   ├── ClientHandler.java      # Ligação e I/O de cada jogador
│   │   ├── HangmanState.java       # Estado partilhado do jogo (thread-safe)
│   │   └── HangmanWords.java       # Lista de 140 palavras e seleção aleatória
│   ├── client/
│   │   └── HangmanClient.java      # Cliente de terminal (ponto de entrada)
│   ├── common/
│   │   └── Protocol.java           # Constantes e builders do protocolo
│   └── test/
│       ├── HangmanStateTest.java   # Testes unitários da lógica de jogo
│       ├── ProtocolTest.java       # Testes unitários do protocolo
│       └── HangmanWordsTest.java   # Testes unitários da lista de palavras
├── lib/
│   ├── junit-platform-console-standalone.jar  # Para testes JUnit 5
│   └── myinputs.jar                           # Biblioteca para leitura de dados
├── Makefile                     # Automação de compilação e execução
├── diagrama.md
└── README.md
```

## Requisitos

- **Java** 11 ou superior
- **Make** (opcional, para usar o Makefile)
- Bibliotecas em `lib/` (`myinputs.jar` e `junit-platform-console-standalone.jar`)

## Compilação e Execução (Via Makefile)

A forma mais simples de gerir o projeto é através do `Makefile` incluído na raiz.

### Compilar tudo
```bash
make
```

### Iniciar o Servidor
```bash
make run-server
```

### Iniciar o Cliente
```bash
make run-client
```

### Limpar ficheiros compilados
```bash
make clean
```

---

## Regras do Jogo

- O servidor seleciona aleatoriamente uma palavra de uma lista de **140 palavras** distribuídas por 14 categorias (animais, países, desportos, alimentos, profissões, objetos, natureza, cores, transportes, tecnologia, plantas, instrumentos, planetas, materiais).
- Participam entre **2 e 4 jogadores** por sessão.
- O jogo inicia quando estão ligados pelo menos 2 jogadores, ou quando expira o timeout do lobby (~20 s) após a entrada do primeiro jogador.
- Cada jogador tem **30 segundos** por ronda para submeter uma jogada.
- Uma jogada pode ser:
  - Uma **letra** (ex: `a`, `M`)
  - A **palavra completa** (ex: `brasil`)
- A comparação é **case-insensitive**.
- O jogador começa com **6 tentativas**.
- Uma tentativa é consumida quando:
  - A letra não existe na palavra.
  - A palavra proposta está incorreta.
  - O jogador não responde dentro do tempo limite (timeout).
- Letra já tentada anteriormente **não consome tentativa**.
- **Concorrência:** Se múltiplos jogadores acertarem na palavra ou na mesma letra correta na mesma ronda, todos são considerados vencedores dessa jogada.
- O jogo termina com **vitória** se algum jogador adivinhar a palavra, ou com **derrota** se as tentativas chegarem a 0.

## Protocolo de Comunicação

### Servidor -> Cliente

| Mensagem | Exemplo | Descrição |
|---|---|---|
| `WELCOME <id> <total>` | `WELCOME 1 3` | Identificação do jogador |
| `START <mask> <attempts> <timeout_ms>` | `START ______ 6 30000` | Início do jogo |
| `ROUND <k> <mask> <attempts> <used>` | `ROUND 2 _A___ 5 A E` | Início de nova ronda |
| `STATE <mask> <attempts> <used>` | `STATE _A___ 4 A E I` | Atualização após ronda |
| `END WIN <ids> <word>` | `END WIN 1,3 BRASIL` | Fim — vitória |
| `END LOSE <word>` | `END LOSE BRASIL` | Fim — derrota |
| `FULL` | `FULL` | Servidor cheio |

### Cliente -> Servidor

| Mensagem | Exemplo |
|---|---|
| `GUESS <texto>` | `GUESS a` ou `GUESS brasil` |

## Grupo

| Nome | Nº de Estudante |
|---|---|
| Bruno Correia | 51741 |
| Henrique Laia | 51667 |
| Francisco Branco | 51829 |
