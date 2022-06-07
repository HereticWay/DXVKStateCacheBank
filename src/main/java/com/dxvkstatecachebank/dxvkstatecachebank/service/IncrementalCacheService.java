package com.dxvkstatecachebank.dxvkstatecachebank.service;

import com.dxvkstatecachebank.dxvkstatecachebank.repository.IncrementalCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncrementalCacheService {
    @Autowired
    private IncrementalCacheRepository incrementalCacheRepository;
}
