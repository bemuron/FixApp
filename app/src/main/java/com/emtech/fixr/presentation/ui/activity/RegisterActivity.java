package com.emtech.fixr.presentation.ui.activity;

import android.app.ProgressDialog;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.helpers.SessionManager;
import com.emtech.fixr.presentation.viewmodels.LoginRegisterActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.LoginRegistrationViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFirstName;
    private EditText inputLastName;
    private EditText inputEmail;
    private RadioGroup radioGender;
    private EditText dob;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = ViewModelProviders.of
                (this, factory).get(LoginRegisterActivityViewModel.class);

        inputFirstName = findViewById(R.id.firstName);
        inputLastName = findViewById(R.id.lastName);
        inputEmail = findViewById(R.id.edit_text_register_email);
        radioGender = findViewById(R.id.radioGender);
        dob = findViewById(R.id.date_of_birth);
        inputPassword = findViewById(R.id.edit_text_register_password);
        btnRegister = findViewById(R.id.btnRegister);
        btnLinkToLogin = findViewById(R.id.btnLinkToLoginScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    HomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        //get the info the user has typed in displaying errors where necessary
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //getting the user values
                int radioButtonId = radioGender.getCheckedRadioButtonId();
                RadioButton userGender = radioGender.findViewById(radioButtonId);

                String firstName = inputFirstName.getText().toString().trim();

                if (TextUtils.isEmpty(firstName)) {
                    inputFirstName.setError("Please enter your first name");
                }

                String lastName = inputLastName.getText().toString().trim();
                if (TextUtils.isEmpty(lastName)) {
                    inputLastName.setError("Please enter your last name");
                }

                String birth_date = dob.getText().toString().trim();
                if (TextUtils.isEmpty(birth_date)) {
                    dob.setError("Please enter your date of birth");
                }

                String email = inputEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Please enter your email");
                }
                String password = inputPassword.getText().toString().trim();
                if (TextUtils.isEmpty(firstName)) {
                    inputFirstName.setError("Please enter your password");
                }

                String gender = (String) userGender.getText();
                if (TextUtils.isEmpty(gender)) {
                    userGender.setError("Please select your gender");
                }

                if (!firstName.isEmpty() && !lastName.isEmpty()  && !birth_date.isEmpty()
                        && !email.isEmpty() && !password.isEmpty() && !gender.isEmpty()) {
                    String fullName = firstName + "" + lastName;
                    registerUser(fullName, birth_date, gender, email, password);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }//close onCreate()

    /**
     * Method to call viewmodel method to post user reg details to database
     * */
    private void registerUser(final String fullName, final String dob,
                              final String gender,  final String email,
                              final String password) {

        //disable clicks on the register button during registration process
        btnRegister.setClickable(false);

        pDialog.setMessage("Registering ...");
        showDialog();

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.createUser(fullName, dob, gender, email, password);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                hideDialog();

                //ResBeanSignup resBean = response.body();
                //String response_status = Encryption.decrypt(resBean.getResponse_status());
                try {

                    if (!response.body().getError()) {
                        hideDialog();
                        //Log.d(LOG_TAG, EnumAppMessages.REGISTER_SUCCESS_TITLE.getValue());
                        Log.d(TAG, response.body().getMessage());

                        Toast toast = Toast.makeText(RegisterActivity.this,
                                "Registration successful. Login now", Toast.LENGTH_LONG);

                        toast.show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "Error msg from inside response body: "+e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                hideDialog();
                //print out any error we may get
                //probably server connection
                Log.e(TAG, t.getMessage());
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                btnRegister.setClickable(true);
            }
        });

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
