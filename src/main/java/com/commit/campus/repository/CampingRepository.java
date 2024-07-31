package com.commit.campus.repository;

import com.commit.campus.entity.Camping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Long> {
}
