package com.example.client.Employee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EmployeesController {
    private String baseURL;
    private EmployeesService employeesService;

    public EmployeesController(String baseURL) {
        this.baseURL = baseURL;

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        employeesService = retrofit.create(EmployeesService.class);
    }

    public EmployeesService getApi() {
        return employeesService;
    }
}
