package com.dxvkstatecachebank.dxvkstatecachebank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Blob;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @Column(nullable = false)
    private String password;

    @Column(
            nullable = false,
            unique = true
    )
    private String email;

    @Lob
    private Blob profilePicture;

    @OneToMany(mappedBy = "uploader")
    private List<CacheFile> contributions;
}
