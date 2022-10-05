package com.remote.silvercare;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RequestsHttp {
    JSONObject jsonObject;
    @RequiresApi(api = Build.VERSION_CODES.N)
    public JSONObject Requests(URL url, JSONObject userParam) {
        String Response = null;
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(userParam.toString());
//            os.writeBytes(URLEncoder.encode(userParam.toString(), "UTF-8"));
            Log.i("JSON", userParam.toString());

            os.flush();
            os.close();
            BufferedReader br = null;

            if (100 <= conn.getResponseCode() && conn.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(),"UTF-8"));
            }

            String responseData = br.lines().collect(Collectors.joining());
            conn.disconnect();

            jsonObject = new JSONObject(responseData);
//            Response = jsonObject.getString("error");
            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("ServerResponse", responseData);
            Log.i("MSG" , conn.getResponseMessage());

        }catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}