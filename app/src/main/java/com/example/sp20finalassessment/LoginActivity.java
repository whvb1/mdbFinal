package com.example.sp20finalassessment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    EditText password, email;
    Button login, register;
    FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /* TODO Part 1
         * Implement login. When the login button is pressed, attempt to login the user, and if
         * that is successful, go to the TabbedActivity. If the register button is pressed, go to
         * the RegisterActivity. Check the layout files for the IDs of the views used in this part
         */
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailT = email.getText().toString().trim();
                String passwordT = password.getText().toString().trim();
                if(TextUtils.isEmpty(emailT)) {
                    Toast toast = Toast.makeText(LoginActivity.this, "Email is required", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(TextUtils.isEmpty(passwordT)) {
                    Toast toast = Toast.makeText(LoginActivity.this, "Password is required", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if(passwordT.length() < 6) {
                    Toast toast = Toast.makeText(LoginActivity.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(emailT, passwordT).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, TabActivity.class));
                        } else {
                            System.out.println("BOOOOM");
                            Toast.makeText(LoginActivity.this, "Error Signing In User! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });





    }

}
