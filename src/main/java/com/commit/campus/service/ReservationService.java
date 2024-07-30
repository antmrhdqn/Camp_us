package com.commit.campus.service;

import com.commit.campus.dto.ReservationDTO;
import org.springframework.stereotype.Service;

public interface ReservationService {
    void createReservation(ReservationDTO reservationData);
}
