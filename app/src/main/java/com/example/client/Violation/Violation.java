package com.example.client.Violation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Violation {

    @SerializedName("Id")
    @Expose
    private Integer id;

    @SerializedName("EmployeeId")
    @Expose
    private Integer employeeId;

    @SerializedName("ViolationDate")
    @Expose
    private String violationDate;

    @SerializedName("Latitude")
    @Expose
    private Double latitude;

    @SerializedName("Longitude")
    @Expose
    private Double longitude;

    public Integer getId() {
        return id;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getViolationDate() {
        return violationDate;
    }

    public void setViolationDate(String violationDate) {
        this.violationDate = violationDate;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}