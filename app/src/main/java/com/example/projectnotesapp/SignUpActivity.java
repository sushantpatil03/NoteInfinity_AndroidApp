package com.example.projectnotesapp;

import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
//import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.IntentSender;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.auth.api.identity.BeginSignInRequest;
//import com.google.android.gms.auth.api.identity.BeginSignInResult;
//import com.google.android.gms.auth.api.identity.Identity;
//import com.google.android.gms.auth.api.identity.SignInClient;
//import com.google.android.gms.auth.api.identity.SignInCredential;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {
    EditText et1,et2;
    Button btn;
    TextView tv;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressDialog dialog;

    SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    TextView textView;
    private static final int RC_SIGN_IN = 10;

//    GoogleSignInOptions gso;
//    GoogleSignInClient gsc;
//    private SignInClient oneTapClient;
////    private BeginSignInRequest signInRequest;
//
//    private static final int REQ_ONE_TAP = 2;
//    private boolean showOneTapUI = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
//        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.theme_blue));
        }

        et1 = findViewById(R.id.signUpEmail);
        et2 = findViewById(R.id.signUpPassword);
        btn = findViewById(R.id.signUp);
        tv = findViewById(R.id.goToLogin);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("179998351181-4ve7j5crlg0ktoq0p6sp4bg8k178j4mm.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();



//        signInRequest = BeginSignInRequest.builder()
//                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.default_web_client_id))
//                        // Only show accounts previously used to sign in.
//                        .setFilterByAuthorizedAccounts(true)
//                        .build())
//                .build();
//
//        oneTapClient = Identity.getSignInClient(this);
//        signInRequest = BeginSignInRequest.builder()
//                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
//                        .setSupported(true)
//                        .build())
//                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.default_web_client_id))
//                        // Only show accounts previously used to sign in.
//                        .setFilterByAuthorizedAccounts(true)
//                        .build())
//                // Automatically sign in when exactly one credential is retrieved.
//                .setAutoSelectEnabled(true)
//                .build();

        dialog = new ProgressDialog(SignUpActivity.this);
        dialog.setTitle("Wait for some seconds");
        dialog.setMessage("Saving Your Info ...");

        tv.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this,MainActivity.class)));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et1.getText().toString().trim();
                String password = et2.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Please enter your Email and Password to Register !", Toast.LENGTH_SHORT).show();
                }else if (password.length()<7){
                    Toast.makeText(SignUpActivity.this, "Password should be greater than 7 characters !", Toast.LENGTH_SHORT).show();
                }else {
                    dialog.show();
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                dialog.dismiss();
                                sendEmailVerification();
                            }else {
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser!=null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
//                    Toast.makeText(SignUpActivity.this, "", Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(SignUpActivity.this)
                            .setTitle("Registration Success")
                            .setMessage("We just sent you an confirmation mail! \nRemember its only for user Privacy :)\nIf you don't find our email, don't forget to check the SPAM folder too!")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    auth.signOut();
                                    finish();
                                    startActivity(new Intent(SignUpActivity.this,MainActivity.class));
                                }
                            }).show();
                }
            });
        }else{
            Toast.makeText(this, "Failed to send the Confirmation Email! Try Again", Toast.LENGTH_LONG).show();
        }
    }

    public void signUpWithGoogleIcon(View view) {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            Intent i = new Intent(SignUpActivity.this,NotesActivity.class);
            i.putExtra("google_sign_up",true);
            startActivity(i);
        }else{
            Toast.makeText(getApplicationContext(),"Sign in cancel",Toast.LENGTH_LONG).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//            case REQ_ONE_TAP:
//                try {
//                    SignInCredential googleCredential = oneTapClient.getSignInCredentialFromIntent(data);
//                    String idToken = googleCredential.getGoogleIdToken();
//                    if (idToken !=  null) {
//                        // Got an ID token from Google. Use it to authenticate
//                        // with Firebase.
//                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
//                        auth.signInWithCredential(firebaseCredential)
//                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<AuthResult> task) {
//                                        if (task.isSuccessful()) {
//                                            // Sign in success, update UI with the signed-in user's information
//                                            Log.d("TAG", "signInWithCredential:success");
//                                            FirebaseUser user = auth.getCurrentUser();
////                                            updateUI(user);
//                                        } else {
//                                            // If sign in fails, display a message to the user.
//                                            Log.w("sdbh", "signInWithCredential:failure", task.getException());
////                                            updateUI(null);
//                                        }
//                                    }
//                                });
//                    }
//
//                } catch (ApiException e) {
//                    // ...
//                }
//                break;
//        }
//    }
//
//    public void signUpWithGoogleIcon(View view) {
//        oneTapClient.beginSignIn(signInRequest)
//                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
//                    @Override
//                    public void onSuccess(BeginSignInResult result) {
//                        try {
//                            startIntentSenderForResult(
//                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
//                                    null, 0, 0, 0);
//                        } catch (IntentSender.SendIntentException e) {
//                            Log.e("TAG", "Couldn't start One Tap UI: " + e.getLocalizedMessage());
//                        }
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // No saved credentials found. Launch the One Tap sign-up flow, or
//                        // do nothing and continue presenting the signed-out UI.
//                        Log.d("TAG", e.getLocalizedMessage());
//                    }
//                });
//    }
}