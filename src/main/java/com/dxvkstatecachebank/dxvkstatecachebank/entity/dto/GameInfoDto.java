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
    String incrementalCacheFileLink; // TODO: Write a mapper that makes a proper link to get the latest cache file info
    private String contributionsLink; // TODO: Write a mapper that makes a proper linkt to get the contributed cache files
    private Long steamId;
}
