package src.test;

import org.junit.jupiter.api.Test;
import src.server.HangmanWords;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HangmanWordsTest {

    // Tamanho e cobertura

    @Test
    void sizeAtLeast100() {
        assertTrue(HangmanWords.size() >= 100,
                "Esperado >= 100 palavras, encontrado: " + HangmanWords.size());
    }

    @Test
    void noDuplicates() {
        String[] words = HangmanWords.getWords();
        Set<String> unique = new HashSet<>(Arrays.asList(words));
        assertEquals(words.length, unique.size(),
                "Existem " + (words.length - unique.size()) + " palavra(s) duplicada(s)");
    }

    // Formato das palavras

    @Test
    void allWordsUppercase() {
        for (String word : HangmanWords.getWords()) {
            assertEquals(word.toUpperCase(), word,
                    "Palavra não está em maiúsculas: " + word);
        }
    }

    @Test
    void allWordsOnlyLetters() {
        for (String word : HangmanWords.getWords()) {
            assertTrue(word.matches("[A-Z]+"),
                    "Palavra com carateres inválidos (acentos?): " + word);
        }
    }

    @Test
    void allWordsMinLength3() {
        for (String word : HangmanWords.getWords()) {
            assertTrue(word.length() >= 3,
                    "Palavra demasiado curta: " + word);
        }
    }

    @Test
    void allWordsNotEmpty() {
        for (String word : HangmanWords.getWords()) {
            assertFalse(word.isBlank(), "Palavra vazia ou só espaços encontrada");
        }
    }

    // getRandomWord

    @Test
    void randomWordNotNull() {
        assertNotNull(HangmanWords.getRandomWord());
    }

    @Test
    void randomWordNotEmpty() {
        assertFalse(HangmanWords.getRandomWord().isBlank());
    }

    @Test
    void randomWordExistsInList() {
        String random = HangmanWords.getRandomWord();
        Set<String> all = new HashSet<>(Arrays.asList(HangmanWords.getWords()));
        assertTrue(all.contains(random),
                "Palavra sorteada não existe na lista: " + random);
    }

    @Test
    void randomWordProducesVariety() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 500; i++)
            seen.add(HangmanWords.getRandomWord());
        assertTrue(seen.size() >= 10,
                "Pouca variedade nas palavras sorteadas: apenas " + seen.size() + " distintas em 500 tentativas");
    }

    // getWords devolve cópia (não a referência interna)

    @Test
    void getWordsReturnsCopy() {
        String[] words1 = HangmanWords.getWords();
        String[] words2 = HangmanWords.getWords();
        assertNotSame(words1, words2);
    }
}
