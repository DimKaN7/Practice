package com.example.client.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.client.Employee.Employee;
import com.example.client.Violation.Violation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaveLoad {

    private Context context;

    private final String TAG = "SaveLoad";

    public SaveLoad(Context context) {
        this.context = context;
    }

    public void saveData(Employee employee, Double latitude, Double longitude, Long time) {
        SharedPreferences preferences = context.getSharedPreferences("com.example.client", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        try {
            editor.putInt("e_id", employee.getId());
            editor.putString("e_code", employee.getCode());
            editor.putString("e_name", employee.getName());
            editor.putString("e_position", employee.getPosition());
            editor.putString("e_latitude", Double.toString(latitude));
            editor.putString("e_longitude", Double.toString(longitude));
            editor.putLong("e_time", time);
        } catch (NullPointerException e) {
            editor.putInt("e_id", employee.getId());
            editor.putString("e_code", employee.getCode());
            editor.putString("e_name", employee.getName());
            editor.putString("e_position", employee.getPosition());
        }
        editor.commit();
    }

    public MyData loadData() {
        SharedPreferences preferences =  context.getSharedPreferences("com.example.client", context.MODE_PRIVATE);
        int id = preferences.getInt("e_id", 0);
        String code = preferences.getString("e_code", "");
        String name = preferences.getString("e_name", "");
        String position = preferences.getString("e_position", "");
        Employee employee = new Employee(id, code, name, position);
        MyData result = new MyData();
        try {
            Double latitude = Double.parseDouble(preferences.getString("e_latitude", ""));
            Double longitude = Double.parseDouble(preferences.getString("e_longitude", ""));
            Long time = preferences.getLong("e_time", 0);

            result.employee = employee;
            result.latitude = latitude;
            result.longitude = longitude;
            result.time = time;
        } catch (Exception e) {
            result.employee = employee;
            result.latitude = null;
            result.longitude = null;
            result.time = null;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        return result;
    }

    public void saveViolation(Violation violation) {
        SharedPreferences preferences =  context.getSharedPreferences("com.example.client_offline", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String violationPresentation = violation.getEmployeeId() + "#" + violation.getLatitude() +
                "#" + violation.getLongitude() + "#" + violation.getViolationDate();

        editor.putString(Integer.toString(preferences.getAll().size()), violationPresentation);
        editor.commit();

        Log.e(TAG, preferences.getAll().size() + " saved");
    }

    public List<Violation> getAllSavedViolations() {
        SharedPreferences preferences =  context.getSharedPreferences("com.example.client_offline", context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();

        List<Violation> result = new ArrayList<>();
        for (int i = 0; i < preferences.getAll().size(); ++i) {
            String employee = preferences.getString(Integer.toString(i), "");
//            Log.e(TAG, employee.split("#")[0]);
            Integer eId = Integer.valueOf(employee.split("#")[0]);
            Double eLat = Double.valueOf(employee.split("#")[1]);
            Double eLon = Double.valueOf(employee.split("#")[2]);
            String eTim = employee.split("#")[3];

            Violation violation = new Violation();
            violation.setEmployeeId(eId);
            violation.setLatitude(eLat);
            violation.setLongitude(eLon);
            violation.setViolationDate(eTim);

            result.add(violation);
        }
//        editor.commit();
        return result;
    }

}
