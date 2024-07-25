package com.commit.campus.dto;

import com.commit.campus.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Builder
public class ReservationDTO {

    private int reservation_id;
    private User user;
//    private long camp_id;     // 캠핑장 아이디를 넣는 것이 좋을지 고민
    private long camp_facs_id;
    private LocalDateTime reservation_date;
    private Date entry_date;
    private Date leaving_date;
    private String gear_rental_status;
}
