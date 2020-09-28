package com.mikep.carrental;

import com.mikep.carrental.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CarRentalApplication implements CommandLineRunner
{
	@Autowired
	private IReservationService reservationService;

	public static void main(String[] args) {
		SpringApplication.run(CarRentalApplication.class, args);
	}

	public void run(String... args) throws Exception
	{
		reservationService.init();
	}
}
