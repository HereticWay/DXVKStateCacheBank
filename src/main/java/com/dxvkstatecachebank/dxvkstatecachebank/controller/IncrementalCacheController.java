package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.service.IncrementalCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incrcachefile")
public class IncrementalCacheController {
    @Autowired
    private IncrementalCacheService incrementalCacheService;
}
