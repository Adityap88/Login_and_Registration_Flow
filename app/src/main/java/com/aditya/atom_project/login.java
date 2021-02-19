package com.aditya.atom_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class login extends AppCompatActivity {
    private static final int GOOGLE_REQUEST_CODE = 150;
    View mLoginForm, mProgressView;
    private TextView tvLoad;
    private TextInputLayout mEmail, mPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "LOGIN";

    @Override
    protected void onStart() {
        super.onStart();
        checkCurrentUser();               //checking if already logged in
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);


        //Hooks
        mLoginForm = findViewById(R.id.LoginForm);
        mProgressView = findViewById(R.id.progress);
        tvLoad = findViewById(R.id.tvLoading);
        Button mSkip = findViewById(R.id.btnSkip);
        mEmail = findViewById(R.id.etEmail);
        mPassword = findViewById(R.id.etPassword);
        Button mLogin = findViewById(R.id.btnLogin);
        TextView mRegister = findViewById(R.id.btnRegister);
        TextView tvReset = findViewById(R.id.tvReset);
        EditText edtMail = findViewById(R.id.edtMail);
        EditText edtPass = findViewById(R.id.edtPass);

        edtMail.addTextChangedListener(emailWatcher);
        edtPass.addTextChangedListener(passwordWatcher);
        mAuth = FirebaseAuth.getInstance();

        createGoogleRequest();

        Button mGoogleAuth = findViewById(R.id.btnGoogleAuth);


        mGoogleAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = Objects.requireNonNull(mEmail.getEditText()).getText().toString().trim();
                resetPass(Email);


            }
        });

        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                tvLoad.setText(R.string.skip_loadingText);
                signInAnonymously();


            }
        });


        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this, Register.class));
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(login.this);

                String Email = Objects.requireNonNull(mEmail.getEditText()).getText().toString().trim();
                String Pass = Objects.requireNonNull(mPassword.getEditText()).getText().toString().trim();
                loginWithEMailPass(Email, Pass);
            }
        });
    }

    //start Google Sign In
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    // authentication with firebase
    private void firebaseAuthWithGoogle(String idToken) {
        showProgress(true);
        tvLoad.setText(R.string.login_loadingText);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(login.this, homeScreen.class));
                            finish();
                        } else {
                            Toast.makeText(login.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // creating google request
    private void createGoogleRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // sign in as Guest
    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(login.this, homeScreen.class));
                            finish();
                        } else {
                            Toast.makeText(login.this, "Unable to Proceed" + task.getException(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    //Login With Email Password
    private void loginWithEMailPass(String Email, String Pass) {
        if (Email.equals("")) {
            mEmail.setError("Email is required");
            mEmail.requestFocus();
        }
        if (Pass.equals("")) {
            mPassword.setError("Password is required");
            mPassword.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            mEmail.setError("Please Enter a valid Email");
            mEmail.requestFocus();
        } else {
            showProgress(true);
            tvLoad.setText(R.string.login_loadingText);
            mAuth.signInWithEmailAndPassword(Email, Pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                assert user != null;
                                if (user.isEmailVerified()) {
                                    showProgress(false);
                                    Log.d(TAG, "Email Verified.");
                                    startActivity(new Intent(login.this, homeScreen.class));
                                    finish();

                                } else {
                                    Toast.makeText(login.this, "Please Verify your Email Or Register", Toast.LENGTH_SHORT).show();
                                }


                            } else {
                                showProgress(false);
                                Toast.makeText(login.this, "Authentication Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //reset password
    private void resetPass(String Email) {
        if (Email.equals("")) {
            mEmail.setError("Email is required");
            mEmail.requestFocus();
            return;
        }
        mAuth.sendPasswordResetEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(login.this, "Please Check your Email for reset link", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // hide keyboard
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // go to home screen if already logged in
    private void checkCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(login.this, homeScreen.class));
            finish();
        }
    }

    // function to show progress bar
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginForm.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
        tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
        tvLoad.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
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
                mPassword.setErrorEnabled(true);
            } else {
                mPassword.setErrorEnabled(false);
            }
        }
    };

    private final TextWatcher emailWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mEmail.setErrorEnabled(true);
            } else {
                mEmail.setErrorEnabled(false);
            }
        }
    };
}