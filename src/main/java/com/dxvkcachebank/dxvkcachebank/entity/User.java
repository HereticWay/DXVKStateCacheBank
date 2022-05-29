package com.dxvkcachebank.dxvkcachebank.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "`user`")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    private String name;

    @Column(
            nullable = false,
            unique = true
    )
    private String email;

    @Lob
    private byte[] profilePicture;

    @OneToMany(mappedBy = "uploader")
    private List<CacheFile> contributions;
}
