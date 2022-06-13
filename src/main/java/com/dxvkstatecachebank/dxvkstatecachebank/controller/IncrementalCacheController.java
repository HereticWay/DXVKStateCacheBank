package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.Game;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.IncrementalCacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.service.IncrementalCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

@RestController
@RequestMapping("/incrcachefile")
public class IncrementalCacheController {
    @Autowired
    private IncrementalCacheService incrementalCacheService;

    @Transactional
    @GetMapping("/{incrementalCacheFileId}/data")
    public ResponseEntity<Resource> getDataById(@PathVariable("incrementalCacheFileId") Long incrementalCacheFileId) throws SQLException {
        IncrementalCacheFile cacheFile = incrementalCacheService.findById(incrementalCacheFileId);
        Blob cacheFileBlob = cacheFile.getData();
        Game game = cacheFile.getGame();
        String cacheFileName = "%s.dxvk-cache".formatted(game.getCacheFileName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(cacheFileName)
                        .build()
        );

        InputStreamResource inputStreamResource = new InputStreamResource(cacheFileBlob.getBinaryStream());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(cacheFileBlob.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(inputStreamResource);
    }
}
