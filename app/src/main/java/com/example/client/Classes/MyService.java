package com.example.client.Classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.client.Employee.Employee;
import com.example.client.LoginActivity;
import com.example.client.MainActivity;
import com.example.client.R;
import com.example.client.Violation.Violation;
import com.example.client.Violation.ViolationsController;
import com.example.client.Violation.ViolationsService;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyService extends Service {

    private static final String TAG = "MyService";
    private Intent returnActivityIntent;
    private LocationManager locationManager;

    private Double longitude;
    private Double latitude;
    private Long time;

    private Employee employee;
    private Context context;

    private ViolationsService violationsService;
    private ViolationsController violationsController;
    private DateConverter converter;
    private SaveLoad saveLoad;

    private final String NOTIFICATION_CHANNEL_ID = "com.example.client";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (intent.getAction().equals(Constants.ACTION.MAIN_ACTION)) {
                startForeground(R.layout.activity_main);
            } else {
                startForeground(R.layout.activity_login);
            }
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5, 1, locationListener);
            employee = (Employee) intent.getSerializableExtra("employee");
            converter = new DateConverter();
            saveLoad = new SaveLoad(getBaseContext());

            violationsController = new ViolationsController("http://192.168.0.56:3000/");
            violationsService = violationsController.getApi();

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("employee", employee);
//        returnActivityIntent.putExtras(bundle);
        super.onDestroy();
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            time = location.getTime();

            Violation violation = new Violation();
            violation.setLatitude(latitude);
            violation.setLongitude(longitude);
            violation.setViolationDate(converter.convert(time));
            violation.setEmployeeId(employee.getId());

            Log.e(TAG, Double.toString(latitude));
            Log.e(TAG, Double.toString(longitude));
            Log.e(TAG, converter.convert(time));

            SaveLoad saver = new SaveLoad(getBaseContext());
            saver.saveData(employee, latitude, longitude, time);

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
                    Call<Void> call = violationsService.addViolation(violation);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) { }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) { }
                    });

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);

                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(2000);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), NOTIFICATION_CHANNEL_ID);
                    notificationBuilder
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.ic_warning)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentTitle(getResources().getString(R.string.organization))
                            .setContentText("Вы вышли за пределы области работы. Вернитесь немедленно!!!")
                            .setContentIntent(pIntent);
                    notificationManager.notify(0, notificationBuilder.build());
                    stopForeground(Service.STOP_FOREGROUND_REMOVE);
                }
            }
            else {
                if (dis >= Constants.WORK.VIOLATION_DIST) {
                    saveLoad.saveViolation(violation);
                    Log.e(TAG, "No connection, saving to sp");

                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);

                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(2000);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), NOTIFICATION_CHANNEL_ID);
                    notificationBuilder
                            .setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.ic_warning)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentTitle(getResources().getString(R.string.organization))
                            .setContentText("Вы вышли за пределы области работы. Вернитесь немедленно!!!")
                            .setContentIntent(pIntent);
                    notificationManager.notify(0, notificationBuilder.build());
                    stopForeground(Service.STOP_FOREGROUND_REMOVE);
                }
            }



//            DistanceCounter distanceCounter = new DistanceCounter(latitude, longitude, 52.273094, 104.291009);
//            Double dis = distanceCounter.getDistance();
//            Log.e(TAG, Double.toString(dis));
//
//            if (dis >= Constants.WORK.VIOLATION_DIST) {
//                Violation violation = new Violation();
//                violation.setLatitude(latitude);
//                violation.setLongitude(longitude);
//                violation.setViolationDate(converter.convert(time));
//                violation.setEmployeeId(employee.getId());
//
//                Call<Void> call = violationsService.addViolation(violation);
//                call.enqueue(new Callback<Void>() {
//                    @Override
//                    public void onResponse(Call<Void> call, Response<Void> response) { }
//
//                    @Override
//                    public void onFailure(Call<Void> call, Throwable t) { }
//                });
//
//                Intent intent = new Intent(getBaseContext(), MainActivity.class);
//                PendingIntent pIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
//
//                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//                vibrator.vibrate(2000);
//
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext(), NOTIFICATION_CHANNEL_ID);
//                notificationBuilder
//                        .setAutoCancel(true)
//                        .setWhen(System.currentTimeMillis())
//                        .setSmallIcon(R.drawable.ic_warning)
//                        .setPriority(NotificationCompat.PRIORITY_HIGH)
//                        .setContentTitle(getResources().getString(R.string.organization))
//                        .setContentText("Вы вышли за пределы области работы. Вернитесь немедленно!!!")
//                        .setContentIntent(pIntent);
//                notificationManager.notify(0, notificationBuilder.build());
//                stopForeground(Service.STOP_FOREGROUND_REMOVE);
//            }
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

    @RequiresApi((Build.VERSION_CODES.O))
    private void startForeground(int currentActivity) {
        if (currentActivity == R.layout.activity_main) {
            returnActivityIntent = new Intent(getBaseContext(), MainActivity.class);
        } else {
            returnActivityIntent = new Intent(getBaseContext(), LoginActivity.class);
        }
        PendingIntent pReturnActivityIntent = PendingIntent.getActivity(getBaseContext(), 0, returnActivityIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Идет получение координат")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pReturnActivityIntent)
                .build();
        startForeground(1, notification);
    }
}