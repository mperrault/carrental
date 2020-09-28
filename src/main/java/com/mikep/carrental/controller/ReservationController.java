package com.mikep.carrental.controller;

import com.mikep.carrental.entity.Car;
import com.mikep.carrental.entity.CarType;
import com.mikep.carrental.entity.Reservation;
import com.mikep.carrental.repo.ReservationAndCarRepository;
import com.mikep.carrental.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class ReservationController
{
    @Autowired
    private IReservationService reservationService;

    @Autowired
    private ReservationAndCarRepository reservationAndCarRepo;
    private List<Car> allCars;

    public ReservationController()
    {
    }

    @GetMapping("/reserve")
    public String getReserveForm(HttpSession session, Model model)
    {
        // lazy initialization of allCars; assume inventory remains constant

        if (allCars == null)
            allCars = reservationAndCarRepo.retreiveAllCars();

        CarType[] carTypes = CarType.values();
        model.addAttribute("carTypes", carTypes);
        ReservationData resData = (ReservationData)session.getAttribute("reservationData");
        if (resData == null)
        {
            resData = new ReservationData();
            LocalDateTime now = LocalDateTime.now();
            LocalDate localDate = now.toLocalDate();
            resData.setStartdate(localDate.toString());
            resData.setStarttime("09:00");
            resData.setEnddate(localDate.plusDays(1).toString());
            resData.setEndtime("17:00");
            session.setAttribute("reservationData", resData);
        }
        model.addAttribute("reservationData", resData);
        model.addAttribute("cars", allCars);
        List<Reservation> allReservations = reservationService.getReservations();
        model.addAttribute("reservations", allReservations);
        return "reserve_form";
    }

    @PostMapping("/reserve")
    public String submitForm(HttpSession session, @ModelAttribute("reservationData") ReservationData reservationData)
    {
        // update data in the session

        ReservationData resData = (ReservationData)session.getAttribute("reservationData");
        if (resData != null)
        {
            resData.copy(reservationData);
            session.setAttribute("reservationData", resData);
        }
        // Error checking: make sure we have expected format

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String startDateTime = resData.getStartdate() + " " + resData.getStarttime();
        String endDateTime = resData.getEnddate() + " " + resData.getEndtime();
        LocalDateTime parsedStartDate = null;
        LocalDateTime parsedEndDate = null;
        try
        {
            parsedStartDate = LocalDateTime.parse(startDateTime, formatter);
        }
        catch (DateTimeParseException parseEx) {
            resData.setError("Error: bad start date format detected");
            return "redirect:/reserve";
        }
        try
        {
            parsedEndDate = LocalDateTime.parse(endDateTime, formatter);
        }
        catch (DateTimeParseException parseEx) {
            resData.setError("Error: bad end date format detected");
            return "redirect:/reserve";
        }

        // Error checking: make sure start date is after end date

        if (parsedEndDate.isBefore(parsedStartDate)) {
            resData.setError("Error: end date before start date");
            return "redirect:/reserve";
        }

        // Error checking: make sure we have a customer name

        if (reservationData.getCustomername() == null || reservationData.getCustomername().trim().length() == 0) {
            resData.setError("Error: customer name expected");
            return "redirect:/reserve";
        }

        // we passed the error checking; find out if a vehicle of the specified type
        // is available; if so, reserve it

        List<Car> availableCars = reservationService.findAvailableCars(resData.getCarType(), parsedStartDate, parsedEndDate);
        if (availableCars.size() == 0) {
            resData.setError("Error: no cars of specified type available for specified period");
            return "redirect:/reserve";
        }

        Reservation reservation = new Reservation(availableCars.get(0),
                parsedStartDate, parsedEndDate, reservationData.getCustomername());
        boolean success = reservationService.reserveCar(reservation);

        // this is a edge case: someone reserved underneath us
        if (! success)
        {
            resData.setError("Error: could not reserve car of type. Please try again");
            return "redirect:/reserve";
        }
        resData.setSuccess("Success: car reserved");
        return "redirect:/reserve";
    }
}
