package com.iff.onepairv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private CardView foodCard, entertainmentCard, retailCard, othersCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("1Pair");

        //add click listener to cards

        foodCard = (CardView) findViewById(R.id.food);
        entertainmentCard = (CardView) findViewById(R.id.entertainment);
        retailCard = (CardView) findViewById(R.id.retail);
        othersCard = (CardView) findViewById(R.id.others);

        //add click listener to cards
        foodCard.setOnClickListener(this);
        entertainmentCard.setOnClickListener(this);
        retailCard.setOnClickListener(this);
        othersCard.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendToStart();

        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        else if(item.getItemId() == R.id.main_profile_btn){
            Intent ProfileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(ProfileIntent);
            finish();
        }

        return true;
    }

    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
            case R.id.food:
                i = new Intent(this, FoodDealsPage.class);
                startActivity(i);
                break;
            case R.id.entertainment:
                i = new Intent(this, EntertainmentDealsPage.class);
                startActivity(i);
                break;
            case R.id.retail:
                i = new Intent(this, RetailDealsPage.class);
                startActivity(i);
                break;
            case R.id.others:
                i = new Intent(this, OthersDealsPage.class);
                startActivity(i);
                break;
        }
    }
}
