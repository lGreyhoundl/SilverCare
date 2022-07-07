package com.remote.silvercare.ui.location;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.remote.silvercare.GetCurrentAddress;
import com.remote.silvercare.LocationInform;
import com.remote.silvercare.R;
import com.remote.silvercare.RequestsHttp;
import com.remote.silvercare.UserInform;
import com.remote.silvercare.databinding.FragmentLocationBinding;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LocationFragment extends Fragment {

    private FragmentLocationBinding binding;
    private GoogleMap mMap;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    LocationInform locationInform = new LocationInform();

    Double latitude;
    Double longitude;

    UserInform userInform = new UserInform();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_location, container, false);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        SharedPreferences userID = getActivity().getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        userInform.setUserId(userID.getString("user_id", null));

        SharedPreferences elderPhone = getActivity().getSharedPreferences("phoneNumber", Activity.MODE_PRIVATE);
        userInform.setUserContactElder(elderPhone.getString("elder_phoneNumber", null));


        DatabaseReference locationRef = mRootRef.child("users").child(userInform.getUserId());
        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locationInform = snapshot.getValue(LocationInform.class);
                latitude = Double.parseDouble(locationInform.getLatitude());
                longitude = Double.parseDouble(locationInform.getLongitude());

//                Toast.makeText(getActivity(), userInform.getUserContactElder(), Toast.LENGTH_LONG).show();

                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {

                        mMap = googleMap;

                        LatLng home = new LatLng(latitude, longitude);

                        GetCurrentAddress currentAddress = new GetCurrentAddress();
                        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                        String location = currentAddress.getCurrentAddress(geocoder, latitude, longitude);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(home);
                        markerOptions.title(userInform.getUserContactElder());
                        markerOptions.snippet(location);
                        mMap.addMarker(markerOptions);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 15));

                        googleMap.setOnInfoWindowClickListener(infoWindowClickListener);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }


    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(@NonNull Marker marker) {
            String markerId = marker.getId();
            Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+ userInform.getUserContactElder()));
            startActivity(call);
        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}