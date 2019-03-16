package com.davtyan.mytaxi.api;


import com.davtyan.mytaxi.model.Driver;
import com.davtyan.mytaxi.model.Geo;
import com.davtyan.mytaxi.model.Message;
import com.davtyan.mytaxi.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface Service {

    @POST("auth/user")
    Call<Message> addUser(
            @Header(Constants.CONTENT_TYPE) String type,
            @Body User user
    );


    @POST("login/user")
    Call<Message> login(
            @Header(Constants.CONTENT_TYPE) String type,
            @Body User user
    );

    @PUT("verification")
    Call<Message> verification(
            @Header(Constants.CONTENT_TYPE) String type,
            @Body User.Code code
    );

    @GET("find")
    Call<List<Driver>> getDrivers();

    @GET("cancel")
    Call<Message> cancel();

    @GET("confirmed")
    Call<Message> confirmed();

    @GET("successfully")
    Call<Message> successfully();

    @POST("rating")
    Call<Message> addRating(
            @Header(Constants.CONTENT_TYPE) String type,
            @Body Driver.Rating rating
    );

    @PUT("changePassword")
    Call<Message> changePassword(
            @Header(Constants.CONTENT_TYPE) String type,
            @Body User user
    );
}