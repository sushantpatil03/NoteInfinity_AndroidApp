package com.example.projectnotesapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotesActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    FirebaseAuth auth;
    FirebaseUser user;
    FloatingActionButton plusButton;
    RecyclerView recyclerView;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder> firestoreRecyclerAdapter;
    FirebaseFirestore firebaseFirestore;
    String tokenId;

    boolean google_sign_up = false;

    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;

    DBHelper helper;
    String titleSize;
    String bodySize;
    String sortBy;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.theme_blue));
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolBar);
        plusButton = findViewById(R.id.plus_button);

        helper = new DBHelper(this);
        getInformationFromDatabase();

        auth = FirebaseAuth.getInstance();

//        Toast.makeText(this, "User uid - "+user.getUid(), Toast.LENGTH_SHORT).show();
        firebaseFirestore = FirebaseFirestore.getInstance();

        gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(NotesActivity.this)
                .enableAutoManage(NotesActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        Intent i = getIntent();
        if (i!=null){
            if (i.hasExtra("id")){
                tokenId = i.getStringExtra("id");
            }else if(i.hasExtra("google_sign_up")){
                google_sign_up = i.getBooleanExtra("google_sign_up",false);
            }
        }

        if (google_sign_up){
            Toast.makeText(NotesActivity.this, "Step 1 done", Toast.LENGTH_SHORT).show();
            OptionalPendingResult<GoogleSignInResult> opr= Auth.GoogleSignInApi.silentSignIn(googleApiClient);
            if(opr.isDone()){
                GoogleSignInResult result=opr.get();
                handleSignInResult(result);
            }else{
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }else {
            user = auth.getCurrentUser();
        }

//        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
//        if(acct!=null){
//            String idToken = acct.getIdToken();
//            Toast.makeText(this, "Id token is - "+idToken, Toast.LENGTH_SHORT).show();
//            AuthCredential credential = GoogleAuthProvider.getCredential(tokenId,null);
//            auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if (task.isSuccessful()){
//                        user = auth.getCurrentUser();
//                    }else{
//                        Toast.makeText(NotesActivity.this, "Error - "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.OpenDrawer,R.string.CloseDrawer);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.logOut){
                    auth.signOut();
                    finish();
                    startActivity(new Intent(NotesActivity.this,MainActivity.class));
                }else if(id == R.id.ourApps){
                    Toast.makeText(NotesActivity.this, "We are releasing such awesome apps very soon :) Stay Tuned", Toast.LENGTH_SHORT).show();
                }else if(id == R.id.reportBug){
                    String subject = "Reporting a Bug in Notes App";
                    String to = "sushantofficial03@gmail.com";
                    String mailTo = "mailto:" + to + "?&subject=" + Uri.encode(subject);
                    Intent emailIntent = new Intent(Intent.ACTION_VIEW);
                    emailIntent.setData(Uri.parse(mailTo));
                    startActivity(emailIntent);
                }else if (id == R.id.aboutUs){
                    startActivity(new Intent(NotesActivity.this,AboutDeveloper.class));
                }else if (id == R.id.customize){
                    finish();
                    startActivity(new Intent(NotesActivity.this,CustomizeActivity.class));
                }else if(id == R.id.homeMenu){
                    drawerLayout.closeDrawers();
                }
                return true;
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NotesActivity.this,NewNoteActivity.class));
            }
        });


//        Query query = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("timeStamp", Query.Direction.DESCENDING);
        Query query = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("title",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<firebasemodel> userallnotes = new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();

        firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(userallnotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull firebasemodel model) {

                ImageView popupbutton=holder.itemView.findViewById(R.id.menupopupbutton);
                int colorCode = getRandomColorCode();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.note.setBackgroundColor(holder.itemView.getResources().getColor(colorCode,null));
                }

                String nTitle = model.getTitle();
                String nContent = model.getContent();
                holder.noteTitle.setTextSize(Float.parseFloat(titleSize));
                holder.noteContent.setTextSize(Float.parseFloat(bodySize));
                holder.noteTitle.setText(model.getTitle());
                holder.noteContent.setText(model.getContent());

                String docId=firestoreRecyclerAdapter.getSnapshots().getSnapshot(position).getId();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Toast.makeText(NotesActivity.this, "Clicked on "+model.getTitle(), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(NotesActivity.this,ViewNoteActivity.class);
                        i.putExtra("title",nTitle);
                        i.putExtra("content",nContent);
                        i.putExtra("noteId",docId);
                        startActivity(i);
                    }
                });

                popupbutton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(),view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Intent i = new Intent(NotesActivity.this,ViewNoteActivity.class);
                                i.putExtra("title",nTitle);
                                i.putExtra("content",nContent);
                                i.putExtra("noteId",docId);
                                startActivity(i);
                                return false;
                            }
                        
                        });
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document(docId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                    Toast.makeText(NotesActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(NotesActivity.this, "Error- "+e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                return false;
                            }
                        });
                        popupMenu.show();
                        
                    }
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(firestoreRecyclerAdapter);

    }

    private void getInformationFromDatabase() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("user_settings", new String[]{"id", "title_size", "body_size", "sort_by"}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            titleSize = cursor.getString(1);
            bodySize = cursor.getString(2);
            sortBy = cursor.getString(3);
        }

    }

    private int getRandomColorCode() {
        List<Integer> colorList = new ArrayList<>();
        colorList.add(R.color.color1);
        colorList.add(R.color.color2);
        colorList.add(R.color.color3);
        colorList.add(R.color.color4);
        colorList.add(R.color.color5);
        colorList.add(R.color.color6);
        colorList.add(R.color.color7);
        colorList.add(R.color.color8);
        colorList.add(R.color.color9);
        colorList.add(R.color.color10);
        colorList.add(R.color.color11);
        colorList.add(R.color.color12);

        Random random = new Random();
        int number = random.nextInt(colorList.size());
        return colorList.get(number);

    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle,noteContent;
        LinearLayout note;
        ImageView menuPopupButton;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            note = itemView.findViewById(R.id.note);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteContent = itemView.findViewById(R.id.notecontent);
            menuPopupButton = itemView.findViewById(R.id.menupopupbutton);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreRecyclerAdapter!=null){
            firestoreRecyclerAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NotesActivity.this);
        alertDialogBuilder.setTitle("Exit Application?");
        alertDialogBuilder
                .setMessage("Click yes to exit!")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void handleSignInResult(GoogleSignInResult result){
        Toast.makeText(NotesActivity.this, "Step 2 done", Toast.LENGTH_SHORT).show();
        if(result.isSuccess()){
            Toast.makeText(NotesActivity.this, "Step 3 done", Toast.LENGTH_SHORT).show();
            GoogleSignInAccount account=result.getSignInAccount();
//            String id = account.getId();
            String idToken = account.getIdToken();
//            Toast.makeText(NotesActivity.this, "Id token is - "+idToken, Toast.LENGTH_SHORT).show();
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
            Toast.makeText(NotesActivity.this, "Step 4 done", Toast.LENGTH_SHORT).show();
            auth.signInWithCredential(credential).addOnCompleteListener(NotesActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Toast.makeText(NotesActivity.this, "Step 5 done", Toast.LENGTH_SHORT).show();
                    if (task.isSuccessful()){
                        Toast.makeText(NotesActivity.this, "Done Hush", Toast.LENGTH_SHORT).show();
                        user = auth.getCurrentUser();
                    }else{
                        Toast.makeText(NotesActivity.this, "Error - "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            startActivity(new Intent(NotesActivity.this,MainActivity.class));
        }
    }
}