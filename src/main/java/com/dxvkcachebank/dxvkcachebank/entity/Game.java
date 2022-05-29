package com.dxvkcachebank.dxvkcachebank.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true
    )
    private String name;

    @OneToMany(mappedBy = "game")
    private List<CacheFile> contributions;

    @Column(
            nullable = false,
            unique = true
    )
    private Long steamId;
}
