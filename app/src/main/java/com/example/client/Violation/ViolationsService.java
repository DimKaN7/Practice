package com.example.client.Violation;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ViolationsService {

    @POST("/api/Violations")
    Call<Void> addViolation(@Body Violation violation);

}
