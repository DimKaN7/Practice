package com.example.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.client.Classes.Constants;
import com.example.client.Classes.DateConverter;
import com.example.client.Classes.MyService;
import com.example.client.Employee.Employee;
import com.example.client.Employee.EmployeesController;
import com.example.client.Employee.EmployeesService;
import com.example.client.Violation.ViolationsController;
import com.example.client.Violation.ViolationsService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextCode;
    private Button buttonContinue;
    private EmployeesService employeesService;
    private EmployeesController employeesController;
    private ViolationsService violationsService;
    private ViolationsController violationsController;

    private Employee temp;
    private String tempCode;

    private final String TAG = "LoginActivity";

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextCode = findViewById(R.id.editTextCode);
        buttonContinue = findViewById(R.id.buttonContinue);

        buttonContinue.setOnClickListener(this);

        employeesController = new EmployeesController(Constants.WORK.BASE_URL);
        employeesService = employeesController.getApi();

        violationsController = new ViolationsController(Constants.WORK.BASE_URL);
        violationsService = violationsController.getApi();

        intent = new Intent(this, MyService.class);
        intent.setAction(Constants.ACTION.LOGIN_ACTION);

        SharedPreferences preferences = getSharedPreferences(Constants.WORK.PACKAGE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

//        DateConverter dateConverter = new DateConverter();
//        Log.e(TAG, dateConverter.convert());


//        +-10m_
//        DistanceCounter counter = new DistanceCounter(52.28774431, 104.34091798, 52.273094, 104.291009);
//        Log.e(TAG, Double.toString(counter.getDistance()));
    }


    @Override
    public void onClick(View v) {
        tempCode = editTextCode.getText().toString();
        if (tempCode.length() == 6) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Пожалуйста, подождите...");
            progressDialog.setMessage("Подключение к серверу");
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

//        temp = new Employee();
//        temp.setName("Name1");
//        temp.setCode("qwe123");
//        temp.setPosition("pos1");
//
//        Call<Void> call = employeesService.addEmployee(temp);
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//
//            }
//        });
//
//        Violation violation = new Violation();
//        violation.setLatitude(12312312.0);
//        violation.setLongitude(64564564.0);
//        violation.setViolationDate(new Long(21122121));
//        violation.setEmployeeId(1);
//
//        Call<Void> call = violationsService.addViolation(violation);
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//
//            }
//        });

            Call<Employee> call = employeesService.getEmployeeByCode(tempCode);
            call.enqueue(new Callback<Employee>() {
                @Override
                public void onResponse(Call<Employee> call, Response<Employee> response) {
                    if (response.isSuccessful()) {
                        temp = response.body();
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                        changeActivity(temp);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Неверный код", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Employee> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Подключение к серверу отсутствует.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(LoginActivity.this, "Код должен состоять из 6 символов", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        //startService(intent);
        super.onPause();
    }

    @Override
    protected void onResume() {
        //stopService(intent);
        super.onResume();
    }

//    @Override
//    protected void onDestroy() {
//        stopService(intent);
//        super.onDestroy();
//    }

    private void changeActivity(Employee employee) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("employee", employee);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);

//        intent.putExtra("id", employee.getId());
//        intent.putExtra("code", employee.getCode());
//        intent.putExtra("name", employee.getName());
//        intent.putExtra("violationDate", employee.getViolationDate());
//        intent.putExtra("latitude", employee.getLatitude());
//        intent.putExtra("longitude", employee.getLongitude());

        startActivity(intent);
        this.finish();
    }
}
