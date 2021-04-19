package com.emtech.fixr.data.network;

import android.content.Context;
import android.util.Log;

import com.emtech.fixr.AppExecutors;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emtech.fixr.presentation.ui.activity.PaymentActivity.paymentActivity;

public class MakePayments {
    private static final String LOG_TAG = MakePayments.class.getSimpleName();
    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static MakePayments sInstance;
    private final Context mContext;

    public MakePayments(Context context, AppExecutors executors) {
        mContext = context;
    }

    /**
     * Get the singleton for this class
     */
    public static MakePayments getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new MakePayments(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    //retrofit call to make mobile money payment
    public void MakeMobileMoneyPayment(int job_id, int poster_id, int fixer_id, int offer_id,
                                       int job_cost, int service_fee, int amnt_fixer_gets){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.makeMobileMoneyPayment(job_id, poster_id,
                fixer_id, offer_id, job_cost, amnt_fixer_gets, service_fee);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, "Response message "+response.body().getMessage());
                        //send response data to the activity
                        //success
                        paymentActivity.paymentByMobileMoneyResponse(true,
                                response.body().getMessage());
                    }else{
                        Log.e(LOG_TAG, "Error occured "+response.body().getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                    Log.e(LOG_TAG, "Response message "+response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                t.printStackTrace();
                Log.e(LOG_TAG, "Error making payment "+t.getMessage());
                paymentActivity.paymentByMobileMoneyResponse(false,
                        t.getMessage());
            }
        });

    }

    //retrofit call to make mobile money payment
    public void MakeCashPayment(int job_id, int poster_id, int fixer_id, int offer_id,
                                       int job_cost, int service_fee, int amnt_fixer_gets){

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.makeCashPayment(job_id, poster_id,
                fixer_id, offer_id, job_cost, amnt_fixer_gets, service_fee);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try {
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        //send response data to the activity
                        //success
                        paymentActivity.paymentByCashResponse(true,
                                response.body().getMessage());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                paymentActivity.paymentByCashResponse(false,
                        t.getMessage());
            }
        });

    }
}
