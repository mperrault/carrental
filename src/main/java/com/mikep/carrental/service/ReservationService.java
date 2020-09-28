package com.mikep.carrental.service;

import com.mikep.carrental.entity.Car;
import com.mikep.carrental.entity.CarType;
import com.mikep.carrental.entity.Reservation;
import com.mikep.carrental.repo.ReservationAndCarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReservationService implements IReservationService
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ReservationAndCarRepository reservationAndCarRepo;

    private Map<String, Car> carMap = new HashMap<>();
    private Map<CarType, Map<String, List>> reservationMap = new ConcurrentHashMap<>();

    public ReservationService()
    {

    }

    public void init()
    {
        // build a map of Car types to Cars

        CarType[] carTypes = CarType.values();
        for (CarType carType : carTypes)
        {
            Map<String, List> carHashList = new HashMap<String, List>();
            reservationMap.put(carType, carHashList);
            Map carTypeMap = reservationMap.get(carType);
            System.out.println(carTypeMap);
        }

        // query all the Cars and populate the Car Reservation List

        List<Car> cars = reservationAndCarRepo.retreiveAllCars();
        for (Car car : cars)
        {
            CarType carType = car.getCarType();
            Map carTypeMap = reservationMap.get(carType);
            carTypeMap.put(car.getVin(), new ArrayList<Reservation>());
            carMap.put(car.getVin(), car);
        }

        // query the database for active reservations and add to the appropriate Car reservation list

        List<Reservation> activeReservations = reservationAndCarRepo.retrieveActiveReservationsAsc();
        for (Reservation reservation : activeReservations)
        {
            Car car = reservation.getCar();
            CarType carType = car.getCarType();

            // insert the reservation into the list

            List<Reservation> carReservationList = reservationMap.get(carType).get(car.getVin());
            carReservationList.add(reservation);
        }
    }

    public List<Reservation> getReservations()
    {
        List<Reservation> reservations = new ArrayList<Reservation>();
        CarType[] carTypes = CarType.values();
        for (CarType thisCarType : carTypes)
        {
            Map<String, List> reservationsBasedOnType = reservationMap.get(thisCarType);
            Iterator<String> carVinIt = reservationsBasedOnType.keySet().iterator();
            while (carVinIt.hasNext())
            {
                String csrVin = carVinIt.next();
                List<Reservation> carReservationList = reservationsBasedOnType.get(csrVin);
                for (Reservation thisRes : carReservationList)
                {
                    reservations.add(thisRes);
                }
            }
        }
        Collections.sort(reservations);
        return reservations;
    }

    public List<Car> findAvailableCars(CarType carType, LocalDateTime startDateTime, LocalDateTime endDateTime)
    {
        List<Car> availableCars = new ArrayList<Car>();
        Map<String, List> reservationsBasedOnType = reservationMap.get(carType);
        Iterator<String> carVinsIt = reservationsBasedOnType.keySet().iterator();
        while (carVinsIt.hasNext())
        {
            String currCarVin = carVinsIt.next();
            List<Reservation> carReservationList = reservationMap.get(carType).get(currCarVin);

            // get lock on carReservationList - we will be reading ; prevent updates while we
            // traverse.

            synchronized(carReservationList)
            {
                boolean available = true;
                int nodeImmediatelyBeforeDesiredStartDateTimeIndex = -1;

                // find the last node that has an end date before the start date

                for (int i = 0; i < carReservationList.size(); i++)
                {
                    Reservation reservation = carReservationList.get(i);
                    if (reservation.getEndDateTime().isBefore(startDateTime))
                        nodeImmediatelyBeforeDesiredStartDateTimeIndex = i;

                    // we can break out of here once we find a reservation
                    // start time after the desired end time

                    if (reservation.getStartDateTime().isAfter(endDateTime))
                        break;

                }

                // handle the case of all reservations coming after our desired start time

                if (nodeImmediatelyBeforeDesiredStartDateTimeIndex == -1)
                {
                    if (carReservationList.size() > 0)
                    {
                        Reservation reservation = carReservationList.get(0);
                        if (((reservation.getStartDateTime().isBefore(endDateTime)) ||
                                (reservation.getStartDateTime().isEqual(endDateTime))))
                            available = false;
                    }
                }

                // handle the case of nodeImmediatelyBeforeDesiredStartDateIndex being the
                // last node in the reservation list

                else if (nodeImmediatelyBeforeDesiredStartDateTimeIndex == carReservationList.size() - 1)
                {
                    Reservation reservation = carReservationList.get(nodeImmediatelyBeforeDesiredStartDateTimeIndex);
                    if ((reservation.getEndDateTime().isAfter(startDateTime)) ||
                            (reservation.getEndDateTime().isEqual(startDateTime)))
                        available = false;
                }

                // handle the case of nodeImmediatelyBeforeDesiredStartDateIndex being in the
                // middle of the reservation list

                else
                {
                    Reservation reservation = carReservationList.get(nodeImmediatelyBeforeDesiredStartDateTimeIndex);
                    if ((reservation.getEndDateTime().isAfter(startDateTime)) ||
                            (reservation.getEndDateTime().isEqual(startDateTime)))
                        available = false;

                        // look at the next node in the list

                    else
                    {
                        Reservation nextReservation = carReservationList.get(nodeImmediatelyBeforeDesiredStartDateTimeIndex + 1);
                        if ((nextReservation.getStartDateTime().isBefore(endDateTime)) ||
                                (nextReservation.getStartDateTime().isEqual(endDateTime)))
                            available = false;
                    }
                }
                if (available)
                    availableCars.add(carMap.get(currCarVin));
            }
        }
        Collections.sort(availableCars);
        return availableCars;
    }

    public boolean reserveCar(Reservation reservation)
    {
        // make sure this car is available

        List<Car> carsAvailable = findAvailableCars(reservation.getCar().getCarType(),
                reservation.getStartDateTime(), reservation.getEndDateTime());

        boolean available = false;
        for (Car car : carsAvailable) {
            if (car.getVin().equals(reservation.getCar().getVin())) {
                available = true;
                break;
            }
        }
        if (! available) {
            logger.info("Can not reserve Car: " + reservation.getCar().getVin() +
                    "; it is not available for specified time slot. " +
                    "; start time: " + reservation.getStartDateTime() +
                    "; end_time: " + reservation.getEndDateTime());
            return false;
        }

        // insert a Reservation record into the database, and if that

        if (reservationAndCarRepo.reserveCar(reservation)) {

            // update our in-memory reservations

            List<Reservation> carReservationList = reservationMap.get(reservation.getCar().getCarType())
                    .get(reservation.getCar().getVin());

            // get lock on carReservationList - prevent reading/writing while we are updating

            synchronized(carReservationList)
            {
                carReservationList.add(reservation);
                Collections.sort(carReservationList);
            }
            return true;
        }
        return false;
    }
}
