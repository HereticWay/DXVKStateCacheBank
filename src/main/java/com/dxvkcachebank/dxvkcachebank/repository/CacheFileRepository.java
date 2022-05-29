package com.dxvkcachebank.dxvkcachebank.repository;

import com.dxvkcachebank.dxvkcachebank.entity.CacheFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheFileRepository extends JpaRepository<CacheFile, Long> {
}
