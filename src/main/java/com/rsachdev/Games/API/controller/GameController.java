package com.rsachdev.Games.API.controller;

import com.mongodb.DuplicateKeyException;
import com.rsachdev.Games.API.GamesApiApplication;
import com.rsachdev.Games.API.exception.ResourceNotFoundException;
import com.rsachdev.Games.API.exception.ServiceException;
import com.rsachdev.Games.API.exception.UnauthorisedDeveloperException;
import com.rsachdev.Games.API.exception.ValidationException;
import com.rsachdev.Games.API.model.Game;
import com.rsachdev.Games.API.model.Games;
import com.rsachdev.Games.API.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
        } catch (ServiceException de) {
            LOG.error("Error when retrieving game with id: " + gameId, de);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (ResourceNotFoundException rnfe) {
            return ResponseEntity.notFound().build();
        }

        LOG.info("Successfully retrieved game with id: " + gameId);
        return ResponseEntity.ok(game);
    }

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody Game game, HttpServletRequest request) {
        Game createdGame;
        String developer = request.getHeader("developer");

        try {
            LOG.info("Creating game: " + game.getTitle());
            createdGame = gameService.createGame(game, developer);
        } catch (ServiceException se) {
            LOG.error("Error when creating game with title: " + game.getTitle(), se);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (DuplicateKeyException dke) {
            LOG.error("Id already exists - try creating " + game.getTitle() + " again", dke);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (UnauthorisedDeveloperException ude) {
            LOG.error("Developer not authorised to create " + game.getTitle(), ude);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ValidationException ve) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ve.getMessage());
        }

        String locationString = request.getRequestURI() + "/" + createdGame.getId();
        URI location = URI.create(locationString);

        LOG.info("Game: " + createdGame.getTitle() + " successfully created");
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity listAll() {
        Games games;

        try {
            LOG.info("Retrieving all games");
            games = gameService.listAllGames();
        } catch (ServiceException de) {
            LOG.error("Error when retrieving all games", de);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (games.getTotalResults() < 1) {
            LOG.error("No games found");
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(games);
    }

    @PutMapping("/{gameId}")
    public ResponseEntity update(@Valid @RequestBody Game game, @PathVariable String gameId, HttpServletRequest request) {
        String developer = request.getHeader("developer");

        try {
            gameService.updateGame(game, gameId, developer);
        } catch (UnauthorisedDeveloperException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (ResourceNotFoundException rnfe) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException ve) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ve.getMessage());
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity delete(@PathVariable String gameId, HttpServletRequest request) {
        String developer = request.getHeader("developer");

        try {
            gameService.deleteGame(gameId, developer);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (UnauthorisedDeveloperException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.noContent().build();
    }

}
