package com.aditya.atom_project;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;

    Button mSignOut, mLogIn;
    TextView txtWelcome;
    private FirebaseUser user;

    public String name;

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isAnonymous()) {
                mLogIn.setVisibility(View.VISIBLE);
                txtWelcome.setText(R.string.welcome_guest);
                mSignOut.setVisibility(View.GONE);
            } else {

                StringBuilder welcomeMsg= UpperCaseFirstLetter("Signed in as "+user.getDisplayName().toLowerCase());
                txtWelcome.setText(welcomeMsg.toString());
            }
        }
        if(!user.isEmailVerified()){
            txtWelcome.setText(R.string.verify_mail_text);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        mSignOut = findViewById(R.id.btnSignOut);
        txtWelcome = findViewById(R.id.txtSignIn);
        mAuth = FirebaseAuth.getInstance();
        mLogIn = findViewById(R.id.btnLogIn);
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                GoogleSignIn.getClient(getApplicationContext(),
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut();
                startActivity(new Intent(HomeScreen.this, Login.class));
                finish();

            }
        });
        mLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    FirebaseAuth.getInstance().getCurrentUser().delete();
                }
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeScreen.this, Login.class));
                finish();

            }
        });
    }

    // double press back to exit
    private long backPressedTime;
    @Override
    public void onBackPressed() {
        long waitTime = 2000;
        if (backPressedTime + waitTime > System.currentTimeMillis()) {
            finish();
        } else {
            Toast.makeText(this, "Press Again to Exit", Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();


    }

    // converts first letter to uppercase
    public StringBuilder UpperCaseFirstLetter(String str){
        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap + " ");
        }
        return builder;
    }
}