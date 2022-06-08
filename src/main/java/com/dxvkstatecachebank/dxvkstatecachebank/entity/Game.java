package com.dxvkstatecachebank.dxvkstatecachebank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @OneToOne(mappedBy = "game")
    private IncrementalCacheFile latestCacheFile;

    @OneToMany(mappedBy = "game")
    private List<CacheFile> contributions;

    @Column(
            nullable = false,
            unique = true
    )
    private Long steamId;
}
