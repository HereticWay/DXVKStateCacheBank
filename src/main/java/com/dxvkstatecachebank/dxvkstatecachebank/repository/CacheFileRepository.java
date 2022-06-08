package com.dxvkstatecachebank.dxvkstatecachebank.repository;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.view.CacheFileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CacheFileRepository extends JpaRepository<CacheFile, Long> {
    List<CacheFileView> findAllByGameId(Long gameId);
}
