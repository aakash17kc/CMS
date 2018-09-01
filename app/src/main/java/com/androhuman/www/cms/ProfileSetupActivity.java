package com.androhuman.www.cms;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;
    Uri profileImage;
    ImageView imageView;
    ProgressBar progressBar;
    String profileImageurl;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(mAuth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(this,LoginActivity.class));


        }
        //updateUI(currentUser);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        imageView = findViewById(R.id.choose_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                chooseImage();

            }
        });
        mAuth = FirebaseAuth.getInstance();
        Button button = findViewById(R.id.set_image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                saveUserInformation();

            }
        });

        loadUserInformation();
    }

    private void loadUserInformation() {

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            if (firebaseUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(firebaseUser.getPhotoUrl().toString())
                        .into(imageView);
            }

            if (firebaseUser.getDisplayName() != null) {
                EditText name = findViewById(R.id.profileName);
                name.setText(firebaseUser.getDisplayName());

            }
        }
    }
    public  void saveUserInformation(){
        EditText editText = findViewById(R.id.profileName);

        String profileName = editText.getText().toString().trim();
        if(profileName.isEmpty()){
            editText.setError("Enter user name");
            editText.requestFocus();
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null && profileImageurl!=null){
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(profileName)
                    .setPhotoUri(Uri.parse(profileImageurl))
                    .build();

            user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ProfileSetupActivity.this,"Profile Updated",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CHOOSE_IMAGE && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            profileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),profileImage);
                imageView.setImageBitmap(bitmap);

                uploadImageToFirebase();

               // InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void uploadImageToFirebase(){
        final StorageReference imageupload = FirebaseStorage.getInstance().getReference("profile pics/"+System.currentTimeMillis()+".jpg");
        if(profileImage!=null){
            progressBar = findViewById(R.id.image_upload_progess);
            progressBar.setVisibility(View.VISIBLE);
            imageupload.putFile(profileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    profileImageurl = imageupload.getDownloadUrl().toString();
                }
            });

            imageupload.putFile(profileImage).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileSetupActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    public void chooseImage(){
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Choose Image"),CHOOSE_IMAGE);*/

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, CHOOSE_IMAGE);
    }
}
