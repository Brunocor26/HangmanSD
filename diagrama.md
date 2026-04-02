# Diagrama de Fluxo — Hangman Multijogador

```mermaid
flowchart TB

    subgraph COMMON["src.common"]
        P["Protocol
        ───────────────
        PORT · MAX_PLAYERS · MIN_PLAYERS
        MAX_ATTEMPTS · LOBBY_TIMEOUT_MS · ROUND_TIMEOUT_MS
        ───────────────
        welcome() · start() · round()
        state() · endWin() · endLose() · guess()"]
    end

    subgraph SERVER["src.server"]
        direction TB

        HW["HangmanWords
        ───────────────
        WORDS[ ] · getRandomWord()"]

        HS["HangmanServer
        ───────────────
        start()
        aceita ligações TCP :11111
        lança threads dos jogadores"]

        STATE["HangmanState  ⟨partilhado⟩
        ───────────────
        word · placeholder · attemptsLeft
        usedLetters · round · finished"]

        CH["ClientHandler  ×N
        ───────────────
        sendWelcome() · sendStart()
        sendRound() · sendState()
        sendEndWin() · sendEndLose()
        waitGuess() · setRoundTimeout()"]

        HS -->|"① getRandomWord()"| HW
        HW -->|palavra sorteada| HS
        HS -->|"② new HangmanState(word)"| STATE
        HS -->|"③ new Thread por jogador"| CH
        CH <-->|"lê / escreve\nsynchronized"| STATE
    end

    subgraph CLIENT["src.client"]
        HC["HangmanClient
        ───────────────
        start(host)
        handleMessage()
        printState() · printHangman()
        sendGuess()"]
    end

    %% -> Protocol usado por todos
    P -.->|"constantes\nPORT · MAX_PLAYERS ..."| HS
    P -.->|"builders\nwelcome() · round() ..."| CH
    P -.->|"prefixos\nWELCOME · ROUND ..."| HC

    %% -> Comunicação TCP
    CH -->|"④  WELCOME  START  ROUND  STATE  END WIN/LOSE"| HC
    HC -->|"GUESS  letra / palavra"| CH
```

---

## Fluxo de mensagens TCP

```mermaid
sequenceDiagram
    actor J as Jogador
    participant HC as HangmanClient
    participant CH as ClientHandler
    participant STATE as HangmanState

    J->>HC: liga (java HangmanClient)
    HC->>CH: TCP connect :11111

    Note over CH: aguarda MIN_PLAYERS<br/>ou LOBBY_TIMEOUT

    CH-->>HC: WELCOME &lt;id&gt; &lt;total&gt;

    loop cada ronda
        CH-->>HC: ROUND &lt;k&gt; &lt;mask&gt; &lt;attempts&gt; &lt;usedLetters&gt;
        HC->>J: mostra estado + forca
        J->>HC: escreve letra ou palavra
        HC->>CH: GUESS &lt;texto&gt;
        CH->>STATE: valida guess · atualiza estado
        CH-->>HC: STATE &lt;mask&gt; &lt;attempts&gt; &lt;usedLetters&gt;
    end

    alt palavra adivinhada
        CH-->>HC: END WIN &lt;winnerIds&gt; &lt;word&gt;
    else tentativas esgotadas
        CH-->>HC: END LOSE &lt;word&gt;
    end
```

---

## Legenda das classes

| Classe | Pacote | Responsabilidade |
|---|---|---|
| `HangmanServer` | `src.server` | Arranca o servidor, aceita jogadores, lança threads |
| `ClientHandler` | `src.server` | Thread por jogador — envia/recebe mensagens, acede ao estado |
| `HangmanState` | `src.server` | Estado partilhado e sincronizado do jogo |
| `HangmanWords` | `src.server` | Banco de palavras, sorteia palavra aleatória |
| `Protocol` | `src.common` | Constantes e builders de mensagens do protocolo |
| `HangmanClient` | `src.client` | Cliente de terminal — mostra jogo e lê input do jogador |
