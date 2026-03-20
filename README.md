# Jogo da Forca Multijogador — Sistemas Distribuídos

Trabalho Prático 1 da Unidade Curricular de Sistemas Distribuídos do 3º ano de Engenharia Informática, UBI.

## Descrição

Implementação de um **Jogo da Forca multijogador síncrono**, onde 2 a 4 jogadores participam em simultâneo para descobrir uma palavra oculta comum. A aplicação segue uma arquitetura **cliente-servidor** com comunicação via **TCP/IP**.

## Estrutura do Repositório

```
.
├── src/
│   ├── server/
│   │   └── HangmanServer.java      # Servidor principal
│   └── client/
│       └── HangmanClient.java      # Cliente
├── words.txt                        # Lista de 100 palavras
├── README.md
└── report/
    └── report.pdf                   # Relatório técnico (LaTeX → PDF)
```

## Requisitos

- **Java** 11 ou superior
- Nenhuma dependência externa

## Compilação

## Execução

### Iniciar o Servidor

### Iniciar um Cliente

## Regras do Jogo

- O servidor seleciona aleatoriamente uma palavra de uma lista de 100 palavras.
- Participam entre **2 e 4 jogadores** por sessão.
- O jogo inicia quando estão ligados pelo menos 2 jogadores, ou quando expira o timeout do lobby (~20 s) após a entrada do primeiro jogador.
- Cada jogador tem `N` segundos por ronda para submeter uma jogada.
- Uma jogada pode ser:
  - Uma **letra** (ex: `a`, `M`)
  - A **palavra completa** (ex: `Marty`)
- A comparação é **case-insensitive**.
- Uma tentativa é consumida quando:
  - A letra não existe na palavra;
  - A palavra proposta está incorreta;
  - O jogador não responde dentro do tempo limite.
- O jogo termina com **vitória** se algum jogador adivinhar a palavra, ou com **derrota** se as tentativas chegarem a 0.

## Protocolo de Comunicação

### Servidor -> Cliente

| Mensagem | Descrição |
|---|---|
| `WELCOME <id> <players_total>` | Identificação do jogador |
| `START <mask> <attempts> <round_timeout_ms>` | Início do jogo |
| `ROUND <k> <mask> <attempts> <used_letters>` | Início de nova ronda |
| `STATE <mask> <attempts> <used_letters>` | Atualização do estado |
| `END WIN <winner_ids> <word>` | Fim do jogo — vitória |
| `END LOSE <word>` | Fim do jogo — derrota |
| `FULL` | Servidor cheio |

### Cliente -> Servidor

| Mensagem | Exemplo |
|---|---|
| `GUESS <text>` | `GUESS a` ou `GUESS Marty` |

## Grupo

| Nome | Nº de Estudante |
|Bruno Correia|51741|
|Henrique Laia|51667|
|Francisco Branco|51829|
