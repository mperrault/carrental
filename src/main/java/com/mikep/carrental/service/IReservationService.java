package com.mikep.carrental.service;

import com.mikep.carrental.entity.Car;
import com.mikep.carrental.entity.CarType;
import com.mikep.carrental.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface IReservationService
{
    public void init();
    public List<Car> findAvailableCars(CarType carType, LocalDateTime startDate, LocalDateTime endDate);
    public boolean reserveCar(Reservation reservation);
    public List<Reservation> getReservations();
}
