package com.iff.onepairv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OthersDealsPage extends AppCompatActivity {

    ListView listView;
    ListViewAdapter adapter;
    String[] title;
    String[] description;
    int[] icon;
    ArrayList<Model> arrayList = new ArrayList<Model>();

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals_main);
        listView = findViewById(R.id.listView);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.deal_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Others");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://128.199.167.80:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BackEndController backEndController = retrofit.create(BackEndController.class);
        Call<ArrayList<Deal>> call = backEndController.getOthersDeals();
        call.enqueue(new Callback<ArrayList<Deal>>() {
            @Override
            public void onResponse(Call<ArrayList<Deal>> call, Response<ArrayList<Deal>> response) {
                if(!response.isSuccessful()){
                    System.out.println("Oops something went wrong!");
                    return;
                }
                ArrayList<Deal> deals = response.body();
                for(Deal deal: deals){
                    //deal.printDeal();
                    Model model = new Model(deal.getName(), "", deal.getImage());
                    arrayList.add(model);
                }
                adapter = new ListViewAdapter(getApplicationContext(), arrayList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<ArrayList<Deal>> call, Throwable t) {
                System.out.println("Oops something went wrong!");
            }
        });
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
            Intent startIntent = new Intent(OthersDealsPage.this, ProfileActivity.class);
            startActivity(startIntent);
        }

        return true;
    }
    private void sendToStart() {
        Intent startIntent = new Intent(OthersDealsPage.this, StartActivity.class);
        startActivity(startIntent);
        finish();
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
}
