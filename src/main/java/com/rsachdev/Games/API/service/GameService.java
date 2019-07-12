package com.rsachdev.Games.API.service;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.rsachdev.Games.API.exception.DataException;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {
    private final GameRepository gameRepository;

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

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
}
