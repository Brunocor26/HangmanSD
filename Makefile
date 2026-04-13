# Variáveis
BIN = bin
SRC = src
LIB = lib/*
CP = "$(LIB):$(SRC)"

# Alvo padrão: compila tudo
all: compile

compile:
	@mkdir -p $(BIN)
	javac -d $(BIN) -cp $(CP) $(SRC)/common/*.java $(SRC)/server/*.java $(SRC)/client/*.java
	@echo "Compilação concluída com sucesso em $(BIN)/"

# Limpa os ficheiros compilados
clean:
	rm -rf $(BIN)/*
	@echo "Limpeza concluída."

# Atalhos para correr o Servidor e o Cliente
run-server:
	java -cp "$(BIN):$(LIB)" src.server.HangmanServer

run-client:
	java -cp "$(BIN):$(LIB)" src.client.HangmanClient localhost
