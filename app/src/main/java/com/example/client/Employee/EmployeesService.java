package com.example.client.Employee;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EmployeesService {
    @GET("/api/Employees/employeeByCode/{Code}")
    Call<Employee> getEmployeeByCode(@Path("Code") String code);
}
