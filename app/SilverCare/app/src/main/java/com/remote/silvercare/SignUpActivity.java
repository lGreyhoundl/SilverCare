package com.remote.silvercare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONObject;

import java.net.URL;

public class SignUpActivity extends AppCompatActivity {
    Thread signUpThread;
    String Response = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        EditText user_id = (EditText) findViewById(R.id.user_id);
        EditText user_pwd = (EditText) findViewById(R.id.user_pwd);
        EditText user_pwd_check = (EditText) findViewById(R.id.user_pwd_check);
        EditText user_contact_protector = (EditText) findViewById(R.id.user_contact_protector);
        EditText user_contact_elder = (EditText) findViewById(R.id.user_contact_elder);
        EditText user_email = (EditText) findViewById(R.id.user_email);
        Button signUp_btn = (Button) findViewById(R.id.btn_signup);

        UserInform UserSignupInform = new UserInform();

        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String UserId = user_id.getText().toString();
                String UserPwd = user_pwd.getText().toString();
                String UserPwdCheck = user_pwd_check.getText().toString();
                String UserContactProtector = user_contact_protector.getText().toString();
                String USerContactElder = user_contact_elder.getText().toString();
                String UserEmail = user_email.getText().toString();

                if (UserId.equals("") == true || UserPwd.equals("") == true || UserPwdCheck.equals("") == true || UserContactProtector.equals("") == true || USerContactElder.equals("") || UserEmail.equals("") == true ) {
                    AlertDialog.Builder nullWarning = new AlertDialog.Builder(SignUpActivity.this);
                    nullWarning.setTitle("공백 오류");
                    nullWarning.setMessage("입력되지 않은 값이 존재합니다.");
                    nullWarning.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 아무것도 안함
                        }
                    });
                    nullWarning.show();

                }else if(UserPwd.equals(UserPwdCheck) == false){
                    AlertDialog.Builder pwdWarning = new AlertDialog.Builder(SignUpActivity.this);
                    pwdWarning.setTitle("비밀번호 오류");
                    pwdWarning.setMessage("입력된 비밀번호가 일치하지 않습니다.");
                    pwdWarning.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            user_pwd_check.setText("");
                            user_pwd.setText("");
                        }
                    });
                    pwdWarning.show();
                }else{
                    UserSignupInform.setUserId(UserId);
                    UserSignupInform.setUserPwd(UserPwd);
                    UserSignupInform.setUserContactProtector(UserContactProtector);
                    UserSignupInform.setUserContactElder(USerContactElder);
                    UserSignupInform.setUserEmail(UserEmail);

                    SignUpThread(UserSignupInform);
                    try {
                        signUpThread.join();
                        try {
                            Log.i("ServerResponse", Response);
                        }catch (Exception e){

                        }
                        if(Response.equals("200")){
                            SharedPreferences phoneNumber = getSharedPreferences("phoneNumber", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor ContactEdit = phoneNumber.edit();
                            ContactEdit.putString("elder_phoneNumber", UserSignupInform.getUserContactElder());
                            ContactEdit.putString("protector_phoneNumber", UserSignupInform.getUserContactProtector());
                            ContactEdit.commit();
                            AlertDialog.Builder success = new AlertDialog.Builder(SignUpActivity.this);
                            success.setTitle("회원가입 성공");
                            success.setMessage("환영합니다 " + user_id.getText().toString()+" 님");
                            success.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(SignUpActivity.this, UserLoginActivity.class);
                                    intent.putExtra("user_id", user_id.getText().toString());
                                    intent.putExtra("user_pwd", user_pwd.getText().toString());
                                    startActivity(intent);
                                }
                            });
                            success.show();
                        }
                        else if(Response.equals("401")){
                            AlertDialog.Builder IdWarning = new AlertDialog.Builder(SignUpActivity.this);
                            IdWarning.setTitle("아이디 오류");
                            IdWarning.setMessage("입력된 아이디가 이미 존재합니다.");
                            IdWarning.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    user_id.setText("");;
                                }
                            });
                            IdWarning.show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    public void SignUpThread(UserInform UserSignupInform){
        signUpThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    RequestsHttp UserSignUp = new RequestsHttp();
                    URL url = new URL("http://tera.dscloud.me:3000/signup");
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("user_id", UserSignupInform.getUserId());
                    jsonParam.put("user_pwd", UserSignupInform.getUserPwd());
                    jsonParam.put("user_contact_protector", UserSignupInform.getUserContactProtector());
                    jsonParam.put("user_contact_elder", UserSignupInform.getUserContactElder());
                    jsonParam.put("user_email", UserSignupInform.getUserEmail());

                    Response = UserSignUp.Requests(url, jsonParam);

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        signUpThread.start();
    }
}