package com.commit.campus.repository;

import com.commit.campus.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByCampIdAndDateBetween(Long campId, Date startDate, Date endDate);
}
