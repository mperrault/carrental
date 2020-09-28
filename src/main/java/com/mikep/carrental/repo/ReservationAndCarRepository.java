package com.mikep.carrental.repo;

import com.mikep.carrental.entity.Car;
import com.mikep.carrental.entity.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class ReservationAndCarRepository
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    EntityManager em;

    public ReservationAndCarRepository()
    {

    }

    public List<Car> retreiveAllCars()
    {
        Query query = em.createQuery("Select c from Car c", Car.class);
        return query.getResultList();
    }

    public List<Reservation> retrieveActiveReservationsAsc()
    {
        LocalDateTime now = LocalDateTime.now();
        Query query = em.createQuery(
                "Select r from Reservation r where end_date_time >= :end order by start_date_time asc");
        query.setParameter("end", now);
        return query.getResultList();
    }

    public boolean reserveCar(Reservation reservation) {

        // find the car, obtaining a pessimistic write lock on it. We lock on the Car row whenever
        // we reserve that particular car.

        TypedQuery<Car> carQuery = em.createNamedQuery("lockingCar", Car.class);
        carQuery.setParameter("vin", reservation.getCar().getVin());
        Car car = carQuery.getSingleResult();

        // sanity check: make sure the reservation time slot is still open - it may not longer
        // be available in the case of another server in our cluster inserting a reservation and we did
        // not yet get notified via message queue (Note that Message Queue support is not yet
        // implemented).

        Query reservationQuery = em.createQuery(
                "Select r from Reservation r where car_vin LIKE :car_vin " +
                "AND end_date_time >= :start_time " +
                "AND start_date_time < :end_time");
        reservationQuery.setParameter("car_vin", reservation.getCar().getVin());
        reservationQuery.setParameter("start_time", reservation.getStartDateTime());
        reservationQuery.setParameter("end_time", reservation.getEndDateTime());

        List<Reservation> reservations = reservationQuery.getResultList();
        if (reservations.size() > 0) {
            logger.error("Cannot reserve car - slot no longer available: " + reservation.getCar().getVin() +
                    "start_time: " + reservation.getStartDateTime() +
                    "; end_time: " + reservation.getEndDateTime());
            return false;
        }

        // insert the Reservation into the database

        em.persist(reservation);

        // bump up the reservation count on the Car

        car.setReservationCount(car.getReservationCount());
        reservation.setCar(car);
        em.persist(car);
        return true;
    }
}
