package com.example.projectnotesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class AboutDeveloper extends AppCompatActivity {

    TextView content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_developer);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.theme_blue));
        }

        content = findViewById(R.id.aboutDeveloperContent);
        content.setText("Hey, I am Sushant\nI thank you so much for using this app\n\nAbout Me -\n♦️ I am a college student. I just completed my engineering dipoloma in Information Technology from Govt. Polytechnic Jalgaon\n" +
                "♦️ I love coding and currently enjoying Android Development so much\n\nPlease let us know your precious feedback about our app :)");
    }

    public void yt(View view) {
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCRmJRwHns1yKNxZkLwnDRDA")));
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")));
    }

    public void ig(View view) {
        Uri uri = Uri.parse("https://www.instagram.com/sushant_patil_03/");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            startActivity(likeIng);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/xxx")));
        }
    }

    public void whatsapp(View view) {
        String url = "https://api.whatsapp.com/send/?phone=919307245056";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.putExtra(Intent.EXTRA_TEXT,"Hello");
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void fb(View view) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/100025764158388"));
            startActivity(intent);
        } catch(Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/appetizerandroid")));
        }
    }

    public void gmail(View view) {
        String subject = "Feedback for Note Infinity";
        String to = "sushantofficial03@gmail.com";
        String mailTo = "mailto:" + to + "?&subject=" + Uri.encode(subject);
        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setData(Uri.parse(mailTo));
        startActivity(emailIntent);
    }

    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(AboutDeveloper.this,NotesActivity.class));
    }

    public void goBack(View view) {
        finish();
        startActivity(new Intent(AboutDeveloper.this,NotesActivity.class));
    }
}