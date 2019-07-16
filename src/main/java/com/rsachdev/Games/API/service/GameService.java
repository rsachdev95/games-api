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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameValidator gameValidator;

    @Autowired
    private AmazonS3Service amazonS3Service;

    public Game getById(String id) throws ServiceException, ResourceNotFoundException {
        Optional<Game> game;

        try {
            game = gameRepository.findById(id);
        } catch (MongoException me) {
            throw new ServiceException("Error when trying to retrieve game with id: " + id, me);
        }

        if (!game.isPresent()) {
            throw new ResourceNotFoundException("Game " + id + " does not exist");
        }

        return game.get();
    }

    public Game createGame(Game game, String developer) throws ServiceException, UnauthorisedDeveloperException, ValidationException {
        Game createdGame;

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        if(!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }

        if(developer == null || !isAuthorisedDeveloper(developer)  || validateDeveloperOfGame(game, developer)) {
            throw new UnauthorisedDeveloperException("Developer not authorised to create game");
        }

        String id = UUID.randomUUID().toString();
        game.setId(id);

        try {
            createdGame = gameRepository.insert(game);
        } catch (DuplicateKeyException dke) {
            throw dke;
        } catch (MongoException me) {
            throw new ServiceException("Error when trying to create game: " + game, me);
        }

        return createdGame;
    }

    public Games listAllGames(String startIndex, String itemsPerPage) throws ServiceException {
        Games games = new Games();

        Pageable pageable = PageRequest.of(Integer.parseInt(startIndex), Integer.parseInt(itemsPerPage));

        try {
            Page<Game> gamePage = gameRepository.findAll(pageable);
            games.setItems(gamePage.getContent());
            games.setItemsPerPage(gamePage.getSize());
            games.setStartIndex(gamePage.getPageable().getPageNumber());
            games.setTotalResults(gamePage.getTotalElements());

        } catch (MongoException me) {
            throw new ServiceException("Error when trying to retrieve all games: ", me);
        }

        return games;
    }

    public void updateGame(Game game, String id, String developer) throws ServiceException, UnauthorisedDeveloperException, ResourceNotFoundException, ValidationException {
        Game existingGame = getById(id);

        List<String> errors = gameValidator.validateNotNullOrEmpty(game);
        if(!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }

        if(developer == null || validateDeveloperOfGame(existingGame, developer)) {
            throw new UnauthorisedDeveloperException("Developer not authorised to update this game");
        }

        game.setId(id);

        try {
            gameRepository.save(game);
        } catch (MongoException me) {
            throw new ServiceException("Error occurred when updating the game: " + game.getTitle());
        }
    }

    public void deleteGame(String id, String developer) throws ResourceNotFoundException, ServiceException, UnauthorisedDeveloperException {
        Game existingGame = getById(id);

        if(developer == null || validateDeveloperOfGame(existingGame, developer)) {
            throw new UnauthorisedDeveloperException("Developer not authorised to delete this game");
        }

        try {
            gameRepository.delete(existingGame);
        } catch (MongoException me) {
            throw new ServiceException("Error occurred when deleting game: " + existingGame.getId());
        }
    }

    private boolean isAuthorisedDeveloper(String developer) throws ServiceException {
        List<Developer> authorisedDevelopers = amazonS3Service.getAuthorisedDevelopers();

        return authorisedDevelopers
                .stream()
                .anyMatch(d -> developer.trim().toLowerCase().equals(d.getName().trim().toLowerCase()));
    }

    private boolean validateDeveloperOfGame(Game game, String developer) {
        return !game.getDeveloper().trim().toLowerCase().equals(developer.trim().toLowerCase());
    }
}
