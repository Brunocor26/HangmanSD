# variaveis
BIN = bin
SRC = src
LIB = lib/*
CP = "$(LIB):."
JUNIT_JAR = lib/junit-platform-console-standalone.jar

# padrao-> compilar
all: compile

compile:
	@mkdir -p $(BIN)
	javac -d $(BIN) -cp $(CP) $(SRC)/common/*.java $(SRC)/server/*.java $(SRC)/client/*.java $(SRC)/test/*.java
	@echo "Compilação concluída com sucesso em $(BIN)/"

clean:
	rm -rf $(BIN)/*
	@echo "Limpeza concluída."

# corre testes junit
test: compile
	java -jar $(JUNIT_JAR) -cp "$(BIN):$(LIB)" --scan-class-path --reports-dir=reports

# atalhos para correr servidor e cliente (cliente tem opcao de meter ip, por omissao é localhost)
# para permitir usar "make run-client <ip>"
ifeq (run-client,$(firstword $(MAKECMDGOALS)))
  CLIENT_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  $(eval $(CLIENT_ARGS):;@:)
endif

IP ?= localhost
ifneq ($(CLIENT_ARGS),)
  IP = $(CLIENT_ARGS)
endif

run-server:
	java -cp "$(BIN):$(LIB)" src.server.HangmanServer

run-client:
	java -cp "$(BIN):$(LIB)" src.client.HangmanClient $(IP)
