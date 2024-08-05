package com.commit.campus.repository;

import com.commit.campus.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    Availability findByCampIdAndDate(Long campId, Date date);
}
