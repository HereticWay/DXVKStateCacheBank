package com.dxvkstatecachebank.dxvkstatecachebank.repository;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface CacheFileRepository extends JpaRepository<CacheFile, Long> {
    List<CacheFile> findAllByGameId(Long gameId);

    List<CacheFile> findAllByUploaderId(Long uploaderId);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE CacheFile
            SET uploader = null
            WHERE uploader.id = :uploaderId
            """)
    void disownAllFromUploaderId(@Param("uploaderId") Long uploaderId);

    @Transactional
    void deleteAllByGameId(Long gameId);
}
