package com.rsachdev.Games.API.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Developers {
    @JsonProperty("developers")
    private List<Developer> developers;

    public List<Developer> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<Developer> developers) {
        this.developers = developers;
    }
}
