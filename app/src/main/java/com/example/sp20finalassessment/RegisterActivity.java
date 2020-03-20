package com.example.sp20finalassessment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;;import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    String emailT, passwordT, picturePath;
    Button createnewuser;
    EditText email2, password2, name, confirmpassword;
    private DatabaseReference mDatabase;
    ImageView imageView;

    int IMAGE_REQUEST_CODE = 1023;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1023;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        /* TODO Part 2
        * Implement registration. If the imageView is clicked, set it to an image from the gallery
        * and store the image as a Uri instance variable (also change the imageView's image to this
        * Uri. If the create new user button is pressed, call createUser using the email and password
        * from the edittexts. Remember that it's email2 and password2 now!
        */
        email2 = findViewById(R.id.email2);
        password2 = findViewById(R.id.password2);
        name = findViewById(R.id.name);
        confirmpassword = findViewById(R.id.confirmpassword);
        createnewuser = findViewById(R.id.createnewuser);
        imageView = findViewById(R.id.imageView);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        createUser(emailT, passwordT);
        createnewuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for picture and name
                emailT = email2.getText().toString().trim();
                passwordT = password2.getText().toString().trim();
                createUser(emailT, passwordT);


            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);

            }
        });
    }


    private void createUser(final String email, final String password) {
        /* TODO Part 2.1
         * This part's long, so listen up!
         * Create a user, and if it fails, display a Toast.
         *
         * If it works, we're going to add their image to the database. To do this, we will need a
         * unique user id to identify the user (push isn't the best answer here. Do some Googling!)
         *
         * Now, if THAT works (storing the image), set the name and photo uri of the user (hint: you
         * want to update a firebase user's profile.)
         *
         * Finally, if updating the user profile works, go to the TabbedActivity
         */

        if(TextUtils.isEmpty(emailT)) {
            Toast toast = Toast.makeText(RegisterActivity.this, "Email is required", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if(TextUtils.isEmpty(passwordT)) {
            Toast toast = Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if(passwordT.length() < 6) {
            Toast toast = Toast.makeText(RegisterActivity.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("REGISTER", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            mDatabase.child("users").setValue(user.getUid());
                            update(user);

                        } else {
                            // If sign in fails, display a message to the user.

                            Log.w("REGISTER", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed, user might already exist",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });




    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(imageUri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(RegisterActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    protected  void update(FirebaseUser user) {
        String uID = user.getUid();
        Uri file;
        try {
            file = Uri.fromFile(new File(picturePath));
            Log.d("UPLOAD", file.toString());
            StorageReference picsRef = mStorage.child(String.format("users/%s.jpg",uID));
            picsRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                            System.out.println("successfully uploaded image");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast toast = Toast.makeText( RegisterActivity.this, "[server error] failure to upload image", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
            System.out.println("Uploaded image");
        } catch (NullPointerException e) {
            Toast.makeText(RegisterActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();

        }
    }


}
