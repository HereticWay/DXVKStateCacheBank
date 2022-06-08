package com.dxvkstatecachebank.dxvkstatecachebank.entity.view;

import java.util.List;

public interface UserView {
    Long getId();
    String getName();
    String getEmail();
    byte[] getProfilePicture();
    List<CacheFileView> getContributions();
}
