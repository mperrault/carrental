package com.mikep.carrental.entity;

import javax.persistence.*;

@Entity
@NamedQuery(name="lockingCar",
    query="SELECT c from Car c WHERE c.vin = :vin",
    lockMode=LockModeType.PESSIMISTIC_WRITE)
public class Car implements Comparable<Car>
{
    @Id
    @Column(name = "vin", nullable = false)
    private String vin;

    @Enumerated(EnumType.STRING)
    private CarType carType;

    private int reservationCount;

    public Car()
    {

    }

    public Car(CarType carType, String vin)
    {
        this.carType = carType;
        this.vin = vin;
    }

    public String getVin()
    {
        return vin;
    }

    public void setVin(String vin)
    {
        this.vin = vin;
    }

    public CarType getCarType()
    {
        return carType;
    }

    public void setCarType(CarType carType)
    {
        this.carType = carType;
    }

    public int getReservationCount()
    {
        return reservationCount;
    }

    public void setReservationCount(int newCount)
    {
        reservationCount = newCount;
    }

    @Override
    public int compareTo(Car o)
    {
        return this.getVin().compareTo(o.getVin());
    }
}
