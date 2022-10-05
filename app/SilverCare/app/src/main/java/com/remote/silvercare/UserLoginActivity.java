package com.remote.silvercare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class UserLoginActivity extends AppCompatActivity {
    Thread LoginThread;
    JSONObject Response = null;
    String Error = "501";
    String ProtectorContact = null;
    String ElderContact = null;
    String HomeAddress = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);



        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }

        Button login_btn = (Button) findViewById(R.id.btn_login);
        Button signup_btn = (Button) findViewById(R.id.btn_signup);
        EditText user_id = (EditText) findViewById(R.id.user_id);
        EditText user_pwd = (EditText) findViewById(R.id.user_pwd);
        CheckBox protector_login = (CheckBox) findViewById(R.id.protector_login);

        UserInform user_login_inform = new UserInform();

        try{
            Intent intent = getIntent();
            String intent_user_id = intent.getStringExtra("user_id");
            String intent_user_pwd = intent.getStringExtra("user_pwd");
            if(intent_user_id.equals(null) == false && intent_user_pwd.equals(null) == false){
                user_id.setText(intent_user_id);
                user_pwd.setText(intent_user_pwd);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
            String UserId = auto.getString("user_id", null);
            String UserPwd = auto.getString("user_pwd", null);
            String Protector = auto.getString("protector", null);
            if(UserId != null && UserPwd != null && Protector.equals("false")){
                Intent intent = new Intent(UserLoginActivity.this, ElderPageActivity.class);
                intent.putExtra("user_id", UserId);
                startActivity(intent);
            }
            else if(UserId != null && UserPwd != null && Protector.equals("true")){
                Intent intent = new Intent(UserLoginActivity.this, ProtectorPageActivity.class);
                intent.putExtra("user_id", UserId);
                startActivity(intent);
            }


        }catch (Exception e){
            e.printStackTrace();
        }


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_login_inform.setUserId(user_id.getText().toString());
                user_login_inform.setUserPwd(user_pwd.getText().toString());

                loginThread(user_login_inform);
                try {
                    LoginThread.join();
                    Error = Response.getString("error");
                    ProtectorContact = Response.getString("protector_contact");
                    ElderContact = Response.getString("elder_contact");
                    HomeAddress = Response.getString("address");

                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
                try{
                    Log.i("ErrorCode", Error);
                }catch (Exception e){
                    Error = "-1";
                }

                if (Error.equals("200")){
                    SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor autoLoginEdit = auto.edit();
                    autoLoginEdit.putString("user_id", user_login_inform.getUserId());
                    autoLoginEdit.putString("user_pwd", user_login_inform.getUserPwd());
                    autoLoginEdit.commit();

                    SharedPreferences phoneNumber = getSharedPreferences("phoneNumber", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor ContactEdit = phoneNumber.edit();
                    ContactEdit.putString("protector_phoneNumber", ProtectorContact);
                    ContactEdit.putString("elder_phoneNumber", ElderContact);
                    ContactEdit.commit();

                    SharedPreferences homeAddress = getSharedPreferences("homeAddress", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor addressEdit = homeAddress.edit();
                    addressEdit.putString("home_address", HomeAddress);
                    addressEdit.commit();

                    Log.i("address", HomeAddress);
                    Log.i("protector_phoneNumber", ProtectorContact);
                    Log.i("elder_phoneNumber", ElderContact);

                    if(protector_login.isChecked()){
                        autoLoginEdit.putString("protector", "true");
                    }
                    else{
                        autoLoginEdit.putString("protector", "false");
                    }
                    autoLoginEdit.commit();

                    if(protector_login.isChecked()) {
                        Intent intent = new Intent(UserLoginActivity.this, ProtectorPageActivity.class);
                        intent.putExtra("user_id", user_id.getText().toString());
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(UserLoginActivity.this, ElderPageActivity.class);
                        intent.putExtra("user_id", user_id.getText().toString());
                        startActivity(intent);
                    }
                }
                else
                {
                    AlertDialog.Builder failLogin = new AlertDialog.Builder(UserLoginActivity.this);
                    failLogin.setTitle("로그인 실패");
                    failLogin.setMessage("아이디 또는 비밀번호가 틀렸습니다.");
                    failLogin.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 아무것도 안함
                        }
                    });
                    failLogin.show();
                }
            }
        });
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLoginActivity.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    public void loginThread(UserInform user_login_inform){
        LoginThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    RequestsHttp LoinRequests = new RequestsHttp();
                    URL url = new URL("http://tera.dscloud.me:3000/login");
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("user_id", user_login_inform.getUserId());
                    jsonParam.put("user_pwd", user_login_inform.getUserPwd());

                    Response = LoinRequests.Requests(url, jsonParam);

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        LoginThread.start();
    }
}
