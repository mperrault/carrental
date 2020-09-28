package com.mikep.carrental.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation implements Comparable<Reservation>
{
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @ManyToOne
    private Car car;

    private String name;

    public Reservation() {

    }

    public Reservation(Car car, LocalDateTime startDateTime, LocalDateTime endDateTime, String name) {
        this.car = car;
        this.name = name;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;

    }
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public LocalDateTime getStartDateTime()
    {
        return startDateTime;
    }

    public void setStartDate(LocalDateTime startDateTime)
    {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime()
    {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime)
    {
        this.endDateTime = endDateTime;
    }

    public Car getCar()
    {
        return car;
    }

    public void setCar(Car car)
    {
        this.car = car;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int compareTo(Reservation r)
    {
        return this.getStartDateTime().compareTo(r.getStartDateTime());
    }
}
