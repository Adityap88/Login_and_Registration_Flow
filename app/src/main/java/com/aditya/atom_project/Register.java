package com.aditya.atom_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Register extends AppCompatActivity {
    View progressBar, registerForm;
    private TextInputLayout mUserName, mEmail, mPass, mConfirmPass;
    private TextView loadingText;
    private FirebaseAuth mAuth;

    private static final String TAG = "USER CREATED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Hooks
        setContentView(R.layout.activity_register);
        mUserName = findViewById(R.id.userName);
        mEmail = findViewById(R.id.EMail);
        mPass = findViewById(R.id.password);
        mConfirmPass = findViewById(R.id.RePassword);
        TextView tvTerms = findViewById(R.id.txtTerms);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.txtLogin);
        loadingText = findViewById(R.id.RtvLoading);
        progressBar = findViewById(R.id.Rprogress);
        registerForm = findViewById(R.id.RegisterForm);
        mAuth = FirebaseAuth.getInstance();

        EditText edtPass = findViewById(R.id.edtPass);
        EditText edtPassRe = findViewById(R.id.edtPassRe);

        edtPass.addTextChangedListener(passwordWatcher);
        edtPassRe.addTextChangedListener(RePasswordWatcher);


        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Email = Objects.requireNonNull(mEmail.getEditText()).getText().toString().trim();
                String Pass = Objects.requireNonNull(mPass.getEditText()).getText().toString().trim();
                String UserName = Objects.requireNonNull(mUserName.getEditText()).getText().toString().trim();
                String PassRe = Objects.requireNonNull(mConfirmPass.getEditText()).getText().toString().trim();
                registerUser(Email, UserName, Pass, PassRe);
            }
        });


    }

    private void registerUser(final String email, final String userName, String pass, String passRe) {
        if (userName.equals("")) {
            mUserName.setError("User Name is Required");
            mUserName.requestFocus();
            return;
        }

        if (!pass.equals(passRe)) {
            mConfirmPass.setError("Passwords Do not Match");
            return;
        }
        if (email.equals("")) {
            mEmail.setError("Email is required");
            mEmail.requestFocus();
            return;
        }
        if (pass.equals("")) {
            mPass.setError("Password is required");
            return;
        }

        if (pass.length() < 6) {
            mPass.setError("Password should be of minimum length 6");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Please Enter a valid Email");
            mEmail.requestFocus();
        } else {
            showProgress(true);
            loadingText.setText(R.string.register_loadingText);
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        showProgress(false);
                        FirebaseUser FireUser = FirebaseAuth.getInstance().getCurrentUser();

                        // sending user verification mail
                        assert FireUser != null;
                        FireUser.sendEmailVerification();
                        showSnackBar("Please check your email to verify yourself", "OK");


                        // updating user info
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .build();
                        FireUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });

                        // adding user to real time database
                        User user = new User(userName, email);
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Toast.makeText(Register.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "User profile updated.");
                            }
                        });

                        startActivity(new Intent(Register.this, HomeScreen.class));
                        finish();
                    } else {
                        showProgress(false);
                        showSnackBar("Unable to Register at this moment", "OK");
                    }
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        registerForm.setVisibility(show ? View.GONE : View.VISIBLE);
        registerForm.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                registerForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
        loadingText.setVisibility(show ? View.VISIBLE : View.GONE);
        loadingText.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loadingText.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private final TextWatcher passwordWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mPass.setErrorEnabled(true);
            } else {
                mPass.setErrorEnabled(false);
            }
        }
    };
    private final TextWatcher RePasswordWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mConfirmPass.setErrorEnabled(true);
            } else {
                mConfirmPass.setErrorEnabled(false);
            }
        }
    };

    public void showSnackBar(String snack_msg, String action_msg) {
        final Snackbar snackbar = Snackbar.make(registerForm, snack_msg, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setAction(action_msg, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
        snackbar.show();
    }
}
