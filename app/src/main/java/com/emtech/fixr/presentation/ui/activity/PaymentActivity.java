package com.emtech.fixr.presentation.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.emtech.fixr.R;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.PaymentSummaryFragment;
import com.emtech.fixr.presentation.viewmodels.MyJobsActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PaymentActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PaymentViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

public class PaymentActivity extends AppCompatActivity implements PaymentSummaryFragment.OnPaymentSummaryListener,
        PaymentSummaryFragment.OnPaymentByMobileMoneyListener{
    private static final String TAG = PaymentActivity.class.getSimpleName();
    public static PaymentActivity paymentActivity;
    private SessionManager session;
    private String userRole, fixerProfPicName, posterProfPicName, posterName, fixerName;
    private int userId, fixer_id, offer_id, job_id, poster_id;
    private PaymentActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        paymentActivity = this;

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

        Log.e(TAG,"User role = "+userRole);

        fixerProfPicName = getIntent().getStringExtra("fixer_prof_pic");
        posterProfPicName = getIntent().getStringExtra("poster_prof_pic");

        fixerName = getIntent().getStringExtra("fixer_name");
        posterName = getIntent().getStringExtra("poster_name");

        fixer_id = getIntent().getIntExtra("fixer_id", 0);
        offer_id = fixer_id = getIntent().getIntExtra("offer_id", 0);
        job_id = getIntent().getIntExtra("job_id", 0);
        poster_id = getIntent().getIntExtra("poster_id", 0);

        PaymentViewModelFactory modelFactory = InjectorUtils.providePaymentActivityViewModelFactory(this.getApplicationContext());
        viewModel = new ViewModelProvider(this, modelFactory).get(PaymentActivityViewModel.class);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.payment_fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }

        int poster_id = getIntent().getIntExtra("poster_id", 0);
        if (userRole.equals("poster") || userId == poster_id) {
            //get intent from which this activity is called and the id of the job
            //this mainly targets a user/poster
            //this is coming from the fcm notification received
            int job_id = getIntent().getIntExtra("job_id", 0);
            boolean job_finished = getIntent().getBooleanExtra("job_finished", false);
            if (job_finished) {
                int job_cost = getIntent().getIntExtra("job_cost", 0);
                PaymentSummaryFragment paymentSummaryFragment =
                        PaymentSummaryFragment.newInstance(job_id,poster_id, job_cost);

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.payment_fragment_container, paymentSummaryFragment)
                        .commit();
            }
        }else {
            //launch the payment summary fragment
            setUpPaymentSummaryFragment();
        }

    }// close oncreate

    public PaymentActivity getInstance(){
        return paymentActivity;
    }

    //set up the payment summary fragment
    private void setUpPaymentSummaryFragment(){

        int job_cost = getIntent().getIntExtra("job_cost", 0);
        PaymentSummaryFragment paymentSummaryFragment = PaymentSummaryFragment.newInstance(job_id, poster_id, job_cost);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.payment_fragment_container, paymentSummaryFragment)
                .commit();
    }

    //called when the fixer is paid in cash
    @Override
    public void onPaymentSummaryInteraction(int job_id, int poster_id, int job_cost, int service_fee, int amnt_fixer_gets) {
        /*
         *TODO
         * record payment in db and also that cash was used
         * */
        //viewModel.makeCashPayment(job_id, poster_id, fixer_id, offer_id, job_cost, amnt_fixer_gets, service_fee);
        viewModel.makeMobileMoneyPayment(job_id, poster_id, fixer_id, offer_id, job_cost, amnt_fixer_gets, service_fee);
    }

    //called when fixer is paid using mobile money
    @Override
    public void onPaymentByMobileMoney(int job_id, int poster_id, int job_cost, int service_fee, int amnt_fixer_gets) {
        /*
         *TODO
         * get the user phone number and pass it here
         * */
        viewModel.makeMobileMoneyPayment(job_id, poster_id, fixer_id, offer_id, job_cost, amnt_fixer_gets, service_fee);
    }

    //called in MakePayments to provide the response to the payment by mobile money action
    public void paymentByMobileMoneyResponse(Boolean isPaymentMade, String message){
        if(isPaymentMade){
            Intent intent = new Intent(PaymentActivity.this, RatingActivity.class);
            intent.putExtra("job_id", job_id);
            intent.putExtra("poster_id", poster_id);
            intent.putExtra("fixer_id", fixer_id);
            intent.putExtra("poster_prof_pic", posterProfPicName);
            intent.putExtra("fixer_prof_pic", fixerProfPicName);
            intent.putExtra("poster_name", posterName);
            intent.putExtra("fixer_name", fixerName);
            startActivity(intent);
            finish();
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //called in MakePayments to provide the response to the payment by cash action
    public void paymentByCashResponse(Boolean isPaymentMade, String message){
        if (isPaymentMade){
            Intent intent = new Intent(PaymentActivity.this, RatingActivity.class);
            intent.putExtra("job_id", job_id);
            intent.putExtra("poster_id", poster_id);
            intent.putExtra("fixer_id", fixer_id);
            intent.putExtra("poster_prof_pic", posterProfPicName);
            intent.putExtra("fixer_prof_pic", fixerProfPicName);
            intent.putExtra("poster_name", posterName);
            intent.putExtra("fixer_name", fixerName);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

    }
}
