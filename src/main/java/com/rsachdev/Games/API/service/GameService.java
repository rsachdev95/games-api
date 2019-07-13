package com.rsachdev.Games.API.service;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.rsachdev.Games.API.exception.DataException;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.model.Games;
import com.rsachdev.Games.API.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    public Game getById(String id) throws DataException {
        Game game;

        try {
            game = gameRepository.findById(id).orElse(null);
        } catch (MongoException me) {
            throw new DataException("Error when trying to retrieve game with id: " + id, me);
        }

        if (game == null) {
            return null;
        }

        return game;
    }

    public Game createGame(Game game) throws DataException {
        Game createdGame;

        String id = UUID.randomUUID().toString();
        game.setId(id);

        try {
            createdGame = gameRepository.insert(game);
        } catch (DuplicateKeyException dke) {
            throw dke;
        } catch (MongoException me) {
            throw new DataException("Error when trying to create game: " + game, me);
        }

        return createdGame;
    }

    public Games listAllGames() throws DataException {
        Games games = new Games();

        Pageable pageable = PageRequest.of(0, 2);

        try {
            Page<Game> gamePage = gameRepository.findAll(pageable);
            games.setItems(gamePage.getContent());
            games.setItemsPerPage(gamePage.getSize());
            games.setStartIndex(gamePage.getPageable().getOffset());
            games.setTotalResults(gamePage.getTotalElements());

        } catch (MongoException me) {
            throw new DataException("Error when trying to retrieve all games: ", me);
        }

        return games;
    }
}
