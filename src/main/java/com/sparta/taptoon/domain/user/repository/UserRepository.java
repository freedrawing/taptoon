package com.sparta.taptoon.domain.user.repository;

import com.sparta.taptoon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
