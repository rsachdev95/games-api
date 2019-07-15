package com.rsachdev.Games.API.service;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.rsachdev.Games.API.exception.ResourceNotFoundException;
import com.rsachdev.Games.API.exception.ServiceException;
import com.rsachdev.Games.API.exception.UnauthorisedDeveloperException;
import com.rsachdev.Games.API.exception.ValidationException;
import com.rsachdev.Games.API.model.Developer;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.model.Games;
import com.rsachdev.Games.API.repository.GameRepository;
import com.rsachdev.Games.API.validation.GameValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceTest {
    private static final String ID = "id";
    private static final String DEVELOPER_NAME = "developer";
    private static final Developer DEVELOPER = new Developer();
    private static final String TITLE = "title";
    private List<Developer> authorisedDevelopers = new ArrayList<>();

    @Mock
    Page<Game> gamePage;

    @Mock
    private AmazonS3Service amazonS3Service;

    @Mock
    private GameValidator gameValidator;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    @DisplayName("Test get by id successful")
    void getByIdSuccessful() throws ResourceNotFoundException, ServiceException {
        Game game = createGame();

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));

        Game serviceGame = gameService.getById(ID);
        assertNotNull(game);
        assertEquals(game, serviceGame);
    }

    @Test
    @DisplayName("Test get by id unsuccessful - MongoException")
    void getByIdUnsuccessfulMongoException() {
        when(gameRepository.findById(ID)).thenThrow(MongoException.class);

        assertThrows(ServiceException.class, () -> gameService.getById(ID));
    }

    @Test
    @DisplayName("Test get by id unsuccessful - not found")
    void getByIdUnsuccessfulNotFound() {
        when(gameRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.getById(ID));
    }

    @Test
    @DisplayName("Test create game successful")
    void createGameSuccessful() throws UnauthorisedDeveloperException, ServiceException, ValidationException, IOException {
        Game game = createGame();
        setAuthorisedDevelopers();

        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());
        when(amazonS3Service.getAuthorisedDevelopers()).thenReturn(authorisedDevelopers);
        when(gameRepository.insert(game)).thenReturn(game);

        Game insertedGame = gameService.createGame(game, DEVELOPER_NAME);
        assertNotNull(insertedGame);
        assertEquals(game, insertedGame);
    }

    @Test
    @DisplayName("Test create game unsuccessful - unauthorised developer")
    void createGameUnsuccessfulUnauthorised() throws ServiceException {
        Game game = createGame();
        game.setDeveloper("Not Authorised");
        setAuthorisedDevelopers();

        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());
        when(amazonS3Service.getAuthorisedDevelopers()).thenReturn(authorisedDevelopers);

        assertThrows(UnauthorisedDeveloperException.class, () -> gameService.createGame(game, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test create game unsuccessful - unauthorised - null developer")
    void createGameUnsuccessfulUnauthorisedNull() throws ServiceException {
        Game game = createGame();

        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());

        assertThrows(UnauthorisedDeveloperException.class, () -> gameService.createGame(game, null));
    }

    @Test
    @DisplayName("Test create game unsuccessful - validation error")
    void createGameUnsuccessfulValidationError() {
        Game game = createGame();
        setAuthorisedDevelopers();
        List<String> errors = new ArrayList<>();
        errors.add("error");

        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(errors);

        assertThrows(ValidationException.class, () -> gameService.createGame(game, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test create game unsuccessful - ServiceException from S3Service")
    void createGameUnsuccessfulS3ServiceException() throws ServiceException {
        Game game = createGame();
        setAuthorisedDevelopers();

        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());
        when(amazonS3Service.getAuthorisedDevelopers()).thenThrow(ServiceException.class);

        assertThrows(ServiceException.class, () -> gameService.createGame(game, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test create game unsuccessful - MongoException")
    void createGameUnsuccessfulMongoException() throws ServiceException {
        Game game = createGame();
        setAuthorisedDevelopers();

        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());
        when(amazonS3Service.getAuthorisedDevelopers()).thenReturn(authorisedDevelopers);
        when(gameRepository.insert(game)).thenThrow(MongoException.class);

        assertThrows(ServiceException.class, () -> gameService.createGame(game, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test create game unsuccessful - DuplicateKeyException")
    void createGameUnsuccessfulDuplicateKeyException() throws ServiceException {
        Game game = createGame();
        setAuthorisedDevelopers();

        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());
        when(amazonS3Service.getAuthorisedDevelopers()).thenReturn(authorisedDevelopers);
        when(gameRepository.insert(game)).thenThrow(DuplicateKeyException.class);

        assertThrows(DuplicateKeyException.class, () -> gameService.createGame(game, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test update game successful")
    void updateGameSuccessful() throws UnauthorisedDeveloperException, ServiceException, ValidationException, IOException, ResourceNotFoundException {
        Game game = createGame();
        ArgumentCaptor<Game> arg = ArgumentCaptor.forClass(Game.class);

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));

        gameService.updateGame(game, ID, DEVELOPER_NAME);
        verify(gameRepository, times(1)).save(arg.capture());
        assertEquals(game, arg.getValue());
    }

    @Test
    @DisplayName("Test update game unsuccessful - validation exception")
    void updateGameUnsuccessfulValidationException() {
        Game game = createGame();
        setAuthorisedDevelopers();
        List<String> errors = new ArrayList<>();
        errors.add("error");

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));
        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(errors);

        assertThrows(ValidationException.class, () -> gameService.updateGame(game, ID, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test update game unsuccessful - not found")
    void updateGameUnsuccessfulNotFound() {
        Game game = createGame();
        when(gameRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.updateGame(game, ID, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test update game unsuccessful - unauthorised")
    void updateGameUnsuccessfulUnauthorised() {
        Game game = createGame();
        game.setDeveloper("Not Authorised");

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));
        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());

        assertThrows(UnauthorisedDeveloperException.class, () -> gameService.updateGame(game, ID, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test update game unsuccessful - unauthorised - null developer")
    void updateGameUnsuccessfulUnauthorisedNullDeveloper() {
        Game game = createGame();
        setAuthorisedDevelopers();

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));
        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());

        assertThrows(UnauthorisedDeveloperException.class, () -> gameService.updateGame(game, ID, null));
    }

    @Test
    @DisplayName("Test update game unsuccessful - MongoException")
    void updateGameUnsuccessfulMongoException() {
        Game game = createGame();
        setAuthorisedDevelopers();

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));
        when(gameValidator.validateNotNullOrEmpty(game)).thenReturn(Collections.emptyList());
        when(gameRepository.save(game)).thenThrow(MongoException.class);

        assertThrows(ServiceException.class, () -> gameService.updateGame(game, ID, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test successful list all games")
    void listAllGamesSuccessful() throws ServiceException {
        Games games = createGames();
        Pageable pageable = PageRequest.of(0, 5);
        when(gameRepository.findAll(pageable)).thenReturn(gamePage);
        when(gamePage.getContent()).thenReturn(createGames().getItems());
        when(gamePage.getTotalElements()).thenReturn(1L);
        when(gamePage.getPageable()).thenReturn(pageable);
        when(gamePage.getSize()).thenReturn(5);

        Games serviceResult = gameService.listAllGames();
        assertNotNull(games);
        assertEquals(games.getItemsPerPage(), serviceResult.getItemsPerPage());
        assertEquals(games.getStartIndex(), serviceResult.getStartIndex());
        assertEquals(games.getTotalResults(), serviceResult.getTotalResults());
        assertEquals(games.getItems().get(0).getDeveloper(), serviceResult.getItems().get(0).getDeveloper());
        assertEquals(games.getItems().get(0).getTitle(), serviceResult.getItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Test unsuccessful list all games - MongoException")
    void testListAllGamesUnsuccessful() {
        Pageable pageable = PageRequest.of(0, 5);

        when(gameRepository.findAll(pageable)).thenThrow(MongoException.class);

        assertThrows(ServiceException.class, () -> gameService.listAllGames());
    }

    @Test
    @DisplayName("Test successful delete of game")
    void testDeleteGameSuccessful() throws ServiceException, UnauthorisedDeveloperException, ResourceNotFoundException {
        Game game = createGame();
        ArgumentCaptor<Game> arg = ArgumentCaptor.forClass(Game.class);

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));

        gameService.deleteGame(ID, DEVELOPER_NAME);

        verify(gameRepository, times(1)).delete(arg.capture());
        assertEquals(game, arg.getValue());
    }

    @Test
    @DisplayName("Test unsuccessful delete of game - ResourceNotFound")
    void testDeleteGameUnsuccessful() {
        when(gameRepository.findById(ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gameService.deleteGame(ID, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test unsuccessful delete of game - unauthorised")
    void testDeleteGameUnsuccessfulUnauthorised() {
        Game game = createGame();
        game.setDeveloper("Not Authorised");

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));

        assertThrows(UnauthorisedDeveloperException.class, () -> gameService.deleteGame(ID, DEVELOPER_NAME));
    }

    @Test
    @DisplayName("Test delete game unsuccessful - unauthorised - null developer")
    void testDeleteGameUnsuccessfulUnauthorisedNullDeveloper() {
        Game game = createGame();

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));

        assertThrows(UnauthorisedDeveloperException.class, () -> gameService.deleteGame(ID, null));
    }

    @Test
    @DisplayName("Test delete game unsuccessful - MongoException")
    void testDeleteGameUnsuccessfulMongoException() {
        Game game = createGame();

        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));
        doThrow(MongoException.class).when(gameRepository).delete(game);

        assertThrows(ServiceException.class, () -> gameService.deleteGame(ID, DEVELOPER_NAME));
    }

    private void setAuthorisedDevelopers() {
        DEVELOPER.setName(DEVELOPER_NAME);
        authorisedDevelopers.add(DEVELOPER);
    }

    private Game createGame() {
        Game game = new Game();
        game.setDeveloper(DEVELOPER_NAME);
        game.setTitle(TITLE);
        game.setId(ID);
        return game;
    }

    private Games createGames() {
        Games games = new Games();
        List<Game> gamesList = new ArrayList<>();
        games.setItemsPerPage(5L);
        games.setStartIndex(0L);
        games.setTotalResults(1L);
        games.setItems(gamesList);
        gamesList.add(createGame());
        return games;
    }
}
