package com.example.googlebookstesttask.Model;

import java.util.ArrayList;
import java.util.List;

public class BooksApiResponseFavourites {
    private BooksApiResponse searchResponse;
    private List<String> favouritesIds;

    public BooksApiResponseFavourites(BooksApiResponse searchResponse, BooksApiResponse favouritesResponse){
        this.searchResponse = searchResponse;
        List<String> list = new ArrayList<>();
        for (BooksApiResponse.BookItem bookItem: favouritesResponse.getItems())
            list.add(bookItem.getId());
        this.favouritesIds = list;
    }


    public BooksApiResponse getSearchResponse() {
        return searchResponse;
    }

    public void setSearchResponse(BooksApiResponse searchResponse) {
        this.searchResponse = searchResponse;
    }

    public List<String> getFavouritesIds() {
        return favouritesIds;
    }

    public void setFavouritesIds(List<String> favouritesIds) {
        this.favouritesIds = favouritesIds;
    }
}
