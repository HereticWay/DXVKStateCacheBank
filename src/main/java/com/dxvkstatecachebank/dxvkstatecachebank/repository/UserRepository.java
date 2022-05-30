package com.dxvkstatecachebank.dxvkstatecachebank.repository;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
