package com.remote.silvercare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.remote.silvercare.databinding.ActivityProtectorPageBinding;
import com.remote.silvercare.ui.home.MoveDetectService;
import com.remote.silvercare.ui.location.LocationFragment;

public class ProtectorPageActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityProtectorPageBinding binding;

    private Intent foregroundServiceIntent;

    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(null == MoveDetectService.serviceIntent){
            foregroundServiceIntent = new Intent(this, MoveDetectService.class);
            startService(foregroundServiceIntent);
            Toast.makeText(getApplicationContext(), "동작감지 서비스를 시작합니다.", Toast.LENGTH_SHORT).show();
        }else{
//            foregroundServiceIntent = UndeadService.serviceIntent;
            Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:119"));
            startActivity(call);
            Toast.makeText(getApplicationContext(), "이미 서비스가 실행중입니다.", Toast.LENGTH_SHORT).show();
        }

        binding = ActivityProtectorPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarProtectorPage.toolbar);
        binding.appBarProtectorPage.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                SharedPreferences elderPhone = getSharedPreferences("phoneNumber", Activity.MODE_PRIVATE);
                String elder_phone = elderPhone.getString("elder_phoneNumber", null);

                Intent call = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"+ elder_phone));
                startActivity(call);
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_device, R.id.nav_location)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_protector_page);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Bundle bundle = new Bundle();

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        user_id = auto.getString("user_id", null);
        bundle.putString("user_id", user_id);
        LocationFragment locationFragment = new LocationFragment();
        locationFragment.setArguments(bundle);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != foregroundServiceIntent){
            stopService(foregroundServiceIntent);
            foregroundServiceIntent = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.protector_page, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings){
            Intent intent = new Intent(this, SettingPage.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_protector_page);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}