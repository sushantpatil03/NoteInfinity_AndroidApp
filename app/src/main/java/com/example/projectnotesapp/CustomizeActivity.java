package com.example.projectnotesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class CustomizeActivity extends AppCompatActivity {
    SeekBar seekBar;
    TextView textView;
    String sizeOfNoteTitle;
    String sizeOfNoteBody;
    Spinner spinner;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    SQLiteDatabase db;
    DBHelper helper;
    String sortBySelection;

    FirebaseAuth auth;

    TextView tv1,tv2;

    static String[] names = {"Newest first", "Oldest first", "A-Z", "Z-A"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.theme_blue));
        }

        spinner = findViewById(R.id.sortBySpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinner.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();

        helper = new DBHelper(this);

        textView = findViewById(R.id.textViewForSizeOfNote);
        seekBar = findViewById(R.id.seekBarForSizeOfNote);

        tv1 = findViewById(R.id.noteTitle);
        tv2 = findViewById(R.id.notecontent);

        getValuesFromDatabase();

        drawerLayout = findViewById(R.id.drawerLayoutForCustomize);
        navigationView = findViewById(R.id.navigationViewOfCustomize);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.logOut) {
                    auth.signOut();
                    finish();
                    startActivity(new Intent(CustomizeActivity.this, MainActivity.class));
                } else if (id == R.id.ourApps) {
                    Toast.makeText(CustomizeActivity.this, "We are releasing such awesome apps very soon :) Stay Tuned", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.reportBug) {
                    String subject = "Reporting a Bug in Notes App";
                    String to = "sushantofficial03@gmail.com";
                    String mailTo = "mailto:" + to + "?&subject=" + Uri.encode(subject);
                    Intent emailIntent = new Intent(Intent.ACTION_VIEW);
                    emailIntent.setData(Uri.parse(mailTo));
                    startActivity(emailIntent);
                } else if (id == R.id.aboutUs) {
                    startActivity(new Intent(CustomizeActivity.this, AboutDeveloper.class));
                } else if (id == R.id.customize) {
                    drawerLayout.closeDrawers();
                }else if(id == R.id.homeMenu){
                    finish();
                    startActivity(new Intent(CustomizeActivity.this,NotesActivity.class));
                }
                return true;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i) {
                    case 1:
                        sizeOfNoteBody = "8";
                        sizeOfNoteTitle = "12";
                        changeSizeOfNote();
                        textView.setText("Very Small");
                        tv1.setTextSize(12);
                        tv2.setTextSize(8);
                        break;
                    case 2:
                        sizeOfNoteBody = "10";
                        sizeOfNoteTitle = "14";
                        changeSizeOfNote();
                        textView.setText("Small");
                        tv1.setTextSize(14);
                        tv2.setTextSize(10);
                        break;
                    case 3:
                        sizeOfNoteBody = "12";
                        sizeOfNoteTitle = "16";
                        changeSizeOfNote();
                        textView.setText("Normal");
                        tv1.setTextSize(16);
                        tv2.setTextSize(12);
                        break;
                    case 4:
                        sizeOfNoteBody = "14";
                        sizeOfNoteTitle = "18";
                        changeSizeOfNote();
                        textView.setText("Big");
                        tv1.setTextSize(18);
                        tv2.setTextSize(14);
                        break;
                    case 5:
                        sizeOfNoteBody = "16";
                        sizeOfNoteTitle = "20";
                        changeSizeOfNote();
                        textView.setText("Very Big");
                        tv1.setTextSize(20);
                        tv2.setTextSize(16);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(CustomizeActivity.this, "Changes Saved !", Toast.LENGTH_SHORT).show();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changeSortByPreference(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void getValuesFromDatabase() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("user_settings", new String[]{"id", "title_size", "body_size", "sort_by"}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            sizeOfNoteTitle = cursor.getString(1);
//            Toast.makeText(this, "sizeOfNoteTitle = "+sizeOfNoteTitle, Toast.LENGTH_SHORT).show();
            switch (sizeOfNoteTitle) {
                case "16":
                    seekBar.setProgress(3);
                    textView.setText("Normal");
                    tv1.setTextSize(16);
                    tv2.setTextSize(12);
                    break;
                case "14":
                    seekBar.setProgress(2);
                    textView.setText("Small");
                    tv1.setTextSize(14);
                    tv2.setTextSize(10);
                    break;
                case "12":
                    seekBar.setProgress(1);
                    textView.setText("Very Small");
                    tv1.setTextSize(12);
                    tv2.setTextSize(8);
                    break;
                case "18":
                    seekBar.setProgress(4);
                    textView.setText("Big");
                    tv1.setTextSize(18);
                    tv2.setTextSize(14);
                    break;
                case "20":
                    seekBar.setProgress(5);
                    textView.setText("Very Big");
                    tv1.setTextSize(20);
                    tv2.setTextSize(16);
                    break;
            }

            sortBySelection = cursor.getString(3);
//            Toast.makeText(this, "SortBySelection - "+sortBySelection, Toast.LENGTH_SHORT).show();
            switch (sortBySelection) {
                case "newest":
                    spinner.setSelection(1);
                    break;
                case "oldest":
                    spinner.setSelection(2);
                    break;
                case "a-z":
                    spinner.setSelection(3);
                    break;
                case "z-a":
                    spinner.setSelection(4);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(CustomizeActivity.this, NotesActivity.class));
    }

    private void changeSizeOfNote() {
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title_size", sizeOfNoteTitle);
        cv.put("body_size", sizeOfNoteBody);
        int result = database.update("user_settings", cv, "id=?", new String[]{"1"});
    }

    private void changeSortByPreference(int i) {
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String s = names[i];
        cv.put("sort_by", s);
        int result = database.update("user_settings", cv, "id=?", new String[]{"1"});
    }
}