package src.server;

import java.util.Random;

public class HangmanWords {
    // TODO: A lista de palavras tem 140 elementos. O requisito pede uma lista fixa de exatamente 100 palavras.
    private static final String[] WORDS = {
        // Animais
        "GATO", "CACAO", "LEAO", "TIGRE", "ZEBRA",
        "URSO", "LOBO", "AGUIA", "COBRA", "PEIXE",
        // Países
        "BRASIL", "FRANCA", "JAPAO", "CANADA", "INDIA",
        "ANGOLA", "RUSSIA", "EGITO", "CHILE", "PERU",
        // Desportos
        "FUTEBOL", "TENIS", "NATACAO", "BOXE", "CICLISMO",
        "VOLEI", "RUGBY", "GOLFE", "HIPISMO", "JUDO",
        // Alimentos
        "PIZZA", "MASSA", "ARROZ", "FEIJAO", "SOPA",
        "BACALHAU", "FRANGO", "TOMATE", "CEBOLA", "ALHO",
        // Profissões
        "MEDICO", "PILOTO", "PINTOR", "MUSICO", "JUIZ",
        "ATOR", "CHEF", "ARQUITETO", "DENTISTA", "FARMACEUTICO",
        // Objetos
        "MESA", "CADEIRA", "JANELA", "ESPELHO", "LAMPADA",
        "TECLADO", "MONITOR", "TELEFONE", "RELOGIO", "MOCHILA",
        // Natureza
        "MONTANHA", "FLORESTA", "OCEANO", "DESERTO", "VULCAO",
        "GLACIAR", "PANTANO", "SAVANA", "TUNDRA", "RECIFE",
        // Cores
        "VERMELHO", "AZUL", "VERDE", "AMARELO", "LARANJA",
        "ROXO", "ROSA", "CINZENTO", "BRANCO", "CASTANHO",
        // Transportes
        "AVIAO", "COMBOIO", "BARCO", "MOTA", "HELICOPTERO",
        "SUBMARINO", "TRATOR", "AUTOCARRO", "BICICLETA", "FOGUETE",
        // Tecnologia
        "SERVIDOR", "REDE", "PROTOCOLO", "SOCKET", "THREAD",
        "ALGORITMO", "SOFTWARE", "HARDWARE", "COMPILADOR", "MEMORIA",
        // Plantas
        "LOUREIRO", "TULIPA", "ORQUIDEA", "CACTO", "BAMBU",
        "PINHEIRO", "CARVALHO", "OLIVEIRA", "GIRASSOL", "MARGARIDA",
        // Instrumentos
        "GUITARRA", "PIANO", "VIOLINO", "FLAUTA", "BATERIA",
        "TROMPETE", "SAXOFONE", "HARPA", "CLARINETE", "ACORDEAO",
        // Planetas
        "MERCURIO", "VENUS", "MARTE", "JUPITER", "SATURNO",
        "URANO", "NEPTUNO", "PLUTAO", "TERRA", "SOL",
        // Materiais
        "MADEIRA", "METAL", "VIDRO", "PEDRA", "PLASTICO",
        "BORRACHA", "TECIDO", "CERAMICA", "CIMENTO", "BRONZE"
    };

    private static final Random RANDOM = new Random();

    public static String getRandomWord() {
        return WORDS[RANDOM.nextInt(WORDS.length)];
    }

    public static int size() {
        return WORDS.length;
    }

    public static String[] getWords() {
        return WORDS.clone();
    }
}
