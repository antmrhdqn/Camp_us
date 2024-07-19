package com.commit.campus.entity;

import lombok.Getter;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;  // 예약 ID

    @ManyToOne
    @JoinColumn(name = "camp_facs_id")
    private CampingFacilities campingFacilities;  // 캠핑장 시설 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // 사용자 ID

    private Date reservationDate;  // 예약 날짜
    private Date entryDate;  // 입실 날짜
    private Date leavingDate;  // 퇴실 날짜
    private String reservationStatus;  // 예약 상태
    private String equipmentRentalStatus;  // 장비 대여 상태
}
