package com.emtech.fixr.presentation.ui.fragment;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.models.User;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class PostJobFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = PostJobFragment.class.getSimpleName();
    OnPostButtonListener mCallback;
    private TextView postJobInstructionsTextView, jobMustHaves;
    private final static int WRITE_EXTERNAL_RESULT = 100;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 55;
    private static final int SELECT_IMAGE_REQUEST_CODE =25 ;
    private EditText jobTitleEditText;
    private EditText jobDescEditText, jobLocationEditText;
    private ImageView jobImage1, jobImage2, jobImage3;
    private ProgressDialog pDialog;
    private Button postButton;
    private Bitmap bitmap;
    private File file;
    private int categoryId, userId;
    private ScrollView layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;
    private String categoryName, mediaPath, currentJobImage = null;
    private Switch jobLocationSwitch;

    public PostJobFragment(){

    }

    public static PostJobFragment newInstance(int userId, int categoryId, String categoryName){
        Bundle arguments = new Bundle();
        arguments.putInt("user_id", userId);
        arguments.putInt("category_id", categoryId);
        arguments.putString("category_name", categoryName);
        PostJobFragment fragment = new PostJobFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_job,container,false);

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getInt("user_id");
            categoryId = bundle.getInt("category_id");
            categoryName = bundle.getString("category_name");
        }

        setUpWidgets(view);
        handleBottomSheet();
        return view;
    }

    // Container Activity must implement this interface
    public interface OnPostButtonListener {
        void jobPostDataCallback(int userId, String jobTitle, String jobDesc, String jobLocation,
                                        File file, int categoryId, PostJobActivity postJobActivity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPostButtonListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnPostButtonListener");
        }
    }

    //initialise the views
    private void setUpWidgets(View view){
        //find the bottom sheet layout
        layoutBottomSheet = view.findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setPeekHeight(0);
        postJobInstructionsTextView = view.findViewById(R.id.post_job_instructions);
        postJobInstructionsTextView.setText("Post your fixer-apper to the community of professional "+categoryName+"s.");
        jobMustHaves = view.findViewById(R.id.job_must_haves);
        jobMustHaves.setOnClickListener(this);
        jobTitleEditText = view.findViewById(R.id.edit_text_job_title);
        jobDescEditText = view.findViewById(R.id.edit_text_job_desc);
        jobLocationEditText = view.findViewById(R.id.edit_text_job_location);
        jobImage1 = view.findViewById(R.id.job_image1);
        jobImage1.setOnClickListener(this);
        jobImage2 = view.findViewById(R.id.job_image2);
        jobImage2.setOnClickListener(this);
        jobImage3 = view.findViewById(R.id.job_image3);
        jobImage3.setOnClickListener(this);
        postButton = view.findViewById(R.id.continue_one);
        postButton.setOnClickListener(this);
        jobLocationSwitch = view.findViewById(R.id.job_location_switch);
        jobLocationSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                jobLocationEditText.setVisibility(View.GONE);

            } else {
                jobLocationEditText.setVisibility(View.VISIBLE);
            }

        });
    }

    //handle bottom sheet state
    private void handleBottomSheet(){
        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        Log.e(TAG, "Expand sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        Log.e(TAG, "close sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    //handles user selection of image
    private void selectJobImage(String jobImage){
        Intent intent = new Intent();
        intent.putExtra("image_pos", jobImage);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);

    }

    //method to get what user has filled in
    public  void getJobDetails(String image_pos) {
        String jobTitle = jobTitleEditText.getText().toString().trim();
        if (TextUtils.isEmpty(jobTitle)) {
            jobTitleEditText.setError("Job Title is required");
            //return false;
        }

        String jobDesc = jobDescEditText.getText().toString().trim();
        if (TextUtils.isEmpty(jobDesc)) {
            jobDescEditText.setError("Job description is required");
            //return false;
        }

        String jobLocation = jobLocationEditText.getText().toString().trim();
        if (TextUtils.isEmpty(jobDesc)) {
            jobLocationEditText.setError("Job location is required");
            //return false;
        }

        //Map is used to multipart the file using okhttp3.RequestBody
        if (mediaPath != null) {
            file = new File(mediaPath);

            if (!jobTitle.isEmpty() && !jobDesc.isEmpty() && file.exists()) {

                //send data to parent activity to be posted to the server
                mCallback.jobPostDataCallback(userId, jobTitle, jobDesc, jobLocation,
                        file, categoryId, PostJobActivity.getInstance());
            }
        }
    }

    //method to display appropriate instructions for posting a job based
    //on the category name
    private void displayPostInstructions(String categoryName){
        switch (categoryName){

        }
    }

    //handle clicks on the various views
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.continue_one:
                //send the data to PostJobActivity to be posted to the server
                getJobDetails("job_image1");
                break;

            case R.id.job_image1:
                //handle click on job image 1 to select the image
                //checking if we have been given the permission
                boolean result = checkAndRequestPermissions();
                if (result) {
                    selectJobImage("job_image1");
                    currentJobImage = "job_image1";
                    postButton.setEnabled(true);
                    //getJobDetails("job_image1");
                }
                break;

            case R.id.job_image2:
                //handle click on job image 2 to select the image
                //checking if we have been given the permission
                boolean result1 = checkAndRequestPermissions();
                if (result1) {
                    selectJobImage("job_image2");
                    currentJobImage = "job_image2";
                    postButton.setEnabled(true);
                    //getJobDetails("job_image2");
                }
                break;

            case R.id.job_image3:
                //handle click on job image 1 to select the image
                //checking if we have been given the permission
                boolean result2 = checkAndRequestPermissions();
                if (result2) {
                    selectJobImage("job_image3");
                    currentJobImage = "job_image3";
                    postButton.setEnabled(true);
                    //getJobDetails("job_image3");
                }
                break;

            case R.id.job_must_haves:
                //handle click on the job must haves text view
                //it will slide up the bottom sheet
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
        }

    }

    //get the pic the user has picked
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        //check for our request code
        if (requestCode == SELECT_IMAGE_REQUEST_CODE){
            //check if the request was successful
            if (resultCode == RESULT_OK && data != null){
                //Uri path = data.getData();

                try {
                    Uri path = data.getData();
                    String[] filePathcolumn = {MediaStore.Images.Media.DATA};
                    //MediaStore.Images.Media.DATA
                    //MediaStore.Images.ImageColumns.DATA

                    Cursor cursor = getActivity().getContentResolver().query(path,
                            null, null, null, null);
                    if (cursor == null) {
                        mediaPath = path.getPath();
                    }else {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        mediaPath = cursor.getString(columnIndex);
                    }

                    //get the image from the gallery
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), path);
                    jobImage1.setImageBitmap(bitmap);

                    /*
                    //get which image position this is
                    String jobImage = data.getStringExtra("image_pos");
                    //set the image to our image view
                    if (jobImage.equals("job_image1")) {
                        jobImage1.setImageBitmap(bitmap);
                    }else if (jobImage.equals("job_image2")){
                        jobImage2.setImageBitmap(bitmap);
                    }else if (jobImage.equals("job_image3")){
                        jobImage3.setImageBitmap(bitmap);
                    }
                    */

                    cursor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    //check if we have permission to write external storage
    private boolean checkAndRequestPermissions() {

        //checking for marshmallow devices and above in order to execute runtime
        //permissions
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            int permisionWriteExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionReadExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            //declare a list to hold the permissions we want to ask the user for
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (permisionWriteExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            //if the permissions list is not empty, then request for the permission
            if (!listPermissionsNeeded.isEmpty()){
                ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray
                        (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }else {
                return true;
            }
        }else {
            return true;
        }
    }


    //get the results of the permissions request
    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        Log.d(TAG, "Permission callback called ----");
        //fill with actual results from the user
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            int currentAPIVersion = Build.VERSION.SDK_INT;
            Map<String, Integer> perms = new HashMap<>();
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                //initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            }
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                //check for both permissions
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write and Read external storage permissions granted");
                    selectJobImage(currentJobImage);
                } else {
                    Log.d(TAG, "Some permissions are not granted, ask again");
                    //permission is denied (this is the first time, when "never ask again" is not checked)
                    //so ask again explaining the use of the permissions
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Permission Request")
                                .setMessage("Permission is required for the app to write and read from storage")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                WRITE_EXTERNAL_RESULT);
                                    }
                                })
                                .show();
                    }
                    //permission is denied and never ask again is checked
                    //shouldShowRequestPermissionRationale will return false
                    else {
                        Toast.makeText(getActivity(), "Go to settings and enable permissions",
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
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
