# Variáveis
BIN = bin
SRC = src
LIB = lib/*
CP = "$(LIB):."
JUNIT_JAR = lib/junit-platform-console-standalone.jar

# Alvo padrão: compila tudo
all: compile

compile:
	@mkdir -p $(BIN)
	javac -d $(BIN) -cp $(CP) $(SRC)/common/*.java $(SRC)/server/*.java $(SRC)/client/*.java $(SRC)/test/*.java
	@echo "Compilação concluída com sucesso em $(BIN)/"

# Limpa os ficheiros compilados
clean:
	rm -rf $(BIN)/*
	@echo "Limpeza concluída."

# Corre os testes com a consola standalone do JUnit 5
test: compile
	java -jar $(JUNIT_JAR) -cp "$(BIN):$(LIB)" --scan-class-path --reports-dir=reports

# Atalhos para correr o Servidor e o Cliente
IP ?= localhost

run-server:
	java -cp "$(BIN):$(LIB)" src.server.HangmanServer

run-client:
	java -cp "$(BIN):$(LIB)" src.client.HangmanClient $(IP)
