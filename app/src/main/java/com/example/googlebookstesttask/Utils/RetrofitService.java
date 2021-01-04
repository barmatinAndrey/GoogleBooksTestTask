package com.example.googlebookstesttask.Utils;

import com.example.googlebookstesttask.Model.BooksApiResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface RetrofitService {

    @GET("/books/v1/volumes")
    Observable<BooksApiResponse> getBooks(@Header("Authorization") String token, @Query("q") String q);

}
