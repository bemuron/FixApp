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
    Call<Result> postJob(
            @Part("posted_by") int posted_by,
            @Part("job_title") String job_title,
            @Part("description") String description,
            @Part("location") String location,
            @Part("must_have_one") String must_have_one,
            @Part("must_have_two") String must_have_two,
            @Part("must_have_three") String must_have_three,
            @Part("is_job_remote") int is_job_remote,
            @Part MultipartBody.Part file,
            @Part("name") RequestBody name,
            @Part("category_id") int category_id);

    //posting a job without an image
    @FormUrlEncoded
    @POST("public/postJobWithoutImage")
    Call<Result> postJobWithoutImage(
            @Field("posted_by") int posted_by,
            @Field("job_title") String job_title,
            @Field("description") String description,
            @Field("location") String location,
            @Field("must_have_one") String must_have_one,
            @Field("must_have_two") String must_have_two,
            @Field("must_have_three") String must_have_three,
            @Field("is_job_remote") int is_job_remote,
            @Field("category_id") int category_id);

    //updating a job with an image attached
    @Multipart
    @POST("public/updateJob/{job_id}")
    Call<Result> updateJob(
            @Path("job_id") int job_id,
            @Part("job_title") String job_title,
            @Part("description") String description,
            @Part("location") String location,
            @Part("must_have_one") String must_have_one,
            @Part("must_have_two") String must_have_two,
            @Part("must_have_three") String must_have_three,
            @Part("is_job_remote") int is_job_remote,
            @Part MultipartBody.Part file,
            @Part("name") RequestBody name);

    //updating a job without an image
    @FormUrlEncoded
    @POST("public/updateJobWithoutImage/{job_id}")
    Call<Result> updateJobWithoutImage(
            @Path("job_id") int job_id,
            @Field("job_title") String job_title,
            @Field("description") String description,
            @Field("location") String location,
            @Field("must_have_one") String must_have_one,
            @Field("must_have_two") String must_have_two,
            @Field("must_have_three") String must_have_three,
            @Field("is_job_remote") int is_job_remote);

    //updating job date time
    @FormUrlEncoded
    @POST("public/updateJobDateTime/{job_id}")
    Call<Result> updateJobDateTime(
            @Path("job_id") int job_id,
            @Field("job_date") String job_date,
            @Field("job_time") String job_time);

    //updating job budget
    @FormUrlEncoded
    @POST("public/updateJobBudget/{job_id}")
    Call<Result> updateJobBudget(
            @Path("job_id") int job_id,
            @Field("total_budget") int total_budget,
            @Field("price_per_hr") int price_per_hr,
            @Field("total_hrs") int total_hrs,
            @Field("est_tot_budget") int est_tot_budget);

    //the login/signing call
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
