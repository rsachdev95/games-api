package com.rsachdev.Games.API.controller;

import com.rsachdev.Games.API.exception.ResourceNotFoundException;
import com.rsachdev.Games.API.exception.ServiceException;
import com.rsachdev.Games.API.exception.UnauthorisedDeveloperException;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.model.Games;
import com.rsachdev.Games.API.service.GameService;
import org.junit.Before;
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
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.URI;
import java.util.ArrayList;
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
    private static final URI LOCATION_HEADER = URI.create(REQUEST_URI + "/" + ID);

    private Validator validator;

    @Mock
    private GameService gameService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GameController gameController;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

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
        when(gameService.listAllGames()).thenReturn(games);

        ResponseEntity response = gameController.listAll();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(games, response.getBody());
    }

    @Test
    @DisplayName("Tests unsuccessful retrieval of all games - not found")
    void listAllGamesUnsuccessfulNotFound() throws ServiceException {
        when(gameService.listAllGames()).thenReturn(null);

        ResponseEntity response = gameController.listAll();
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Test unsuccessful retrieval of all games - ServiceException")
    void listAllGamesUnsuccessfulDataException() throws ServiceException {
        when(gameService.listAllGames()).thenThrow(ServiceException.class);

        ResponseEntity response = gameController.listAll();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Test successful create of game")
    void createGameSuccessful() throws UnauthorisedDeveloperException, ServiceException {
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
    @DisplayName("Test unsuccessful create of game - bad request")
    void createGameUnsuccessfulBadRequest() throws
            UnauthorisedDeveloperException, ServiceException {
        Game game = createGame();
        game.setTitle(null);

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful create of game - unauthorised")
    void createGameUnsuccessfulUnauthorised() throws ServiceException, UnauthorisedDeveloperException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        when(gameService.createGame(game, DEVELOPER)).thenThrow(UnauthorisedDeveloperException.class);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful create of game - ServiceException")
    void createGameUnsuccessfulServiceException() throws ServiceException, UnauthorisedDeveloperException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        when(gameService.createGame(game, DEVELOPER)).thenThrow(ServiceException.class);

        ResponseEntity response = gameController.create(game, request);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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
    void updateGameUnsuccessfulUnauthorised() throws ResourceNotFoundException, UnauthorisedDeveloperException, ServiceException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(UnauthorisedDeveloperException.class).when(gameService).updateGame(game, ID, DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful update of game - ServiceException")
    void updateGameUnsuccessfulServiceException() throws ResourceNotFoundException, UnauthorisedDeveloperException, ServiceException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(ServiceException.class).when(gameService).updateGame(game, ID, DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Test unsuccessful update of game - ResourceNotFoundException")
    void updateGameUnsuccessfulResourceNotFound() throws ResourceNotFoundException, UnauthorisedDeveloperException, ServiceException {
        Game game = createGame();

        when(request.getHeader(DEVELOPER)).thenReturn(DEVELOPER);
        doThrow(ResourceNotFoundException.class).when(gameService).updateGame(game, ID, DEVELOPER);

        ResponseEntity response = gameController.update(game, ID, request);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
        games.setItemsPerPage(2L);
        games.setStartIndex(1L);
        games.setTotalResults(3L);
        games.setItems(gamesList);
        gamesList.add(createGame());
        return games;
    }
}
