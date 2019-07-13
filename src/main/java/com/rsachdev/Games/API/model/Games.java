package com.rsachdev.Games.API.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Games {

    @JsonProperty("items_per_page")
    private long itemsPerPage;

    @JsonProperty("start_index")
    private long startIndex;

    @JsonProperty("total_results")
    private long totalResults;

    @JsonProperty("items")
    private List<Game> items;

    public long getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(long itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public List<Game> getItems() {
        return items;
    }

    public void setItems(List<Game> items) {
        this.items = items;
    }
}
