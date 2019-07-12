package com.rsachdev.Games.API.repository;

import com.rsachdev.Games.API.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, String> {
}
