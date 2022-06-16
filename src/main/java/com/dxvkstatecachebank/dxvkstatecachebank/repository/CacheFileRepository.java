package com.dxvkstatecachebank.dxvkstatecachebank.repository;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CacheFileRepository extends JpaRepository<CacheFile, Long> {
    List<CacheFile> findAllByGameId(Long gameId);
    List<CacheFile> findAllByUploaderId(Long uploaderId);
}
