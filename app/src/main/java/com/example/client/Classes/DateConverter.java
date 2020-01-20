package com.example.client.Classes;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class DateConverter {

    private HashMap<String, String> days;
    private HashMap<String, String> months;

    public DateConverter() {
        days = new HashMap<>();
        months = new HashMap<>();

        days.put("Sun", "Вск");
        days.put("Mon", "Пн");
        days.put("Tue", "Вт");
        days.put("Wed", "Ср");
        days.put("Thu", "Чт");
        days.put("Fri", "Пт");
        days.put("Sat", "Суб");

        months.put("Jan", "01");
        months.put("Feb", "02");
        months.put("Mar", "03");
        months.put("Apr", "04");
        months.put("May", "05");
        months.put("Jun", "06");
        months.put("Jul", "07");
        months.put("Aug", "08");
        months.put("Sep", "09");
        months.put("Oct", "10");
        months.put("Nov", "11");
        months.put("Dec", "12");
    }

    public String convert(Long longMillis) {
        String date = (new Date(longMillis)).toString();
        String dow = days.get(date.split(" ")[0]);
        String mon = months.get(date.split(" ")[1]);
        String num = date.split(" ")[2];
        String time = date.split(" ")[3];
        String year = date.split(" ")[5].substring(2);

        String result = num + "." + mon + "." + year + ", " + dow + ", " + time;

        return result;
    }

}
