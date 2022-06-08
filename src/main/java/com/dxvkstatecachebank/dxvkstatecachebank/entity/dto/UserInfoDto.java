package com.dxvkstatecachebank.dxvkstatecachebank.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoDto {
    private Long id;
    private String name;
    private String email;
    //private Blob profilePicture;
    //private List<CacheFileView> contributions;
    private String profilePictureLink;
    private String contributionsLink;
}
