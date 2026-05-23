package com.example.data.repository;

import com.example.data.entity.RoleType;
import com.example.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRole(RoleType roleType);
    User findByUsername(String username);
}
