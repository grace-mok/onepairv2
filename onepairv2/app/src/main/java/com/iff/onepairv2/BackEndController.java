package com.iff.onepairv2;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BackEndController {
    public static final String URL = "http://128.199.167.80:8080/";

    @GET("getAllDeals")
    Call<ArrayList<Deal>> getAllDeals();

    @GET("getFoodDeals")
    Call<ArrayList<Deal>> getFoodDeals();

    @GET("getEntertainmentDeals")
    Call<ArrayList<Deal>> getEntertainmentDeals();

    @GET("getRetailDeals")
    Call<ArrayList<Deal>> getRetailDeals();

    @GET("getOthersDeals")
    Call<ArrayList<Deal>> getOthersDeals();

    @GET("addUser/{uid}")
    Call<Void> addUser(@Path("uid") String uid);

    @GET("addBlacklist/{uid1}/{uid2}")
    Call<Void> addBlacklist(@Path("uid1") String uid1, @Path("uid2") String uid2);

    @GET("addRequest/{uid}/{dealid}/{c}")
    Call<Void> addRequest(@Path("uid") String uid, @Path("dealid") int dealid, @Path("c") String c);

    @GET("deleteRequest/{uid}/{dealid}")
    Call<Void> deleteRequest(@Path("uid") String uid, @Path("dealid") int dealid);

    @GET("getAllLocation")
    Call<ArrayList<Location>> getAllLocation();

    @GET("updateToken/{uid}/{token}")
    Call<Void> updateToken(@Path("uid") String uid, @Path("token") String token);
}
