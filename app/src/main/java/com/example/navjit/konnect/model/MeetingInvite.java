package com.example.navjit.konnect.model;

import java.util.Calendar;

public class MeetingInvite {

    private int day;
    private int month;
    private int year;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public Calendar getMeetingStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, startHour, startMinute);
        return calendar;
    }

    public Calendar getMeetingEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, endHour, endMinute);
        return calendar;
    }
}
