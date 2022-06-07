package com.dxvkstatecachebank.dxvkstatecachebank.repository;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.IncrementalCacheFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncrementalCacheRepository extends JpaRepository<IncrementalCacheFile, Long> {
}
