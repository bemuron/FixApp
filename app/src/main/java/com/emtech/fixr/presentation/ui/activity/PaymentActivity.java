package com.emtech.fixr.presentation.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.emtech.fixr.R;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.ui.fragment.PaymentSummaryFragment;

public class PaymentActivity extends AppCompatActivity implements PaymentSummaryFragment.OnPaymentSummaryListener {
    private static final String TAG = PaymentActivity.class.getSimpleName();
    private SessionManager session;
    private String userRole;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // session manager
        session = new SessionManager(getApplicationContext());
        userRole = session.getUserRole();
        userId = session.getUserId();

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
            //this mainly targets a user/student
            //this is coming from the fcm notification received
            int job_id = getIntent().getIntExtra("job_id", 0);
            boolean session_finished = getIntent().getBooleanExtra("session_finished", false);
            if (session_finished) {
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

    //set up the payment summary fragment
    private void setUpPaymentSummaryFragment(){
        int job_id = getIntent().getIntExtra("job_id", 0);
        int fixer_id = getIntent().getIntExtra("fixer_id", 0);
        int poster_id = getIntent().getIntExtra("poster_id", 0);
        int job_cost = getIntent().getIntExtra("job_cost", 0);
        PaymentSummaryFragment paymentSummaryFragment = PaymentSummaryFragment.newInstance(job_id, poster_id, job_cost);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.payment_fragment_container, paymentSummaryFragment)
                .commit();
    }

    @Override
    public void onPaymentSummaryInteraction(int job_id, int poster_id) {
        Intent intent = new Intent(PaymentActivity.this, RatingActivity.class);
        intent.putExtra("job_id", job_id);
        intent.putExtra("poster_id", poster_id);
        startActivity(intent);
    }
}
