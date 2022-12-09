package com.example.projectnotesapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    EditText et1,et2;
    Button bt1;
    TextView tv,bt2;
    FirebaseAuth auth;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView googleBtn;
    String idToken;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.theme_blue));
        }

        et1 = findViewById(R.id.loginEmail);
        et2 = findViewById(R.id.loginPassword);
        bt1 = findViewById(R.id.loginButton);
        bt2 = findViewById(R.id.goToSignUp);
        tv = findViewById(R.id.goToForgotPassword);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("Please wait a second");
        dialog.setMessage("Verifying your info ...");

        googleBtn = findViewById(R.id.googleLogIn);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (auth.getCurrentUser()!=null){
            finish();
            startActivity(new Intent(MainActivity.this,NotesActivity.class));
        }

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,SignUpActivity.class));
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ForgotPasswordActivity.class));
            }
        });

//        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("179998351181-4ve7j5crlg0ktoq0p6sp4bg8k178j4mm.apps.googleusercontent.com")
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            finish();
            idToken = acct.getIdToken();
            Intent intent = new Intent(MainActivity.this,NotesActivity.class);
            intent.putExtra("id",idToken);
            startActivity(intent);
        }


        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et1.getText().toString().trim();
                String password = et2.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter full information", Toast.LENGTH_SHORT).show();
                }else {
                    dialog.show();
                    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                dialog.dismiss();
                                isVerified();
//                                startActivity(new Intent(MainActivity.this,NotesActivity.class));
                            }else {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

    }
    private void isVerified() {
        FirebaseUser user = auth.getCurrentUser();
        if (user.isEmailVerified()){
            Toast.makeText(this, "Welcome back Chief!", Toast.LENGTH_LONG).show();
            finish();
            startActivity(new Intent(MainActivity.this,NotesActivity.class));
        }else {
            Toast.makeText(this, "Your Email is not verified yet. Please verify it first !", Toast.LENGTH_LONG).show();
            auth.signOut();
        }
    }
    void signIn(){
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent,69);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 69){
//            GoogleSignInResult result = auth.GoogleSignIn.getSignInResultFromIntent(data);
//            if (result.isSuccess()) {
//                GoogleSignInAccount account = result.getSignInAccount();
//                firebaseAuthWithGoogle(account);
//            }
//        }
//
//    }
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        System.out.println("firebaseAuthWithGoogle:" + acct.getId());
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        System.out.println("signInWithCredential:onComplete:" + task.isSuccessful());
//
//                        if (!task.isSuccessful()) {
//                            System.out.println("signInWithCredential" + task.getException());
//                            Toast.makeText(MainActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 69){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                String id = acct.getIdToken();
                Toast.makeText(this, "Token id = "+id, Toast.LENGTH_SHORT).show();
                task.getResult(ApiException.class);
                finish();
                Intent intent = new Intent(MainActivity.this,NotesActivity.class);
                intent.putExtra("id",id);
                startActivity(intent);
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

}