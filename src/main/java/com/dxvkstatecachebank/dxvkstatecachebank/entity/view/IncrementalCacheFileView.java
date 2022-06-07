package com.dxvkstatecachebank.dxvkstatecachebank.entity.view;

import java.time.LocalDateTime;

public interface IncrementalCacheFileView {
    Long getId();
    LocalDateTime getLastUpdateTime();
    byte[] getData();
}
