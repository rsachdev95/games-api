package com.rsachdev.Games.API.controller;

import com.mongodb.DuplicateKeyException;
import com.rsachdev.Games.API.GamesApiApplication;
import com.rsachdev.Games.API.exception.DataException;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.model.Games;
import com.rsachdev.Games.API.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/games")
public class GameController {
    private static final Logger LOG = LoggerFactory.getLogger(GamesApiApplication.APPLICATION_NAMESPACE);

    @Autowired
    private GameService gameService;

    @GetMapping("/{gameId}")
    public ResponseEntity fetch(@PathVariable String gameId) {
        Game game;

        try {
            LOG.info("Getting game with id: " + gameId);
            game = gameService.getById(gameId);
        } catch (DataException de) {
            LOG.error("Error when retrieving game with id: " + gameId, de);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (game == null) {
            LOG.error("Game with id: " + gameId + " not found");
            return ResponseEntity.notFound().build();
        }

        LOG.info("Successfully retrieved game with id: " + gameId);
        return ResponseEntity.ok(game);
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody Game game, HttpServletRequest request) {
        Game createdGame;

        try {
            LOG.info("Creating game: " + game.getTitle());
            createdGame = gameService.createGame(game);
        } catch (DataException de) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (DuplicateKeyException dke) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String locationString = request.getRequestURI() + "/" + createdGame.getId();
        URI location = URI.create(locationString);

        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity listAll() {
        Games games;

        try {
            LOG.info("Retrieving all games");
            games = gameService.listAllGames();
        } catch (DataException de) {
            LOG.error("Error when retrieving all games", de);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (games == null) {
            LOG.error("No games found");
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(games);
    }

}
