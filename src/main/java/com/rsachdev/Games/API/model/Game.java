package com.rsachdev.Games.API.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Document(collection="games")
public class Game {
    @Id
    @Field("_id")
    @JsonProperty("id")
    private String id;

    @NotNull
    @NotEmpty
    @Field("title")
    @JsonProperty("title")
    private String title;

    @Field("release_date")
    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @NotEmpty
    @Field("genres")
    @JsonProperty("genres")
    private List<String> genres;

    @NotNull
    @NotEmpty
    @Field("developer")
    @JsonProperty("developer")
    private String developer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }
}
