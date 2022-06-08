package com.dxvkstatecachebank.dxvkstatecachebank.entity.mapper;

import com.dxvkstatecachebank.dxvkstatecachebank.entity.IncrementalCacheFile;
import com.dxvkstatecachebank.dxvkstatecachebank.entity.dto.IncrementalCacheFileInfoDto;
import org.springframework.stereotype.Component;

@Component
public class IncrementalCacheFileInfoMapper {
    public IncrementalCacheFileInfoDto toDto(IncrementalCacheFile incrementalCacheFile) {
        Long gameId = incrementalCacheFile.getGame().getId();

        return IncrementalCacheFileInfoDto.builder()
                .id(incrementalCacheFile.getId())
                .lastUpdateTime(incrementalCacheFile.getLastUpdateTime())
                .gameLink("/game/%d".formatted(gameId))
                .dataLink("/cachefile/%d/data".formatted(incrementalCacheFile.getId()))
                .build();
    }
}
