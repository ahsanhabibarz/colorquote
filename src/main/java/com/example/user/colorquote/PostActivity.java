package com.example.user.colorquote;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class PostActivity extends AppCompatActivity {


    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;

    EditText PQuote,PSaidby;

    private String squote,ssaidby,SerialString,Date;

    String[] col = {

            //shade 1

            "#E3F2FD",//blue
            "#FFEBEE",//red
            "#FCE4EC",//pink
            "#F3E5F5",//purple
            "#EDE7F6",//deep purple
            "#E8EAF6",//indigo
            "#FFF8E1",//amber
            "#E0F7FA",//cyan
            "#E0F2F1",//teal
            "#E8F5E9",//green
            "#F1F8E9",//light green
            "#FFF3E0",//orange
            "#FBE9E7",//deep orange
            "#F9FBE7"//lime

    };

    Calendar cal;
    SimpleDateFormat simpleDateFormat;

    ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Quote_Lists");
        mDatabase.keepSynced(true);

        PQuote = (EditText)findViewById(R.id.pQuote);
        PSaidby = (EditText)findViewById(R.id.pSaidby);
        constraintLayout = (ConstraintLayout)findViewById(R.id.postBackground);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Random randomcolor = new Random();

        //constraintLayout.setBackgroundColor(Color.parseColor(col[randomcolor.nextInt(14)]));

    }



    public void startPost(){

        try {

            if(PSaidby.getText().toString().isEmpty()){

                ssaidby = "Unknown";
            }

            else if (!PSaidby.getText().toString().isEmpty()){

                ssaidby = PSaidby.getText().toString().trim();
            }

            squote = PQuote.getText().toString().trim();

            cal = Calendar.getInstance();
            simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
            Date = simpleDateFormat.format(cal.getTime());

            SerialString = String.valueOf(Long.parseLong("90000000000000") - Long.parseLong(Date) + "-" + mAuth.getCurrentUser().getDisplayName());


            DatabaseReference newPost = mDatabase.child(SerialString);

            newPost.child("quote").setValue(squote);
            newPost.child("saidby").setValue(ssaidby);
            newPost.child("postedby").setValue(mAuth.getCurrentUser().getDisplayName().toString());
            newPost.child("likecount").setValue("0");
            newPost.child(mAuth.getCurrentUser().getUid().toString()).setValue(mAuth.getCurrentUser().getDisplayName());
            newPost.child("uid").setValue(mAuth.getCurrentUser().getUid().toString());

            Toast.makeText(this, "Quote Added Successfully", Toast.LENGTH_SHORT).show();

            onBackPressed();

        }catch (Exception e){

            Toast.makeText(this, "Failed To Post Your Quote", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.post_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.item_add_quote){

            if(!PQuote.getText().toString().isEmpty()){

                startPost();
            }


            else{

                Toast.makeText(this, "Please Fill Up Everything", Toast.LENGTH_SHORT).show();
            }

        }

        else if(id ==  android.R.id.home){

            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
