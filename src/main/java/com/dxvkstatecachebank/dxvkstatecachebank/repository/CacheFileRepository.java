package com.dxvkstatecachebank.dxvkstatecachebank.repository;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheFileRepository extends JpaRepository<CacheFile, Long> {
}
