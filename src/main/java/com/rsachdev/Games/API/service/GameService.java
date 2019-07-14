package com.rsachdev.Games.API.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.rsachdev.Games.API.exception.DataException;
import com.rsachdev.Games.API.exception.ServiceException;
import com.rsachdev.Games.API.exception.UnauthorisedDeveloperException;
import com.rsachdev.Games.API.model.Developer;
import com.rsachdev.Games.API.model.Developers;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.model.Games;
import com.rsachdev.Games.API.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GameService {
    private static final String BUCKET = "ch-senior-dev-test";
    private static final String FILE = "developers.json";

    @Autowired
    private AmazonS3 amazonS3Client;

    @Autowired
    private GameRepository gameRepository;

    private List<Developer> authorisedDevelopers;

    public Game getById(String id) throws ServiceException {
        Game game;

        try {
            game = gameRepository.findById(id).orElse(null);
        } catch (MongoException me) {
            throw new ServiceException("Error when trying to retrieve game with id: " + id, me);
        }

        if (game == null) {
            return null;
        }

        return game;
    }

    public Game createGame(Game game, String developer) throws ServiceException, UnauthorisedDeveloperException, DataException {
        Game createdGame;

        if(!isAuthorisedDeveloper(developer)) {
            throw new UnauthorisedDeveloperException("Developer not authorised to create game");
        }

        if(!validateDeveloperOfGame(game, developer)){
            throw new DataException("Developer creating the game is not the developer of the game");
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

    public Games listAllGames() throws ServiceException {
        Games games = new Games();

        Pageable pageable = PageRequest.of(0, 2);

        try {
            Page<Game> gamePage = gameRepository.findAll(pageable);
            games.setItems(gamePage.getContent());
            games.setItemsPerPage(gamePage.getSize());
            games.setStartIndex(gamePage.getPageable().getOffset());
            games.setTotalResults(gamePage.getTotalElements());

        } catch (MongoException me) {
            throw new ServiceException("Error when trying to retrieve all games: ", me);
        }

        return games;
    }

    private boolean isAuthorisedDeveloper(String developer) throws ServiceException {
        if(authorisedDevelopers == null) {
            S3ObjectInputStream inputStream;

            try {
                inputStream = getDevelopers();
                authorisedDevelopers = unmarshallJson(inputStream);
            } catch (IOException | AmazonS3Exception e) {
                throw new ServiceException("Error when retrieving list of authorised developers", e);
            }
        }
        return authorisedDevelopers
                .stream()
                .anyMatch(d -> developer.trim().toLowerCase().equals(d.getName().trim().toLowerCase()));
    }

    private List<Developer> unmarshallJson(S3ObjectInputStream objectStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Developers developers;

        try {
            developers = mapper.readValue(objectStream, Developers.class);
        } catch (IOException ioe) {
            throw new  IOException("Error occurred unmarshalling json", ioe);
        }

        return developers.getDevelopers();
    }

    private S3ObjectInputStream getDevelopers() throws AmazonS3Exception {
        S3Object object;

        try {
            object = amazonS3Client.getObject(BUCKET, FILE);
        } catch(AmazonS3Exception ase) {
            throw new AmazonS3Exception(FILE + " or " + BUCKET + " doesn't exist", ase);
        }

        return object.getObjectContent();
    }

    private boolean validateDeveloperOfGame(Game game, String developer) {
        return game.getDeveloper().trim().toLowerCase().equals(developer.trim().toLowerCase());
    }
}
