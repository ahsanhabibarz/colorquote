package com.example.user.colorquote;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private long doubleBackToExitPressedOnce;

    private RecyclerView QuoteList;

    private TextView username,useremail;
    private CircleImageView userimage;

    private Boolean likeStatus = false;

    private ProgressBar loadingText;

    private Boolean removePost = false;

    private Boolean editPost = false;

    boolean authState = false;

    boolean valEvent = false;

    ClipboardManager clipboardManager;

    FloatingActionButton fab;

    FirebaseRecyclerAdapter<quotes,BlogViewHolder> firebaseRecyclerAdapter;

    private Query cuserQuery,searchQuery,mainQuery;

    private NavigationView navigationView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Quote_Lists");
        mainQuery = mDatabase.orderByKey();
        mDatabase.keepSynced(true);
        QuoteList = (RecyclerView)findViewById(R.id.quoteList);
        QuoteList.setHasFixedSize(true);
        QuoteList.setLayoutManager(new LinearLayoutManager(this));
        loadingText = (ProgressBar) findViewById(R.id.loading);
        clipboardManager = (ClipboardManager)getSystemService(getApplicationContext().CLIPBOARD_SERVICE);


        loadingText.postDelayed(new Runnable() {
            @Override
            public void run() {

                loadingText.setVisibility(View.GONE);
            }
        },6000);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fab.animate().rotation(fab.getRotation()+360).start();
                startActivity(new Intent(MainActivity.this,PostActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        username = (TextView)headerView.findViewById(R.id.tuname);
        useremail = (TextView)headerView.findViewById(R.id.uemail);
        userimage = (CircleImageView) headerView.findViewById(R.id.uimage);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() == null){

                    startActivity(new Intent(MainActivity.this,LoginActivity.class));

                    authState = false;

                }

                else if(firebaseAuth.getCurrentUser() != null){

                    authState = true;
                    cuserQuery = mDatabase.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());
                    username.setText(mAuth.getCurrentUser().getDisplayName().toString().trim());
                    useremail.setText(mAuth.getCurrentUser().getEmail().toString().trim());
                    Picasso.with(getApplicationContext()).load(mAuth.getCurrentUser().getPhotoUrl()).into(userimage);

                }

            }
        };

    }


    @Override
    protected void onStart() {

        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);

        navigationView.setCheckedItem(R.id.nav_home);

        loadQuotes(mainQuery);

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

        QuoteList.getRecycledViewPool().clear();
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }



    public void loadQuotes(Query query){

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<quotes, BlogViewHolder>(

                quotes.class,
                R.layout.quote_model_row,
                BlogViewHolder.class,
                query




        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, final quotes model, final int position) {



                final String post_id = getRef(position).getKey();

                viewHolder.setQuote(model.getQuote());
                viewHolder.setSaidby(model.getSaidby());
                viewHolder.setPostedby(model.getPostedby());
                viewHolder.setLikecount(model.getLikecount());

                viewHolder.changeLikebutton(post_id);

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        valEvent = true;

                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(valEvent&&authState){

                                    if(dataSnapshot.child(post_id).hasChild(mAuth.getCurrentUser().getUid())||mAuth.getCurrentUser().getUid().toString().equals("2AaXU2zv41Q00i6lwi1LwhHWzWf2")){

                                        final PopupMenu popupMenu = new PopupMenu(MainActivity.this,viewHolder.mView,Gravity.RIGHT);
                                        popupMenu.getMenuInflater().inflate(R.menu.popup_menu,popupMenu.getMenu());
                                        popupMenu.show();


                                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {

                                                if(item.getItemId() == R.id.share){

                                                    Intent shareQuote = new Intent();
                                                    shareQuote.setAction(Intent.ACTION_SEND);
                                                    shareQuote.putExtra(Intent.EXTRA_TEXT,model.getQuote()+"\n"+model.getSaidby());
                                                    shareQuote.setType("text/plain");
                                                    startActivity(shareQuote);

                                                }

                                                else if(item.getItemId() == R.id.copy){

                                                    ClipData clipData = ClipData.newPlainText("Copy",model.getQuote()+"\n"+model.getSaidby());
                                                    clipboardManager.setPrimaryClip(clipData);
                                                    Toast.makeText(MainActivity.this, "Quote Copied", Toast.LENGTH_SHORT).show();

                                                }

                                                else if(item.getItemId() == R.id.remove){

                                                    removePost = true;

                                                    mDatabase.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            if(removePost && authState){

                                                                mDatabase.child(post_id).removeValue();

                                                                removePost = false;

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }

                                                else if(item.getItemId() == R.id.edit){

                                                    editPost = true;

                                                    mDatabase.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                            if(editPost && authState) {

                                                                if (dataSnapshot.hasChild(post_id)) {

                                                                    Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                                                                    intent.putExtra("postID", post_id);
                                                                    startActivity(intent);

                                                                    editPost = false;

                                                                }

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });


                                                }

                                                return false;
                                            }
                                        });

                                        valEvent = false;

                                    }

                                    else{

                                        final PopupMenu popMenu = new PopupMenu(MainActivity.this,viewHolder.mView,Gravity.RIGHT);
                                        popMenu.getMenuInflater().inflate(R.menu.pop_menu,popMenu.getMenu());
                                        popMenu.show();

                                        popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {


                                                if(item.getItemId() == R.id.pop_share){

                                                    Intent shareQuote = new Intent();
                                                    shareQuote.setAction(Intent.ACTION_SEND);
                                                    shareQuote.putExtra(Intent.EXTRA_TEXT,model.getQuote()+"\n"+model.getSaidby());
                                                    shareQuote.setType("text/plain");
                                                    startActivity(shareQuote);

                                                }

                                                else if(item.getItemId() == R.id.pop_copy){

                                                    ClipData clipData = ClipData.newPlainText("Copy",model.getQuote()+"\n"+model.getSaidby());
                                                    clipboardManager.setPrimaryClip(clipData);
                                                    Toast.makeText(MainActivity.this, "Quote Copied", Toast.LENGTH_SHORT).show();

                                                }

                                                return false;
                                            }
                                        });


                                        valEvent = false;

                                    }


                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        return false;
                    }
                });



                viewHolder.mlikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        viewHolder.mlikeButton.animate().rotation(viewHolder.mlikeButton.getRotation()+360).start();

                        likeStatus = true;

                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(likeStatus && authState){

                                    if(dataSnapshot.child(post_id).child("likes").hasChild(mAuth.getCurrentUser().getUid())){

                                        mDatabase.child(post_id).child("likes").child(mAuth.getCurrentUser().getUid()).removeValue();

                                        String oldLike = dataSnapshot.child(post_id).child("likecount").getValue().toString();

                                        String newLike = String.valueOf(Integer.parseInt(oldLike)-1);

                                        mDatabase.child(post_id).child("likecount").setValue(newLike);

                                        likeStatus = false;
                                    }

                                    else{

                                        mDatabase.child(post_id).child("likes").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getDisplayName());
                                        String oldLike = dataSnapshot.child(post_id).child("likecount").getValue().toString();

                                        String newLike = String.valueOf(Integer.parseInt(oldLike)+1);

                                        mDatabase.child(post_id).child("likecount").setValue(newLike);

                                        likeStatus = false;
                                    }




                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

                    }
                });

            }
        };

        QuoteList.setAdapter(firebaseRecyclerAdapter);


    }



    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        View mView;

        ImageButton mlikeButton;

        DatabaseReference ndatabase;

        FirebaseAuth nAuth;

        boolean nAuthstate = false;

        private FirebaseAuth.AuthStateListener nAuthStateListener;

        public BlogViewHolder(View itemView) {
            super(itemView);

            ndatabase = FirebaseDatabase.getInstance().getReference().child("Quote_Lists");
            nAuth = FirebaseAuth.getInstance();
            ndatabase.keepSynced(true);

            if(nAuth.getCurrentUser() == null){

                nAuthstate =false;
            }

            else if(nAuth.getCurrentUser() != null){

                nAuthstate = true;
            }

            Random rnd = new Random();
            String[] col = {

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


            String[] tcol = {

                    "#4A148C",
                    "#B71C1C",
                    "#311B92",
                    "#000000",
                    "#1B5E20"
            };

            mView = itemView;

            //GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int [] {Color.parseColor(col[rnd.nextInt(14)]),Color.parseColor(col[rnd.nextInt(14)])});

            mlikeButton = (ImageButton)mView.findViewById(R.id.likeButton) ;

            //itemView.findViewById(R.id.linearLayout).setBackgroundColor(Color.parseColor(col[rnd.nextInt(14)]));
            //TextView tv = itemView.findViewById(R.id.tquote);

            //tv.setTextColor(Color.parseColor(tcol[rnd.nextInt(5)]));

            //itemView.findViewById(R.id.linearLayout).setBackgroundDrawable(gradientDrawable);
        }


        public void changeLikebutton(final String post_id){

            ndatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(nAuthstate) {
                        if (dataSnapshot.child(post_id).child("likes").hasChild(nAuth.getCurrentUser().getUid())) {

                            mlikeButton.setImageResource(R.drawable.ic_action_like_red);

                        } else {

                            mlikeButton.setImageResource(R.drawable.ic_action_like);

                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public  void setQuote(String quote){

            TextView vquote  = (TextView)mView.findViewById(R.id.tquote);

            vquote.setText(quote);

        }

        public  void setSaidby(String saidby){

            TextView vsaidby = (TextView)mView.findViewById(R.id.tsaidby);


            vsaidby.setText(saidby);

        }

        public  void setPostedby(String postedby){


            TextView vpostedby = (TextView)mView.findViewById(R.id.tpostedby);


            vpostedby.setText(postedby);

        }



        public  void setLikecount(String likecount){


            TextView vlikecount = (TextView)mView.findViewById(R.id.likeCount);


            vlikecount.setText(likecount);

        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (doubleBackToExitPressedOnce + 2000 > System.currentTimeMillis()) {

                finishAffinity();

                super.onBackPressed();
            } else {

                Toast.makeText(getBaseContext(),
                        "Press once again to exit!", Toast.LENGTH_SHORT)
                        .show();
            }
            doubleBackToExitPressedOnce = System.currentTimeMillis();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView search = (SearchView)item.getActionView();

        search.setMaxWidth(Integer.MAX_VALUE);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(!newText.isEmpty()){

                searchQuery = mDatabase.orderByChild("quote").startAt(newText).endAt(newText+"\uf8ff");

                    QuoteList.getRecycledViewPool().clear();
                    firebaseRecyclerAdapter.notifyDataSetChanged();

                    loadQuotes(searchQuery);

                }else{

                    QuoteList.getRecycledViewPool().clear();
                    firebaseRecyclerAdapter.notifyDataSetChanged();

                    loadQuotes(mainQuery);

                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_home){

            QuoteList.getRecycledViewPool().clear();
            firebaseRecyclerAdapter.notifyDataSetChanged();

            loadQuotes(mainQuery);

        } else if (id == R.id.nav_quotes) {

            if(authState) {

                QuoteList.getRecycledViewPool().clear();
                firebaseRecyclerAdapter.notifyDataSetChanged();

                loadQuotes(cuserQuery);

            }

        }else if (id == R.id.nav_logout) {

            mAuth.signOut();

        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
