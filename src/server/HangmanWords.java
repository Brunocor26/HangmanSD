package src.server;

import java.util.Random;

public class HangmanWords {
    private static final String[] WORDS = {
        // Animais
        "GATO", "CACAO", "LEAO", "TIGRE", "ZEBRA",
        "URSO", 
        // Países
        "BRASIL", "FRANCA", "JAPAO", "CANADA", "INDIA",
        "ANGOLA", 
        // Desportos
        "FUTEBOL", "TENIS", "NATACAO", "BOXE", "CICLISMO",
        "VOLEI",
        // Alimentos
        "PIZZA", "MASSA", "ARROZ", "FEIJAO", "SOPA",
        "BACALHAU", 
        // Profissões
        "MEDICO", "PILOTO", "PINTOR", "MUSICO", "JUIZ",
        "ATOR", "CHEF",
        // Objetos
        "MESA", "CADEIRA", "JANELA", "ESPELHO", "LAMPADA",
        "TECLADO", "MONITOR", 
        // Natureza
        "MONTANHA", "FLORESTA", "OCEANO", "DESERTO", "VULCAO",
        "GLACIAR", "PANTANO",
        // Cores
        "VERMELHO", "AZUL", "VERDE", "AMARELO", "LARANJA",
        "ROXO", "ROSA", "CINZENTO",
        // Transportes
        "AVIAO", "COMBOIO", "BARCO", "MOTA", "HELICOPTERO",
        "SUBMARINO", "TRATOR", "AUTOCARRO",
        // Tecnologia
        "SERVIDOR", "REDE", "PROTOCOLO", "SOCKET", "THREAD",
        "ALGORITMO", "SOFTWARE", "COMPILADOR",
        // Plantas
        "LOUREIRO", "TULIPA", "ORQUIDEA", "CACTO", "BAMBU",
        "PINHEIRO", "CARVALHO", "OLIVEIRA",
        // Instrumentos
        "GUITARRA", "PIANO", "VIOLINO", "FLAUTA", "BATERIA",
        "TROMPETE", "SAXOFONE", "HARPA",
        // Planetas
        "MERCURIO", "VENUS", "MARTE", "JUPITER", "SATURNO",
        "URANO", "TERRA", "SOL",
        // Materiais
        "MADEIRA", "METAL", "VIDRO", "PEDRA", "PLASTICO",
        "BORRACHA", "TECIDO"
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
