package com.rsachdev.Games.API.controller;

import com.mongodb.DuplicateKeyException;
import com.rsachdev.Games.API.exception.ResourceNotFoundException;
import com.rsachdev.Games.API.exception.ServiceException;
import com.rsachdev.Games.API.exception.UnauthorisedDeveloperException;
import com.rsachdev.Games.API.exception.ValidationException;
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

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class GameControllerTest {
    private static final String ID = "id";
    private static final String DEVELOPER = "developer";
    private static final String TITLE = "title";
    private static final String REQUEST_URI = "/games";
    private static final String START_INDEX = "0";
    private static final String ITEMS_PER_PAGE = "10";
    private static final URI LOCATION_HEADER = URI.create(REQUEST_URI + "/" + ID);

    @Mock
    private GameService gameService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GameController gameController;

    @Test
    @DisplayName("Test successful retrieval of game by id")
    void getGameByIdSuccessful() throws ServiceException, ResourceNotFoundException {
        Game game = createGame();
        when(gameService.getById(ID)).thenReturn(game);

        ResponseEntity response = gameController.fetch(ID);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(game, response.getBody());
    }

    @Test
    @DisplayName("Test unsuccessful retrieval of game by id - not found")
    void getGameByIdUnsuccessfulNotFound() throws ServiceException, ResourceNotFoundException {
        when(gameService.getById(ID)).thenThrow(ResourceNotFoundException.class);

        ResponseEntity response = gameController.fetch(ID);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Test unsuccessful retrieval of game by id - ServiceException")
    void getGameByIdUnsuccessfulDataException() throws ServiceException, ResourceNotFoundException {
        when(gameService.getById(ID)).thenThrow(ServiceException.class);

        ResponseEntity response = gameController.fetch(ID);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Tests successful retrieval of all games")
    void listAllGamesSuccessful() throws ServiceException {
        Games games = createGames();
        when(gameService.listAllGames(START_INDEX, ITEMS_PER_PAGE)).thenReturn(games);

        ResponseEntity response = gameController.listAll(START_INDEX, ITEMS_PER_PAGE);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(games, response.getBody());
    }

    @Test
    @DisplayName("Tests unsuccessful retrieval of all games - not found")
    void listAllGamesUnsuccessfulNotFound() throws ServiceException {
        Games games = createGames();
        games.setTotalResults(0);
        when(gameService.listAllGames(START_INDEX, ITEMS_PER_PAGE)).thenReturn(games);

        ResponseEntity response = gameController.listAll(START_INDEX, ITEMS_PER_PAGE);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Tests unsuccessful retrieval of all games - not found - empty list")
    void listAllGamesUnsuccessfulNotFoundEmptyList() throws ServiceException {
        Games games = createGames();
        games.setItems(Collections.emptyList());
        when(gameService.listAllGames(START_INDEX, ITEMS_PER_PAGE)).thenReturn(games);

        ResponseEntity response = gameController.listAll(START_INDEX, ITEMS_PER_PAGE);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    @DisplayName("Test unsuccessful retrieval of all games - ServiceException")
    void listAllGamesUnsuccessfulDataException() throws ServiceException {
        when(gameService.listAllGames(START_INDEX, ITEMS_PER_PAGE)).thenThrow(ServiceException.class);

        ResponseEntity response = gameController.listAll(START_INDEX, ITEMS_PER_PAGE);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Test successful create of game")
    void createGameSuccessful() throws UnauthorisedDeveloperException, ServiceException, ValidationException {
        Game game = createGame();
        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(gameService.createGame(game, DEVELOPER)).thenReturn(game);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(LOCATION_HEADER, response.getHeaders().getLocation());
    }

    @Test
    @DisplayName("Test unsuccessful create of game - ValidationException")
    void createGameUnsuccessfulValidationException() throws
            UnauthorisedDeveloperException, ServiceException, ValidationException {
        Game game = createGame();
        game.setTitle(null);

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);

        when(gameService.createGame(game, DEVELOPER)).thenThrow(ValidationException.class);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful create of game - unauthorised")
    void createGameUnsuccessfulUnauthorised() throws ServiceException, UnauthorisedDeveloperException, ValidationException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        when(gameService.createGame(game, DEVELOPER)).thenThrow(UnauthorisedDeveloperException.class);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful create of game - ServiceException")
    void createGameUnsuccessfulServiceException() throws ServiceException, UnauthorisedDeveloperException, ValidationException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        when(gameService.createGame(game, DEVELOPER)).thenThrow(ServiceException.class);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful create of game - DuplicateKeyException")
    void createGameUnsuccessfulDuplicateKeyException() throws UnauthorisedDeveloperException, ServiceException, ValidationException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        when(gameService.createGame(game, DEVELOPER)).thenThrow(DuplicateKeyException.class);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Test successful update of game")
    void updateGameSuccessful() {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful update of game - unauthorised developer")
    void updateGameUnsuccessfulUnauthorised() throws ResourceNotFoundException, UnauthorisedDeveloperException, ServiceException, ValidationException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(UnauthorisedDeveloperException.class).when(gameService).updateGame(game, ID, DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful update of game - ServiceException")
    void updateGameUnsuccessfulServiceException() throws ResourceNotFoundException, UnauthorisedDeveloperException, ServiceException, ValidationException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(ServiceException.class).when(gameService).updateGame(game, ID, DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful update of game - ResourceNotFoundException")
    void updateGameUnsuccessfulResourceNotFound() throws ResourceNotFoundException, UnauthorisedDeveloperException, ServiceException, ValidationException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(ResourceNotFoundException.class).when(gameService).updateGame(game, ID, DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful update of game - ValidationException")
    void updateGameUnsuccessfulValidationException() throws ServiceException, ResourceNotFoundException, UnauthorisedDeveloperException, ValidationException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(ValidationException.class).when(gameService).updateGame(game, ID, DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test successful delete of game")
    void deleteGameSuccessful() {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);

        ResponseEntity response = gameController.delete(ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful delete of game - ResourceNotFoundException")
    void deleteGameUnsuccessfulResourceNotFound() throws ServiceException, UnauthorisedDeveloperException, ResourceNotFoundException {
        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(ResourceNotFoundException.class).when(gameService).deleteGame(ID, DEVELOPER);

        ResponseEntity response = gameController.delete(ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful delete of game - ServiceException")
    void deleteGameUnsuccessfulServiceException() throws ServiceException, UnauthorisedDeveloperException, ResourceNotFoundException {
        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(ServiceException.class).when(gameService).deleteGame(ID, DEVELOPER);

        ResponseEntity response = gameController.delete(ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful delete of game - Unauthorised")
    void deleteGameUnsuccessfulUnauthorised() throws ServiceException, UnauthorisedDeveloperException, ResourceNotFoundException {
        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(UnauthorisedDeveloperException.class).when(gameService).deleteGame(ID, DEVELOPER);

        ResponseEntity response = gameController.delete(ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private Game createGame() {
        Game game = new Game();
        game.setDeveloper(DEVELOPER);
        game.setTitle(TITLE);
        game.setId(ID);
        return game;
    }

    private Games createGames() {
        Games games = new Games();
        List<Game> gamesList = new ArrayList<>();
        games.setItemsPerPage(Long.parseLong(ITEMS_PER_PAGE));
        games.setStartIndex(Long.parseLong(START_INDEX));
        games.setTotalResults(3L);
        games.setItems(gamesList);
        gamesList.add(createGame());
        return games;
    }
}
