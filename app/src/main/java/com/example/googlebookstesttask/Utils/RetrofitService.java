package com.example.googlebookstesttask.Utils;

import com.example.googlebookstesttask.Model.BooksApiResponse;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface RetrofitService {

    @GET("/books/v1/volumes")
    Observable<BooksApiResponse> getBooks(@Header("Authorization") String token, @Query("q") String q);

    @GET("/books/v1/mylibrary/bookshelves/0/volumes")
    Observable<BooksApiResponse> getFavouriteBooks(@Header("Authorization") String token);

//    @Headers({"Content-Type: application/json", "Content-Length: CONTENT_LENGTH"})
    @POST("/books/v1/mylibrary/bookshelves/0/addVolume")
    Completable addToFavorites(@Header("Authorization") String token, @Query("volumeId") String volumeId);

}
