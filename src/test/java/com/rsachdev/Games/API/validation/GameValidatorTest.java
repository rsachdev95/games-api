package com.rsachdev.Games.API.validation;

import com.rsachdev.Games.API.model.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameValidatorTest {
    private static final String ID = "id";
    private static final String DEVELOPER_NAME = "developer";
    private static final String TITLE = "title";

    private GameValidator gameValidator = new GameValidator();

    @Test
    @DisplayName("Test game is valid")
    void testGameValid() {
        Game game = createGame();

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(Collections.emptyList(), errors);
    }

    @Test
    @DisplayName("Test game invalid - null title")
    void testGameInvalidNullTitle() {
        Game game = createGame();
        game.setTitle(null);

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - null developer")
    void testGameInvalidNullDeveloper() {
        Game game = createGame();
        game.setDeveloper(null);

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - null title and null developer")
    void testGameInvalidNullTitleAndNullDeveloper() {
        Game game = createGame();
        game.setDeveloper(null);
        game.setTitle(null);

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(2, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - empty title")
    void testGameInvalidEmptyTitle() {
        Game game = createGame();
        game.setTitle("");

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - empty developer")
    void testGameInvalidEmptyDeveloper() {
        Game game = createGame();
        game.setDeveloper("");

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - empty title and empty developer")
    void testGameInvalidEmptyTitleAndEmptyDeveloper() {
        Game game = createGame();
        game.setDeveloper("");
        game.setTitle("");

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(2, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - whitespace title")
    void testGameInvalidWhitespaceTitle() {
        Game game = createGame();
        game.setTitle(" ");

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - whitespace developer")
    void testGameInvalidWhitespaceDeveloper() {
        Game game = createGame();
        game.setDeveloper(" ");

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Test game invalid - whitespace title and whitespace developer")
    void testGameInvalidWhitespaceTitleAndWhitespaceDeveloper() {
        Game game = createGame();
        game.setDeveloper(" ");
        game.setTitle(" ");

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        assertEquals(2, errors.size());
    }

    private Game createGame() {
        Game game = new Game();
        game.setDeveloper(DEVELOPER_NAME);
        game.setTitle(TITLE);
        game.setId(ID);
        return game;
    }
}
