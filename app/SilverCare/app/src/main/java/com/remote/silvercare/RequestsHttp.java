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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RequestsHttp {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String Requests(URL url, JSONObject userParam) {
        String Response = null;
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            Log.i("JSON", userParam.toString());
            os.writeBytes(userParam.toString());

            os.flush();
            os.close();
            BufferedReader br = null;

            if (100 <= conn.getResponseCode() && conn.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String responseData = br.lines().collect(Collectors.joining());
            conn.disconnect();

            JSONObject jsonObject = new JSONObject(responseData);
            Response = jsonObject.getString("error");
            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("ServerResponse", responseData);
            Log.i("MSG" , conn.getResponseMessage());

        }catch (Exception e) {
            e.printStackTrace();
        }
        return Response;
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public HashMap<String, String> RequestsLocation(URL url, JSONObject userParam) {
        HashMap<String, String> location = new HashMap<>();
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            Log.i("JSON", userParam.toString());
            os.writeBytes(userParam.toString());

            os.flush();
            os.close();
            BufferedReader br = null;

            if (100 <= conn.getResponseCode() && conn.getResponseCode() <= 399) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String responseData = br.lines().collect(Collectors.joining());
            conn.disconnect();

            JSONObject jsonObject = new JSONObject(responseData);
            location.put("latitude", jsonObject.getString("latitude"));
            location.put("longitude", jsonObject.getString("longitude"));

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("ServerResponse", responseData);
            Log.i("MSG" , conn.getResponseMessage());

        }catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

}