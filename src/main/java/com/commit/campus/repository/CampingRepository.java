package com.commit.campus.repository;

import com.commit.campus.entity.Camping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Long> {

    Page<Camping> findByCampIdIn(List<Long> reviewedCampIds, Pageable pageable);

    List<Camping> findByContentId(int i);
}
