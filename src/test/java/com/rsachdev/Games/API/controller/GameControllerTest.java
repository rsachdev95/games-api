package com.rsachdev.Games.API.controller;

import com.rsachdev.Games.API.exception.DataException;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.model.Games;
import com.rsachdev.Games.API.service.GameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class GameControllerTest {
    private static final String ID = "id";

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    @Test
    @DisplayName("Test successful retrieval of game by id")
    void getGameByIdSuccessful() throws DataException {
        Game game = createGame();
        when(gameService.getById(ID)).thenReturn(game);

        ResponseEntity response = gameController.fetch(ID);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(game, response.getBody());
    }

    @Test
    @DisplayName("Test unsuccessful retrieval of game by id - not found")
    void getGameByIdUnsuccessfulNotFound() throws DataException {
        when(gameService.getById(ID)).thenReturn(null);

        ResponseEntity response = gameController.fetch(ID);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Test unsuccessful retrieval of game by id - DataException")
    void getGameByIdUnsuccessfulDataException() throws DataException {
        when(gameService.getById(ID)).thenThrow(DataException.class);

        ResponseEntity response = gameController.fetch(ID);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Tests successful retrieval of all games")
    void listAllGamesSuccessful() throws DataException {
        Games games = createGames();
        when(gameService.listAllGames()).thenReturn(games);

        ResponseEntity response = gameController.listAll();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(games, response.getBody());
    }

    @Test
    @DisplayName("Tests unsuccessful retrieval of all games - not found")
    void listAllGamesUnsuccessfulNotFound() throws DataException {
        when(gameService.listAllGames()).thenReturn(null);

        ResponseEntity response = gameController.listAll();
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Test unsuccessful retrieval of all games - DataException")
    void listAllGamesUnsuccessfulDataException() throws DataException {
        when(gameService.listAllGames()).thenThrow(DataException.class);

        ResponseEntity response = gameController.listAll();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    private Game createGame() {
        Game game = new Game();
        game.setDeveloper("developer");
        game.setTitle("title");
        game.setId(ID);
        return game;
    }

    private Games createGames() {
        Games games = new Games();
        List<Game> gamesList = new ArrayList<>();
        games.setItemsPerPage(2L);
        games.setStartIndex(1L);
        games.setTotalResults(3L);
        games.setItems(gamesList);
        gamesList.add(createGame());
        return games;
    }
}
