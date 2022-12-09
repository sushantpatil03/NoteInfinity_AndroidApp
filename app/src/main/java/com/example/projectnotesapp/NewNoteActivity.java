package com.example.projectnotesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewNoteActivity extends AppCompatActivity {
    EditText etTitle,etContent;
    FloatingActionButton saveNote;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        saveNote = findViewById(R.id.save_note_icon);

        Toolbar toolbar;
        toolbar = findViewById(R.id.createNoteToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();


        saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                functionSaveNote();
            }
        });
    }

    private void functionSaveNote() {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        if (title.isEmpty() && content.isEmpty()){
            Toast.makeText(NewNoteActivity.this, "Please enter the note title and content", Toast.LENGTH_LONG).show();
        }else if (title.isEmpty()){
            Toast.makeText(NewNoteActivity.this, "Please give a title to the note", Toast.LENGTH_LONG).show();
        }else if (content.isEmpty()){
            Toast.makeText(NewNoteActivity.this, "Please enter some content to the note", Toast.LENGTH_LONG).show();
        }else {
            DocumentReference reference = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document();
            Map<String,Object> note = new HashMap<>();
            note.put("title",title);
            note.put("content",content);
            reference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(NewNoteActivity.this, "Note saved successfully :)", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(NewNoteActivity.this,NotesActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewNoteActivity.this, "Error - "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        if (title.isEmpty() && content.isEmpty()){
            startActivity(new Intent(NewNoteActivity.this,NotesActivity.class));
        }else{
            if (title.isEmpty()){
                Toast.makeText(NewNoteActivity.this, "Please give a title to the note", Toast.LENGTH_LONG).show();
            }else if (content.isEmpty()){
                Toast.makeText(NewNoteActivity.this, "Please enter some content to the note", Toast.LENGTH_LONG).show();
            }else{
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Text Infinity");
                alertDialogBuilder
                        .setMessage("Do you want to save the note?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        functionSaveNote();
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
        }
    }
}