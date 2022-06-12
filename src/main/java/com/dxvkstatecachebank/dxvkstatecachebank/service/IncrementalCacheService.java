package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.IncrementalCacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.repository.IncrementalCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncrementalCacheService {
    @Autowired
    private IncrementalCacheRepository incrementalCacheRepository;

    public IncrementalCacheFile save(IncrementalCacheFile incrementalCacheFile) {
        return incrementalCacheRepository.save(incrementalCacheFile);
    }

    public IncrementalCacheFile findById(Long incrementalCacheFileId) {
        return incrementalCacheRepository.findById(incrementalCacheFileId)
                .get();  // It's okay now to throw an exception here
    }
}
