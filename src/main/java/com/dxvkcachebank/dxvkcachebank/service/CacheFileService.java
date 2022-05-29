package com.dxvkcachebank.dxvkcachebank.service;

import com.dxvkcachebank.dxvkcachebank.repository.CacheFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheFileService {
    @Autowired
    private CacheFileRepository cacheFileRepository;
}
