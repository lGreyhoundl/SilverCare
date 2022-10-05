package com.remote.silvercare;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);

        Button logout_btn = (Button) findViewById(R.id.btn_logout);

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder Logout = new AlertDialog.Builder(SettingPage.this);
                Logout.setTitle("로그아웃 하시겠습니까?");
                Logout.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences auto = getSharedPreferences("autoLogin", MODE_PRIVATE);
                        SharedPreferences.Editor editor = auto.edit();
                        editor.clear();
                        editor.commit();

                        auto = getSharedPreferences("phoneNumber", MODE_PRIVATE);
                        editor = auto.edit();
                        editor.clear();
                        editor.commit();

                        auto = getSharedPreferences("homeAddress", MODE_PRIVATE);
                        editor = auto.edit();
                        editor.clear();
                        editor.commit();

                        Intent intent = new Intent(SettingPage.this, UserLoginActivity.class);
                        startActivity(intent);
                    }
                });
                Logout.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                Logout.show();


            }
        });
    }
}