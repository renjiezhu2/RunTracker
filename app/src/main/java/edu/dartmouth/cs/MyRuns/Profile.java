package edu.dartmouth.cs.MyRuns;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

//import com.google.android.gms.common.api.GoogleApiClient;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Profile extends Activity {
    public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;

    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    private static final int REQUEST_CODE_TAKE_FROM_GALLERY = 1;
    private Uri mImageCaptureUri;
    private Uri mTempImageUri;

    private ImageView mImageView;
    private boolean isTakenFromCamera;

    private EditText mName;
    private EditText mEmail;
    private EditText mClass;
    private EditText mMajor;
    private EditText mPhone;

    // private int selected;
    private RadioGroup mgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mImageView = (ImageView) findViewById(R.id.picture);
        mImageView.setImageResource(0);

        mName = (EditText) findViewById(R.id.NameText);
        mEmail = (EditText) findViewById(R.id.email);
        mPhone = (EditText) findViewById(R.id.phone);
        mMajor = (EditText) findViewById(R.id.majorText);
        mClass = (EditText) findViewById(R.id.classNumber);

        if (savedInstanceState != null) {
            mImageCaptureUri = savedInstanceState
                    .getParcelable(URI_INSTANCE_STATE_KEY);
            mImageView.setImageURI(mImageCaptureUri);
        }
        if (savedInstanceState == null || mImageCaptureUri == null){
            loadSnap();
        }

        loadProfile();
    }

    // if save button is clicked
    public void onSaveClick(View v) {
        saveProfile();
        saveSnap();
        Toast.makeText(getApplicationContext(),
                getString(R.string.ui_profile_toast_save_text),
                Toast.LENGTH_SHORT).show();
        finish();
    }

    // if cancel button is clicked
    public void onCancelClick(View v) {
        Toast.makeText(Profile.this, R.string.cancel_string, Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image capture uri before the activity goes into background
        outState.putParcelable(URI_INSTANCE_STATE_KEY, mImageCaptureUri);
    }

    public void changePhotoClicked(View v) {
        // changing the profile image, show the dialog asking the user
        // to choose between taking a picture
        // Go to ProfileImageDialogFragment for details.
        //displayDialog(ProfileImageDialogFragment.DIALOG_ID_PHOTO_PICKER);
        displayDialog(ProfileImageDialogFragment.DIALOG_ID_PHOTO_PICKER);
    }

    // Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Construct temporary image path and name to save the taken
    // photo
    public void displayDialog(int id){
        DialogFragment fragment = ProfileImageDialogFragment.newInstance(id);
        fragment.show(getFragmentManager(), getString(R.string.dialog_fragment_tag_photo_picker));
    }

    //prompt dialog for user to choose either pick from gallery or take a photo
    public void onPhotoPickerItemSelected(int item) {
        Intent intent;
        switch (item) {
            case ProfileImageDialogFragment.ID_PHOTO_PICKER_FROM_CAMERA:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mTempImageUri = Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), "tmp_"
                        + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        mTempImageUri);
                intent.putExtra("return-data", true);
                try {
                    // Start a camera capturing activity
                    // REQUEST_CODE_TAKE_FROM_CAMERA is an integer tag you
                    // defined to identify the activity in onActivityResult()
                    // when it returns
                    startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                isTakenFromCamera = true;
                break;


            case ProfileImageDialogFragment.REQUEST_CODE_TAKE_FROM_GALLERY:
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,REQUEST_CODE_TAKE_FROM_GALLERY);
                break;


        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_TAKE_FROM_CAMERA:
                // Send image taken from camera for cropping
                beginCrop(mTempImageUri);
                break;

            case Crop.REQUEST_CROP: //We changed the RequestCode to the one being used by the library.
                // Update image view after image crop
                handleCrop(resultCode, data);

                // Delete temporary image taken by camera after crop.
                if (isTakenFromCamera) {
                    File f = new File(mTempImageUri.getPath());
                    if (f.exists()) {
                        f.delete();
                        mImageCaptureUri=Crop.getOutput(data);
                    }
                }else{
                    mImageCaptureUri = mTempImageUri;
                }

                break;
            case REQUEST_CODE_TAKE_FROM_GALLERY:
                super.onActivityResult(requestCode, resultCode, data);
                mTempImageUri = data.getData();
                beginCrop(mTempImageUri);
                break;
        }
    }

    // ****************** private helper functions ***************************//
    //crop the image
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    //put cropped image into the image view
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            mImageView.setImageResource(0);
            mImageView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //load snap into the image view
    private void loadSnap() {
        // Load profile photo from internal storage
        try {
            FileInputStream fis = openFileInput(getString(R.string.profile_photo_file_name));
            Bitmap bmap = BitmapFactory.decodeStream(fis);
            mImageView.setImageBitmap(bmap);
            fis.close();
        } catch (IOException e) {
            // Default profile photo if no photo saved before.
            mImageView.setImageResource(R.drawable.image);
        }
    }

    //save snap information
    private void saveSnap() {
        // Commit all the changes into preference file
        // Save profile image into internal storage.
        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(
                    getString(R.string.profile_photo_file_name), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    //save user profile information
    private void saveProfile() {
        SharedPreferences sharePreferrences = getSharedPreferences("profileData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharePreferrences.edit();
        editor.putString("name", mName.getText().toString());
        editor.putString("email", mEmail.getText().toString());
        editor.putString("phone", mPhone.getText().toString());
        editor.putString("class", mClass.getText().toString());
        editor.putString("major", mMajor.getText().toString());

        mgroup = (RadioGroup) findViewById(R.id.gender);
        int selected = mgroup.getCheckedRadioButtonId();
        if (selected == R.id.femaleButton)
            editor.putInt("gender", 0);
        if (selected == R.id.maleButton)
            editor.putInt("gender", 1);
        editor.commit();
    }

    // load the user's profile into the app
    private void loadProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("profileData", Context.MODE_PRIVATE);
        String nameData = sharedPreferences.getString("name", null);
        String emailAddress = sharedPreferences.getString("email", null);
        String phoneNum = sharedPreferences.getString("phone", null);
        String major = sharedPreferences.getString("major", null);
        String classNum = sharedPreferences.getString("class", null);

        mName.setText(nameData);
        mEmail.setText(emailAddress);
        mPhone.setText(phoneNum);
        mClass.setText(classNum);
        mMajor.setText(major);

        RadioButton female = (RadioButton) findViewById(R.id.femaleButton);
        RadioButton male = (RadioButton) findViewById(R.id.maleButton);

        if (sharedPreferences.getInt("gender", -1) == 0)
            female.setChecked(true);
        else if (sharedPreferences.getInt("gender", -1) == 1)
            male.setChecked(true);

    }
}
