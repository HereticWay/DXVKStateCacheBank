package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.CacheFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CacheFileService {
    @Autowired
    private CacheFileRepository cacheFileRepository;

    public List<CacheFile> findAllByGameId(Long gameId) {
        return cacheFileRepository.findAllByGameId(gameId);
    }

    public CacheFile save(CacheFile cacheFile) {
        return cacheFileRepository.save(cacheFile);
    }

    public CacheFile findById(Long cacheFileId) {
        return cacheFileRepository.findById(cacheFileId)
                .get();  // It's okay now to throw an exception here
    }

    public void deleteById(Long cacheFileId) {
        cacheFileRepository.deleteById(cacheFileId);
    }
}
