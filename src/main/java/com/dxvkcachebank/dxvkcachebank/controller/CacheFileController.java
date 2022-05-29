package com.dxvkcachebank.dxvkcachebank.controller;

import com.dxvkcachebank.dxvkcachebank.service.CacheFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cachefile")
public class CacheFileController {
    @Autowired
    private CacheFileService cacheFileService;
}
