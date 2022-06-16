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
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    private String name;

    @Column(nullable = false)
    private String cacheFileName;

    @Lob
    private Blob incrementalCacheFile;

    @OneToMany(mappedBy = "game")
    private List<CacheFile> cacheFiles;

    @Column(unique = true)
    private Long steamId;
}
