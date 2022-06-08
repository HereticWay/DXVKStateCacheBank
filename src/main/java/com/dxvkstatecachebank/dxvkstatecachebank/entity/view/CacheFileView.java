package com.dxvkstatecachebank.dxvkstatecachebank.entity.view;

import java.time.LocalDateTime;

public interface CacheFileView {
    Long getId();
    LocalDateTime getUploadDateTime();
    UserView getUploader();
    GameView getGame();
    byte[] getData();
}
