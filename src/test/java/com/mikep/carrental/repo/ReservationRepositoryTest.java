package com.mikep.carrental.repo;

import org.junit.jupiter.api.Assertions;

import com.mikep.carrental.CarRentalApplication;
import com.mikep.carrental.entity.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=CarRentalApplication.class)
public class ReservationRepositoryTest
{
    @Autowired
    ReservationAndCarRepository reservationRepo;

    @Test
    public void testActiveReservations()
    {
        // verify that there are 3 active reservations on our database (there were 2 in the
        // past in our sample data).
        List<Reservation> activeReservations = reservationRepo.retrieveActiveReservationsAsc();
        Assertions.assertEquals(activeReservations.size(), 3);
    }
}
