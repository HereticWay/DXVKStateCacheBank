package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameInfoDto {
    private Long id;
    private String name;
    //private IncrementalCacheFileView incrementalCacheFile;
    //private List<CacheFileView> contributions;
    String incrementalCacheFileLink;
    private String contributionsLink;
    private Long steamId;
}
