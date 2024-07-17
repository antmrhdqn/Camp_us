package com.commit.camp_reserve.repository;

import com.commit.camp_reserve.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
