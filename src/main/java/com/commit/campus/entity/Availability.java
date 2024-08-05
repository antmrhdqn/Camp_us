package com.commit.campus.entity;

import lombok.*;
import jakarta.persistence.*;

import java.util.Date;

@Table(name = "availability")
@Entity
@Getter
@Setter
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avail_id")
    private Long availId;

    @Column(name = "camp_id", nullable = false)
    private Long campId;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "general_site_avail")
    private int generalSiteAvail;

    @Column(name = "car_site_avail")
    private int carSiteAvail;

    @Column(name = "glamping_site_avail")
    private int glampingSiteAvail;

    @Column(name = "caravan_site_avail")
    private int caravanSiteAvail;
}
