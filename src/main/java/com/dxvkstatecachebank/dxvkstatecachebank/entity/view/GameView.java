package com.dxvkstatecachebank.dxvkstatecachebank.entity.view;

import java.util.List;

public interface GameView {
    Long getId();
    String getName();
    IncrementalCacheFileView getIncrementalCacheFile();
    List<CacheFileView> getContributors();
    Long getSteamId();
}
