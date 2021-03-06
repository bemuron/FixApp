package com.emtech.fixr.data.network.api;

import android.database.Observable;

import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.models.Categories;
import com.emtech.fixr.models.UserJobs;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

    //getting the app categories
    @GET("public/categories")
    Call<Categories> getCategories();

    //the login/signing call
    @FormUrlEncoded
    @POST("public/login")
    Call<Result> userLogin(
            @Field("email") String email,
            @Field("password") String password);

    //register device with fcm
    @FormUrlEncoded
    @POST("public/updateFcm/{user_id}")
    Call<Result> updateFcm(
            @Path("user_id") int user_id,
            @Field("fcm_registration_id") String fcm_registration_id);

    //The register call
    @FormUrlEncoded
    @POST("public/register")
    Call<Result> createUser(
            @Field("name") String name,
            @Field("date_of_birth") String date_of_birth,
            @Field("gender") String gender,
            @Field("email") String email,
            @Field("password") String password,
            @Field("phoneNumber") String phoneNumber);

    //getting category images
    @GET("public/messages/{image}")
    Call<Result> getMessages(@Path("image") String image);

    //creating a job
    //only job name, user id, job desc and category
    //are needed to create the job
    @FormUrlEncoded
    @POST("public/createJob")
    Call<Result> createJob(
            @Field("posted_by") int posted_by,
            @Field("job_title") String job_title,
            @Field("description") String description,
            @Field("category_id") int category_id);

    //updating a job
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
            @Part MultipartBody.Part[] file,
            @Part("size") int partsSize,
            @Part("category_id") int category_id);

    /*@Multipart
    @POST("/UploadFileDemo/android_upload_file/uploads.php")
    Observable<Result> uploadFiles(
            @Body MultipartTypedOutput multipartTypedOutput);*/

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
            @Part MultipartBody.Part[] file,
            @Part("size") int partsSize);

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
            @Field("est_tot_budget") int est_tot_budget,
            @Field("job_status") int job_status);

    //uploading user job image
    @Multipart
    @POST("uploadUserJobImage/{user_id}")
    Call<Result> uploadUserJobImage(
            @Path("user_id") int user_id,
            @Part MultipartBody.Part file,
            @Part("name") RequestBody name);

    //getting all the jobs associated with this user based on the status they have chosen
    // 0 - draft, 1 - posted, 2 - assigned, 3 - offers, 4 - in progress, 5 - complete
    @GET("public/getJobsByStatus/{user_id}/{status}")
    Call<UserJobs> getJobsByStatus(
            @Path("user_id") int user_id,
            @Path("status") int status);

    //get the job status for the poster
    @GET("public/getJobStatusForPoster/{job_id}")
    Call<Result> getStatusForPoster(
            @Path("job_id") int job_id);

    //get the job status for the fixer
    @GET("public/getJobStatusForFixer/{job_id}")
    Call<Result> getStatusForFixer(
            @Path("job_id") int job_id);

    //getting the details of a job
    @GET("public/getJobDetailsByStatus/{job_id}")
    Call<Result> getJobDetailsByStatus(
            @Path("job_id") int job_id);

    //getting all the jobs by and for this user
    // not status based
    @GET("public/getAllJobsForUser/{user_id}")
    Call<UserJobs> getAllJobsForUser(
            @Path("user_id") int user_id);

    //getting the details of a job minus status
    @GET("public/getJobDetails/{job_id}")
    Call<UserJobs> getJobDetails(
            @Path("job_id") int job_id);

    //getting jobs for browsing
    @GET("public/getJobsForBrowsing/{page}/{page_size}")
    Call<UserJobs> browseAllJobs(
            @Path("page") int page,
            @Path("page_size") int page_size);

    //getting search results
    @GET("public/searchJobs/{searchQuery}/{page}/{page_size}")
    Call<UserJobs> searchForJobs(
            @Path("searchQuery") String searchQuery,
            @Path("page") int page,
            @Path("page_size") int page_size);

    //posting an offer made for a job
    @FormUrlEncoded
    @POST("public/saveOffer")
    Call<Result> saveOffer(
            @Field("amount_offered") int amount_offered,
            @Field("offer_message") String offer_message,
            @Field("user_id") int user_id,
            @Field("job_id") int job_id);

    //updating an offer made for a job
    @FormUrlEncoded
    @POST("public/updateOffer/{offer_id}")
    Call<Result> updateOffer(
            @Path("offer_id") int offer_id,
            @Field("amount_offered") int amount_offered,
            @Field("offer_message") String offer_message,
            @Field("edit_count") int edit_count);

    //getting all the jobs the fixer has made an offer to
    @GET("public/getOffersMade/{user_id}")
    Call<UserJobs> getOffersMade(
            @Path("user_id") int user_id);

    //getting all the jobs the fixer made an offer to and have been accepted
    @GET("public/getOffersAcceptedForFixer/{user_id}")
    Call<UserJobs> getOffersAcceptedForFixer(
            @Path("user_id") int user_id);

    //getting all the jobs the poster has accepted
    @GET("public/getOffersAcceptedForPoster/{user_id}")
    Call<UserJobs> getOffersAcceptedForPoster(
            @Path("user_id") int user_id);

    //getting the details of an offer made for a job for the fixer
    @GET("public/getOfferDetailsForFixer/{offer_id}")
    Call<UserJobs> getOfferDetailsForFixer(
            @Path("offer_id") int offer_id);

    //getting all the jobs posted by the poster which have offers made to
    @GET("public/getOffersForJobs/{user_id}")
    Call<UserJobs> getOffersForJobs(
            @Path("user_id") int user_id);

    //getting the details of an offer made for a job
    @GET("public/getOfferDetailsForPoster/{offer_id}")
    Call<UserJobs> getOfferDetailsForPoster(
            @Path("offer_id") int offer_id);

    //updating offer seen status to 1 - seen by poster
    @FormUrlEncoded
    @POST("public/updateOfferSeenStatus/{offer_id}")
    Call<Result> updateOfferSeenStatus(
            @Path("offer_id") int offer_id,
            @Field("status") int status);

    //updating offer to 1 - accepted status
    @FormUrlEncoded
    @POST("public/posterAcceptOffer")
    Call<Result> posterAcceptOffer(
            @Field("offer_id") int offer_id,
            @Field("job_id") int job_id,
            @Field("status") int status);

    //updating offer to 2 - rejected status by poster
    //when an offer is rejected, it is deleted from the offers table
    @FormUrlEncoded
    @POST("public/posterRejectOffer")
    Call<Result> posterRejectOffer(
            @Field("offer_id") int offer_id,
            @Field("job_id") int job_id,
            @Field("status") int status);

    //updating offer to 3 - rejected status by fixer
    @FormUrlEncoded
    @POST("public/fixerRejectOffer/{offer_id}")
    Call<Result> fixerRejectOffer(
            @Path("offer_id") int offer_id,
            @Field("job_id") int job_id,
            @Field("status") int status);

    //check if fixer has already made an offer for a job
    @GET("public/checkOfferAlreadyMade/{userId}/{jobId}")
    Call<Result> checkOfferAlreadyMade(
            @Path("userId") int userId,
            @Path("jobId") int jobId);

    //updating job to 4 - Job in Progress
    @FormUrlEncoded
    @POST("public/fixerStartJob")
    Call<Result> fixerStartJob(
            @Field("offer_id") int offer_id,
            @Field("job_id") int job_id);

    //updating job to 5 - completed / finished
    @FormUrlEncoded
    @POST("public/fixerFinishJob")
    Call<Result> fixerFinishJob(
            @Field("offer_id") int offer_id,
            @Field("job_id") int job_id);

    //getting the details of a job in progress
    @GET("public/getJIPDetails/{offer_id}")
    Call<UserJobs> getJIPDetails(
            @Path("offer_id") int offer_id);

    //to make mobile money payment
    //called a collection request using the Beyonic API
    @FormUrlEncoded
    @POST("public/makeMobileMoneyPayment")
    Call<Result> makeMobileMoneyPayment(
            @Field("job_id") int job_id,
            @Field("poster_id") int poster_id,
            @Field("fixer_id") int fixer_id,
            @Field("offer_id") int offer_id,
            @Field("job_cost") int job_cost,
            @Field("amnt_fixer_gets") int amnt_fixer_gets,
            @Field("service_fee") int service_fee);

    //to make cash payment
    @FormUrlEncoded
    @POST("public/makeCashPayment")
    Call<Result> makeCashPayment(
            @Field("job_id") int job_id,
            @Field("poster_id") int poster_id,
            @Field("fixer_id") int fixer_id,
            @Field("offer_id") int offer_id,
            @Field("job_cost") int job_cost,
            @Field("amnt_fixer_gets") int amnt_fixer_gets,
            @Field("service_fee") int service_fee);

    //submitting poster rating
    @FormUrlEncoded
    @POST("public/submitPosterRating")
    Call<Result> submitPosterRating(
            @Field("job_id") int job_id,
            @Field("fixer_id") int fixer_id,
            @Field("poster_id") int poster_id,
            @Field("rating_value") float rating_value,
            @Field("fixer_comment") String comment);

    //submitting fixer rating
    @FormUrlEncoded
    @POST("public/submitFixerRating")
    Call<Result> submitFixerRating(
            @Field("job_id") int job_id,
            @Field("poster_id") int poster_id,
            @Field("fixer_id") int fixer_id,
            @Field("rating_value") float rating_value,
            @Field("poster_comment") String comment);

    //send the phone number o which the otp will be
    //sent
    @FormUrlEncoded
    @POST("public/sendVerifyCode/{user_id}")
    Call<Result> sendPhoneNumber(
            @Path("user_id") int user_id,
            @Field("phone_number") String phone_number);

    //send the otp received on the user's phone
    @FormUrlEncoded
    @POST("public/verifyPhoneNumber/{user_id}")
    Call<Result> sendOtp(
            @Path("user_id") int user_id,
            @Field("otp") String otp);

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
