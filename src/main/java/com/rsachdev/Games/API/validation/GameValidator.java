package com.rsachdev.Games.API.validation;

import com.rsachdev.Games.API.model.Game;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GameValidator {
    public List<String> validateNotNullOrEmpty(Game game) {
        List<String> errors = new ArrayList<>();

        if(game.getTitle().trim().isEmpty() || game.getTitle() == null) {
            errors.add("Game must have a title");
        }

        if(game.getDeveloper().trim().isEmpty() || game.getDeveloper() == null) {
            errors.add("Game must have a developer");
        }

        return errors;
    }
}
