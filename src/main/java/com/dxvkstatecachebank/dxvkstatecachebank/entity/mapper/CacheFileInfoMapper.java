package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.CacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.CacheFileInfoDto;
import org.springframework.stereotype.Component;

@Component
public class CacheFileInfoMapper {
    public CacheFileInfoDto toDto(CacheFile cacheFile) {
        Long uploaderId = cacheFile.getUploader().getId();
        Long gameId = cacheFile.getGame().getId();

        return CacheFileInfoDto.builder()
                .id(cacheFile.getId())
                .uploadDateTime(cacheFile.getUploadDateTime())
                .uploaderLink("/user/%d".formatted(uploaderId))
                .gameLink("/game/%d".formatted(gameId))
                .dataLink("/cachefile/%d/data")
                .build();
    }
}
