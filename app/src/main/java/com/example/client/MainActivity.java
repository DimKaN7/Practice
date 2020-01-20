package com.example.client;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.client.Classes.Constants;
import com.example.client.Classes.DateConverter;
import com.example.client.Classes.DistanceCounter;
import com.example.client.Classes.MyData;
import com.example.client.Classes.MyService;
import com.example.client.Classes.SaveLoad;
import com.example.client.Employee.Employee;
import com.example.client.Violation.Violation;
import com.example.client.Violation.ViolationsController;
import com.example.client.Violation.ViolationsService;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView textViewEmployee;
    private TextView textViewLongitude;
    private TextView textViewLatitude;
    private TextView textViewTime;
    private Intent intent;
    private LocationManager locationManager;
    private LinearLayout linearLayout;

    private final String TAG = "MainActivity";

    private Double latitude;
    private Double longitude;
    private Long time;

    private Employee employee;

    private ViolationsService violationsService;
    private ViolationsController violationsController;
    private DateConverter converter;
    private SaveLoad saveLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewEmployee = findViewById(R.id.textViewEmployee);
        textViewLongitude = findViewById(R.id.textViewLongitude);
        textViewLatitude = findViewById(R.id.textViewLatitude);
        textViewTime = findViewById(R.id.textViewTime);
        linearLayout = findViewById(R.id.linearLayout);

        try {
            employee = (Employee) getIntent().getSerializableExtra("employee");
            textViewEmployee.setText(employee.getName() + ", " + employee.getPosition());
        } catch (NullPointerException e) {

        }

        violationsController = new ViolationsController("http://172.20.10.3:3000/"); // 104
        violationsService = violationsController.getApi();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        converter = new DateConverter();
        saveLoad = new SaveLoad(this);

        Bundle bundle = new Bundle();
        bundle.putSerializable("employee", employee);
        intent = new Intent(this, MyService.class);
        intent.setAction(Constants.ACTION.MAIN_ACTION);
        intent.putExtras(bundle);
    }

    @Override
    protected void onPause() {
        SaveLoad saver = new SaveLoad(this);
        saver.saveData(employee, latitude, longitude, time);
        startService(intent);
        super.onPause();
    }

    @Override
    protected void onResume() {
        stopService(intent);
        if (!isSharedPreferencesEmpty("com.example.client")) {
            MyData myData = saveLoad.loadData();
            employee = myData.employee;
            try {
                latitude = myData.latitude;
                longitude = myData.longitude;
                time = myData.time;

                textViewEmployee.setText(employee.getName() + ", " + employee.getPosition());
                textViewLongitude.setText(Double.toString(longitude));
                textViewLatitude.setText(Double.toString(latitude));
                textViewTime.setText(converter.convert(time));

                DistanceCounter distanceCounter = new DistanceCounter(latitude, longitude, 52.273094, 104.291009);
                Double dis = distanceCounter.getDistance();
                if (dis >= Constants.WORK.VIOLATION_DIST) {
                    linearLayout.setBackground(getDrawable(R.drawable.border_red));
                }
                else {
                    linearLayout.setBackground(getDrawable(R.drawable.border_green));
                }

            } catch (Exception e) {
                textViewEmployee.setText(employee.getName() + ", " + employee.getPosition());
            }
        }

        if (!isLocationEnabled()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5, 1, locationListener);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        clearSharedPreferences("com.example.client");
        super.onDestroy();
    }

    @Override
    public ComponentName startService(Intent service) {
        locationManager.removeUpdates(locationListener);
        return super.startService(service);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5, 1, locationListener);
            }
        }
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            time = location.getTime();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewLongitude.setText(Double.toString(longitude));
                    textViewLatitude.setText(Double.toString(latitude));
                    textViewTime.setText(converter.convert(time));
                }
            });

            Violation violation = new Violation();
            violation.setLatitude(latitude);
            violation.setLongitude(longitude);
            violation.setViolationDate(converter.convert(time));
            violation.setEmployeeId(employee.getId());

            DistanceCounter distanceCounter = new DistanceCounter(latitude, longitude, 52.273094, 104.291009);
            Double dis = distanceCounter.getDistance();
            Log.e(TAG, "Distance: " + dis);

            if (isConnected()) {
                // если имеются сохраненные на устройстве нарушения - пытаемся их отправить
                // (нарушения сохрнаяются тогда, когда нет связи с сервером)
                if (!isSharedPreferencesEmpty("com.example.client_offline")) {
                    for (Violation v : saveLoad.getAllSavedViolations()) {
                        Call<Void> call = violationsService.addViolation(v);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.e(TAG, "Connection established. Sending saved violations");
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) { }
                        });
                    }
                    clearSharedPreferences("com.example.client_offline");
                }

                if (dis >= Constants.WORK.VIOLATION_DIST) {
                    linearLayout.setBackground(getDrawable(R.drawable.border_red));

                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(2000);

                    Call<Void> call = violationsService.addViolation(violation);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) { }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) { }
                    });
                }
                else {
                    linearLayout.setBackground(getDrawable(R.drawable.border_green));
                }
            }
            else {
                if (dis >= Constants.WORK.VIOLATION_DIST) {
                    linearLayout.setBackground(getDrawable(R.drawable.border_red));

                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(2000);

                    saveLoad.saveViolation(violation);
                    Log.e(TAG, "No connection, saving to sp");
                }
                else {
                    linearLayout.setBackground(getDrawable(R.drawable.border_green));
                }
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private boolean isSharedPreferencesEmpty(String name) {
        SharedPreferences preferences = getSharedPreferences(name, MODE_PRIVATE); // "com.example.client"
        return preferences.getAll().size() == 0;
    }

    private void clearSharedPreferences(String name) {
        SharedPreferences preferences = getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    private boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

}