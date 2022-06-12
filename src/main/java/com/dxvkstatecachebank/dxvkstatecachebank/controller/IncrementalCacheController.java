package com.dxvkstatecachebank.dxvkstatecachebank.controller;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.IncrementalCacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.service.IncrementalCacheService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.sql.SQLException;

@RestController
@RequestMapping("/incrcachefile")
public class IncrementalCacheController {
    @Autowired
    private IncrementalCacheService incrementalCacheService;

    @Transactional
    @GetMapping("/{incrementalCacheFileId}/data")
    public ResponseEntity<StreamingResponseBody> getDataById(@PathVariable("incrementalCacheFileId") Long incrementalCacheFileId, final HttpServletResponse response) {
        IncrementalCacheFile cacheFile = incrementalCacheService.findById(incrementalCacheFileId);
        String cacheFileName = cacheFile.getGame()
                .getCacheFileName();

        response.setContentType("application/octet-stream");
        response.setHeader(
                "Content-Disposition",
                "attachment;filename=%s.dxvk-cache".formatted(cacheFileName)
        );
        StreamingResponseBody streamingResponseBody = outputStream -> {
            InputStream inputBlobFromDb;
            try {
                inputBlobFromDb = cacheFile.getData().getBinaryStream();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            inputBlobFromDb.transferTo(outputStream);
            outputStream.close();
            inputBlobFromDb.close();
        };

        return ResponseEntity.ok(streamingResponseBody);
    }
}
