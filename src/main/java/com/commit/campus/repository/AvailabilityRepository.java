package com.commit.campus.repository;

import com.commit.campus.entity.Availability;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
//    List<Availability> findByCampIdAndDateBetween(Long campId, Date startDate, Date endDate);
    @Query("SELECT a FROM Availability a WHERE a.campId = :campId AND a.date BETWEEN :startDate AND :endDate")
    List<Availability> findByCampIdAndDateBetween(@Param("campId") Long campId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
//    Availability findByCampIdAndDate(Long campId, Date date);

    @Query("SELECT a FROM Availability a WHERE a.campId = :campId AND a.date = :date")
    Availability findByCampIdAndDate(@Param("campId") Long campId, @Param("date") Date date);
}
