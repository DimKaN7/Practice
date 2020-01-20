package com.example.client.Violation;

import com.example.client.Employee.EmployeesController;
import com.example.client.Employee.EmployeesService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ViolationsController {

    private String baseURL;
    private ViolationsService violationsService;

    public ViolationsController(String baseURL) {
        this.baseURL = baseURL;

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.baseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        violationsService = retrofit.create(ViolationsService.class);
    }

    public ViolationsService getApi() {
        return this.violationsService;
    }

}
