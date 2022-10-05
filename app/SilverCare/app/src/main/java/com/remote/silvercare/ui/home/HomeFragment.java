package com.remote.silvercare.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.remote.silvercare.DeviceInform;
import com.remote.silvercare.HomeAdapter;
import com.remote.silvercare.UserInform;
import com.remote.silvercare.databinding.FragmentHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    UserInform userInform = new UserInform();
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DeviceInform deviceInform;

    Intent foregroundServiceIntent;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        SharedPreferences userID = getActivity().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        userInform.setUserId(userID.getString("user_id", null));

        DatabaseReference deviceInformRef = mRootRef.child("users").child(userInform.getUserId()).child("Device");

        deviceInformRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Task<DataSnapshot> deviceInformRef = mRootRef.child("users").child(userInform.getUserId()).child("Device").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            String val =  String.valueOf(task.getResult().getValue());
                            JSONObject json = null;
                            try {
                                json = new JSONObject(val);
//                                Log.i("firebase", json.getString("-NA3a-1qdrRKryLy785w"));
                                Iterator<String> keys = json.keys();
                                final GridView gridView = binding.Device;
                                HomeAdapter adapter = new HomeAdapter();

                                while(keys.hasNext())
                                {
//                                    Log.i("firebase", json.getString(String.valueOf(keys.next())));
                                    JSONObject data = (JSONObject) json.get(keys.next());

                                    Log.i("firebase_room", data.getString("room"));
                                    Log.i("firebase_status", data.getString("status"));
                                    adapter.addItem(new DeviceInform(data.getString("room"), data.getString("status")));
                    //                adapter.addItem(deviceInform);
                                    gridView.setAdapter(adapter);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        adapter.addItem(new DeviceInform("안방", "오류", "움직임 없음"));
//        adapter.addItem(new DeviceInform("침실", "정상", "움직임 없음"));

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}