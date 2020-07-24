package com.example.user.colorquote;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by USER on 11/4/2017.
 */

public class ColorQuote extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
