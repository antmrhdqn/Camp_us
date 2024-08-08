package com.commit.campus.service;

import com.commit.campus.dto.ReservationDTO;
import com.commit.campus.entity.Availability;
import com.commit.campus.entity.Camping;
import com.commit.campus.entity.Reservation;
import com.commit.campus.repository.AvailabilityRepository;
import com.commit.campus.repository.CampingRepository;
import com.commit.campus.repository.ReservationRepository;
import com.commit.campus.service.impl.ReservationServiceImpl;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTests {

    @Mock private ReservationRepository reservationRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private CampingRepository campingRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private RedisCommands<String, String> redisCommands;

    @InjectMocks
    private ReservationServiceImpl reservationServiceImpl;

    private Reservation reservation, reservation2;
    private ReservationDTO reservationDTO, reservationDTO2;
    private Availability availability;
    private Camping camping;

    @BeforeEach
    void setUp() {
        camping = new Camping();
        camping.setCampId(1000L);
        camping.setGeneralSiteCnt(10);
        camping.setCarSiteCnt(5);
        camping.setGlampingSiteCnt(2);
        camping.setCaravanSiteCnt(3);

        reservation = Reservation.builder()
                .reservationId(20240807000001L)
                .userId(1)
                .campId(1000)
                .campFacsId(3)
                .reservationDate(LocalDateTime.parse("2024-06-30T00:00:00"))
                .entryDate(LocalDate.parse("2024-08-01"))
                .leavingDate(LocalDate.parse("2024-08-02"))
                .reservationStatus("예약 확정")
                .gearRentalStatus("N")
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        reservation2 = Reservation.builder()
                .reservationId(20240807000002L)
                .userId(2)
                .campId(1000)
                .campFacsId(3)
                .reservationDate(LocalDateTime.parse("2024-07-03T00:00:00"))
                .entryDate(LocalDate.parse("2024-08-01"))
                .leavingDate(LocalDate.parse("2024-08-03"))
                .reservationStatus("예약 확정")
                .gearRentalStatus("N")
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        reservationDTO = new ReservationDTO();
    }

    @Test
    void confirmReservation_예약_확정_성공() {
        String reservationId = "20240807000001";
        String key = "reservationInfo:" + reservationId;

        Map<String, String> reservationInfo = new HashMap<>();
        reservationInfo.put("reservationId", "20240807000001");
        reservationInfo.put("userId", "1");
        reservationInfo.put("campId", "1000");
        reservationInfo.put("campFacsId", "3");
        reservationInfo.put("reservationDate", "2024-06-30T00:00:00");
        reservationInfo.put("entryDate", "2024-08-01");
        reservationInfo.put("leavingDate", "2024-08-02");
        reservationInfo.put("gearRentalStatus", "N");
        reservationInfo.put("campFacsType", "3");

        when(redisCommands.hgetall(key)).thenReturn(reservationInfo);
        when(reservationRepository.findById(Long.valueOf(reservationId))).thenReturn(Optional.empty());
        when(campingRepository.findById(1000L)).thenReturn(Optional.of(camping));

        ReservationDTO confirmedReservation = reservationServiceImpl.confirmReservation(reservationId);

        assertNotNull(confirmedReservation);
        assertEquals(20240807000001L, confirmedReservation.getReservationId());
        assertEquals(1L, confirmedReservation.getUserId());
        assertEquals(1000L, confirmedReservation.getCampId());
        assertEquals(3L, confirmedReservation.getCampFacsId());
        assertEquals(LocalDateTime.parse("2024-06-30T00:00:00"), confirmedReservation.getReservationDate());
        assertEquals(LocalDate.parse("2024-08-01"), confirmedReservation.getEntryDate());
        assertEquals(LocalDate.parse("2024-08-02"), confirmedReservation.getLeavingDate());

        verify(redisCommands, times(1)).hgetall(key);
        verify(reservationRepository, times(1)).findById(Long.valueOf(reservationId));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(availabilityRepository, times(1)).findByCampIdAndDateBetween(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void confirmReservation_예약_존재_예외() {
        String reservationId = "20240807000001";
        String key = "reservationInfo:" + reservationId;

        Map<String, String> reservationInfo = new HashMap<>();
        reservationInfo.put("reservationId", "20240807000001");

        when(redisCommands.hgetall(key)).thenReturn(reservationInfo);
        when(reservationRepository.findById(Long.valueOf(reservationId))).thenReturn(Optional.of(reservation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservationServiceImpl.confirmReservation(reservationId);
        });

        assertEquals("이미 존재하는 예약입니다: 20240807000001", exception.getMessage());

        verify(redisCommands, times(1)).hgetall(key);
        verify(reservationRepository, times(1)).findById(Long.valueOf(reservationId));
    }

    @Test
    void confirmReservation_예약_취소_예외() {
        String reservationId = "20240807000001";
        String key = "reservationInfo:" + reservationId;

        reservation = reservation.toBuilder()
                .reservationStatus("예약 취소")
                .build();

        Map<String, String> reservationInfo = new HashMap<>();
        reservationInfo.put("reservationId", "20240807000001");

        when(redisCommands.hgetall(key)).thenReturn(reservationInfo);
        when(reservationRepository.findById(Long.valueOf(reservationId))).thenReturn(Optional.of(reservation));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservationServiceImpl.confirmReservation(reservationId);
        });

        assertEquals("이미 취소된 예약입니다.", exception.getMessage());

        verify(redisCommands, times(1)).hgetall(key);
        verify(reservationRepository, times(1)).findById(Long.valueOf(reservationId));
    }

    @Test
    void confirmReservation_만료된_예약_예외() {
        String reservationId = "20240807000001";
        String key = "reservationInfo:" + reservationId;

        when(redisCommands.hgetall(key)).thenReturn(new HashMap<>());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationServiceImpl.confirmReservation(reservationId);
        });

        assertEquals("이미 만료되었거나 존재하지 않는 예약입니다.", exception.getMessage());

        verify(redisCommands, times(1)).hgetall(key);
        verify(reservationRepository, times(0)).findById(anyLong());
    }
}