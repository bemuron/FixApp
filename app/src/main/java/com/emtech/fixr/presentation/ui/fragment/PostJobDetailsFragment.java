package com.emtech.fixr.presentation.ui.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.emtech.fixr.BuildConfig;
import com.emtech.fixr.data.network.PostFixAppJob;
import com.emtech.fixr.helpers.CircleTransform;
import com.emtech.fixr.models.UploadImage;
import com.emtech.fixr.presentation.adapters.UploadImagesAdapter;
import com.emtech.fixr.presentation.ui.activity.HomeActivity;
import com.emtech.fixr.presentation.ui.activity.PostJobActivity;
import com.emtech.fixr.presentation.viewmodels.PostJobActivityViewModel;
import com.emtech.fixr.presentation.viewmodels.PostJobViewModelFactory;
import com.emtech.fixr.utilities.InjectorUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.emtech.fixr.R;
import com.emtech.fixr.models.JobMustHave;
import com.emtech.fixr.presentation.adapters.MustHavesAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.CAMERA_SERVICE;
import static android.os.Environment.DIRECTORY_PICTURES;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PostJobDetailsFragment extends Fragment implements View.OnClickListener,
        MustHavesAdapter.MustHavesAdapterListener, UploadImagesAdapter.UploadImagesAdapterListener
{
    private static final String TAG = PostJobDetailsFragment.class.getSimpleName();
    OnPostButtonListener mCallback;
    OnImageClickListener mImageClickedCallback;
    OnCreateJobButtonListener mCreateJobCallback;
    private PostJobActivityViewModel postJobActivityViewModel;
    private TextView postJobInstructionsTextView, jobMustHaves;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 55;
    private static final int SELECT_IMAGE_REQUEST_CODE =25 ;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;
    private static final int M_MAX_ENTRIES = 5;
    private final static int AUTO_COMPLETE_REQUEST_CODE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 4;
    private TextInputEditText jobTitleEditText;
    private TextInputEditText jobDescEditText,jobLocationEditText;
    int[] resImg = {R.mipmap.ic_camera, R.drawable.ic_menu_gallery};
    String[] title = {"Camera", "Gallery"};
    private EditText mustHavesEditText;
    private TextInputLayout jobLocationTextInputLayout;
    private TextView mustHaveOneTv, mustHaveTwoTv, mustHaveThreeTv;
    private ProgressDialog pDialog;
    private Button postButton, addMustHaveButton, saveMustHavesButton, addImage;
    private MaterialButton createJobButton;
    private Bitmap bitmap;
    private RecyclerView recyclerView,imagesRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    private ArrayList<JobMustHave> mustHavesArrayList;
    private ArrayList<UploadImage> uploadImageArrayList;
    private ArrayList<File> imageFilesList = new ArrayList<>();
    private ArrayList<String> selectedImageList;
    private MustHavesAdapter mAdapter;
    private UploadImagesAdapter uploadImagesAdapter;
    private File file;
    private LinearLayout slideUpView;
    private SwitchMaterial isJobRemoteSwitch;
    private int categoryId, userId, mJobId = 0, isJobRemote = 0, imagePos;
    private ScrollView layoutBottomSheet;
    private boolean locationSwitchChecked = false;
    private BottomSheetBehavior sheetBehavior;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient mPlacesClient;
    private Boolean mLocationPermissionGranted = false;
    private Boolean mDisplayedUserLocation = false;
    private UploadImage imageModel;
    //private AutocompleteSupportFragment autocompleteFragment;
    //private JobMustHave mustHave;
    private String categoryName, mediaPath, currentJobImage = null,
            mustHaveOne = null, mustHaveTwo = null, mustHaveThree = null,
            mstHaveOne, mstHaveTwo, mstHaveThree, musthave,
            jobName, jobDescription, mCurrentPhotoPath, jobTitle, jobDesc;
    private TextInputLayout textInputLayout;
    String[] projection = {MediaStore.MediaColumns.DATA};
    private File imageFile, mPhotoFile;

    public PostJobDetailsFragment(){

    }

    public static PostJobDetailsFragment newInstance(int userId, int categoryId,
                                                     String categoryName, int jobId, String jobName,
                                                     String jobDescription, String mustHaveOne,
                                                     String mustHaveTwo, String mustHaveThree,
                                                     int isJobRemote, String jobImage){
        Bundle arguments = new Bundle();
        arguments.putInt("user_id", userId);
        arguments.putInt("category_id", categoryId);
        arguments.putString("category_name", categoryName);
        arguments.putInt("job_id", jobId);
        arguments.putString("job_name", jobName);
        arguments.putString("job_description", jobDescription);
        arguments.putString("must_have_one", mustHaveOne);
        arguments.putString("must_have_two", mustHaveTwo);
        arguments.putString("must_have_three", mustHaveThree);
        arguments.putInt("is_remote", isJobRemote);
        arguments.putString("job_image", jobImage);

        PostJobDetailsFragment fragment = new PostJobDetailsFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Places.
        //mPlacesClient = Places.createClient(getActivity());

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_post_job_details,container,false);

        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        /*Initialize Places.*/
        if (!Places.isInitialized()){
            Places.initialize(getContext(), "AIzaSyDOLKjt9f5qDpwcTCYAhJkUJzLBeEeMz1c");
        }

        // Initialize Places.
        mPlacesClient = Places.createClient(getActivity());

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getInt("user_id");
            categoryId = bundle.getInt("category_id");
            categoryName = bundle.getString("category_name");
            mJobId = bundle.getInt("job_id");
            jobName = bundle.getString("job_name");
            jobDescription = bundle.getString("job_description");
            mustHaveOne = bundle.getString("must_have_one");
            mustHaveTwo = bundle.getString("must_have_two");
            mustHaveThree = bundle.getString("must_have_three");
            isJobRemote = bundle.getInt("is_remote");
            currentJobImage = bundle.getString("job_image");
        }

        setUpWidgets(view);
        //searchPlacesWidget();
        //if we have a job id then the user is editing a job
        if (mJobId > 0){
            slideUp(slideUpView);
            inflateViews();
        }else{
            slideDown(slideUpView);
            createJobButton.setVisibility(View.VISIBLE);
        }
        //updateUserLocation();
        setUpMustHavesAdapter();

        setUpJobImagesAdapter();

        //handleBottomSheet();

        try {
            if (mLocationPermissionGranted) {
                updateUserLocation();
            }else{
                getLocationPermission();
            }
        }catch (SecurityException e)  {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
            Log.e("Exception: %s", e.getMessage());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PostJobViewModelFactory factory = InjectorUtils.providePostJobActivityViewModelFactory
                (PostJobActivity.getInstance().getApplicationContext());
        postJobActivityViewModel = new ViewModelProvider(PostJobActivity.getInstance(),
                factory).get(PostJobActivityViewModel.class);

        /*postJobActivityViewModel.getNewJobId().observe(PostJobActivity.getInstance(), jobId ->{
            mJobId = jobId;
            Log.e(TAG,"Created job id "+mJobId);

            if (mJobId > 0){
                slideUp(slideUpView);
            }else{
                slideDown(slideUpView);
                createJobButton.setVisibility(View.VISIBLE);
            }
        });*/
    }

    /*private void searchPlacesWidget(){
        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getActivity().getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                jobLocationEditText.setText(place.getName() + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.e(TAG, "An error occurred: " + status);
            }
        });

    }*/

    private void getJobLocation(){
        //set the fields to specify which types of place data to
        //return after the user has made a selection
        List<Place.Field> fields = Arrays.asList(Place.Field.ID);

        //start the autocomplete intent
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                fields).build(getActivity());
        startActivityForResult(intent, AUTO_COMPLETE_REQUEST_CODE);
    }

    //remove the must have when the user clicks on the delete button
    @Override
    public void onIconDeleteClicked(int position) {
        //mAdapter.resetAnimationIndex();
        //List<Integer> selectedItemPositions =
          //      mAdapter.getSelectedItems();
        //for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            //mAdapter.removeData(selectedItemPositions.get(i));
        mAdapter.removeData(position);
        //}
        mAdapter.notifyDataSetChanged();
    }

    //remove the image the user had selected for upload
    @Override
    public void onDeleteImageClicked(int position) {

    }

    //handle different actions when the image position is clicked
    @Override
    public void onImageClicked(int position, String imagePath) {
        if (position == 0) {
            if (checkAndRequestPermissions()){
                takePicture();
            }
        } else if (position == 1) {
            if (checkAndRequestPermissions())
                selectJobImage();
        }else{
            //show full screen image
            Log.e(TAG, "Image at Position clicked " +position);
            mImageClickedCallback.onUploadImageClicked(imagePath);

        }
    }

    @Override
    public void onImageLongClicked(int position) {
         if (position > 1){
             Log.e(TAG, "Image picker Position " +position);
            try {
                if (!uploadImageArrayList.get(position).isSelected()) {
                    selectImage(position);
                } else {
                    unSelectImage(position);
                }
            } catch (ArrayIndexOutOfBoundsException ed) {
                ed.printStackTrace();
            }
        }
    }

    // create the new job using mainly the title and description
    public interface OnCreateJobButtonListener {
        void createJobCallback(int userId, String jobTitle, String jobDesc,
                               int categoryId);
    }

    // Container Activity must implement this interface
    public interface OnPostButtonListener {
        void jobPostDataCallback(int userId, String jobTitle, String jobDesc, String jobLocation,
                                 String mustHaveOne, String mustHaveTwo, String mustHaveThree, int isJobRemote,
                                 ArrayList<File> imageFilesList, int categoryId, PostJobActivity postJobActivityInstance);
    }

    //display full screen of the image the user selected for upload
    public interface OnImageClickListener{
        void onUploadImageClicked(String imagePath);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPostButtonListener) context;
            mImageClickedCallback = (OnImageClickListener) context;
            mCreateJobCallback = (OnCreateJobButtonListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnPostButtonListener or OnImageClickListener or OnCreateJobButtonListener");
        }
    }

    //inflate the views in case the user is editing a job
    private void inflateViews(){
        //job name and desc
        jobTitleEditText.setText(jobName);
        jobDescEditText.setText(jobDescription);

        //is job remote or not
        if (isJobRemote == 1){
            isJobRemoteSwitch.setChecked(true);
        }else if (isJobRemote == 0){
            isJobRemoteSwitch.setChecked(false);
        }

        //must haves
        if (mustHaveOne != null || mustHaveTwo != null || mustHaveThree != null){
            mustHaveOneTv.setText(mustHaveOne);
            mustHaveTwoTv.setText(mustHaveTwo);
            mustHaveTwoTv.setText(mustHaveTwo);
        }

        //Attached image
        /*Glide.with(getActivity()).load("http://www.emtechint.com/fixapp/assets/images/"+currentJobImage)
                .thumbnail(0.5f)
                .transition(withCrossFade())
                .apply(new RequestOptions().fitCenter()
                        .transform(new CircleTransform(getActivity())).diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(jobImage1);
        jobImage1.setColorFilter(null);*/

    }

    //initialise the views
    private void setUpWidgets(View view){
        slideUpView = view.findViewById(R.id.slide_up_layout);
        recyclerView = view.findViewById(R.id.must_haves_list);
        imagesRecyclerView = view.findViewById(R.id.recyclerview_upload_images);
        //find the bottom sheet layout
        layoutBottomSheet = view.findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setPeekHeight(0);
        postJobInstructionsTextView = view.findViewById(R.id.post_job_instructions);
        postJobInstructionsTextView.setText("Post your job/task to the community of professional "+categoryName+"s.");
        jobMustHaves = view.findViewById(R.id.job_must_haves);
        jobMustHaves.setOnClickListener(this);
        jobLocationTextInputLayout = view.findViewById(R.id.job_location);
        isJobRemoteSwitch = view.findViewById(R.id.is_remote_job_switch);
        isJobRemoteSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                jobLocationTextInputLayout.setVisibility(View.GONE);
                locationSwitchChecked = true;
                isJobRemote = 1;//true

            } else {
                jobLocationTextInputLayout.setVisibility(View.VISIBLE);
                locationSwitchChecked = false;
                isJobRemote = 0;//false
            }
        });
        jobTitleEditText = view.findViewById(R.id.edit_text_job_title);
        jobDescEditText = view.findViewById(R.id.edit_text_job_desc);
        jobLocationEditText = view.findViewById(R.id.edit_text_job_location);
        jobLocationEditText.setOnClickListener(this);
        mustHavesEditText = view.findViewById(R.id.edit_text_must_haves_input);
        postButton = view.findViewById(R.id.continue_one);
        postButton.setOnClickListener(this);
        addMustHaveButton = view.findViewById(R.id.add_must_have_button);
        addMustHaveButton.setOnClickListener(this);
        saveMustHavesButton = view.findViewById(R.id.save_must_haves_button);
        saveMustHavesButton.setOnClickListener(this);
        mustHaveOneTv = view.findViewById(R.id.must_have_one);
        mustHaveTwoTv = view.findViewById(R.id.must_have_two);
        mustHaveThreeTv = view.findViewById(R.id.must_have_three);
        mustHavesArrayList = new ArrayList<>();
        selectedImageList = new ArrayList<>();
        uploadImageArrayList = new ArrayList<>();
        createJobButton = view.findViewById(R.id.createJobButton);
        createJobButton.setOnClickListener(this);
    }

    //slide the view from below itself to the current position
    public void slideUp(LinearLayout view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(0,0,view.getHeight(),0);
        animation.setDuration(700);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    //slide the view from current position to below itself
    public void slideDown(LinearLayout view){
        TranslateAnimation animation = new TranslateAnimation(0,0,0,view.getHeight());
        animation.setDuration(700);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    //set up the list adapter to handle the job must haves
    private void setUpMustHavesAdapter(){
        mAdapter = new MustHavesAdapter(PostJobActivity.getInstance(), mustHavesArrayList,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(PostJobActivity.getInstance().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(PostJobActivity.getInstance(),
                LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
    }

    //set up the list adapter to handle the images the user adds for upload
    private void setUpJobImagesAdapter(){
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(PostJobActivity.getInstance().getApplicationContext(), 4);
        imagesRecyclerView.setLayoutManager(layoutManager);
        //imagesRecyclerView.setHasFixedSize(true);
        //imagesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        //imagesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        uploadImagesAdapter = new UploadImagesAdapter(PostJobActivity.getInstance(), uploadImageArrayList,this);
        imagesRecyclerView.setAdapter(uploadImagesAdapter);
        setImagePickerList();
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
    private void selectJobImage(){
        Intent intent = new Intent();
        //intent.putExtra("image_pos", jobImage);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
    }

    // Add image in SelectedArrayList
    public void selectImage(int position) {
        // Check before add new item in ArrayList;
        if (!selectedImageList.contains(uploadImageArrayList.get(position).getImage())) {
            uploadImageArrayList.get(position).setSelected(true);
            selectedImageList.add(0, uploadImageArrayList.get(position).getImage());
            //selectedImageAdapter.notifyDataSetChanged();
            uploadImagesAdapter.notifyDataSetChanged();
        }
    }

    // Remove image from selectedImageList
    public void unSelectImage(int position) {
        for (int i = 0; i < selectedImageList.size(); i++) {
            if (uploadImageArrayList.get(position).getImage() != null) {
                if (selectedImageList.get(i).equals(uploadImageArrayList.get(position).getImage())) {
                    uploadImageArrayList.get(position).setSelected(false);
                    selectedImageList.remove(i);
                    //selectedImageAdapter.notifyDataSetChanged();
                    uploadImagesAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    // Add Camera and Folder in ArrayList
    public void setImagePickerList(){
        for (int i = 0; i < resImg.length; i++) {
            UploadImage imageModel = new UploadImage();
            imageModel.setResImg(resImg[i]);
            imageModel.setTitle(title[i]);
            uploadImageArrayList.add(i, imageModel);
        }
        uploadImagesAdapter.notifyDataSetChanged();
    }

    // start the image capture Intent
    public void takePicture(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Continue only if the File was successfully created;
        File photoFile = createImageFile();
        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri imageUri = FileProvider.getUriForFile(PostJobActivity.getInstance(),
                        BuildConfig.APPLICATION_ID + ".provider",photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else{
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public File createImageFile() {
        // Create an image file name
        String formattedDate = new SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());
        String imageFileName = "FAIMG_" + formattedDate + "_P"+userId+"J"+mJobId+"_";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File mFile = null;

            try {
                //imageFile = createImageFile();
                File storageDir = getActivity().getExternalFilesDir(DIRECTORY_PICTURES);
                mFile = File.createTempFile(imageFileName,".jpg",storageDir);
                Log.e(TAG,"Cam image File in try block = "+ mFile);
            }catch (Exception e){
                e.printStackTrace();
            }
            assert mFile != null;
            mPhotoFile = mFile;
            mCurrentPhotoPath = mFile.getAbsolutePath();
            Log.e(TAG,"Abs Path = "+ mFile.getAbsolutePath());
            //mCurrentPhotoPath = file.getAbsolutePath();
            Log.e(TAG,"Cam image File = "+ mFile);
            return mFile;
        }else {
            File storageDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
            try {
                imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPhotoFile = imageFile;
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
            Log.e(TAG,"Path = "+mCurrentPhotoPath);
            Log.e(TAG,"Cam image File = "+ imageFile);
            return imageFile;
        }
    }

    //method to create the job
    private void createNewJob(){
        jobTitle = jobTitleEditText.getText().toString().trim();
        if (TextUtils.isEmpty(jobTitle)) {
            jobTitleEditText.setError("Job Title is required");
            //return false;
        }

        jobDesc = jobDescEditText.getText().toString().trim();
        if (TextUtils.isEmpty(jobDesc)) {
            jobDescEditText.setError("Job description is required");
            //return false;
        }

        if (!jobTitle.isEmpty() && !jobDesc.isEmpty()){
            Log.e(TAG, "Job details: userid = " + userId+ ", job title = "+ jobTitle+
                    ", job desc = " +jobDesc+", categoryid = " +categoryId);
            mCreateJobCallback.createJobCallback(userId, jobTitle, jobDesc, categoryId);
            //observes for a new job id received
            postJobActivityViewModel.getNewJobId().observe(PostJobActivity.getInstance(), jobId ->{
                mJobId = jobId;

                if (mJobId > 0){
                    slideUp(slideUpView);
                    createJobButton.setVisibility(View.GONE);
                }else{
                    slideDown(slideUpView);
                    createJobButton.setVisibility(View.VISIBLE);
                }

                Log.e(TAG,"New Job id "+mJobId);
            });
        }else{
            Toast.makeText(getActivity(), "Please provide a job title " +
                    "and description to continue.", Toast.LENGTH_LONG).show();
        }
    }

    //method to get what user has filled in
    public  void getJobDetails() {
        jobTitle = jobTitleEditText.getText().toString().trim();
        if (TextUtils.isEmpty(jobTitle)) {
            jobTitleEditText.setError("Job Title is required");
            //return false;
        }

        jobDesc = jobDescEditText.getText().toString().trim();
        if (TextUtils.isEmpty(jobDesc)) {
            jobDescEditText.setError("Job description is required");
            //return false;
        }

        String jobLocation = jobLocationEditText.getText().toString().trim();

        //job title and description are mandatory
        //after making making sure they are there we can update the job details,
        //then just keep updating with more details as the user adds
        if (!jobTitle.isEmpty() && !jobDesc.isEmpty()){
            //if location is specified
            //if location switch is not on then location should be specified
            //otherwise job is remote
            if (!locationSwitchChecked && TextUtils.isEmpty(jobLocation)){ //we want a location specified
                Toast.makeText(getActivity(), "Please include job location", Toast.LENGTH_LONG).show();
                jobLocationEditText.setError("Job location is required");
            }else if (!locationSwitchChecked && !TextUtils.isEmpty(jobLocation)){//location is specified
                //send data to parent activity to be posted to the server
                //image is not added but location is
                mCallback.jobPostDataCallback(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                        mustHaveThree, isJobRemote, null, categoryId, PostJobActivity.getInstance());
                Log.e(TAG, "1st musthaveone = "
                        +mustHaveOne+ ", musthavetwo = "+mustHaveTwo+ ", musthavethree = "+
                        mustHaveThree);
                //if image has been added
                //Map is used to multipart the file using okhttp3.RequestBody
                if (imageFilesList != null) {
                    //file = new File(mediaPath);
                    //if (file.exists()) {//image is added

                            //send data to parent activity to be posted to the server
                            mCallback.jobPostDataCallback(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                                    mustHaveThree, isJobRemote, imageFilesList, categoryId, PostJobActivity.getInstance());
                        Log.e(TAG, "2nd musthaveone = "
                                +mustHaveOne+ ", musthavetwo = "+mustHaveTwo+ ", musthavethree = "+
                                mustHaveThree);
                    //}
                }
                //image is not added but location is
            }/*else if (!locationSwitchChecked && mediaPath == null){
                    //send data to parent activity to be posted to the server
                    mCallback.jobPostDataCallback(userId, jobTitle, jobDesc, jobLocation, mustHaveOne, mustHaveTwo,
                            mustHaveThree, isJobRemote, null, categoryId, PostJobActivity.getInstance());
            }*/
            //image selected but location not
            if (imageFilesList != null && locationSwitchChecked){
                //file = new File(mediaPath);
                //if (file.exists()) {//image is added

                    //send data to parent activity to be posted to the server
                    mCallback.jobPostDataCallback(userId, jobTitle, jobDesc, null, mustHaveOne, mustHaveTwo,
                            mustHaveThree, isJobRemote, imageFilesList, categoryId, PostJobActivity.getInstance());
                    Log.e(TAG, "3rd musthaveone = "
                            +mustHaveOne+ ", musthavetwo = "+mustHaveTwo+ ", musthavethree = "+
                            mustHaveThree);
                //}
            }else if (imageFilesList == null && locationSwitchChecked){

                    //send data to parent activity to be posted to the server
                    mCallback.jobPostDataCallback(userId, jobTitle, jobDesc, null, mustHaveOne, mustHaveTwo,
                            mustHaveThree, isJobRemote, null, categoryId, PostJobActivity.getInstance());
                Log.e(TAG, "4th musthaveone = "
                        +mustHaveOne+ ", musthavetwo = "+mustHaveTwo+ ", musthavethree = "+
                        mustHaveThree);
            }

        }else{
            Toast.makeText(PostJobActivity.getInstance(),
                    "Job title and description are required", Toast.LENGTH_LONG).show();
        }
    }

    //getting the file name
    private void getFileName(Uri uri){
        Cursor returnCursor = PostJobActivity.getInstance().getContentResolver().query(uri,null,
                null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        createTempFile(name, uri);
        Log.e(TAG, "Name of file = "+name);
        //return name;
    }

    //creating a temp file
    private File createTempFile(String name,Uri uri){
        String formattedDate = new SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());
        String imageFileName = "FAIMG_" + formattedDate + "_P"+userId+"J"+mJobId+"_";
        File file = null;
        try {
            File storageDir = PostJobActivity.getInstance().getExternalFilesDir(DIRECTORY_PICTURES);
            file = File.createTempFile(imageFileName, ".jpg", storageDir);
        }catch (IOException e){
            e.printStackTrace();
        }
        saveContentToFile(uri, file);
        return file;
    }

    //save the temp file using Okio
    private void saveContentToFile(Uri uri, File file){
        try {
            InputStream stream = PostJobActivity.getInstance().getContentResolver().openInputStream(uri);
            BufferedSource source = Okio.buffer(Okio.source(stream));
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(source);
            sink.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        //imageFilesList = new ArrayList<>();
        imageFilesList.add(file);
        Log.e(TAG, "Uri added = "+uri);
        Log.e(TAG, "Image file added = "+file);
        //return file;
    }

    private void getImageFile(Uri uri){

        File file = null;
        String[] project = {MediaStore.Images.Media.DATA};
        Cursor cursor = PostJobActivity.getInstance().getContentResolver().query(uri,null,
                null, null, null);
        if (cursor != null) {
            try {
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                imageFilesList = new ArrayList<>();
                file = new File(cursor.getString(column_index));
                Log.e(TAG, "Image file added = "+file);
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"Error "+e.getMessage());
            }
        }
        imageFilesList.add(file);
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
            case R.id.createJobButton:
                //check for internet connectivity
                if (isNetworkAvailable()) {
                    //send the data to PostJobActivity to be posted to the server
                    createNewJob();
                }else{
                    Toast.makeText(getActivity(),"Try checking your internet connection",
                            Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.continue_one:
                //check for internet connectivity
                if (isNetworkAvailable()) {
                    //send the data to PostJobActivity to be posted to the server
                    getJobDetails();
                }else{
                    Toast.makeText(getActivity(),"Try checking your internet connection",
                            Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.job_must_haves:
                //handle click on the job must haves text view
                //it will slide up the bottom sheet
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;

            case R.id.save_must_haves_button:
                //handle click on the save must haves button
                //it will collapse the bottom sheet
                //if no musthaves have been added yet, put the first one in the first text view
                mustHaveOne = mstHaveOne;
                mustHaveTwo = mstHaveTwo;
                mustHaveThree = mstHaveThree;
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;

            case R.id.add_must_have_button:
                musthave = mustHavesEditText.getText().toString().trim();
                if (TextUtils.isEmpty(musthave)) {
                    mustHavesEditText.setError("Please type in a must have");
                }else {
                   mstHaveOne = mustHaveOneTv.getText().toString().trim();
                   mstHaveTwo = mustHaveTwoTv.getText().toString().trim();
                   mstHaveThree = mustHaveThreeTv.getText().toString().trim();
//                    //if no musthaves have been added yet, put the first one in the first text view
                    if (mstHaveOne.length() == 0) {
                        //display what the user typed in the tv
                        mustHaveOneTv.setText("\u2713 " + musthave);
                        mustHavesEditText.setText("");
                        //mustHaveOne = mstHaveOne;
                    }if (mstHaveTwo.length() == 0 && mstHaveOne.length() > 0){
                        //display what the user typed in the tv
                        mustHaveTwoTv.setText("\u2713 " + musthave);
                        mustHavesEditText.setText("");
                        //mustHaveTwo = mstHaveTwo;

                    }if (mstHaveThree.length() == 0 && mstHaveTwo.length() > 0 && mstHaveOne.length() > 0){
                        //display what the user typed in the tv
                        mustHaveThreeTv.setText("\u2713 " + musthave);
                        mustHavesEditText.setText("");
                        //mustHaveThree = mstHaveThree;

                    }
                    if (mstHaveOne.length() > 0 && mstHaveTwo.length() > 0 && mstHaveThree.length() > 0){
                        //inform user that no more must haves cn be added
                        Toast.makeText(getActivity(), "Only 3 must haves can be added",
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case R.id.edit_text_job_location:
                try {
                    if (mLocationPermissionGranted) {
                        getJobLocation();
                    }else{
                        getLocationPermission();
                    }
                }catch (SecurityException e)  {
                    // The user has not granted permission.
                    Log.i(TAG, "The user did not grant location permission.");
                    Log.e("Exception: %s", e.getMessage());
                }
                break;
        }

    }

    //get the pic the user has picked
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        //check for our request code
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE_REQUEST_CODE){
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.e(TAG, "URI when getClipData not null: " + uri);
                        getImageFilePath(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    Log.e(TAG, "URI when getData not null: " + uri);
                    getImageFilePath(uri);
                }
            }else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (mCurrentPhotoPath != null) {
                    addImage(mCurrentPhotoPath);
                }
            }else if (requestCode == AUTO_COMPLETE_REQUEST_CODE){
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName());
                jobLocationEditText.setText(place.getName());
            }
        }else if (resultCode == AutocompleteActivity.RESULT_ERROR){
            Status status = Autocomplete.getStatusFromIntent(data);
            //handle error
            Log.i(TAG, status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED){
            //the user cancelled the operation
        }
    }

    //check if we have permission to camera and write external storage
    private boolean checkAndRequestPermissions() {

        //checking for marshmallow devices and above in order to execute runtime
        //permissions
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            int permisionWriteExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionReadExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int permissionCamera = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);

            //declare a list to hold the permissions we want to ask the user for
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (permisionWriteExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissionCamera != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
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
                //initialize the map with the permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
            }
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                //check for the permissions
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write, Read external storage and Camera permissions granted");
                    selectJobImage();
                    takePicture();
                } else {
                    Log.d(TAG, "Some permissions are not granted, ask again");
                    //permission is denied (this is the first time, when "never ask again" is not checked)
                    //so ask again explaining the use of the permissions
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)){
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Permission Request")
                                .setMessage("Permission is required for the app to write and read from storage")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.CAMERA},
                                                REQUEST_ID_MULTIPLE_PERMISSIONS);
                                    }
                                })
                                .show();
                    }
                    //permission is denied and never ask again is checked
                    //shouldShowRequestPermissionRationale will return false
                    else {
                        Toast.makeText(getActivity(), "Please go to settings and enable required permissions",
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
        }else
        //if location permission is granted
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //method to update the edit text with the location of the user
    //which the app has automatically received
    private void updateUserLocation(){
        showCurrentPlace();
        Log.e(TAG, "Attempt to show user selected location");
    }

    //get the current place
    private void showCurrentPlace() {
        //mDisplayedUserLocation = false;
        if (mLocationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    mPlacesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse likelyPlaces = task.getResult();

                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {

                        // update the edit text with the likely place the user is in
                        jobLocationEditText.setText(placeLikelihood.getPlace().getName());
                        Log.e(TAG, "Possible user location = "+placeLikelihood.getPlace().getName());
                    }
                    mDisplayedUserLocation = true;
                }
                else {
                    Log.e(TAG, "Exception: %s", task.getException());
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");
            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    public String getRealPathFromURI(Uri uri){
            String[] proj = {MediaStore.Images.Media.DATA};
            CursorLoader loader = new CursorLoader(PostJobActivity.getInstance(),uri,proj,
                    null,null,null);

        Cursor cursor = loader.loadInBackground();
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        return result;
    }

    // Get image file path
    public void getImageFilePath(Uri uri) {
        imageModel = new UploadImage();
        imageModel.setImagePath(uri);
        getFileName(uri);
        Cursor cursor = PostJobActivity.getInstance().getContentResolver()
                .query(uri, projection, null,    null, null);
        if (cursor != null) {
            while  (cursor.moveToNext()) {
                String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                if (absolutePathOfImage != null) {
                    checkImage(absolutePathOfImage);
                } else {
                    checkImage(String.valueOf(uri));
                }
            }
        }
    }

    public void checkImage(String filePath) {
        // Check before adding a new image to ArrayList to avoid duplicate images
        if (!selectedImageList.contains(filePath)) {
            for (int pos = 0; pos < uploadImageArrayList.size(); pos++) {
                if (uploadImageArrayList.get(pos).getImage() != null) {
                    if (uploadImageArrayList.get(pos).getImage().equalsIgnoreCase(filePath)) {
                        uploadImageArrayList.remove(pos);
                    }
                }
            }
            //addImage(filePath);
            addImageFromGallery(filePath);
        }
    }

    // add image in selectedImageList and imageList
    public void addImage(String filePath) {
        imageModel = new UploadImage();
        imageModel.setImage(filePath);
        imageModel.setSelected(false);
        uploadImageArrayList.add(imageModel);
        imageFilesList.add(mPhotoFile);
        //imageFilesList.add(filePath);
        Log.e(TAG, "photo file "+mPhotoFile);
        //Log.e(TAG, "filepath object "+filePath);
        uploadImagesAdapter.refreshImageList(uploadImageArrayList);
    }

    private void addImageFromGallery(String filePath){
        imagePos = uploadImageArrayList.size();
        Log.e(TAG, "Image list size "+uploadImageArrayList.size());
        //uploadImagesAdapter.addNewImageToList(filePath);

        //imageModel = new UploadImage();
        imageModel.setImage(filePath);
        imageModel.setSelected(false);
        uploadImageArrayList.add(imageModel);
        //get the actual files for uploading
        //using the uri
        //getFileName(uploadImageArrayList);
        uploadImagesAdapter.refreshImageList(uploadImageArrayList);
    }

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
