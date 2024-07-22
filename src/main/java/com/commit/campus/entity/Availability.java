package com.commit.campus.entity;

import lombok.Getter;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availabilityId;  // 예약 가능 ID

    @ManyToOne
    @JoinColumn(name = "camp_id")
    private Camping camping;  // 캠핑장 ID

    private Date date;  // 날짜
    private boolean available;  // 예약 가능 여부
}
