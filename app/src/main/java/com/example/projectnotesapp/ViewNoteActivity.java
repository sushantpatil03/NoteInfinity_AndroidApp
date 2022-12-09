package com.example.projectnotesapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

public class ViewNoteActivity extends AppCompatActivity {
    EditText vnTitle,vnContent;
    FloatingActionButton vnEditNote;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    Boolean editButtonClicked = false;
    String noteId;
    String noteTitle,noteContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        vnTitle = findViewById(R.id.vnTitle);
        vnContent = findViewById(R.id.vnContent);
        vnEditNote = findViewById(R.id.view_note_icon);

        noteTitle = vnTitle.getText().toString();
        noteContent = vnContent.getText().toString();

        vnTitle.setFocusable(false);
        vnTitle.setEnabled(false);
        vnTitle.setCursorVisible(false);
//        vnTitle.setKeyListener(null);
//        vnTitle.setBackgroundColor(Color.TRANSPARENT);
        vnTitle.setTextIsSelectable(true);

        vnContent.setFocusable(false);
        vnContent.setEnabled(false);
        vnContent.setCursorVisible(false);
//        vnContent.setKeyListener(null);
        vnContent.setTextIsSelectable(true);
        vnContent.setBackgroundColor(Color.TRANSPARENT);

        Toolbar toolbar;
        toolbar = findViewById(R.id.viewNoteToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        vnTitle.setText(i.getStringExtra("title"));
        vnContent.setText(i.getStringExtra("content"));
        noteId = i.getStringExtra("noteId");

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        vnEditNote.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (editButtonClicked){
                    saveNote();
                }else {
                    vnTitle.setFocusable(true);
                    vnTitle.setEnabled(true);
                    vnTitle.setCursorVisible(true);
                    vnTitle.setFocusableInTouchMode(true);

                    vnContent.setFocusable(true);
                    vnContent.setEnabled(true);
                    vnContent.setCursorVisible(true);
                    vnContent.setFocusableInTouchMode(true);

                    vnEditNote.setImageResource(R.drawable.save_icon);
                    vnEditNote.setMaxImageSize(170);
                    vnContent.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(vnContent, InputMethodManager.SHOW_IMPLICIT);
                    editButtonClicked = true;
                }

                if (!editButtonClicked) {
                    editButtonClicked = true;
                }
            }
        });
    }

    private void saveNote() {
        String title = vnTitle.getText().toString();
        String content = vnContent.getText().toString();
        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(ViewNoteActivity.this, "Please enter the note title and content", Toast.LENGTH_LONG).show();
        } else if (title.isEmpty()) {
            Toast.makeText(ViewNoteActivity.this, "Please give a title to the note", Toast.LENGTH_LONG).show();
        } else if (content.isEmpty()) {
            Toast.makeText(ViewNoteActivity.this, "Please enter some content to the note", Toast.LENGTH_LONG).show();
        } else {
            deleteNote();
            DocumentReference reference = firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document();
            Map<String, Object> note = new HashMap<>();
            note.put("title", title);
            note.put("content", content);
            reference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(ViewNoteActivity.this, "Note updated successfully :)", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(ViewNoteActivity.this, NotesActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ViewNoteActivity.this, "Error - " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteNote() {
        firebaseFirestore.collection("notes").document(user.getUid()).collection("myNotes").document(noteId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(ViewNoteActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewNoteActivity.this, "Error- "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (!editButtonClicked){
            finish();
            startActivity(new Intent(ViewNoteActivity.this,NotesActivity.class));
        }else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Text Infinity");
            alertDialogBuilder
                    .setMessage("Do you want to save changes in note?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveNote();
//                                    finish();
//                                    startActivity(new Intent(ViewNoteActivity.this,NotesActivity.class));
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            String title = vnTitle.getText().toString();
            String content = vnContent.getText().toString();
            if (title.equals(noteTitle) && content.equals(noteContent)){
                finish();
                startActivity(new Intent(ViewNoteActivity.this,NotesActivity.class));
            }else{
                saveNote();
            }
        }
        return true;
    }
}
