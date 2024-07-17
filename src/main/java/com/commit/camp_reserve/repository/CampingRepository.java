package com.commit.camp_reserve.repository;

import com.commit.camp_reserve.entity.Camping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Integer> {
}
