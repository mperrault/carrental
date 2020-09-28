package com.mikep.carrental.controller;

import com.mikep.carrental.entity.CarType;

public class ReservationData
{
    private String customername;
    private String startdate;
    private String starttime;
    private String enddate;
    private String endtime;
    private CarType carType;
    private String error;
    private String success;

    public ReservationData() {

    }

    public void copy(ReservationData other)
    {
        customername = other.customername;
        startdate = other.startdate;
        starttime = other.starttime;
        enddate = other.enddate;
        endtime = other.endtime;
        carType = other.carType;
        error = "";
        success = "";
    }

    public String getCustomername()
    {
        return customername;
    }

    public void setCustomername(String customerName)
    {
        this.customername = customerName;
    }

    public String getStarttime()
    {
        return starttime;
    }

    public void setStarttime(String starttime)
    {
        this.starttime = starttime;
    }

    public String getEnddate()
    {
        return enddate;
    }

    public void setEnddate(String enddate)
    {
        this.enddate = enddate;
    }

    public String getEndtime()
    {
        return endtime;
    }

    public void setEndtime(String endtime)
    {
        this.endtime = endtime;
    }

    public CarType getCarType()
    {
        return carType;
    }

    public void setCarType(CarType carType)
    {
        this.carType = carType;
    }

    public String getStartdate()
    {
        return startdate;
    }

    public void setStartdate(String startDate)
    {
        this.startdate = startDate;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public String getSuccess()
    {
        return success;
    }

    public void setSuccess(String success)
    {
        this.success = success;
    }
}
