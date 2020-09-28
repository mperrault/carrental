package com.mikep.carrental;

import org.junit.jupiter.api.Assertions;

import com.mikep.carrental.entity.Car;
import com.mikep.carrental.entity.CarType;
import com.mikep.carrental.entity.Reservation;
import com.mikep.carrental.service.IReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class CarRentalApplicationTests
{
	@Autowired
	private IReservationService reservationService;

	@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
	@Test
	public void testAllVansAvailable()
	{
		// all Vans should be available for rental at any date
		LocalDateTime startDate = LocalDateTime.parse("2020-11-01T09:00");
		LocalDateTime endDate = LocalDateTime.parse("2020-11-02T12:00");
		List<Car> availCars = reservationService.findAvailableCars(CarType.VAN, startDate, endDate);
		Assertions.assertTrue(availCars.size() == 3);
	}

	@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
	@Test
	public void testSingleReservation()
	{
		// make sure we can only make a reservation once
		LocalDateTime startDate = LocalDateTime.parse("2020-10-25T09:00");
		LocalDateTime endDate = LocalDateTime.parse("2020-10-30T12:00");
		List<Car> availCars = reservationService.findAvailableCars(CarType.SEDAN, startDate, endDate);

		// reserve a car

		Assertions.assertTrue(availCars.size() > 0);
		Car car = availCars.get(0);
		Reservation reservation = new Reservation(car, startDate, endDate, "John Griffin");
		boolean success = reservationService.reserveCar(reservation);
		Assertions.assertTrue(success);
	}

	@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
	@Test
	public void testDoubleBookedSingleReservation()
	{
		// make sure we can only make a reservation once
		LocalDateTime startDate = LocalDateTime.parse("2020-10-25T09:00");
		LocalDateTime endDate = LocalDateTime.parse("2020-10-30T12:00");
		List<Car> availCars = reservationService.findAvailableCars(CarType.SEDAN, startDate, endDate);

		// reserve a car

		Assertions.assertTrue(availCars.size() > 0);
		Car car = availCars.get(0);
		Reservation reservation = new Reservation(car, startDate, endDate, "John Griffin");
		boolean success = reservationService.reserveCar(reservation);
		Assertions.assertTrue(success);

		// Book same car and overlaps with John Griffins reservation

		startDate = LocalDateTime.parse("2020-10-30T09:00");
		endDate = LocalDateTime.parse("2020-11-01T12:00");
		reservation = new Reservation(car, startDate, endDate, "Eddie Tibbits");
		success = reservationService.reserveCar(reservation);
		Assertions.assertFalse(success);
	}

	@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
	@Test
	public void testAllCarsOfTypeBooked()
	{
		// Book all SUVs
		LocalDateTime startDate = LocalDateTime.parse("2020-12-25T09:00");
		LocalDateTime endDate = LocalDateTime.parse("2020-12-30T12:00");
		List<Car> availSUVs = reservationService.findAvailableCars(CarType.SUV, startDate, endDate);
		Assertions.assertTrue(availSUVs.size() == 6);


		// reserve all the SUVs for the given period

		for (Car thisSuv : availSUVs)
		{
			Reservation reservation = new Reservation(thisSuv, startDate, endDate, "John Mandel");
			boolean success = reservationService.reserveCar(reservation);
			Assertions.assertTrue(success);
		}

		// now, search for SUVs during same time period - there should be none available

		availSUVs = reservationService.findAvailableCars(CarType.SUV, startDate, endDate);
		Assertions.assertEquals(availSUVs.size(), 0);
	}

	@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
	@Test
	public void testBookingBetweenReservations()
	{
		LocalDateTime startDate = LocalDateTime.parse("2020-12-25T09:00");
		LocalDateTime endDate = LocalDateTime.parse("2020-12-27T09:00");
		List<Car> availSedans = reservationService.findAvailableCars(CarType.SEDAN, startDate, endDate);
		Assertions.assertTrue(availSedans.size() == 4);

		// grab the last sedan and make a couple reservations close in time

		Car sedan = availSedans.get(availSedans.size() - 1);
		endDate = LocalDateTime.parse("2020-12-26T09:00");
		Reservation reservation = new Reservation(sedan, startDate, endDate, "Garrett Franks");
		boolean success = reservationService.reserveCar(reservation);
		Assertions.assertTrue(success);
		startDate = LocalDateTime.parse("2020-12-26T15:00");
		endDate = LocalDateTime.parse("2020-12-27T09:00");
		reservation = new Reservation(sedan, startDate, endDate, "Garrett Franks");
		success = reservationService.reserveCar(reservation);
		Assertions.assertTrue(success);

		// attempt to reserve slot to end right at the begin time of next reservation -
		// this should fail

		startDate = LocalDateTime.parse("2020-12-26T09:01");
		//endDate = LocalDateTime.parse("2020-12-26T14:59");
		endDate = LocalDateTime.parse("2020-12-26T15:00");
		reservation = new Reservation(sedan, startDate, endDate, "Garrett Franks");
		success = reservationService.reserveCar(reservation);
		Assertions.assertFalse(success);

		// now, try reservation to end a minute before next reservation -
		// this should succeed.

		startDate = LocalDateTime.parse("2020-12-26T09:01");
		//endDate = LocalDateTime.parse("2020-12-26T14:59");
		endDate = LocalDateTime.parse("2020-12-26T14:59");
		reservation = new Reservation(sedan, startDate, endDate, "Garrett Franks");
		success = reservationService.reserveCar(reservation);
		Assertions.assertTrue(success);
	}
}
