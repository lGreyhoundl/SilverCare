package com.remote.silvercare;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {
    Thread signUpThread;
    JSONObject Response = null;
    String Error = "401";

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        EditText user_id = (EditText) findViewById(R.id.user_id);
        EditText user_pwd = (EditText) findViewById(R.id.user_pwd);
        EditText user_pwd_check = (EditText) findViewById(R.id.user_pwd_check);
        EditText user_contact_protector = (EditText) findViewById(R.id.user_contact_protector);
        EditText user_contact_elder = (EditText) findViewById(R.id.user_contact_elder);
        EditText user_residence = (EditText) findViewById(R.id.user_residence);
        ImageButton location_btn = (ImageButton) findViewById(R.id.location_btn);
        Button signUp_btn = (Button) findViewById(R.id.btn_signup);

        UserInform UserSignupInform = new UserInform();

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GpsTracker gpsTracker = new GpsTracker(SignUpActivity.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                user_residence.setText(address);
            }
        });

        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String UserId = user_id.getText().toString();
                String UserPwd = user_pwd.getText().toString();
                String UserPwdCheck = user_pwd_check.getText().toString();
                String UserContactProtector = user_contact_protector.getText().toString();
                String USerContactElder = user_contact_elder.getText().toString();
                String UserResidence = user_residence.getText().toString();

                if (UserId.equals("") == true || UserPwd.equals("") == true || UserPwdCheck.equals("") == true || UserContactProtector.equals("") == true || USerContactElder.equals("") || UserResidence.equals("") == true ) {
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
                    UserSignupInform.setUserResidence(UserResidence);

                    SignUpThread(UserSignupInform);
                    try {
                        signUpThread.join();
                        Log.i("Response", String.valueOf(Response));
                        Error = Response.getString("error");
                        try {
                            Log.i("ServerResponse", Error);
                        }catch (Exception e){

                        }
                        if(Error.equals("200")){
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
                        else if(Error.equals("400")){
                            AlertDialog.Builder IdWarning = new AlertDialog.Builder(SignUpActivity.this);
                            IdWarning.setTitle("서버 오류");
                            IdWarning.setMessage("서버에 문제가 있습니다");
                            IdWarning.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    user_id.setText("");;
                                }
                            });
                            IdWarning.show();
                        }
                        else if(Error.equals("401")){
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
                    } catch (InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(SignUpActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {

                    Toast.makeText(SignUpActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showDialogForLocationServiceSetting() {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SignUpActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(SignUpActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(SignUpActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(SignUpActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(SignUpActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(SignUpActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    public String getCurrentAddress( double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
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
                    jsonParam.put("user_residence", URLEncoder.encode(UserSignupInform.getUserResidence(),"UTF-8"));

                    Response = UserSignUp.Requests(url, jsonParam);

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        signUpThread.start();
    }
}