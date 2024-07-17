package com.comm1t.Camp_Us.repository;

import com.comm1t.Camp_Us.entity.Camping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Integer> {
}
