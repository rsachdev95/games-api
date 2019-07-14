package com.rsachdev.Games.API.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Developer {
    @JsonProperty("name")
    private String name;

    @JsonProperty("headquarters")
    private String headquarters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }
}
