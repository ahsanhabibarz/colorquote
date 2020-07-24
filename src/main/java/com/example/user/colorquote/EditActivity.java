package com.example.user.colorquote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditActivity extends AppCompatActivity {


    private String post_id;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private EditText editQuote,editSaidby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Quote_Lists");
        mDatabase.keepSynced(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editQuote = (EditText)findViewById(R.id.eQuote);
        editSaidby = (EditText)findViewById(R.id.eSaidby);

        Bundle extras = getIntent().getExtras();
        if(extras != null){

           post_id = extras.getString("postID");
        }


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(post_id)) {

                    String quote = dataSnapshot.child(post_id).child("quote").getValue().toString().trim();
                    String saidby = dataSnapshot.child(post_id).child("saidby").getValue().toString().trim();
                    editQuote.setText(quote);
                    editSaidby.setText(saidby);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void startPost(){

        if(post_id != null) {

            DatabaseReference newPost = mDatabase.child(post_id);

            if(editSaidby.getText().toString().isEmpty()){

                newPost.child("saidby").setValue("Unknown");

            }else {

                newPost.child("saidby").setValue(editSaidby.getText().toString().trim());

            }

            newPost.child("quote").setValue(editQuote.getText().toString().trim());

            Toast.makeText(this, "Post Updated", Toast.LENGTH_SHORT).show();

            super.onBackPressed();
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

            if(!editQuote.getText().toString().isEmpty()){

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
