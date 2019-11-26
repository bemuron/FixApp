package com.emtech.fixr.data.network.api;

import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.models.Categories;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIService {

    //getting the app categories
    @GET("public/categories")
    Call<Categories> getCategories();

    //getting category images
    @GET("public/messages/{image}")
    Call<Result> getMessages(@Path("image") String image);

    //posting a job
    @Multipart
    @POST("public/postjob")
    Call<Result> postJob(//@Path("id") int id,
            //@Path("posted_by") int posted_by,
            @Part("posted_by") int posted_by,
            @Part("job_title") String job_title,
            @Part("description") String description,
            @Part("location") String location,
            @Part MultipartBody.Part file,
            @Part("name") RequestBody name,
            @Part("category_id") int category_id);

    //the login/signin call
    @FormUrlEncoded
    @POST("public/login")
    Call<Result> userLogin(
            @Field("email") String email,
            @Field("password") String password);

    //The register call
    @FormUrlEncoded
    @POST("public/register")
    Call<Result> createUser(
            @Field("name") String name,
            @Field("date_of_birth") String date_of_birth,
            @Field("gender") String gender,
            @Field("email") String email,
            @Field("password") String password);

    //uploading user job image
    @Multipart
    @POST("uploadUserJobImage/{user_id}")
    Call<Result> uploadUserJobImage(
            @Path("user_id") int user_id,
            @Part MultipartBody.Part file,
            @Part("name") RequestBody name);

    /*
    //The register call
    @FormUrlEncoded
    @POST("public/register")
    Call<Result> createUser(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("gender") String gender);

    //the signin call
    @FormUrlEncoded
    @POST("public/login")
    Call<Result> userLogin(
            @Field("email") String email,
            @Field("password") String password);

/   //sending message
    @FormUrlEncoded
    @POST("public/sendmessage")
    Call<MessageResponse> sendMessage(
            @Field("from") int from,
            @Field("to") int to,
            @Field("title") String title,
            @Field("message") String message);

    //updating user
    @FormUrlEncoded
    @POST("public/update/{id}")
    Call<Result> updateUser(
            @Path("id") int id,
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("gender") String gender);

    //getting messages
    @GET("public/messages/{id}")
    Call<Messages> getMessages(@Path("id") int id);*/
}
